package io.mosip.testrig.dslrig.ivv.orchestrator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
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

import io.mosip.testrig.apirig.utils.S3Adapter;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.dslrig.ivv.core.dtos.Scenario;

/**
 * Reporter that generates a single-page HTML report of the test results.
 */
public class EmailableReport implements IReporter {
	static Logger logger = Logger.getLogger(EmailableReport.class);

	protected PrintWriter writer;

	protected final List<SuiteResult> suiteResults = Lists.newArrayList();

	private final StringBuilder buffer = new StringBuilder();

	private String fileName = "emailable-report.html";

	private static final String JVM_ARG = "emailable.report2.name";

	int totalPassedTests = 0;
	int totalIgnoredTests = 0;
	int totalSkippedTests = 0;
	int totalKnownIssuesTests = 0;
	int totalFailedTests = 0;

	static {
		if (dslConfigManager.IsDebugEnabled())
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
		String failedReportName = generateFailedAndSkippedReport(outputDirectory);

		int totalTestCases = totalPassedTests + totalSkippedTests + totalIgnoredTests + totalKnownIssuesTests
				+ totalFailedTests;
		String oldString = System.getProperty("emailable.report2.name");
		String temp = "-report_T-" + totalTestCases + "_P-" + totalPassedTests + "_KI-" + totalKnownIssuesTests + "_I-"
				+ totalIgnoredTests + "_S-" + totalSkippedTests + "_F-" + totalFailedTests;
		String newString = oldString.replace("-report", temp);

		File orignialReportFile = new File(System.getProperty("user.dir") + "/"
				+ System.getProperty("testng.outpur.dir") + "/" + System.getProperty("emailable.report2.name"));
		logger.info("reportFile is::" + System.getProperty("user.dir") + "/" + System.getProperty("testng.outpur.dir")
				+ "/" + System.getProperty("emailable.report2.name"));
		File newReportFile = new File(
				System.getProperty("user.dir") + "/" + System.getProperty("testng.outpur.dir") + "/" + newString);
		logger.info("New reportFile is::" + System.getProperty("user.dir") + "/"
				+ System.getProperty("testng.outpur.dir") + "/" + newString);
		String excelFilePath = null;
		if (orignialReportFile.exists()) {
			if (orignialReportFile.renameTo(newReportFile)) {
				orignialReportFile.delete();
				logger.info("Report File re-named successfully!");
				try {
					excelFilePath = HtmlToExcelReport.CreateExcelReport(
							System.getProperty("user.dir") + "/" + System.getProperty("testng.outpur.dir") + "/",
							newString);
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
				if (dslConfigManager.getPushReportsToS3().equalsIgnoreCase("yes")) {
					S3Adapter s3Adapter = new S3Adapter();
					boolean isStoreSuccess = false;
					boolean isStoreSuccess2 = false;
					boolean isStoreSuccess3 = false;
					boolean isStoreSuccess4 = false;

					try {

						isStoreSuccess = s3Adapter.putObject(dslConfigManager.getS3Account(), BaseTestCase.testLevel,
								null, null, newString, newReportFile);

						logger.info("Main report uploaded: " + isStoreSuccess);

						File extentReport = new File(BaseTestCaseUtil.getExtentReportName());

						isStoreSuccess2 = s3Adapter.putObject(dslConfigManager.getS3Account(), BaseTestCase.testLevel,
								null, null, "ExtentReport-" + newString, extentReport);

						logger.info("Extent report uploaded: " + isStoreSuccess2);

						isStoreSuccess3 = s3Adapter.putObject(dslConfigManager.getS3Account(), BaseTestCase.testLevel,
								null, null, "comparison_vs_BASE_LINE.xlsx", new File(excelFilePath));

						logger.info("Excel report uploaded: " + isStoreSuccess3);

						File failedReportFile = new File(System.getProperty("user.dir") + "/"
								+ System.getProperty("testng.outpur.dir") + "/" + failedReportName);

						isStoreSuccess4 = s3Adapter.putObject(dslConfigManager.getS3Account(), BaseTestCase.testLevel,
								null, null, failedReportName, failedReportFile);

						logger.info("Failed report uploaded: " + isStoreSuccess4);

					} catch (Exception e) {
						logger.error("Error occurred while pushing report to S3: " + e.getMessage());
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

			return "Commit Id : " + properties.getProperty("git.commit.id.abbrev") + " & Branch Name : "
					+ properties.getProperty("git.branch");

		} catch (IOException e) {
			logger.error("Error getting git branch information: " + e.getMessage());
			return "";
		}

	}

	private String buildFailedReportName() {

		long timestamp = System.currentTimeMillis();

		int totalTests = totalPassedTests + totalSkippedTests + totalIgnoredTests + totalKnownIssuesTests
				+ totalFailedTests;

		String env = System.getProperty("env.user").replaceAll("https?://", "").replaceAll("[^a-zA-Z0-9.-]", "");

		return "DSL-" + env + "-" + BaseTestCase.testLevel + "-error-" + timestamp + "-report_T-" + totalTests + "_P-"
				+ totalPassedTests + "_S-" + totalSkippedTests + "_F-" + totalFailedTests + ".html";
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

		writer.print("table {margin-bottom:10px;border-collapse:collapse;empty-cells:show;width:100%;}");
		writer.print("th:nth-child(3), td:nth-child(3) { width:160px; white-space:nowrap; }");
		writer.print("th, td {border:1px solid #009;padding:.25em .5em;background-color:#FFF;vertical-align:middle;}");
		writer.print("th {background-color:#f2f2f2; text-align:center; border:1px solid #ccc;}");
		writer.print("table a {font-weight:bold}");
		writer.print(".stripe tr:nth-child(odd) td {background-color: #E6EBF9}");
		writer.print(".stripe tr:nth-child(even) td {background-color: #FFF}");
		writer.print(".passedodd td, .passedeven td {background-color: #0A0; color: #FFF; text-align:center;}");
		writer.print(".skippedodd td, .skippedeven td {background-color: #FFA500; color: #FFF; text-align:center;}");
		writer.print(".ignoredodd td, .ignoredeven td {background-color: #FFA500; color: #FFF; text-align:center;}");
		writer.print(".failedodd td, .failedeven td {background-color: #eb5050; color: #FFF; text-align:center;}");
		writer.print(
				".knownissueodd td, .knownissueeven td {background-color: #fff9db; color: #333; text-align:center;}");
		writer.print(".stacktrace {white-space:pre;font-family:monospace}");
		writer.print(".totop {font-size:85%;text-align:center;border-bottom:2px solid #000}");
		writer.print(".box {padding: 10px; border-radius: 5px; color: #FFF; word-wrap: break-word; max-width: 100%;}");
		writer.print(".orange-bg {background-color: #FFA500;}");
		writer.print(".green-bg {background-color: #0A0;}");
		writer.print(".black-bg {background-color: black;}");
		writer.print(".yellow-bg {background-color: #fff9db; color: #333;}");
		writer.print(".darkgray-bg {background-color: darkgray;}");
		writer.print(".num-center {text-align:center;}");
		writer.print(".scenario-step {text-align:left;}");
		writer.print(".log-box {background-color: black;}");
		writer.print(
				".textarea-box {border:solid 1px gray; background-color: darkgray; padding: 10px; border-radius: 5px; width: 100%; resize: none;}");
		writer.print(".left-aligned {text-align:left;}");
		writer.print(".attn { background-color: #eb5050 !important; }");
		writer.print(".red-text { color: #000 !important; font-weight: normal; }");
		writer.print(".bug-column { width:180px; white-space:nowrap; text-align:center; }");
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

	private static String convertNanosToTime(long nanoseconds) {
		long totalSeconds = nanoseconds / 1_000_000_000;
		long seconds = totalSeconds % 60;
		long totalMinutes = totalSeconds / 60;
		long minutes = totalMinutes % 60;
		long hours = totalMinutes / 60; 

		// Format time into HH:MM:SS
		return String.format("%02d:%02d:%02d", hours, Math.abs(minutes), Math.abs(seconds));
	}

	public static String getExecutionTime() {
		long duration = BaseTestCaseUtil.exectionEndTime - BaseTestCaseUtil.exectionStartTime;
		long totalSeconds = duration / 1000;
		long seconds = totalSeconds % 60;
		long totalMinutes = totalSeconds / 60;
		long minutes = totalMinutes % 60;
		long hours = totalMinutes / 60; 
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}

	protected void writeSuiteSummary() {
		NumberFormat integerFormat = NumberFormat.getIntegerInstance();
		NumberFormat decimalFormat = NumberFormat.getNumberInstance();
		LocalDate currentDate = LocalDate.now();
		String formattedDate = null;
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			formattedDate = currentDate.format(formatter);
		} catch (Exception e) {
			logger.info(e);
		}
		totalPassedTests = 0;
		totalIgnoredTests = 0;
		totalSkippedTests = 0;
		totalKnownIssuesTests = 0;
		totalFailedTests = 0;
		double passPercent = 0;
		double failPercent = 0;
		double knownPercent = 0;
		double ignoredPercent = 0;
		double skippedPercent = 0;

		writer.print("<table style='width:100%; table-layout:fixed;'>");
		int testIndex = 0;

		for (SuiteResult suiteResult : suiteResults) {
			writer.print("<tr><th colspan='7'>");
			writer.print("<div style='text-align:center; padding:15px; "
					+ "background-color:#f4f6f9; border-radius:8px; " + "font-family:Arial;'>");
			writer.print("<h2 style='margin:5px; color:#2c3e50;'>DSL Scenarios Test Report</h2>");
			writer.print("<p style='margin:4px; font-size:14px;'>");
			writer.print("<b>Report Date : </b> " + formattedDate + " &nbsp; | &nbsp; ");
			writer.print("<b>Environment : </b> " + System.getProperty("env.endpoint").replaceAll("https?://", "")
					+ " &nbsp; | &nbsp; ");
			writer.print("<b>" + getCommitId() + "</b> &nbsp; | &nbsp; ");
			writer.print("<b>Thread Count : </b> " + dslConfigManager.getThreadCount());
			writer.print("</p>");
			writer.print("</div>");
			writer.print("</th></tr>");
			writer.print("<tr><th colspan='7'><strong>Summary of Test Results</strong></th></tr>");
			writer.print("<tr>");
			writer.print("<th style='text-align:center;'># Total</th>");
			writer.print("<th style='text-align:center;'># Passed</th>");
			writer.print("<th style='text-align:center;'># Ignored</th>");
			writer.print("<th style='text-align:center;'># Known Issues</th>");
			writer.print("<th style='text-align:center;'># Skipped</th>");
			writer.print("<th style='text-align:center;'># Failed</th>");
			writer.print("<th style='text-align:center;'>Time (HH:MM:SS)</th>");
			writer.print("</tr>");
			for (TestResult testResult : suiteResult.getTestResults()) {
				int passedTests = testResult.getPassedTestCount();
				int ignoredTests = testResult.getIgnoredTestCount();
				int skippedTests = testResult.getSkippedTestCount();
				int knownIssuesTests = testResult.getKnownIssuesTestCount();
				int failedTests = testResult.getFailedTestCount();
				int totalTests = passedTests + ignoredTests + skippedTests + knownIssuesTests + failedTests;

				if (totalTests > 0) {
					passPercent = (passedTests * 100.0) / totalTests;
					failPercent = (failedTests * 100.0) / totalTests;
					knownPercent = (knownIssuesTests * 100.0) / totalTests;
					ignoredPercent = (ignoredTests * 100.0) / totalTests;
					skippedPercent = (skippedTests * 100.0) / totalTests;
				}

				writer.print("<tr" + ((testIndex % 2 == 1) ? " class='stripe'" : "") + ">");

				buffer.setLength(0);
				writeTableData(decimalFormat.format(totalTests), "num num-center");
				String passedDisplay = passedTests + " (" + String.format("%.0f", passPercent) + "%)";
				String ignoredDisplay = ignoredTests + " (" + String.format("%.0f", ignoredPercent) + "%)";
				String knownDisplay = knownIssuesTests + " (" + String.format("%.0f", knownPercent) + "%)";
				String skippedDisplay = skippedTests + " (" + String.format("%.0f", skippedPercent) + "%)";
				String failedDisplay = failedTests + " (" + String.format("%.0f", failPercent) + "%)";

				writeTableData(passedDisplay, (passedTests > 0 ? "num green-bg num-center" : "num num-center"));

				writeTableData(ignoredDisplay, (ignoredTests > 0 ? "num orange-bg num-center" : "num num-center"));

				writeTableData(knownDisplay, (knownIssuesTests > 0 ? "num yellow-bg num-center" : "num num-center"));

				writeTableData(skippedDisplay, (skippedTests > 0 ? "num orange-bg num-center" : "num num-center"));

				writeTableData(failedDisplay, (failedTests > 0 ? "num attn num-center red-text" : "num num-center"));

				writeTableData(getExecutionTime(), "num num-center");
				writer.print("</tr>");
				totalPassedTests += passedTests;
				totalIgnoredTests += ignoredTests;
				totalKnownIssuesTests += knownIssuesTests;
				totalSkippedTests += skippedTests;
				totalFailedTests += failedTests;
				testIndex++;
			}
		}

		if (testIndex > 1) {
			writer.print("<tr>");
			writer.print("<th>Total</th>");
			writeTableHeader(integerFormat.format(totalPassedTests), "num num-center");
			writeTableHeader(integerFormat.format(totalIgnoredTests), "num num-center");
			writeTableHeader(integerFormat.format(totalKnownIssuesTests), "num num-center");
			writeTableHeader(integerFormat.format(totalSkippedTests), "num num-center");
			writeTableHeader(integerFormat.format(totalFailedTests), "num num-center");
			writeTableHeader(getExecutionTime(), "num num-center");
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
		writer.print("<th>Scenario</th>");
		writer.print("<th>Scenario Description</th>");
		writer.print("<th class='bug-column'>Time / Bug ID</th>");
		writer.print("</tr>");
		writer.print("</thead>");

		int testIndex = 0;
		int scenarioIndex = 0;
		for (SuiteResult suiteResult : suiteResults) {
			for (TestResult testResult : suiteResult.getTestResults()) {
				writer.print("<tbody id=\"t");
				writer.print(testIndex);
				writer.print("\">");

				String testName = Utils.escapeHtml("Scenarios");
				scenarioIndex += writeScenarioSummary(testName + " &#8212; Failed", testResult.getFailedTestResults(),
						"failed", scenarioIndex);
				scenarioIndex += writeScenarioSummary(testName + " &#8212; Ignored", testResult.getIgnoredTestResults(),
						"ignored", scenarioIndex);
				scenarioIndex += writeScenarioSummary(testName + " &#8212; Known Issues",
						testResult.getknownIssuesTestResults(), "knownissue", scenarioIndex);
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

					for (int i = 0; i < resultsCount; i++) {

						ITestResult result = results.get(i);
						String[] scenarioDetails = getScenarioDetails(result);

						String scenarioName = Utils.escapeHtml("Scenario_" + scenarioDetails[0]);
						String scenarioDescription = Utils.escapeHtml(scenarioDetails[1]);

						String scenarioStart = BaseTestCaseUtil.sceanrioExecutionStatistics
								.get("Scenario_" + scenarioDetails[0] + "_startTime");
						String scenarioEnd = BaseTestCaseUtil.sceanrioExecutionStatistics
								.get("Scenario_" + scenarioDetails[0] + "_endTime");
						long endTime;
						if (scenarioEnd == null || scenarioEnd.isEmpty())
							endTime = System.nanoTime();
						else
							endTime = Long.parseLong(scenarioEnd);

						long startTime = Long.parseLong(scenarioStart);
						long scenarioDuration = endTime - startTime;

						String displayValue;

						if ("knownissue".equals(cssClassPrefix)) {

							String bugId = dslConfigManager.getBugId("S-" + scenarioDetails[0]);

							if (bugId != null && !bugId.isEmpty()) {
								displayValue = "<a href='https://mosip.atlassian.net/browse/" + bugId
										+ "' target='_blank' style='text-decoration:none;'>" + "🔗 " + bugId + "</a>";

							} else {
								displayValue = "NA";
							}

						} else {
							displayValue = convertNanosToTime(scenarioDuration);
						}

						buffer.append("<tr class=\"").append(cssClass).append("\">").append("<td><a href=\"#m")
								.append(scenarioIndex).append("\">").append(scenarioName).append("</a></td>")
								.append("<td style=\"text-align: left;\">").append(scenarioDescription).append("</td>")
								.append("<td class='bug-column'>").append(displayValue).append("</td></tr>");

						scenarioIndex++;
					}
					scenariosPerClass += resultsCount;
					methodIndex++;
				}
				writer.print(buffer);
				classIndex++;
			}
			scenarioCount = scenarioIndex - startingScenarioIndex;
		}
		return scenarioCount;
	}

	private String[] getScenarioDetails(ITestResult result) {
		Object[] parameters = result.getParameters();
		Scenario s = (Scenario) parameters[1];

		String[] s1 = new String[2];
		s1[0] = s.getId();
		s1[1] = s.getDescription();
		return s1;
	}

	private String generateFailedAndSkippedReport(String outputDirectory) {

	    PrintWriter originalWriter = writer;
	    String failedReportName = buildFailedReportName();

	    try {

	        writer = new PrintWriter(
	                new BufferedWriter(
	                        new FileWriter(new File(outputDirectory, failedReportName))));

	        writeDocumentStart();
	        writeHead();

	        writer.print("<body>");

	        writeSuiteSummaryForFailedReport();
	        writeScenarioSummaryForFailedReport();
	        writeScenarioDetailsForFailedReport();

	        writer.print("</body>");
	        writeDocumentEnd();

	        writer.close();

	        logger.info("Failed + Skipped report generated: " + failedReportName);

	    } catch (Exception e) {
	        logger.error("Error generating failed/skipped report", e);
	    }

	    writer = originalWriter;
	    return failedReportName;
	}


	protected void writeSuiteSummaryForFailedReport() {

		writer.print("<table style='width:100%; table-layout:fixed;'>");

		writer.print("<tr><th colspan='4'>Failed & Skipped Summary</th></tr>");

		writer.print("<tr>");
		writer.print("<th># Failed</th>");
		writer.print("<th># Skipped</th>");
		writer.print("<th>Total</th>");
		writer.print("<th>Time</th>");
		writer.print("</tr>");

		int failed = 0;
		int skipped = 0;

		for (SuiteResult suiteResult : suiteResults) {
			for (TestResult testResult : suiteResult.getTestResults()) {
				failed += testResult.getFailedTestCount();
				skipped += testResult.getSkippedTestCount();
			}
		}

		int total = failed + skipped;

		writer.print("<tr>");
		writeTableData(String.valueOf(failed), "num attn num-center red-text");
		writeTableData(String.valueOf(skipped), "num orange-bg num-center");
		writeTableData(String.valueOf(total), "num num-center");
		writeTableData(getExecutionTime(), "num num-center");
		writer.print("</tr>");

		writer.print("</table>");
	}

	protected void writeScenarioSummaryForFailedReport() {

		writer.print("<table id='summary'>");
		writer.print("<thead>");
		writer.print("<tr>");
		writer.print("<th>Scenario</th>");
		writer.print("<th>Description</th>");
		writer.print("<th>Time</th>");
		writer.print("</tr>");
		writer.print("</thead>");

		int scenarioIndex = 0;

		for (SuiteResult suiteResult : suiteResults) {
			for (TestResult testResult : suiteResult.getTestResults()) {

				scenarioIndex += writeScenarioSummary("Failed Scenarios", testResult.getFailedTestResults(), "failed",
						scenarioIndex);

				scenarioIndex += writeScenarioSummary("Skipped Scenarios", testResult.getSkippedTestResults(),
						"skipped", scenarioIndex);
			}
		}

		writer.print("</table>");
	}

	protected void writeScenarioDetailsForFailedReport() {

		int scenarioIndex = 0;

		for (SuiteResult suiteResult : suiteResults) {
			for (TestResult testResult : suiteResult.getTestResults()) {

				scenarioIndex += writeScenarioDetails(testResult.getFailedTestResults(), scenarioIndex);

				scenarioIndex += writeScenarioDetails(testResult.getSkippedTestResults(), scenarioIndex);
			}
		}
	}

	/**
	 * Writes the details for all test scenarios.
	 */
	protected void writeScenarioDetails() {
		int scenarioIndex = 0;
		for (SuiteResult suiteResult : suiteResults) {
			for (TestResult testResult : suiteResult.getTestResults()) {
				scenarioIndex += writeScenarioDetails(testResult.getFailedConfigurationResults(), scenarioIndex);
				scenarioIndex += writeScenarioDetails(testResult.getFailedTestResults(), scenarioIndex);
				scenarioIndex += writeScenarioDetails(testResult.getIgnoredConfigurationResults(), scenarioIndex);
				scenarioIndex += writeScenarioDetails(testResult.getknownIssuesTestResults(), scenarioIndex);
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
		if (dslConfigManager.IsDebugEnabled()) {
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
		private final List<ClassResult> ignoredTestResults;
		private final List<ClassResult> skippedTestResults;
		private final List<ClassResult> knownIssuesTestResults;
		private final List<ClassResult> passedTestResults;
		private final int failedTestCount;
		private final int ignoredTestCount;
		private final int skipTestCount;
		private final int KnownIssuesTestsCount;
		private final int passedTestCount;
		private final long duration;
		private final String includedGroups;
		private final String excludedGroups;

		public TestResult(ITestContext context) {
			testName = context.getName();

			Set<ITestResult> failedConfigurations = context.getFailedConfigurations().getAllResults();
			Set<ITestResult> failedTests = context.getFailedTests().getAllResults();
			Set<ITestResult> SkippedConfigurations = context.getSkippedConfigurations().getAllResults();
			Set<ITestResult> knownIssueTests = getResultsSubSet(context.getSkippedTests().getAllResults(),
					GlobalConstants.KNOWN_ISSUES_STRING);
			Set<ITestResult> ignoredTests = getResultsSubSet(context.getSkippedTests().getAllResults(),
					GlobalConstants.IGNORED_SUBSET_STRING);
			Set<ITestResult> skippedTests = getResultsSubSet(context.getSkippedTests().getAllResults(),
					GlobalConstants.SKIPPED_SUBSET_STRING);

			Set<ITestResult> passedTests = context.getPassedTests().getAllResults();

			failedConfigurationResults = groupResults(failedConfigurations);
			failedTestResults = groupResults(failedTests);
			skippedConfigurationResults = groupResults(SkippedConfigurations);
			ignoredTestResults = groupResults(ignoredTests);
			skippedTestResults = groupResults(skippedTests);
			knownIssuesTestResults = groupResults(knownIssueTests);
			passedTestResults = groupResults(passedTests);

			failedTestCount = failedTests.size();
			ignoredTestCount = ignoredTests.size();
			skipTestCount = skippedTests.size();
			passedTestCount = passedTests.size();
			KnownIssuesTestsCount = knownIssueTests.size();

			duration = context.getEndDate().getTime() - context.getStartDate().getTime();

			includedGroups = formatGroups(context.getIncludedGroups());
			excludedGroups = formatGroups(context.getExcludedGroups());
		}

		protected static Set<ITestResult> getResultsSubSet(Set<ITestResult> resultsSet, String subSetString) {
			List<ITestResult> testResultsSubList = Lists.newArrayList();
			if (!resultsSet.isEmpty()) {
				List<ITestResult> resultsList = Lists.newArrayList(resultsSet);
				Iterator<ITestResult> resultsIterator = resultsList.iterator();
				while (resultsIterator.hasNext()) {
					ITestResult result = resultsIterator.next();
					Throwable throwable = result.getThrowable();
					if (throwable != null) {
						if (subSetString.contains(GlobalConstants.KNOWN_ISSUES_STRING)) {
							if (containsAny(throwable.getMessage(), subSetString)) {
								testResultsSubList.add(result);
							}
						}
						if (subSetString.contains(GlobalConstants.IGNORED_SUBSET_STRING)) {
							if (containsAny(throwable.getMessage(), subSetString)) {
								testResultsSubList.add(result);
							}
						}
						if (subSetString.contains(GlobalConstants.SKIPPED_SUBSET_STRING)) {
							if (containsAny(throwable.getMessage(), subSetString)) {
								testResultsSubList.add(result);
							}
						}
					}
				}
			}
			Set<ITestResult> testResultsSubSet = Set.copyOf(testResultsSubList);
			return testResultsSubSet;
		}

		public static boolean containsAny(String stringToCheckIn, String delimitedString) {
			if (stringToCheckIn == null) return false;
			String[] stringsToCheckFor = delimitedString.split(";");

			for (String str : stringsToCheckFor) {
				if (stringToCheckIn.contains(str)) {
					return true; // If any string is found, return true
				}
			}
			return false; // If none of the strings are found, return false
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
		public List<ClassResult> getIgnoredConfigurationResults() {
			return ignoredTestResults;
		}

		public List<ClassResult> getknownIssuesTestResults() {
			return knownIssuesTestResults;
		}

		/**
		 * @return the results for skipped tests (possibly empty)
		 */
		public List<ClassResult> getIgnoredTestResults() {
			return ignoredTestResults;
		}

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

		public int getIgnoredTestCount() {
			return ignoredTestCount;
		}

		public int getSkippedTestCount() {
			return skipTestCount;
		}

		public int getKnownIssuesTestCount() {
			return KnownIssuesTestsCount;
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
