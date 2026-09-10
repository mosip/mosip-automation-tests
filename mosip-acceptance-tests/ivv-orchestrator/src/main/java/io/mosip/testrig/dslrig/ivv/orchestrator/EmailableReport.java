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


public class EmailableReport implements IReporter {
	static Logger logger = Logger.getLogger(EmailableReport.class);

	private static final Logger REPORT_TREE_LOG = Logger.getLogger("io.mosip.dsl.ivv.report.treeview");

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
	private long totalScenarioDuration = 0;
	private long fastestScenarioDuration = Long.MAX_VALUE;
	private long slowestScenarioDuration = Long.MIN_VALUE;
	private String fastestScenarioName = "";
	private String slowestScenarioName = "";
	private int totalScenarioCount = 0;

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
		REPORT_TREE_LOG.setLevel(Level.INFO);
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
		try {
			writeDocumentStart();
			writeHead();
			writeBody();
			writeDocumentEnd();
		} catch (Exception e) {
			logger.error("[REPORT-INTEGRITY] generateReport() failed during HTML body generation; "
					+ "the report file may be incomplete. Cause: " + e.getMessage(), e);
		} finally {
			writer.close();
		}

		File primaryReportFile = new File(outputDirectory, fileName).getAbsoluteFile();
		applyTreeViewEnhancement(primaryReportFile, "primary emailable report");

		String failedReportName = generateFailedAndSkippedReport(outputDirectory);

		File failedDigestReportFile = new File(outputDirectory, failedReportName).getAbsoluteFile();
		applyTreeViewEnhancement(failedDigestReportFile, "failed and skipped digest report");

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
					// Upload each artifact independently so one failure (e.g. missing Excel
					// baseline in Docker) does not skip the error report.
					uploadReportToS3(s3Adapter, "Main report", newString, newReportFile);

					String extentReportName = BaseTestCaseUtil.getExtentReportName();
					if (extentReportName != null && !extentReportName.isBlank()) {
						uploadReportToS3(s3Adapter, "Extent report", "ExtentReport-" + newString,
								new File(extentReportName));
					} else {
						logger.warn("Extent report name is not set; skipping S3 upload for extent report");
					}

					if (excelFilePath != null) {
						uploadReportToS3(s3Adapter, "Excel report", "comparison_vs_BASE_LINE.xlsx",
								new File(excelFilePath));
					} else {
						logger.warn("Excel report path is null; skipping S3 upload for comparison_vs_BASE_LINE.xlsx");
					}

