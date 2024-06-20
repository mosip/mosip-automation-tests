package io.mosip.testrig.dslrig.ivv.orchestrator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.collections.Lists;
import org.testng.internal.Utils;
import org.testng.xml.XmlSuite;

import io.mosip.testrig.apirig.admin.fw.util.AdminTestUtil;
import io.mosip.testrig.apirig.global.utils.GlobalConstants;
import io.mosip.testrig.apirig.kernel.util.ConfigManager;
import io.mosip.testrig.apirig.kernel.util.S3Adapter;
import io.mosip.testrig.apirig.service.BaseTestCase;
import io.mosip.testrig.dslrig.ivv.core.dtos.Scenario;

/**
 * Reporter that generates a single-page HTML report of the test results.
 */
public class EmailableReport implements IReporter {
	static Logger logger = Logger.getLogger(EmailableReport.class);

	protected PrintWriter writer;

	protected final List<SuiteResult> suiteResults = Lists.newArrayList();

	// Reusable buffer
	private final StringBuilder buffer = new StringBuilder();

	private String fileName = "emailable-report.html";

	private static final String JVM_ARG = "emailable.report2.name";

	int totalPassedTests = 0;
	int totalSkippedTests = 0;
	int totalFailedTests = 0;

	static {
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
		try {
			writer = createWriter(outputDirectory);
		} catch (IOException e) {
			logger.error("Unable to create output file", e);
			return;
		}
		for (ISuite suite : suites) {
			suiteResults.add(new SuiteResult(suite));
		}
		writeDocumentStart();
		writeHead();
		writeBody();
		writeDocumentEnd();
		writer.close();

		int totalTestCases = totalPassedTests + totalSkippedTests + totalFailedTests;
		String oldString = System.getProperty("emailable.report2.name");
		String temp = "-report_T-" + totalTestCases + "_P-" + totalPassedTests + "_S-" + totalSkippedTests + "_F-"
				+ totalFailedTests;
		String newString = oldString.replace("-report", temp);

		File orignialReportFile = new File(System.getProperty("user.dir") + "/"
				+ System.getProperty("testng.outpur.dir") + "/" + System.getProperty("emailable.report2.name"));
		logger.info("reportFile is::" + System.getProperty("user.dir") + "/" + System.getProperty("testng.outpur.dir")
				+ "/" + System.getProperty("emailable.report2.name"));

		File newReportFile = new File(
				System.getProperty("user.dir") + "/" + System.getProperty("testng.outpur.dir") + "/" + newString);
		logger.info("New reportFile is::" + System.getProperty("user.dir") + "/"
				+ System.getProperty("testng.outpur.dir") + "/" + newString);

		if (orignialReportFile.exists()) {
			if (orignialReportFile.renameTo(newReportFile)) {
				orignialReportFile.delete();
				logger.info("Report File re-named successfully!");

				if (ConfigManager.getPushReportsToS3().equalsIgnoreCase("yes")) {
					S3Adapter s3Adapter = new S3Adapter();
					boolean isStoreSuccess = false;
					boolean isStoreSuccess2 = false;
					try {
						isStoreSuccess = s3Adapter.putObject(ConfigManager.getS3Account(), BaseTestCase.testLevel, null,
								null, newString, newReportFile);
						logger.info("isStoreSuccess:: " + isStoreSuccess);

						/* Need to figure how to handle EXTENT report handling */

						File extentReport = new File(BaseTestCaseUtil.getExtentReportName());

						isStoreSuccess2 = s3Adapter.putObject(ConfigManager.getS3Account(), BaseTestCase.testLevel,

								null, null, "ExtentReport-" + newString, extentReport);

					} catch (Exception e) {
						logger.error("error occured while pushing the object" + e.getMessage());
					}
					if (isStoreSuccess && isStoreSuccess2) {
						logger.info("Pushed report to S3");
					} else {
						logger.error("Failed while pushing file to S3");
					}
				}
			} else {
				logger.error("Renamed report file doesn't exist");
			}
		} else {
			logger.error("Original report File does not exist!");
		}
	}