					// Prefer the file already written under TestNG outputDirectory
					uploadReportToS3(s3Adapter, "Failed/error report", failedReportName, failedDigestReportFile);
				}

			} else {
				logger.error("Renamed report file doesn't exist");
			}
		} else {
			logger.error("Original report File does not exist!");
		}
	}

	private void uploadReportToS3(S3Adapter s3Adapter, String reportLabel, String objectName, File reportFile) {
		if (reportFile == null || !reportFile.isFile()) {
			logger.warn(reportLabel + " not found for S3 upload"
					+ (reportFile == null ? "" : " at " + reportFile.getAbsolutePath()) + "; skip.");
			return;
		}
		try {
			boolean uploaded = s3Adapter.putObject(dslConfigManager.getS3Account(), BaseTestCase.testLevel, null, null,
					objectName, reportFile);
			logger.info(reportLabel + " uploaded to S3 (" + objectName + "): " + uploaded);
		} catch (Exception e) {
			logger.error("Error uploading " + reportLabel + " to S3 (" + objectName + "): " + e.getMessage(), e);
		}
	}

	private void applyTreeViewEnhancement(File reportFile, String reportLabel) {
		if (reportFile == null) {
			REPORT_TREE_LOG.warn("DSL report tree-view: no file for " + reportLabel + "; skip.");
			return;
		}
		if (!reportFile.isFile()) {
			REPORT_TREE_LOG.warn(
					"DSL report tree-view: original report not found for " + reportLabel + " at "
							+ reportFile.getAbsolutePath() + "; skip.");
			return;
		}
		REPORT_TREE_LOG.info(
				"DSL report tree-view: original plain report detected (" + reportLabel + "): "
						+ reportFile.getAbsolutePath());
		REPORT_TREE_LOG.info("DSL report tree-view: enhancement starting for " + reportLabel + ".");
		boolean ok = ReportTreeViewEnhancer.enhanceReportFileInPlace(reportFile, REPORT_TREE_LOG);
		if (ok) {
			REPORT_TREE_LOG.info(
					"DSL report tree-view: enhancement finished for " + reportLabel
							+ "; plain HTML was removed after successful replace in place.");
		} else {
			REPORT_TREE_LOG.error(
					"DSL report tree-view: enhancement did not complete for " + reportLabel
							+ "; plain report file left unchanged.");
		}
	}

	private String getGitProperty(String key) {
		Properties properties = new Properties();
		try (InputStream is = EmailableReport.class.getClassLoader().getResourceAsStream("git.properties")) {

			if (is != null) {
				properties.load(is);
				return properties.getProperty(key);
			}
		} catch (IOException e) {
			logger.error("Error reading git.properties: " + e.getMessage());
		}
		return null;
	}

	private String buildFailedReportName() {

		long timestamp = System.currentTimeMillis();

		int totalTests = totalPassedTests + totalSkippedTests + totalIgnoredTests + totalKnownIssuesTests
				+ totalFailedTests;

		String envProperty = System.getProperty("env.user");
		String env = (envProperty == null || envProperty.isBlank())
				? "unknown"
				: envProperty.replaceAll("https?://", "").replaceAll("[^a-zA-Z0-9.-]", "");

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
		writer.print(
				"table:not(#summary) th:nth-child(3), table:not(#summary) td:nth-child(3) { width:160px; white-space:nowrap; }");
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
		writer.print(".blue-bg {background-color: #dbeafe; color: #1e3a5f;}");
		writer.print(".darkgray-bg {background-color: darkgray;}");
		writer.print(".num-center {text-align:center;}");
		writer.print(".scenario-step {text-align:left;}");
		writer.print(".log-box {background-color: black;}");
		writer.print(
				".textarea-box {border:solid 1px gray; background-color: darkgray; padding: 10px; border-radius: 5px; width: 100%; resize: none;}");
		writer.print(".left-aligned {text-align:left;}");
		writer.print(".attn { background-color: #eb5050 !important; }");
		writer.print(".red-text { color: #000 !important; font-weight: normal; }");
		writer.print(".bug-column { width:150px; min-width:150px; white-space:nowrap; text-align:center; overflow:visible; }");
		writer.print(
				".dsl-scenario-summary-wrap { border:1px solid #d1d1d1; border-radius:8px; overflow:hidden; margin:12px 0 18px; background:#fff; }");
		writer.print(".dsl-scenario-summary-wrap #summary.scenario-table { table-layout:fixed; width:100%; margin-bottom:0; }");
		writer.print(
				".dsl-scenario-summary-wrap #summary.scenario-table th:nth-child(1), .dsl-scenario-summary-wrap #summary.scenario-table td:nth-child(1) { width:12%; min-width:120px; text-align:center; }");
		writer.print(
				".dsl-scenario-summary-wrap #summary.scenario-table th:nth-child(2) { width:74%; vertical-align:top; text-align:center; }");
		writer.print(
				".dsl-scenario-summary-wrap #summary.scenario-table td:nth-child(2) { width:74%; vertical-align:top; text-align:left; }");
		writer.print(
				".dsl-scenario-summary-wrap #summary.scenario-table td:nth-child(2), .dsl-scenario-summary-wrap #summary.scenario-table td.scenario-desc-col, .dsl-scenario-summary-wrap #summary.scenario-table td:nth-child(2) * { white-space:normal !important; overflow:visible !important; text-overflow:clip !important; word-break:break-word !important; overflow-wrap:anywhere !important; }");
		writer.print(
				".dsl-scenario-summary-wrap #summary.scenario-table th:nth-child(3), .dsl-scenario-summary-wrap #summary.scenario-table td:nth-child(3) { width:14%; min-width:150px; text-align:center; vertical-align:top; overflow:visible; white-space:nowrap; }");
		writer.print(
				".dsl-scenario-summary-wrap #summary thead th { background:#d6dfe8 !important; color:#1a1a1a !important; font-weight:700; text-transform:uppercase; letter-spacing:0.05em; text-align:center !important; border-color:#d1d1d1 !important; }");
		writer.print(
				".dsl-scenario-summary-wrap #summary tbody tr.summary-section-header > th { background:#eceff1 !important; color:#1a1a1a !important; font-weight:700; text-align:center !important; border-color:#d1d1d1 !important; }");
		writer.print(
				".dsl-scenario-summary-wrap #summary .passedodd td, .dsl-scenario-summary-wrap #summary .passedeven td { background-color:#dff7ea !important; color:#111 !important; text-align:left !important; border-color:#d1d1d1 !important; }");
		writer.print(".dsl-scenario-summary-wrap #summary .passedeven td { background-color:#ecfdf5 !important; }");
		writer.print(
				".dsl-scenario-summary-wrap #summary .failedodd td, .dsl-scenario-summary-wrap #summary .failedeven td { background-color:#fee2e2 !important; color:#7f1d1d !important; text-align:left !important; border-color:#d1d1d1 !important; }");
		writer.print(
				".dsl-scenario-summary-wrap #summary .skippedodd td, .dsl-scenario-summary-wrap #summary .skippedeven td, .dsl-scenario-summary-wrap #summary .ignoredodd td, .dsl-scenario-summary-wrap #summary .ignoredeven td { background-color:#fff4e6 !important; color:#7c2d12 !important; text-align:left !important; border-color:#d1d1d1 !important; }");
		writer.print(
				".dsl-scenario-summary-wrap #summary .knownissueodd td, .dsl-scenario-summary-wrap #summary .knownissueeven td { background-color:#fef9c3 !important; color:#422006 !important; text-align:left !important; border-color:#d1d1d1 !important; }");
		writer.print(
				".dsl-scenario-summary-wrap #summary tbody td:nth-child(1) a { color:#1565c0 !important; text-decoration:underline !important; font-weight:600; }");
		writer.print(
				".dsl-scenario-summary-wrap #summary .bug-column a { text-decoration:underline !important; }");
		writer.print("</style>");

	}

	protected void writeBody() {
		writer.print("<body>");
		calculateExecutionStats();
		writeSuiteSummary();
		writeScenarioSummary();
		writeScenarioDetails();
		writer.print("</body>");
	}

	protected void writeDocumentEnd() {
		writer.print("</html>");
	}

	public static String getExecutionTime() {
		long startTime = BaseTestCaseUtil.exectionStartTime;
		long endTime = BaseTestCaseUtil.exectionEndTime;
		// Fallback when @AfterSuite has not set end time yet (e.g. report runs first,
		// or suite exited early). Always prefer a positive wall-clock duration.
		if (endTime <= 0L) {
			endTime = System.currentTimeMillis();
			BaseTestCaseUtil.exectionEndTime = endTime;
		}
		if (startTime <= 0L) {
			return "00:00:00";
		}
		long duration = Math.max(0L, endTime - startTime);
		long totalSeconds = duration / 1000;
		long seconds = totalSeconds % 60;
		long totalMinutes = totalSeconds / 60;
		long minutes = totalMinutes % 60;
		long hours = totalMinutes / 60;
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}

	private String getDockerImage() {
		String docker = System.getenv("DOCKER_IMAGE");
		if (docker == null || docker.isEmpty()) {
			return "Not Available";
		}
		return docker;
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
		String commitId = getGitProperty("git.commit.id.abbrev");
		String branch = getGitProperty("git.branch");

		String commitUrl = "https://github.com/mosip/mosip-automation-tests/commit/" + commitId;
		String branchUrl = "https://github.com/mosip/mosip-automation-tests/tree/" + branch;

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
			writer.print("<div class=\"report-hero\">");
			writer.print("<h2 class=\"report-hero__title main-title\">DSL Scenarios Test Report</h2>");
			writer.print("<p class=\"report-hero__meta\">");
			String endpoint = System.getProperty("env.endpoint");
			endpoint = endpoint.replaceAll("https?://", "");
			endpoint = endpoint.replace("api-internal.", "");
			if (!endpoint.endsWith("/")) {
				endpoint = endpoint + "/";
			}
			String finalUrl = "https://" + endpoint;
			writer.print("</p>");
			writer.print("</div>");
			writer.print("</th></tr>");


			writer.print("<tr>");
			writer.print("<td colspan='7' class=\"exec-section-title section-title report-section-bar\">");
			writer.print("Execution Details");
			writer.print("</td>");
			writer.print("</tr>");


			long averageDuration = 0;
			if (totalScenarioCount > 0) {
				averageDuration = totalScenarioDuration / totalScenarioCount;
			}

			String fastestDisplay = "NA";
			if (fastestScenarioDuration != Long.MAX_VALUE) {
				fastestDisplay = fastestScenarioName + " (" + PacketUtility.convertNanosToTime(fastestScenarioDuration)
						+ ")";
			}

			String slowestDisplay = "NA";
			if (slowestScenarioDuration != Long.MIN_VALUE) {
				slowestDisplay = slowestScenarioName + " (" + PacketUtility.convertNanosToTime(slowestScenarioDuration)
						+ ")";
			}

			String host;
			try {
				host = java.net.InetAddress.getLocalHost().getHostName();
			} catch (Exception e) {
				host = "Unknown";
			}

			String dockerImage = System.getenv("DOCKER_IMAGE");
			if (dockerImage == null || dockerImage.isEmpty()) {
				dockerImage = "Local Execution";
			}


			String rowOpen = "<tr class=\"exec-kv-row\">";


			writer.print(rowOpen);
			writer.print("<td colspan='3'><b>📅 Report Date</b></td>");
			writer.print("<td colspan='4'>" + formattedDate + "</td>");
			writer.print("</tr>");

			writer.print(rowOpen);
			writer.print("<td colspan='3'><b>🌐 Environment</b></td>");
			writer.print("<td colspan='4'><a href='" + finalUrl + "' target='_blank' "
			        + "style='color:#2E86C1;text-decoration:none;font-weight:bold;'>"
			        + finalUrl + "</a></td>");
			writer.print("</tr>");

			writer.print(rowOpen);
			writer.print("<td colspan='3'><b>🔖 Commit Id</b></td>");
			writer.print("<td colspan='4'><a href='" + commitUrl + "' target='_blank' "
			        + "style='color:#2E86C1;text-decoration:none;font-weight:bold;'>"
			        + commitId + "</a></td>");
			writer.print("</tr>");

			writer.print(rowOpen);
			writer.print("<td colspan='3'><b>🌿 Branch</b></td>");
			writer.print("<td colspan='4'><a href='" + branchUrl + "' target='_blank' "
			        + "style='color:#2E86C1;text-decoration:none;font-weight:bold;'>"
			        + branch + "</a></td>");
			writer.print("</tr>");

			writer.print(rowOpen);
			writer.print("<td colspan='3'><b>🧵 Thread Count</b></td>");
			writer.print("<td colspan='4'>" + dslConfigManager.getThreadCount() + "</td>");
			writer.print("</tr>");

			writer.print(rowOpen);
			writer.print("<td colspan='3'><b>⏱️ Average Scenario Time</b></td>");
			writer.print("<td colspan='4'>" + PacketUtility.convertNanosToTime(averageDuration) + "</td>");
			writer.print("</tr>");

			writer.print(rowOpen);
			writer.print("<td colspan='3'><b>⚡ Fastest Scenario</b></td>");
			writer.print("<td colspan='4'>" + fastestDisplay + "</td>");
			writer.print("</tr>");

			writer.print(rowOpen);
			writer.print("<td colspan='3'><b>🐢 Slowest Scenario</b></td>");
			writer.print("<td colspan='4'>" + slowestDisplay + "</td>");
			writer.print("</tr>");

			writer.print(rowOpen);
			writer.print("<td colspan='3'><b>☕ Java</b></td>");
			writer.print("<td colspan='4'>" + System.getProperty("java.version") + "</td>");
			writer.print("</tr>");

			writer.print(rowOpen);
			writer.print("<td colspan='3'><b>💻 OS</b></td>");
			writer.print("<td colspan='4'>" + System.getProperty("os.name") + "</td>");
			writer.print("</tr>");

			writer.print(rowOpen);
			writer.print("<td colspan='3'><b>🖥️ Host</b></td>");
			writer.print("<td colspan='4'>" + host + "</td>");
			writer.print("</tr>");

			writer.print("<tr class=\"exec-kv-row exec-kv-row--footer\">");
			writer.print("<td colspan='3'><b>🐳 Docker Image</b></td>");
			writer.print("<td colspan='4'>" + dockerImage + "</td>");
			writer.print("</tr>");

			writer.print("<tr><td colspan='7' class=\"exec-separator\" style='background:#AED6F1; height:2px; padding:0;'></td></tr>");
			writer.print("<tr id=\"report-exec-summary\">");
			writer.print("<td colspan='7' class=\"exec-section-title section-title\" style='background:#D6EAF8; font-size:13px; font-weight:700; "
					+ "padding:8px 10px; border:1px solid #AED6F1; text-align:center; color:#000000; "
					+ "text-transform:uppercase; letter-spacing:0.06em;'>");
			writer.print("Summary of Test Results");
			writer.print("</td>");
			writer.print("</tr>");
			writer.print("<tr>");
			writer.print("<td colspan='7' class=\"exec-legend\" style='background:#FFFFFF; padding:10px 12px; font-size:12px; text-align:left; border:1px solid #AED6F1;'>");
			writer.print("<b>Result Legend:</b><br/>");
			writer.print("&#9989; <b>Passed</b> – Scenario executed successfully.<br/>");
			writer.print("&#10060; <b>Failed</b> – Scenario executed but failed due to validation or exception.<br/>");
			writer.print("&#9888; <b> Known Issue</b> – Scenario failed but mapped to an existing bug ID.<br/>");
			writer.print("&#9193; <b>Skipped</b> – Scenario skipped due to runtime issues (e.g., before suite failure, execution timeout > 2 hours).<br/>");
			writer.print("&#128683; <b>Ignored</b> – Scenario intentionally excluded from execution via configuration.");
			writer.print("</td>");
			writer.print("</tr>");
			writer.print("<tr class=\"exec-stats-header-row\">");
			writer.print("<th style='text-align:center;'>📊  Total</th>");
			writer.print("<th style='text-align:center;'>✅  Passed</th>");
			writer.print("<th style='text-align:center;'>🚫  Ignored</th>");
			writer.print("<th style='text-align:center;'>⚠️  Known Issues</th>");
			writer.print("<th style='text-align:center;'>⏭️  Skipped</th>");
			writer.print("<th style='text-align:center;'>❌  Failed</th>");
			writer.print("<th style='text-align:center;'>⏱️ Time (HH:MM:SS)</th>");
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

				writer.print("<tr class=\"exec-stats-data-row"
						+ ((testIndex % 2 == 1) ? " stripe" : "") + "\">");

				buffer.setLength(0);
				writeTableData(decimalFormat.format(totalTests), "num num-center");
				String passedDisplay = passedTests + " (" + String.format("%.0f", passPercent) + "%)";
				String ignoredDisplay = ignoredTests + " (" + String.format("%.0f", ignoredPercent) + "%)";
				String knownDisplay = knownIssuesTests + " (" + String.format("%.0f", knownPercent) + "%)";
				String skippedDisplay = skippedTests + " (" + String.format("%.0f", skippedPercent) + "%)";
				String failedDisplay = failedTests + " (" + String.format("%.0f", failPercent) + "%)";

				writeTableData(passedDisplay, "num green-bg num-center");
				writeTableData(ignoredDisplay, "num orange-bg num-center");
				writeTableData(knownDisplay, "num yellow-bg num-center");
				writeTableData(skippedDisplay, "num blue-bg num-center");
				writeTableData(failedDisplay, "num attn num-center red-text");

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


	protected void writeScenarioSummary() {
		writer.print("<div class=\"dsl-scenario-summary-wrap\">");
		writer.print("<table id='summary' class='scenario-table'>");
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
		writer.print("</div>");

	}

	private void calculateExecutionStats() {

		totalScenarioDuration = 0;
		totalScenarioCount = 0;
		fastestScenarioDuration = Long.MAX_VALUE;
		slowestScenarioDuration = Long.MIN_VALUE;

		for (SuiteResult suiteResult : suiteResults) {
			for (TestResult testResult : suiteResult.getTestResults()) {

				calculateFromClassResults(testResult.getFailedTestResults());
				calculateFromClassResults(testResult.getPassedTestResults());
				calculateFromClassResults(testResult.getSkippedTestResults());
				calculateFromClassResults(testResult.getknownIssuesTestResults());
				calculateFromClassResults(testResult.getIgnoredTestResults());
			}
		}
	}

	private void calculateFromClassResults(List<ClassResult> classResults) {

		for (ClassResult classResult : classResults) {
			for (MethodResult methodResult : classResult.getMethodResults()) {
				for (ITestResult result : methodResult.getResults()) {

					String[] scenarioDetails = getScenarioDetails(result);

					// Skip placeholder entries produced for results with no Scenario parameter.
					if ("UNKNOWN".equals(scenarioDetails[0])) {
						continue;
					}

					String scenarioStart = BaseTestCaseUtil.sceanrioExecutionStatistics
							.get("Scenario_" + scenarioDetails[0] + "_startTime");
					String scenarioEnd = BaseTestCaseUtil.sceanrioExecutionStatistics
							.get("Scenario_" + scenarioDetails[0] + "_endTime");

					if (scenarioStart == null)
						continue;

					long startTime = Long.parseLong(scenarioStart);
					long endTime = (scenarioEnd == null) ? System.nanoTime() : Long.parseLong(scenarioEnd);

					long scenarioDuration = endTime - startTime;

					totalScenarioDuration += scenarioDuration;
					totalScenarioCount++;

					if (scenarioDuration < fastestScenarioDuration) {
						fastestScenarioDuration = scenarioDuration;
						fastestScenarioName = "Scenario_" + scenarioDetails[0];
					}

					if (scenarioDuration > slowestScenarioDuration) {
						slowestScenarioDuration = scenarioDuration;
						slowestScenarioName = "Scenario_" + scenarioDetails[0];
					}
				}
			}
		}
	}

	private int writeScenarioSummary(String description, List<ClassResult> classResults, String cssClassPrefix,
			int startingScenarioIndex) {
		int scenarioCount = 0;
		if (!classResults.isEmpty()) {
			writer.print(
					"<tr class=\"summary-section-header summary-section-header--" + cssClassPrefix + "\"><th colspan=\"3\">");
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
						String scenarioDesc = scenarioDetails[1] != null ? scenarioDetails[1] : "";
						String scenarioDescEscaped = Utils.escapeHtml(scenarioDesc);

						String scenarioStart = BaseTestCaseUtil.sceanrioExecutionStatistics
								.get("Scenario_" + scenarioDetails[0] + "_startTime");
						String scenarioEnd = BaseTestCaseUtil.sceanrioExecutionStatistics
								.get("Scenario_" + scenarioDetails[0] + "_endTime");

						// RC#1 FIX: scenarioStart may be null when a scenario was skipped before
						// execution (before-suite failure, dependency failure) and never recorded its
						// start time. Guard against null to prevent NumberFormatException that would
						// corrupt the entire report file.
						if (scenarioStart == null) {
							logger.warn("[REPORT] Scenario=" + scenarioDetails[0]
									+ " Thread=" + Thread.currentThread().getId()
									+ " has no recorded startTime in sceanrioExecutionStatistics."
									+ " Sidebar entry will show 0ms duration. ScenarioIndex=" + scenarioIndex);
						}

						long startTime = (scenarioStart != null) ? Long.parseLong(scenarioStart) : 0L;
						long endTime;
						if (scenarioEnd == null || scenarioEnd.isEmpty())
							endTime = (scenarioStart != null) ? System.nanoTime() : 0L;
						else
							endTime = Long.parseLong(scenarioEnd);

						long scenarioDuration = (startTime == 0L) ? 0L : (endTime - startTime);

						// RC#3 FIX: Do NOT mutate totalScenarioDuration/totalScenarioCount here.
						// calculateExecutionStats() already computed these values correctly and
						// writeSuiteSummary() already consumed them. Mutating them again doubles
						// (and later triples) the average/fastest/slowest stats.

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
							displayValue = PacketUtility.convertNanosToTime(scenarioDuration);
						}

						logger.debug("[REPORT] Scenario=" + scenarioDetails[0]
								+ " ScenarioId=scn_" + scenarioIndex
								+ " Thread=" + Thread.currentThread().getId()
								+ " CssClass=" + cssClass
								+ " SidebarEntry=true");

						buffer.append("<tr class=\"").append(cssClass).append("\">").append("<td><a href=\"#m")
								.append(scenarioIndex).append("\">").append(scenarioName).append("</a></td>")
								.append("<td class='scenario-desc-col'>").append(scenarioDescEscaped).append("</td>")
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
		// S6201: pattern-matching instanceof binds 's' in one step, no separate cast needed
		if (parameters != null && parameters.length >= 2 && parameters[1] instanceof Scenario s) {
			return new String[]{s.getId() != null ? s.getId() : "UNKNOWN", s.getDescription()};
		}
		logger.warn("[REPORT] getScenarioDetails: result '"
				+ (result.getMethod() != null ? result.getMethod().getMethodName() : "?")
				+ "' has no Scenario parameter (paramCount="
				+ (parameters == null ? "null" : parameters.length)
				+ "). Using placeholder.");
		return new String[]{"UNKNOWN", ""};
	}

	private String generateFailedAndSkippedReport(String outputDirectory) {

		PrintWriter originalWriter = writer;
		String failedReportName = buildFailedReportName();

		// S2093: try-with-resources guarantees failedWriter is always closed even when
		// a write method throws. The explicit finally still restores the primary writer.
		try (PrintWriter failedWriter = new PrintWriter(
				new BufferedWriter(new FileWriter(new File(outputDirectory, failedReportName))))) {
			writer = failedWriter;

			writeDocumentStart();
			writeHead();

			writer.print("<body>");

			writeSuiteSummaryForFailedReport();
			writeScenarioSummaryForFailedReport();
			writeScenarioDetailsForFailedReport();

			writer.print("</body>");
			writeDocumentEnd();

			logger.info("Failed + Skipped report generated: " + failedReportName);

		} catch (Exception e) {
			logger.error("Error generating failed/skipped report", e);
		} finally {
			writer = originalWriter;
		}

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

		writer.print("<div class=\"dsl-scenario-summary-wrap\">");
		writer.print("<table id='summary' class='scenario-table'>");
		writer.print("<thead>");
		writer.print("<tr>");
		writer.print("<th>Scenario</th>");
		writer.print("<th>Scenario Description</th>");
		writer.print("<th class='bug-column'>Time / Bug ID</th>");
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
		writer.print("</div>");
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


	protected void writeScenarioDetails() {
		int scenarioIndex = 0;
		for (SuiteResult suiteResult : suiteResults) {
			for (TestResult testResult : suiteResult.getTestResults()) {
				scenarioIndex += writeScenarioDetails(testResult.getFailedTestResults(), scenarioIndex);
				scenarioIndex += writeScenarioDetails(testResult.getIgnoredTestResults(), scenarioIndex);
				scenarioIndex += writeScenarioDetails(testResult.getknownIssuesTestResults(), scenarioIndex);
				scenarioIndex += writeScenarioDetails(testResult.getSkippedTestResults(), scenarioIndex);
				scenarioIndex += writeScenarioDetails(testResult.getPassedTestResults(), scenarioIndex);
			}
		}
	}


	private int writeScenarioDetails(List<ClassResult> classResults, int startingScenarioIndex) {
		int scenarioIndex = startingScenarioIndex;
		for (ClassResult classResult : classResults) {
			for (MethodResult methodResult : classResult.getMethodResults()) {
				List<ITestResult> results = methodResult.getResults();
				assert !results.isEmpty();

				for (ITestResult result : results) {
					try {
						writeScenario(scenarioIndex, result);
					} catch (Exception ex) {
						// One scenario must not silently prevent all subsequent ones from getting
						// their anchor. Write a minimal error stub so the H3 id exists and the
						// sidebar link does not become a dead anchor.
						logger.error("[REPORT] writeScenario failed for m" + scenarioIndex
								+ "; writing error stub. Cause: " + ex.getMessage(), ex);
						try {
							writer.print("<h3 id=\"m");
							writer.print(scenarioIndex);
							writer.print("\" class=\"scenario-anchor\"></h3>");
							writer.print("<table class=\"result\"><tr><td colspan=\"1\">");
							writer.print(Utils.escapeHtml("Error rendering scenario detail: "
									+ ex.getClass().getSimpleName() + ": " + ex.getMessage()));
							writer.print("</td></tr></table>");
							writer.print("<p class=\"totop\"><a href=\"#summary\">back to summary</a></p>");
						} catch (Exception ignored) {
							// secondary write failure — nothing more we can do
						}
					}
					scenarioIndex++;
				}
			}
		}

		return scenarioIndex - startingScenarioIndex;
	}


	private void writeScenario(int scenarioIndex, ITestResult result) {
		String anchorClass = "scenario-anchor";
		String scenarioId = "?";
		Object[] parameters = result.getParameters();
		// S6201: pattern-matching instanceof removes the separate cast
		if (parameters != null && parameters.length > 1 && parameters[1] instanceof Scenario s) {
			scenarioId = s.getId() != null ? s.getId() : "?";
			if ("0".equalsIgnoreCase(scenarioId)) {
				anchorClass += " scenario-anchor--before-suite";
			} else if ("AFTER_SUITE".equalsIgnoreCase(scenarioId)) {
				anchorClass += " scenario-anchor--after-suite";
			}
		}

		logger.debug("[REPORT] Scenario=" + scenarioId
				+ " ScenarioId=m" + scenarioIndex
				+ " Thread=" + Thread.currentThread().getId()
				+ " MainPanel=true");

		writer.print("<h3 id=\"m");
		writer.print(scenarioIndex);
		writer.print("\" class=\"");
		writer.print(anchorClass);
		writer.print("\">");
		writer.print("</h3>");

		writer.print("<table class=\"result\">");


		int parameterCount = (parameters == null ? 0 : parameters.length);
		int detailColspan = Math.max(parameterCount, 1);
		List<String> reporterMessages = Reporter.getOutput(result);
		if (!reporterMessages.isEmpty()) {
			writer.print("<tr><td colspan=\"" + detailColspan + "\">");
			writeReporterMessages(reporterMessages);
			writer.print("</td></tr>");
		}


		Throwable throwable = result.getThrowable();
		if (throwable != null) {
			writer.print("<tr><th colspan=\"" + detailColspan + "\">"
					+ (result.getStatus() == ITestResult.SUCCESS ? "Expected Exception" : "Exception") + "</th></tr>");
			writer.print("<tr><td colspan=\"" + detailColspan + "\">");
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
		writeReporterMessageChunk(iterator.next());
		while (iterator.hasNext()) {
			writer.print("<br/>");
			writeReporterMessageChunk(iterator.next());
		}
		writer.print("</div>");
	}


	private void writeReporterMessageChunk(String message) {
		if (message == null || message.isEmpty()) {
			return;
		}
		if (Reporter.getEscapeHtml() && !isReporterHtmlBlock(message)) {
			writer.print(Utils.escapeHtml(message));
		} else {
			writer.print(message);
		}
	}

	private static boolean isReporterHtmlBlock(String message) {
		String m = message.trim().toLowerCase();
		return m.startsWith("<div")
				&& (m.contains("dsl-internal-api-log")
						|| m.contains("step-capture-card")
						|| m.contains("http-payload")
						|| m.contains("validation-panel")
						|| m.contains("tc-outcome-banner"));
	}

	protected void writeStackTrace(Throwable throwable) {
		if (dslConfigManager.IsDebugEnabled()) {
			writer.print("<div class=\"stacktrace\">");
			writer.print(Utils.shortStackTrace(throwable, true));
			writer.print("</div>");
		}
	}


	protected void writeTableHeader(String html, String cssClasses) {
		writeTag("th", html, cssClasses);
	}


	protected void writeTableData(String html) {
		writeTableData(html, null);
	}


	protected void writeTableData(String html, String cssClasses) {
		writeTag("td", html, cssClasses);
	}


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


		public List<TestResult> getTestResults() {
			return testResults;
		}
	}


	protected static class TestResult {

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
			// RC#5 FIX: Set.copyOf() returns an unordered set whose iteration order
			// is JVM-internal and non-reproducible. groupResults() sorts by class+method
			// name, but when all DSL scenarios share the same class and method the
			// comparator returns 0 for every pair, making the sort order fully dependent
			// on the input order. Use LinkedHashSet to preserve the insertion order
			// established by the testResultsSubList iteration above.
			Set<ITestResult> testResultsSubSet = new java.util.LinkedHashSet<>(testResultsSubList);
			return testResultsSubSet;
		}

		public static boolean containsAny(String stringToCheckIn, String delimitedString) {
			if (stringToCheckIn == null)
				return false;
			String[] stringsToCheckFor = delimitedString.split(";");

			for (String str : stringsToCheckFor) {
				if (stringToCheckIn.contains(str)) {
					return true; 
				}
			}
			return false; 
		}


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


		public List<ClassResult> getFailedConfigurationResults() {
			return failedConfigurationResults;
		}


		public List<ClassResult> getFailedTestResults() {
			return failedTestResults;
		}


		public List<ClassResult> getIgnoredConfigurationResults() {
			return skippedConfigurationResults;
		}

		public List<ClassResult> getknownIssuesTestResults() {
			return knownIssuesTestResults;
		}


		public List<ClassResult> getIgnoredTestResults() {
			return ignoredTestResults;
		}

		public List<ClassResult> getSkippedTestResults() {
			return skippedTestResults;
		}


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


	protected static class ClassResult {
		private final String className;
		private final List<MethodResult> methodResults;


		public ClassResult(String className, List<MethodResult> methodResults) {
			this.className = className;
			this.methodResults = methodResults;
		}

		public String getClassName() {
			return className;
		}


		public List<MethodResult> getMethodResults() {
			return methodResults;
		}
	}


	protected static class MethodResult {
		private final List<ITestResult> results;


		public MethodResult(List<ITestResult> results) {
			this.results = results;
		}


		public List<ITestResult> getResults() {
			return results;
		}
	}

}