	private String getCommitId() {
		Properties properties = new Properties();
		try (InputStream is = EmailableReport.class.getClassLoader().getResourceAsStream("git.properties")) {
			properties.load(is);

			return properties.getProperty("git.commit.id.abbrev");

		} catch (IOException io) {
			logger.error(io.getMessage());
			return "";
		}

	}

	protected PrintWriter createWriter(String outdir) throws IOException {
		new File(outdir).mkdirs();
		String jvmArg = System.getProperty(JVM_ARG);
		if (jvmArg != null && !jvmArg.trim().isEmpty()) {
			fileName = jvmArg;
		}
		return new PrintWriter(new BufferedWriter(new FileWriter(new File(outdir, fileName))));
	}

	protected void writeDocumentStart() {
		writer.println(
				"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">");
		writer.print("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
	}

	protected void writeHead() {
		writer.print("<head>");
		writer.print("<title>TestNG Report</title>");
		writeStylesheet();
		writer.print("</head>");
	}

	protected void writeStylesheet() {
	    writer.print("<style type=\"text/css\">");
	    
	    // General table styling
	    writer.print("table {margin-bottom:10px;border-collapse:collapse;empty-cells:show;width:100%;}");
	    
	    // Uniform cell styling for all tables with reduced padding
	    writer.print("th, td {border:1px solid #009;padding:.25em .5em;background-color:#FFF;vertical-align:middle;}");
	    
	    // Header specific styling
	    writer.print("th {background-color:#f2f2f2; text-align:center;}"); // Central alignment for headers
	    
	    // Links inside table cells
	    writer.print("table a {font-weight:bold}");
	    
	    // Striped rows for all tables
	    writer.print(".stripe tr:nth-child(odd) td {background-color: #E6EBF9}");
	    writer.print(".stripe tr:nth-child(even) td {background-color: #FFF}");
	    
	    // Passed rows styling
	    writer.print(".passedodd td, .passedeven td {background-color: #0A0; color: #FFF; text-align:center;}"); // Central alignment for passed rows
	    
	    // Skipped rows styling
	    writer.print(".skippedodd td, .skippedeven td {background-color: #FFA500; color: #FFF; text-align:center;}"); // Central alignment for skipped rows
	    
	    // Failed rows styling
	    writer.print(".failedodd td, .failedeven td {background-color: #F33; color: #FFF; text-align:center;}"); // Central alignment for failed rows
	    
	    // Specific styling for stacktrace
	    writer.print(".stacktrace {white-space:pre;font-family:monospace}");
	    
	    // To top link
	    writer.print(".totop {font-size:85%;text-align:center;border-bottom:2px solid #000}");
	    
	    // Background color utility classes
	    writer.print(".orange-bg {background-color: #FFA500; color: #FFF;}");
	    writer.print(".green-bg {background-color: #0A0; color: #FFF;}");
	    
	    // Uniform width for specific columns and center alignment for specific cells
	    writer.print(".num-center {text-align:center;}");
	    
	    // Left alignment for scenario steps
	    writer.print(".scenario-step {text-align:left;}");
	    
	    // New class for log box
	    writer.print(".log-box {background-color: black; color: white; padding: 10px; border-radius: 5px;}");
	    
	    writer.print("</style>");
	}


	protected void writeBody() {
		writer.print("<body>");
		writeSuiteSummary();
		writeScenarioSummary();
		writeScenarioDetails();
		writer.print("</body>");
	}

	protected void writeDocumentEnd() {
		writer.print("</html>");
	}

	protected void writeSuiteSummary() {
	    NumberFormat integerFormat = NumberFormat.getIntegerInstance();
	    NumberFormat decimalFormat = NumberFormat.getNumberInstance();
	    LocalDate currentDate = LocalDate.now();
	    String formattedDate = null;
	    String branch = null;

	    // Format the current date as per your requirement
	    try {
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	        formattedDate = currentDate.format(formatter);
	        Process process = Runtime.getRuntime().exec("git rev-parse --abbrev-ref HEAD");
	        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        branch = reader.readLine();
	    } catch (Exception e) {
	        // TODO: handle exception
	    }

	    totalPassedTests = 0;
	    totalSkippedTests = 0;
	    totalFailedTests = 0;
	    long totalDuration = 0;
	    writer.print("<table>");
	    int testIndex = 0;
	    for (SuiteResult suiteResult : suiteResults) {

	        writer.print("<tr><th colspan=\"7\">");
	        writer.print(Utils.escapeHtml(
	            suiteResult.getSuiteName() + " ---- " +
	            "Report Date: " + formattedDate + " ---- " +
	            "Tested Environment: " + System.getProperty("env.endpoint").replaceAll(".*?\\.([^\\.]+)\\..*", "$1") + " ---- " +
	            "Testrig details: Branch Name - " + branch + ", Commit ID - " + getCommitId()));
	        
	        writer.print("</th></tr>");
	        
	        writer.print("<tr>");

	        // Left column: "Server Component Details" with central alignment
	        writer.print("<th style=\"text-align: center; vertical-align: middle;\" colspan=\"2\"><span class=\"not-bold\"><pre>");
	        writer.print(Utils.escapeHtml("Tested Component Details"));
	        writer.print("</span></th>");

	        // Right column: Details from AdminTestUtil.getServerComponentsDetails() without bold formatting
	        writer.print("<td colspan=\"5\"><pre>");
	        writer.print(Utils.escapeHtml(AdminTestUtil.getServerComponentsDetails()));
	        writer.print("</pre></td>");

	        writer.print("</tr>");
	        writer.print(GlobalConstants.TRTR);

	        writer.print("<tr>");
	        writer.print("<th colspan=\"5\"><strong>Summary of Test Results</strong></th>");
	        writer.print("</tr>");
	        
	        writer.print("<tr>");
	        writer.print("<th># Total</th>");
	        writer.print("<th># Passed</th>");
	        writer.print("<th># Skipped</th>");
	        writer.print("<th># Failed</th>");
	        writer.print("<th>Time (ms)</th>");
	        writer.print("</tr>");

	        for (TestResult testResult : suiteResult.getTestResults()) {
	            int passedTests = testResult.getPassedTestCount();
	            int skippedTests = testResult.getSkippedTestCount();
	            int failedTests = testResult.getFailedTestCount();
	            int totalTests = passedTests + skippedTests + failedTests;
	            long duration = testResult.getDuration();

	            writer.print("<tr");
	            if ((testIndex % 2) == 1) {
	                writer.print(" class=\"stripe\"");
	            }
	            writer.print(">");

	            buffer.setLength(0);
	            writeTableData(decimalFormat.format(totalTests), "num num-center"); // Center alignment for total
	            writeTableData(integerFormat.format(passedTests), (passedTests > 0 ? "num green-bg num-center" : "num num-center")); // Center alignment for passed
	            writeTableData(integerFormat.format(skippedTests), (skippedTests > 0 ? "num orange-bg num-center" : "num num-center")); // Center alignment for skipped
				writeTableData(integerFormat.format(failedTests),  (failedTests > 0 ? "num attn num-center red-text" : "num num-center"));
	            writeTableData(decimalFormat.format(duration), "num num-center"); // Center alignment for time

	            writer.print("</tr>");

	            totalPassedTests += passedTests;
	            totalSkippedTests += skippedTests;
	            totalFailedTests += failedTests;
	            totalDuration += duration;

	            testIndex++;
	        }
	    }

	    // Print totals if there was more than one test
	    if (testIndex > 1) {
	        writer.print("<tr>");
	        writer.print("<th>Total</th>");
	        writeTableHeader(integerFormat.format(totalPassedTests), "num num-center"); // Center alignment for total
	        writeTableHeader(integerFormat.format(totalSkippedTests), (totalSkippedTests > 0 ? "num attn num-center" : "num num-center")); // Center alignment for skipped
	        writeTableHeader(integerFormat.format(totalFailedTests), (totalFailedTests > 0 ? "num attn num-center" : "num num-center")); // Center alignment for failed
	        writeTableHeader(decimalFormat.format(totalDuration), "num num-center"); // Center alignment for time
	        writer.print("<th colspan=\"2\"></th>");
	        writer.print("</tr>");
	    }

	    writer.print("</table>");
	}

	/**
	 * Writes a summary of all the test scenarios.
	 */
	protected void writeScenarioSummary() {
		writer.print("<table id='summary'>");
		writer.print("<thead>");
		writer.print("<tr>");
		// writer.print("<th>Class</th>");
		writer.print("<th>Scenario</th>");
		writer.print("<th>Scenario Description</th>");
		writer.print("<th>Time (ms)</th>");
		writer.print("</tr>");
		writer.print("</thead>");

		int testIndex = 0;
		int scenarioIndex = 0;
		for (SuiteResult suiteResult : suiteResults) {
			/*
			 * writer.print("<tbody><tr><th colspan=\"4\">"); //
			 * writer.print(Utils.escapeHtml(suiteResult.getSuiteName()));
			 * writer.print("</th></tr></tbody>");
			 */

			for (TestResult testResult : suiteResult.getTestResults()) {
				writer.print("<tbody id=\"t");
				writer.print(testIndex);
				writer.print("\">");

				String testName = Utils.escapeHtml("Scenarios");

				scenarioIndex += writeScenarioSummary(testName + " &#8212; Failed (configuration methods)",
						testResult.getFailedConfigurationResults(), "failed", scenarioIndex);
				scenarioIndex += writeScenarioSummary(testName + " &#8212; Failed", testResult.getFailedTestResults(),
						"failed", scenarioIndex);
				scenarioIndex += writeScenarioSummary(testName + " &#8212; Skipped (configuration methods)",
						testResult.getSkippedConfigurationResults(), "skipped", scenarioIndex);
				scenarioIndex += writeScenarioSummary(testName + " &#8212; Skipped", testResult.getSkippedTestResults(),
						"skipped", scenarioIndex);
				scenarioIndex += writeScenarioSummary(testName + " &#8212; Passed", testResult.getPassedTestResults(),
						"passed", scenarioIndex);

				writer.print("</tbody>");

				testIndex++;
			}
		}

		writer.print("</table>");
	}

	/**
	 * Writes the scenario summary for the results of a given state for a single
	 * test.
	 */
	private int writeScenarioSummary(String description, List<ClassResult> classResults, String cssClassPrefix,
			int startingScenarioIndex) {
		int scenarioCount = 0;
		if (!classResults.isEmpty()) {
			writer.print("<tr><th colspan=\"3\">");
			writer.print(description);
			writer.print("</th></tr>");

			int scenarioIndex = startingScenarioIndex;
			int classIndex = 0;
			for (ClassResult classResult : classResults) {
				String cssClass = cssClassPrefix + ((classIndex % 2) == 0 ? "even" : "odd");

				buffer.setLength(0);

				int scenariosPerClass = 0;
				int methodIndex = 0;
				for (MethodResult methodResult : classResult.getMethodResults()) {
					List<ITestResult> results = methodResult.getResults();
					int resultsCount = results.size();
					assert resultsCount > 0;
					ITestResult firstResult = results.iterator().next();

					// Write the remaining scenarios for the method

					for (int i = 0; i < resultsCount; i++) {
						
						ITestResult result = results.get(i);
						String [] scenarioDetails = getScenarioDetails(result);
						
						String scenarioName = Utils.escapeHtml("Scenario_" + scenarioDetails[0]);
						String scenarioDescription = Utils.escapeHtml(scenarioDetails[1]);
						
						long scenarioStart = result.getStartMillis();
						long scenarioDuration = result.getEndMillis() - scenarioStart;

						buffer.append("<tr class=\"").append(cssClass).append("\">").append("<td><a href=\"#m")
					      .append(scenarioIndex).append("\">").append(scenarioName).append("</a></td>")
					      .append("<td style=\"text-align: left;\">").append(scenarioDescription).append("</td>")
					      .append("<td>").append(scenarioDuration).append("</td></tr>");

						scenarioIndex++;
					}
					scenariosPerClass += resultsCount;
					methodIndex++;
				}

				// Write the test results for the class
				writer.print(buffer);
				classIndex++;
			}
			scenarioCount = scenarioIndex - startingScenarioIndex;
		}
		return scenarioCount;
	}

	private String[] getScenarioDetails(ITestResult result) {
		Object[] parameters = result.getParameters();
		Scenario s = (Scenario)parameters[1];
		
		String [] s1 = new String[2];
		s1[0] = s.getId();
		s1[1] = s.getDescription();
		
		/*
		 * String substringBeforeColon =""; Class<?> clazz = instance.getClass(); for
		 * (Field field : clazz.getDeclaredFields()) { try { field.setAccessible(true);
		 * Object value = field.get(instance); if
		 * (field.getName().equalsIgnoreCase("message")) { int colonIndex =
		 * value.toString().indexOf(":"); if (colonIndex != -1) { // Extract the
		 * substring before the colon substringBeforeColon =
		 * value.toString().substring(0, colonIndex).trim(); return
		 * substringBeforeColon; } } } catch (IllegalAccessException e) {
		 * e.printStackTrace(); } } return substringBeforeColon;
		 */
        return s1;
	}

	/**
	 * Writes the details for all test scenarios.
	 */
	protected void writeScenarioDetails() {
		int scenarioIndex = 0;
		for (SuiteResult suiteResult : suiteResults) {
			for (TestResult testResult : suiteResult.getTestResults()) {
				/*
				 * writer.print("<h2>");
				 * writer.print(Utils.escapeHtml(testResult.getTestName()));
				 * writer.print("</h2>");
				 */

				scenarioIndex += writeScenarioDetails(testResult.getFailedConfigurationResults(), scenarioIndex);
				scenarioIndex += writeScenarioDetails(testResult.getFailedTestResults(), scenarioIndex);
				scenarioIndex += writeScenarioDetails(testResult.getSkippedConfigurationResults(), scenarioIndex);
				scenarioIndex += writeScenarioDetails(testResult.getSkippedTestResults(), scenarioIndex);
				scenarioIndex += writeScenarioDetails(testResult.getPassedTestResults(), scenarioIndex);
			}
		}
	}

	/**
	 * Writes the scenario details for the results of a given state for a single
	 * test.
	 */
	private int writeScenarioDetails(List<ClassResult> classResults, int startingScenarioIndex) {
		int scenarioIndex = startingScenarioIndex;
		for (ClassResult classResult : classResults) {
			String className = classResult.getClassName();
			for (MethodResult methodResult : classResult.getMethodResults()) {
				List<ITestResult> results = methodResult.getResults();
				assert !results.isEmpty();

				String label = Utils
						.escapeHtml(className + "#" + results.iterator().next().getMethod().getMethodName());
				for (ITestResult result : results) {
					writeScenario(scenarioIndex, label, result);
					scenarioIndex++;
				}
			}
		}

		return scenarioIndex - startingScenarioIndex;
	}

	/**
	 * Writes the details for an individual test scenario.
	 */
	private void writeScenario(int scenarioIndex, String label, ITestResult result) {
		writer.print("<h3 id=\"m");
		writer.print(scenarioIndex);
		writer.print("\">");
		// writer.print(label);
		writer.print("</h3>");

		writer.print("<table class=\"result\">");

		// Write test parameters (if any)
		Object[] parameters = result.getParameters();
		int parameterCount = (parameters == null ? 0 : parameters.length);

		/*
		 * if (parameterCount > 0) { writer.print("<tr class=\"param\">"); for (int i =
		 * 1; i <= parameterCount; i++) { writer.print("<th>Parameter #");
		 * writer.print(i); writer.print("</th>"); }
		 * writer.print("</tr><tr class=\"param stripe\">"); for (Object parameter :
		 * parameters) { writer.print("<td>");
		 * writer.print(Utils.escapeHtml(Utils.toString(parameter)));
		 * writer.print("</td>"); } writer.print("</tr>"); }
		 */

		// Write reporter messages (if any)
		List<String> reporterMessages = Reporter.getOutput(result);
		if (!reporterMessages.isEmpty()) {
			writer.print("<tr><td colspan=\"" + parameterCount + "\">");
			writeReporterMessages(reporterMessages);
			writer.print("</td></tr>");
		}

		// Write exception (if any)
		Throwable throwable = result.getThrowable();
		if (throwable != null) {
			writer.print("<tr><th colspan=\"" + parameterCount + "\">"
					+ (result.getStatus() == ITestResult.SUCCESS ? "Expected Exception" : "Exception") + "</th></tr>");
			writer.print("<tr><td colspan=\"" + parameterCount + "\">");
			writeStackTrace(throwable);
			writer.print("</td></tr>");
		}

		writer.print("</table>");
		writer.print("<p class=\"totop\"><a href=\"#summary\">back to summary</a></p>");
	}

	protected void writeReporterMessages(List<String> reporterMessages) {
		writer.print("<div class=\"messages\">");
		Iterator<String> iterator = reporterMessages.iterator();
		assert iterator.hasNext();
		if (Reporter.getEscapeHtml()) {
			writer.print(Utils.escapeHtml(iterator.next()));
		} else {
			writer.print(iterator.next());
		}
		while (iterator.hasNext()) {
			writer.print("<br/>");
			if (Reporter.getEscapeHtml()) {
				writer.print(Utils.escapeHtml(iterator.next()));
			} else {
				writer.print(iterator.next());
			}
		}
		writer.print("</div>");
	}

	protected void writeStackTrace(Throwable throwable) {
		if(ConfigManager.IsDebugEnabled()) {
		writer.print("<div class=\"stacktrace\">");
		writer.print(Utils.shortStackTrace(throwable, true));
		writer.print("</div>");
		}
	}

	/**
	 * Writes a TH element with the specified contents and CSS class names.
	 * 
	 * @param html       the HTML contents
	 * @param cssClasses the space-delimited CSS classes or null if there are no
	 *                   classes to apply
	 */
	protected void writeTableHeader(String html, String cssClasses) {
		writeTag("th", html, cssClasses);
	}

	/**
	 * Writes a TD element with the specified contents.
	 * 
	 * @param html the HTML contents
	 */
	protected void writeTableData(String html) {
		writeTableData(html, null);
	}

	/**
	 * Writes a TD element with the specified contents and CSS class names.
	 * 
	 * @param html       the HTML contents
	 * @param cssClasses the space-delimited CSS classes or null if there are no
	 *                   classes to apply
	 */
	protected void writeTableData(String html, String cssClasses) {
		writeTag("td", html, cssClasses);
	}

	/**
	 * Writes an arbitrary HTML element with the specified contents and CSS class
	 * names.
	 * 
	 * @param tag        the tag name
	 * @param html       the HTML contents
	 * @param cssClasses the space-delimited CSS classes or null if there are no
	 *                   classes to apply
	 */
	protected void writeTag(String tag, String html, String cssClasses) {
		writer.print("<");
		writer.print(tag);
		if (cssClasses != null) {
			writer.print(" class=\"");
			writer.print(cssClasses);
			writer.print("\"");
		}
		writer.print(">");
		writer.print(html);
		writer.print("</");
		writer.print(tag);
		writer.print(">");
	}

	/**
	 * Groups {@link TestResult}s by suite.
	 */
	protected static class SuiteResult {
		private final String suiteName;
		private final List<TestResult> testResults = Lists.newArrayList();

		public SuiteResult(ISuite suite) {
			suiteName = suite.getName();
			for (ISuiteResult suiteResult : suite.getResults().values()) {
				testResults.add(new TestResult(suiteResult.getTestContext()));
			}
		}

		public String getSuiteName() {
			return suiteName;
		}

		/**
		 * @return the test results (possibly empty)
		 */
		public List<TestResult> getTestResults() {
			return testResults;
		}
	}

	/**
	 * Groups {@link ClassResult}s by test, type (configuration or test), and
	 * status.
	 */
	protected static class TestResult {
		/**
		 * Orders test results by class name and then by method name (in lexicographic
		 * order).
		 */
		protected static final Comparator<ITestResult> RESULT_COMPARATOR = new Comparator<ITestResult>() {
			@Override
			public int compare(ITestResult o1, ITestResult o2) {
				int result = o1.getTestClass().getName().compareTo(o2.getTestClass().getName());
				if (result == 0) {
					result = o1.getMethod().getMethodName().compareTo(o2.getMethod().getMethodName());
				}
				return result;
			}
		};

		private final String testName;
		private final List<ClassResult> failedConfigurationResults;
		private final List<ClassResult> failedTestResults;
		private final List<ClassResult> skippedConfigurationResults;
		private final List<ClassResult> skippedTestResults;
		private final List<ClassResult> passedTestResults;
		private final int failedTestCount;
		private final int skippedTestCount;
		private final int passedTestCount;
		private final long duration;
		private final String includedGroups;
		private final String excludedGroups;

		public TestResult(ITestContext context) {
			testName = context.getName();

			Set<ITestResult> failedConfigurations = context.getFailedConfigurations().getAllResults();
			Set<ITestResult> failedTests = context.getFailedTests().getAllResults();
			Set<ITestResult> skippedConfigurations = context.getSkippedConfigurations().getAllResults();
			Set<ITestResult> skippedTests = context.getSkippedTests().getAllResults();
			Set<ITestResult> passedTests = context.getPassedTests().getAllResults();

			failedConfigurationResults = groupResults(failedConfigurations);
			failedTestResults = groupResults(failedTests);
			skippedConfigurationResults = groupResults(skippedConfigurations);
			skippedTestResults = groupResults(skippedTests);
			passedTestResults = groupResults(passedTests);

			failedTestCount = failedTests.size();
			skippedTestCount = skippedTests.size();
			passedTestCount = passedTests.size();

			duration = context.getEndDate().getTime() - context.getStartDate().getTime();

			includedGroups = formatGroups(context.getIncludedGroups());
			excludedGroups = formatGroups(context.getExcludedGroups());
		}

		/**
		 * Groups test results by method and then by class.
		 */
		protected List<ClassResult> groupResults(Set<ITestResult> results) {
			List<ClassResult> classResults = Lists.newArrayList();
			if (!results.isEmpty()) {
				List<MethodResult> resultsPerClass = Lists.newArrayList();
				List<ITestResult> resultsPerMethod = Lists.newArrayList();

				List<ITestResult> resultsList = Lists.newArrayList(results);
				Collections.sort(resultsList, RESULT_COMPARATOR);
				Iterator<ITestResult> resultsIterator = resultsList.iterator();
				assert resultsIterator.hasNext();

				ITestResult result = resultsIterator.next();
				resultsPerMethod.add(result);

				String previousClassName = result.getTestClass().getName();
				String previousMethodName = result.getMethod().getMethodName();
				while (resultsIterator.hasNext()) {
					result = resultsIterator.next();

					String className = result.getTestClass().getName();
					if (!previousClassName.equals(className)) {
						// Different class implies different method
						assert !resultsPerMethod.isEmpty();
						resultsPerClass.add(new MethodResult(resultsPerMethod));
						resultsPerMethod = Lists.newArrayList();

						assert !resultsPerClass.isEmpty();
						classResults.add(new ClassResult(previousClassName, resultsPerClass));
						resultsPerClass = Lists.newArrayList();

						previousClassName = className;
						previousMethodName = result.getMethod().getMethodName();
					} else {
						String methodName = result.getMethod().getMethodName();
						if (!previousMethodName.equals(methodName)) {
							assert !resultsPerMethod.isEmpty();
							resultsPerClass.add(new MethodResult(resultsPerMethod));
							resultsPerMethod = Lists.newArrayList();

							previousMethodName = methodName;
						}
					}
					resultsPerMethod.add(result);
				}
				assert !resultsPerMethod.isEmpty();
				resultsPerClass.add(new MethodResult(resultsPerMethod));
				assert !resultsPerClass.isEmpty();
				classResults.add(new ClassResult(previousClassName, resultsPerClass));
			}
			return classResults;
		}

		public String getTestName() {
			return testName;
		}

		/**
		 * @return the results for failed configurations (possibly empty)
		 */
		public List<ClassResult> getFailedConfigurationResults() {
			return failedConfigurationResults;
		}

		/**
		 * @return the results for failed tests (possibly empty)
		 */
		public List<ClassResult> getFailedTestResults() {
			return failedTestResults;
		}

		/**
		 * @return the results for skipped configurations (possibly empty)
		 */
		public List<ClassResult> getSkippedConfigurationResults() {
			return skippedConfigurationResults;
		}

		/**
		 * @return the results for skipped tests (possibly empty)
		 */
		public List<ClassResult> getSkippedTestResults() {
			return skippedTestResults;
		}

		/**
		 * @return the results for passed tests (possibly empty)
		 */
		public List<ClassResult> getPassedTestResults() {
			return passedTestResults;
		}

		public int getFailedTestCount() {
			return failedTestCount;
		}

		public int getSkippedTestCount() {
			return skippedTestCount;
		}

		public int getPassedTestCount() {
			return passedTestCount;
		}

		public long getDuration() {
			return duration;
		}

		public String getIncludedGroups() {
			return includedGroups;
		}

		public String getExcludedGroups() {
			return excludedGroups;
		}

		/**
		 * Formats an array of groups for display.
		 */
		protected String formatGroups(String[] groups) {
			if (groups.length == 0) {
				return "";
			}

			StringBuilder builder = new StringBuilder();
			builder.append(groups[0]);
			for (int i = 1; i < groups.length; i++) {
				builder.append(", ").append(groups[i]);
			}
			return builder.toString();
		}
	}

	/**
	 * Groups {@link MethodResult}s by class.
	 */
	protected static class ClassResult {
		private final String className;
		private final List<MethodResult> methodResults;

		/**
		 * @param className     the class name
		 * @param methodResults the non-null, non-empty {@link MethodResult} list
		 */
		public ClassResult(String className, List<MethodResult> methodResults) {
			this.className = className;
			this.methodResults = methodResults;
		}

		public String getClassName() {
			return className;
		}

		/**
		 * @return the non-null, non-empty {@link MethodResult} list
		 */
		public List<MethodResult> getMethodResults() {
			return methodResults;
		}
	}

	/**
	 * Groups test results by method.
	 */
	protected static class MethodResult {
		private final List<ITestResult> results;

		/**
		 * @param results the non-null, non-empty result list
		 */
		public MethodResult(List<ITestResult> results) {
			this.results = results;
		}

		/**
		 * @return the non-null, non-empty result list
		 */
		public List<ITestResult> getResults() {
			return results;
		}
	}

}
