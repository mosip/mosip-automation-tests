package io.mosip.testrig.dslrig.ivv.orchestrator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.management.OperatingSystemMXBean;

import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.dtos.ParserInputDTO;
import io.mosip.testrig.dslrig.ivv.core.dtos.RegistrationUser;
import io.mosip.testrig.dslrig.ivv.core.dtos.Scenario;
import io.mosip.testrig.dslrig.ivv.core.dtos.Store;
import io.mosip.testrig.dslrig.ivv.core.exceptions.FeatureNotSupportedError;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.core.utils.Utils;
import io.mosip.testrig.dslrig.ivv.dg.DataGenerator;
import io.mosip.testrig.dslrig.ivv.parser.Parser;

public class Orchestrator {
	private static Logger logger = Logger.getLogger(Orchestrator.class);
	String message = null;
	int countScenarioPassed = 0;
	static int totalScenario = 0;
	StringBuilder messageBuilder = new StringBuilder();
	private static ExtentHtmlReporter htmlReporter;
	public static ExtentReports extent;
	private Properties properties;

	public static volatile boolean beforeSuiteFailed = false;
	/** True once Scenario 0 (before suite) has finished — pass or fail. */
	public static volatile boolean beforeSuiteSetupComplete = false;
	/** @deprecated Use {@link #beforeSuiteSetupComplete}. */
	@Deprecated
	public static Boolean beforeSuiteExeuted = false;
	public static final Object lock = new Object();
	public static long suiteStartTime = 0;
	public static long suiteMaxTimeInMillis = 3600000L * dslConfigManager.getMaxSuiteTime();
	static AtomicInteger counterLock = new AtomicInteger(0);
	private static AtomicInteger totalFailedScenarios = new AtomicInteger(0);
	private static final int MAX_FAILED_SCENARIOS_BEFORE_STOP_RETRY = 20;
	public static volatile boolean disableAllRetries = false;
	private static final long SUITE_COORDINATION_POLL_MS = 2000L;
	private static CountDownLatch beforeSuiteLatch = new CountDownLatch(1);
	private static volatile boolean suiteRequiresScenario0 = true;
	/** Scenarios finished (pass, fail, or skip), excluding AFTER_SUITE. */
	private static final AtomicInteger completedScenarioCount = new AtomicInteger(0);
	private static volatile int executableScenarioCount = 0;

	private HashMap<String, String> packages = new HashMap<String, String>() {
		{
			put("e2e", "io.mosip.testrig.dslrig.ivv.e2e.methods");
		}
	};

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@BeforeSuite
	public void beforeSuite() {
		TestRunner.initializeResourcePaths();

		// Ensure BaseTestCase path context is set for scenarios that use AdminTestUtil.loadyaml()
		// (e.g. GetEmailByUIN, GetPhoneByUIN, WritePersonaData). When not run via TestRunner.main(),
		// setRunContext() is never called and getGlobalResourcePath() returns a fallback string.
		BaseTestCase.setRunContext(TestRunner.checkRunType(), TestRunner.jarUrl);

		// Ensure MosipTemporaryTestResource is populated. copyTestResources() is normally called
		// by TestRunner.main(), but IDE/Surefire runs skip main(). Without this, all scenarios
		// using yml files from idaData/ (and other subdirectories) fail mid-run once 20 have
		// exhausted retries and the global threshold fires.
		File globalResourceDir = new File(TestRunner.getGlobalResourcePath());
		boolean resourcesMissing = !globalResourceDir.exists()
				|| !new File(globalResourceDir, "config").isDirectory()
				|| !new File(globalResourceDir, "idaData").isDirectory()
				|| !new File(globalResourceDir, "testngFile").isDirectory();
		if (resourcesMissing) {
			logger.info("MosipTemporaryTestResource missing or incomplete; copying test resources (IDE/Surefire run without TestRunner.main())");
			TestRunner.copyTestResources();
		}

		beforeSuiteFailed = false;
		beforeSuiteSetupComplete = false;
		beforeSuiteExeuted = false;
		beforeSuiteLatch = new CountDownLatch(1);
		suiteRequiresScenario0 = true;
		completedScenarioCount.set(0);
		executableScenarioCount = 0;

		DslStepTimingCollector.clear();
		DslPacketTemplateCache.clear();

		suiteStartTime = System.currentTimeMillis();
		BaseTestCaseUtil.exectionStartTime = suiteStartTime;
		logger.info("Suite start time is: " + BaseTestCaseUtil.exectionStartTime);
		this.properties = Utils.getProperties(TestRunner.getExternalResourcePath() + "/config/config.properties");
		Utils.setupLogger(System.getProperty("user.dir") + "/" + System.getProperty("testng.outpur.dir") + "/"
				+ this.properties.getProperty("ivv._path.auditlog"));
		String emailableReportName = null;
		if (TestRunner.checkRunType().equalsIgnoreCase("IDE")) {
			emailableReportName = System.getProperty("user.dir") + "/" + System.getProperty("testng.outpur.dir") + "/"
					+ this.properties.getProperty("ivv._path.reports")
					+ BaseTestCase.generateRandomAlphaNumericString(7) + ".html";
			logger.info("Extent Report path :" + emailableReportName);
		} else if (TestRunner.checkRunType().equalsIgnoreCase("JAR")) {
			emailableReportName = System.getProperty("user.dir") + "/"
					+ this.properties.getProperty("ivv._path.reports")
					+ BaseTestCase.generateRandomAlphaNumericString(7) + ".html";
			logger.info("Extent Report path :" + emailableReportName);
		}

		BaseTestCaseUtil.setExtentReportName(emailableReportName);

		htmlReporter = new ExtentHtmlReporter(BaseTestCaseUtil.getExtentReportName());

		extent = new ExtentReports();

		extent.attachReporter(htmlReporter);

		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);

	}

	@BeforeTest
	public static void create_proxy_server() {

	}

	@AfterSuite
	public void afterSuite() {
		BaseTestCaseUtil.exectionEndTime = System.currentTimeMillis();
		logger.info("Suite end time is: " + BaseTestCaseUtil.exectionEndTime);
		DslStepTimingCollector.logReport();
		extent.flush();
		if (dslConfigManager.IsDebugEnabled()) {
			logger.info("Debug mode enabled; suite teardown limited to timing report and extent flush");
		}
	}

	@DataProvider(name = "ScenarioDataProvider", parallel = true)
	public static Object[][] dataProvider() throws RigInternalError {
		int threadCount = Integer.parseInt(dslConfigManager.getThreadCount());

		System.out.println("Executing with thread count: " + threadCount);
		logger.info("Executing DataProvider with thread count: " + threadCount);

		System.setProperty("dataproviderthreadcount", String.valueOf(threadCount));

		String scenarioSheet = null;

		String configFile = TestRunner.getExternalResourcePath() + "/config/config.properties";
		Properties properties = Utils.getProperties(configFile);

		scenarioSheet = getScenarioSheet();
		ParserInputDTO parserInputDTO = new ParserInputDTO();
		parserInputDTO.setConfigProperties(properties);
		parserInputDTO.setDocumentsFolder(
				TestRunner.getLocalResourcePath() + "/" + properties.getProperty("ivv.path.documents.folder"));
		parserInputDTO.setBiometricsFolder(
				TestRunner.getLocalResourcePath() + "/" + properties.getProperty("ivv.path.biometrics.folder"));
		parserInputDTO.setPersonaSheet(
				TestRunner.getLocalResourcePath() + "/" + properties.getProperty("ivv.path.persona.sheet"));
		parserInputDTO.setScenarioSheet(scenarioSheet);

		parserInputDTO.setRcSheet(
				TestRunner.getLocalResourcePath() + "/" + properties.getProperty("ivv.path.rcpersona.sheet"));
		parserInputDTO.setPartnerSheet(
				TestRunner.getLocalResourcePath() + "/" + properties.getProperty("ivv.path.partner.sheet"));
		parserInputDTO.setIdObjectSchema(
				TestRunner.getLocalResourcePath() + "/" + properties.getProperty("ivv.path.idobject"));
		parserInputDTO.setDocumentsSheet(
				TestRunner.getLocalResourcePath() + "/" + properties.getProperty("ivv.path.documents.sheet"));
		parserInputDTO.setBiometricsSheet(
				TestRunner.getLocalResourcePath() + "/" + properties.getProperty("ivv.path.biometrics.sheet"));
		parserInputDTO.setGlobalsSheet(
				TestRunner.getLocalResourcePath() + "/" + properties.getProperty("ivv.path.globals.sheet"));
		parserInputDTO.setConfigsSheet(
				TestRunner.getLocalResourcePath() + "/" + properties.getProperty("ivv.path.configs.sheet"));

		Parser parser = new Parser(parserInputDTO);
		DataGenerator dg = new DataGenerator();
		ArrayList<Scenario> scenarios = new ArrayList<>();
		try {
			scenarios = dg.prepareScenarios(parser.getScenarios(), parser.getPersonas());
		} catch (RigInternalError rigInternalError) {
			logger.error(rigInternalError.getMessage());
		}

		for (int i = 0; i < scenarios.size(); i++) {
			scenarios.get(i).setRegistrationUsers(parser.getRCUsers());
			scenarios.get(i).setPartners(parser.getPartners());
		}
		HashMap<String, String> configs = parser.getConfigs();
		HashMap<String, String> globals = parser.getGlobals();
		ArrayList<RegistrationUser> rcUsers = parser.getRCUsers();
		totalScenario = scenarios.size();
		ArrayList<Scenario> filteredScenarios = new ArrayList<>();
		if (ConfigManager.getproperty("scenariosToExecute").isEmpty()
				&& BaseTestCase.testLevel.equalsIgnoreCase("sanity")) {
			for (Scenario scenario : scenarios) {
				if (scenario.getId().equalsIgnoreCase("0") || scenario.getId().equalsIgnoreCase("AFTER_SUITE")
						|| scenario.getId().equalsIgnoreCase("2")) {
					filteredScenarios.add(scenario);
				}
			}
		} else {
			for (Scenario scenario : scenarios) {
				String toExecuteList = ConfigManager.getproperty("scenariosToExecute");
				if (scenario.getId().equalsIgnoreCase("0") || scenario.getId().equalsIgnoreCase("AFTER_SUITE")
						|| (dslConfigManager.isInTobeExecuteList(scenario.getId())
								&& dslConfigManager.isInTobeGroupExecuteList(scenario.getGroupName()))) {
					if (toExecuteList.contains("@@") && !scenario.getId().equalsIgnoreCase("0")
							&& !scenario.getId().equalsIgnoreCase("AFTER_SUITE")) {

						String[] pairs = toExecuteList.split(",");

						for (String pair : pairs) {
							String[] parts = pair.split("@@");
							if (parts.length == 2) {
								String scenarioId = parts[0].trim();
								int scenarioCount = Integer.parseInt(parts[1].trim());
								if (scenario.getId().equalsIgnoreCase(scenarioId)) {
									for (int i = 1; i <= scenarioCount; i++) {
										Scenario scenarioCopy = new Scenario();
										scenarioCopy.setDescription(scenario.getDescription());
										scenarioCopy.setTags(new ArrayList<>(scenario.getTags()));
										scenarioCopy.setPersonaClass(scenario.getPersonaClass());
										scenarioCopy.setGroupName(scenario.getGroupName());
										scenarioCopy.setModules(new ArrayList<>(scenario.getModules()));
										scenarioCopy.setVariables(new HashMap<>(scenario.getVariables()));
										scenarioCopy.setObjectVariables(new HashMap<>(scenario.getObjectVariables()));
										scenarioCopy.setResidentTemplatePaths(
												new LinkedHashMap<>(scenario.getResidentTemplatePaths()));
										scenarioCopy.setResidentPathsPrid(
												new LinkedHashMap<>(scenario.getResidentPathsPrid()));
										scenarioCopy.setTemplatePacketPath(
												new LinkedHashMap<>(scenario.getTemplatePacketPath()));
										scenarioCopy.setManualVerificationRid(
												new LinkedHashMap<>(scenario.getManualVerificationRid()));
										scenarioCopy.setResidentPersonaIdPro(
												(Properties) scenario.getResidentPersonaIdPro().clone());
										scenarioCopy.setPridsAndRids(new LinkedHashMap<>(scenario.getPridsAndRids()));
										scenarioCopy.setUinReqIds(new LinkedHashMap<>(scenario.getUinReqIds()));
										scenarioCopy.setGeneratedResidentData(
												new ArrayList<>(scenario.getGeneratedResidentData()));
										scenarioCopy.setTemplatPath_updateResident(
												scenario.getTemplatPath_updateResident());
										scenarioCopy.setRid_updateResident(scenario.getRid_updateResident());
										scenarioCopy.setUin_updateResident(scenario.getUin_updateResident());
										scenarioCopy.setPrid_updateResident(scenario.getPrid_updateResident());
										scenarioCopy
												.setRidPersonaPath(new LinkedHashMap<>(scenario.getRidPersonaPath()));
										scenarioCopy
												.setVidPersonaProp((Properties) scenario.getVidPersonaProp().clone());
										scenarioCopy.setOidcPmsProp((Properties) scenario.getOidcPmsProp().clone());
										scenarioCopy
												.setAppointmentDate((Properties) scenario.getAppointmentDate().clone());
										scenarioCopy.setResidentPathGuardianRid(
												scenario.getResidentPathGuardianRid() == null ? null
														: new LinkedHashMap<>(scenario.getResidentPathGuardianRid()));
										scenarioCopy.setId(scenarioId + "_" + i);

										List<Scenario.Step> copiedSteps = new ArrayList<>();
										for (Scenario.Step step : scenario.getSteps()) {
											copiedSteps.add(deepCopyStep(step, scenarioCopy));
										}
										scenarioCopy.setSteps(copiedSteps);
										scenarioCopy.setPersona(scenario.getPersona());
										scenarioCopy.setRegistrationUsers(scenario.getRegistrationUsers());
										scenarioCopy.setPartners(scenario.getPartners());
										filteredScenarios.add(scenarioCopy);
									}
								}
							}
						}
					} else
						filteredScenarios.add(scenario);
				}
			}
		}
		filteredScenarios.sort((a, b) -> {
			if (a.getId().equalsIgnoreCase("0")) {
				return -1;
			}
			if (b.getId().equalsIgnoreCase("0")) {
				return 1;
			}
			if (a.getId().equalsIgnoreCase("AFTER_SUITE")) {
				return 1;
			}
			if (b.getId().equalsIgnoreCase("AFTER_SUITE")) {
				return -1;
			}
			return 0;
		});
		suiteRequiresScenario0 = filteredScenarios.stream().anyMatch(s -> s.getId().equalsIgnoreCase("0"));
		if (!suiteRequiresScenario0) {
			beforeSuiteSetupComplete = true;
			beforeSuiteExeuted = true;
			logger.info("Scenario 0 not in this run; skipping before-suite gate.");
		}
		filteredScenarios.sort((a, b) -> {
			if (a.getId().equalsIgnoreCase("0")) {
				return -1;
			}
			if (b.getId().equalsIgnoreCase("0")) {
				return 1;
			}
			if (a.getId().equalsIgnoreCase("AFTER_SUITE")) {
				return 1;
			}
			if (b.getId().equalsIgnoreCase("AFTER_SUITE")) {
				return -1;
			}
			return 0;
		});
		suiteRequiresScenario0 = filteredScenarios.stream().anyMatch(s -> s.getId().equalsIgnoreCase("0"));
		if (!suiteRequiresScenario0) {
			beforeSuiteSetupComplete = true;
			beforeSuiteExeuted = true;
			logger.info("Scenario 0 not in this run; skipping before-suite gate.");
		}
		totalScenario = filteredScenarios.size();
		executableScenarioCount = (int) filteredScenarios.stream()
				.filter(s -> !s.getId().equalsIgnoreCase("AFTER_SUITE")).count();
		Object[][] dataArray = new Object[filteredScenarios.size()][5];
		for (int i = 0; i < filteredScenarios.size(); i++) {
			dataArray[i][0] = i;
			dataArray[i][1] = filteredScenarios.get(i);
			dataArray[i][2] = configs;
			dataArray[i][3] = globals;
			dataArray[i][4] = properties;
		}

		System.setProperty("testng.threadcount", String.valueOf(dslConfigManager.getThreadCount()));
		return dataArray;
	}

	@BeforeMethod
	public void beforeMethod(Method method) {

	}

	private synchronized void updateRunStatistics(Scenario scenario) {
		int executed = counterLock.getAndIncrement();
		if (!scenario.getId().equalsIgnoreCase("AFTER_SUITE")) {
			completedScenarioCount.incrementAndGet();
		}
		logger.info("Updating statistics for scenario: " + scenario.getId() + " -- executed count: " + executed
				+ ", completed (excl. AFTER_SUITE): " + completedScenarioCount.get() + "/"
				+ executableScenarioCount);

		long endTime = System.nanoTime();
		BaseTestCaseUtil.sceanrioExecutionStatistics.put("Scenario_" + scenario.getId() + "_endTime",
				String.valueOf(endTime));
	}

	private synchronized void signalBeforeSuiteComplete(boolean passed) {
		if (beforeSuiteSetupComplete) {
			return;
		}
		beforeSuiteSetupComplete = true;
		beforeSuiteExeuted = true;
		beforeSuiteFailed = !passed;
		if (!passed) {
			disableAllRetries = true;
			logger.error("Scenario 0 (before suite) FAILED. All other scenarios will be skipped.");
		} else {
			logger.info("Scenario 0 (before suite) PASSED. Other scenarios may proceed.");
		}
		beforeSuiteLatch.countDown();
	}

	private void failBeforeSuiteIfScenario0Skipped(Scenario scenario) {
		if (scenario.getId().equalsIgnoreCase("0")) {
			signalBeforeSuiteComplete(false);
		}
	}

	private void awaitBeforeSuiteSetup(Scenario scenario) throws InterruptedException {
		if (!suiteRequiresScenario0 || scenario.getId().equalsIgnoreCase("0")) {
			return;
		}
		logger.info("Scenario " + scenario.getId() + " waiting for Scenario 0 (before suite) to complete...");
		long remainingMs = suiteMaxTimeInMillis - (System.currentTimeMillis() - suiteStartTime);
		if (remainingMs <= 0) {
			// Only flag before-suite as failed if Scenario 0 has not finished yet.
			// If it already completed (latch released), leave beforeSuiteFailed as-is so
			// the scenario falls through to the suite-time-exceeded check instead of being
			// invisibly dropped with a "Scenario 0 failed" message.
			if (!beforeSuiteSetupComplete) {
				beforeSuiteFailed = true;
			}
			return;
		}
		boolean completed = beforeSuiteLatch.await(remainingMs, TimeUnit.MILLISECONDS);
		if (!completed) {
			logger.error("Timed out waiting for Scenario 0 (before suite) to complete.");
			beforeSuiteFailed = true;
		}
	}

	@Test(dataProvider = "ScenarioDataProvider")
	private void run(int i, Scenario scenario, HashMap<String, String> configs, HashMap<String, String> globals,
			Properties properties) throws SQLException, InterruptedException, ClassNotFoundException,
			IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

		// Create the ExtentTest entry immediately so every scenario — including those
		// skipped before any step runs — appears in the report.
		ExtentTest extentTest = extent.createTest("Scenario_" + scenario.getId() + ": " + scenario.getDescription());

		if (scenario.getId().equalsIgnoreCase("AFTER_SUITE") && dslConfigManager.isAfterSuiteTeardownSkipped()) {
			String skipMsg = "Skipping AFTER_SUITE scenario because skipAfterSuiteTeardown=yes";
			logger.info(skipMsg);
			extentTest.skip(skipMsg);
			updateRunStatistics(scenario);
			throw new SkipException(skipMsg);
		}

		awaitBeforeSuiteSetup(scenario);

		if (beforeSuiteFailed && !scenario.getId().equalsIgnoreCase("AFTER_SUITE")
				&& !scenario.getId().equalsIgnoreCase("0")) {
			String skipMsg = "Skipping scenarios execution: scenario " + scenario.getId()
					+ " skipped because before-suite (Scenario 0) failed.";
			extentTest.skip(skipMsg);
			updateRunStatistics(scenario);
			throw new SkipException(skipMsg);
		}

		long startTime = System.nanoTime();
		BaseTestCaseUtil.sceanrioExecutionStatistics.put("Scenario_" + scenario.getId() + "_startTime",
				String.valueOf(startTime));
		OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

		logger.info("getProcessCpuLoad What % CPU load this current JVM is taking, from 0.0-1.0"
				+ osBean.getProcessCpuLoad());
		logger.info("getSystemCpuLoad What % load the overall system is at, from 0.0-1.0" + osBean.getSystemCpuLoad());
		logger.info(
				"Returns the amount of virtual memory that is guaranteed to be available to the running process in bytes, or -1 if this operation is not supported:"
						+ Long.toString(osBean.getCommittedVirtualMemorySize()));
		logger.info("Returns the amount of free physical memory in bytes:"
				+ Long.toString(osBean.getFreePhysicalMemorySize()));
		logger.info("Returns the amount of free swap space in bytes:" + Long.toString(osBean.getFreeSwapSpaceSize()));
		logger.info("Returns the recent cpu usage for the Java Virtual Machine process:"
				+ Double.toString(osBean.getProcessCpuLoad()));
		logger.info(
				"Returns the CPU time used by the process on which the Java virtual machine is running in nanoseconds:"
						+ Long.toString(osBean.getProcessCpuTime()));
		logger.info("Returns the recent cpu usage for the whole system:" + Double.toString(osBean.getSystemCpuLoad()));
		logger.info("Returns the total amount of physical memory in bytes:"
				+ Long.toString(osBean.getTotalPhysicalMemorySize()));
		logger.info("Returns the total amount of swap space in bytes:" + Long.toString(osBean.getTotalSwapSpaceSize()));

		if (!scenario.getId().equalsIgnoreCase("0")) {


			if (scenario.getId().equalsIgnoreCase("AFTER_SUITE")) 
			{
				while (completedScenarioCount.get() < executableScenarioCount) 
				{
					long currentTime = System.currentTimeMillis();
					if (currentTime - suiteStartTime >= suiteMaxTimeInMillis) {
						logger.error("Exhausted the maximum suite execution time while waiting for scenarios to finish "
								+ "(completed " + completedScenarioCount.get() + "/" + executableScenarioCount + ")");
						break;
					}

					Thread.sleep(SUITE_COORDINATION_POLL_MS);
				}
				startTime = System.nanoTime();
				BaseTestCaseUtil.sceanrioExecutionStatistics.put("Scenario_" + scenario.getId() + "_startTime",
						String.valueOf(startTime));

			}
		}

		logger.info(" Thread ID: " + Thread.currentThread().getId() + " scenario :- " + counterLock.get()
				+ scenario.getId());

		String testLevel = BaseTestCase.testLevel;
		String identifier = null;
		if (scenario.getId().equalsIgnoreCase("AFTER_SUITE") && beforeSuiteFailed) {
			String skipMsg = "Skipping AFTER_SUITE teardown because Scenario 0 (before suite) failed — "
					+ "WritePreReq(1/2/3) data was never stored. Fix Scenario 0 (track2b steps 17-25 for index 2) first.";
			logger.error(skipMsg);
			extentTest.skip(skipMsg);
			updateRunStatistics(scenario);
			throw new SkipException(skipMsg);
		}
		if (testLevel == null || testLevel.isEmpty() || testLevel.equalsIgnoreCase("regression")) {
			logger.info("Running Scenario #" + scenario.getId());
		} else if (matchTags("Negative_Test", scenario.getTags()) && testLevel.equalsIgnoreCase("smoke")) {
			extentTest.skip(
					"S-" + scenario.getId() + "Ignoring scenario since it is classified as a negative test case.");
			failBeforeSuiteIfScenario0Skipped(scenario);
			failBeforeSuiteIfScenario0Skipped(scenario);
			updateRunStatistics(scenario);
			throw new SkipException(
					"S-" + scenario.getId() + "Ignoring scenario since it is classified as a negative test case.");
		}

		message = "Scenario_" + scenario.getId() + ": " + scenario.getDescription();
		logger.info("-- *** Scenario " + scenario.getId() + ": " + scenario.getDescription() + " *** --");


		if (dslConfigManager.isInTobeSkippedList("I-" + scenario.getId()) && isFullSuiteRun()) {
			extentTest.skip("I-" + scenario.getId()
					+ "Ignoring scenario as it is marked to be excluded in the current environment due to unsupported feature or undeployed service.");
			failBeforeSuiteIfScenario0Skipped(scenario);
			failBeforeSuiteIfScenario0Skipped(scenario);
			updateRunStatistics(scenario);
			throw new SkipException("I-" + scenario.getId()
					+ "Ignoring scenario as it is marked to be excluded in the current environment due to unsupported feature or undeployed service.");
		}
		if (dslConfigManager.isInTobeBugList("S-" + scenario.getId()) && isFullSuiteRun()) {
			extentTest.skip("S-" + scenario.getId() + ": Skipping scenario due to known platform known issue");
			failBeforeSuiteIfScenario0Skipped(scenario);
			failBeforeSuiteIfScenario0Skipped(scenario);
			updateRunStatistics(scenario);
			throw new SkipException("S-" + scenario.getId() + ": Skipping scenario due to platform known issue");
		}
		if (dslConfigManager.isInTobeSkippedList("A-" + scenario.getId()) && isFullSuiteRun()) {
			extentTest.skip("A-" + scenario.getId()
					+ ": Ignoring scenario as it is marked to be excluded due to a known automation issue");
			failBeforeSuiteIfScenario0Skipped(scenario);
			failBeforeSuiteIfScenario0Skipped(scenario);
			updateRunStatistics(scenario);
			throw new SkipException("A-" + scenario.getId()
					+ ": Ignoring scenario as it is marked to be excluded due to a known automation issue");
		}
		if (System.currentTimeMillis() - suiteStartTime >= suiteMaxTimeInMillis && !scenario.getId().equalsIgnoreCase("AFTER_SUITE")) {
			extentTest.skip("S-" + scenario.getId()
					+ ": Skipping scenarios execution As Exhausted the maximum suite execution time.Hence, terminating the execution");
			failBeforeSuiteIfScenario0Skipped(scenario);
			failBeforeSuiteIfScenario0Skipped(scenario);
			updateRunStatistics(scenario);
			throw new SkipException((" Thread ID: " + Thread.currentThread().getId()
					+ " Skipping scenarios execution As Exhausted the maximum suite execution time.Hence, terminating the execution- "
					+ scenario.getId()));
		}

		Reporter.log(
				"<div class='box black-bg left-aligned' style='max-width: 100%; word-wrap: break-word;'><b><u>Scenario_"
						+ scenario.getId() + ": " + scenario.getDescription() + "</u></b></div>");


		Store store = new Store();
		store.setConfigs(configs);
		store.setGlobals(globals);
		store.setPersona(scenario.getPersona());
		store.setRegistrationUsers(scenario.getRegistrationUsers());
		store.setPartners(scenario.getPartners());
		store.setProperties(this.properties);

		int maxAttempts = 1;
		String maxAttemptsProp = BaseTestCaseUtil.props.getProperty("maxAttempts");
		if (maxAttemptsProp != null) {
			try {
				maxAttempts = Math.max(1, Integer.parseInt(maxAttemptsProp));
			} catch (NumberFormatException nfe) {
				logger.warn("Invalid maxAttempts '" + maxAttemptsProp + "', falling back to 1", nfe);
			}
		}
		boolean scenarioSucceeded = false;
		Exception finalException = null;

		for (int attempt = 1; attempt <= maxAttempts && !scenarioSucceeded; attempt++) {

			boolean willRetry =
			        !disableAllRetries
			        && !"sanity".equalsIgnoreCase(BaseTestCase.testLevel)
			        && (attempt < maxAttempts)
			        && (totalFailedScenarios.get() < MAX_FAILED_SCENARIOS_BEFORE_STOP_RETRY);


			store = new Store();
			store.setConfigs(configs);
			store.setGlobals(globals);
			store.setPersona(scenario.getPersona());
			store.setRegistrationUsers(scenario.getRegistrationUsers());
			store.setPartners(scenario.getPartners());
			store.setProperties(this.properties);

			int jumpBackIndex = 0;
			int iterationCount = 0;
			try {
				if (scenario.getId().equalsIgnoreCase("0")) {
					logger.info("Scenario 0: using parallel phased execution for before-suite setup");
					store = Scenario0ParallelRunner.run(scenario, store, willRetry,
							(sc, st, from, to, retry) -> executeScenarioStepsRange(sc, st, extentTest, properties,
									from, to, retry),
							this::copyScenarioForParallelTrack);
					scenarioSucceeded = true;
					continue;
				}
				if (scenario.getId().equalsIgnoreCase("0")) {
					logger.info("Scenario 0: using parallel phased execution for before-suite setup");
					store = Scenario0ParallelRunner.run(scenario, store, willRetry,
							(sc, st, from, to, retry) -> executeScenarioStepsRange(sc, st, extentTest, properties,
									from, to, retry),
							this::copyScenarioForParallelTrack);
					scenarioSucceeded = true;
					continue;
				}
				for (int stepIndex = 0; stepIndex < scenario.getSteps().size(); stepIndex++) {
					Scenario.Step step = scenario.getSteps().get(stepIndex);

					identifier = "> #[Test Step: " + step.getName() + "] [Test Parameters: " + step.getParameters()
							+ "]  [Test outVarName: " + step.getOutVarName() + "] [module: " + step.getModule()
							+ "] [variant: "

							+ step.getVariant() + "]";
					logger.info(identifier);

					extentTest.info(identifier + " - running"); 
					extentTest.info("parameters: " + step.getParameters().toString());
					StepInterface st = getInstanceOf(step);
					st.setExtentInstance(extentTest);
					st.setSystemProperties(properties);
					st.setState(store);
					st.setStep(step);
					String stepAction = "e2e_" + step.getName() + step.getParameters();
					stepAction = trimSpaceWithinSquareBrackets(stepAction);

					if (step.getOutVarName() != null)
						stepAction = step.getOutVarName() + "=" + stepAction;

					String stepParams[] = getStepDetails("S_" + step.getScenario().getId() + stepAction);
					if (stepParams == null && step.getScenario().getId().contains("_")) {

						String baseScenarioId = step.getScenario().getId().split("_")[0];
						stepParams = getStepDetails("S_" + baseScenarioId + stepAction);
					}

					if (!step.getName().contains("loopWindow")) {

						StringBuilder sb = new StringBuilder();

						sb.append(
								"<div style='padding: 0; margin: 0;'><textarea style='border: solid 1px gray; background-color: lightgray; width: 100%; padding: 0; margin: 0;' name='headers' rows='3' readonly='true'>");
						sb.append("Step Name: " + step.getName() + "\n");
						if (stepParams != null) {
							sb.append("Step Description: " + stepParams[0] + "\n");
							sb.append("Step Parameters: " + stepParams[1]);
						} else {
							sb.append("Step Description: [ERROR: stepParams is null]\n");
							sb.append("Step Parameters: [ERROR: stepParams is null]");
						}
						sb.append("</textarea></div>");

						Reporter.log(sb.toString());

					}


					if (step.getName().contains("loopWindow")) {

						if (step.getParameters().get(0).contains("START")) {
							jumpBackIndex = stepIndex + 1;
							iterationCount = 1;
						} else if (step.getParameters().size() > 1 && step.getParameters().get(0).contains("END")) {
							int loopCount = Integer.parseInt(step.getParameters().get(1));
							if (iterationCount < loopCount) {
								stepIndex = jumpBackIndex - 1;
								iterationCount++;
								logger.info("Repeating loop, iteration: " + iterationCount + " of " + loopCount);
								continue;
							} else {
								logger.info("Loop completed after " + iterationCount + " iterations.");
							}
						}
					}


					StepRunner.runLifecycle(st);
					if (StepRunner.lifecycleFailed(st)) {
						failStep(extentTest, identifier, willRetry, "Step reported error");
					}
					store = st.getState();

					extentTest.pass(identifier + " - passed");
				}


				scenarioSucceeded = true;
			} catch (SkipException e) {
				extentTest.skip(identifier + " - skipped");
				if (scenario.getId().equalsIgnoreCase("0")) {
					signalBeforeSuiteComplete(false);
				}
				if (scenario.getId().equalsIgnoreCase("0")) {
					signalBeforeSuiteComplete(false);
				}
				updateRunStatistics(scenario);
				logger.error(e.getMessage());
				Reporter.log(e.getMessage());
				throw new SkipException(e.getMessage());
			} catch (FeatureNotSupportedError e) {
				logger.warn(e.getMessage());
				Reporter.log(e.getMessage());

				scenarioSucceeded = true;
			} catch (Error e) {
				finalException = new Exception(e);
				String failMessage = "Attempt " + attempt + " failed for scenario " + scenario.getId() + " : "
						+ e.getMessage();
				logger.error(failMessage, e);
				Reporter.log("<span style='color:red; font-weight:bold;'>" + failMessage + "</span>");
				updateRunStatistics(scenario);
				throw new RuntimeException(failMessage, e);
			} catch (Exception e) {
				finalException = e;
				Throwable root = rootCause(e);
				String failMessage = "Attempt " + attempt + " failed for scenario " + scenario.getId() + " : "
						+ (root != null ? root.getMessage() : e.getMessage());
				logger.error(failMessage, root != null ? root : e);


				String redFailMessage = "<span style='color:red; font-weight:bold;'>" + failMessage + "</span>";
				Reporter.log(redFailMessage);


				if (totalFailedScenarios.get() >= MAX_FAILED_SCENARIOS_BEFORE_STOP_RETRY) {
					String thresholdMessage = "Global retry threshold (" + MAX_FAILED_SCENARIOS_BEFORE_STOP_RETRY
							+ ") reached. Marking scenario " + scenario.getId() + " as failed.";

					logger.error(thresholdMessage);
					extentTest.fail(thresholdMessage);
					Reporter.log("<span style='color:red; font-weight:bold;'>" + thresholdMessage + "</span>");
					updateRunStatistics(scenario);


					throw new RuntimeException(thresholdMessage);
				}
				if (beforeSuiteFailed || disableAllRetries) {
				    extentTest.fail("Before Suite failed. No retries allowed.");
				    updateRunStatistics(scenario);
				    throw new RuntimeException("Before Suite failed. Aborting scenario execution.");
				}

				if (attempt < maxAttempts) {
					String humanRetryMessage = ordinalWord(attempt) + " try for scenario " + scenario.getId()
							+ " failed; trying for " + ordinalWord(attempt + 1) + " time.";

					extentTest.info("Scenario failed on attempt " + attempt + ". Retrying attempt " + (attempt + 1));
					String yellowRetryMessage = "<span style='color:orange;'>" + humanRetryMessage + "</span>";
					Reporter.log(yellowRetryMessage);

					logger.info(humanRetryMessage);
					Thread.sleep(2000);
					continue; 
				}

				else {

					totalFailedScenarios.incrementAndGet();
					String finalFail = "<span style='color:red; font-weight:bold;'>Scenario failed after " + maxAttempts
							+ " attempts: " + e.getMessage() + "</span>";

					extentTest.fail("Scenario failed after " + maxAttempts + " attempts: " + e.getMessage());
					Reporter.log(finalFail);
					if (scenario.getId().equalsIgnoreCase("0")) {
						signalBeforeSuiteComplete(false);
					}
					updateRunStatistics(scenario);

					if (e instanceof RuntimeException)
						throw (RuntimeException) e;
					else
						throw new RuntimeException(e);
				}
			}
		}
		if (scenario.getId().equalsIgnoreCase("0")) {
			signalBeforeSuiteComplete(scenarioSucceeded);
		}
		updateRunStatistics(scenario);

	}

	private String getPackage(Scenario.Step step) {
		String pack = packages.get(step.getModule().toString());
		return pack;
	}

	@AfterMethod
	public void afterMethod(ITestResult result) {
		String status = "Fail";
		if (result.getStatus() == ITestResult.SUCCESS) {
			status = "Pass";
			countScenarioPassed++;
		} else if (result.getStatus() == ITestResult.SKIP)
			status = "Skip";

	}

	@AfterClass
	public void publishResult() {

		return;
	}

	public StepInterface getInstanceOf(Scenario.Step step)
			throws ClassNotFoundException, IllegalAccessException, InstantiationException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		Class<?> clazz = StepClassResolver.resolve(getPackage(step), step.getName());

		try {
			return (StepInterface) clazz.getDeclaredConstructor().newInstance();
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			if (cause instanceof Error) {
				throw (Error) cause;
			}
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			}
			if (cause instanceof Exception) {
				throw new RuntimeException(cause);
			}
			throw e;
		}
	}


	private void configToSystemProperties() {
		Set<String> keys = this.properties.stringPropertyNames();
		for (String key : keys) {
			System.setProperty(key, this.properties.getProperty(key));
		}
	}

	private static Throwable rootCause(Throwable t) {
		Throwable current = t;
		while (current.getCause() != null && current.getCause() != current) {
			current = current.getCause();
		}
		return current;
	}

	private static boolean isFullSuiteRun() {
		String scenariosToExecute = ConfigManager.getproperty("scenariosToExecute");
		String flowsToExecute = ConfigManager.getproperty("scenariosFlowToExecute");
		String testLevel = BaseTestCase.testLevel;
		boolean regressionLevel = testLevel == null || testLevel.isEmpty() || testLevel.equalsIgnoreCase("regression");
		return regressionLevel
				&& (scenariosToExecute == null || scenariosToExecute.trim().isEmpty())
				&& (flowsToExecute == null || flowsToExecute.trim().isEmpty());
	}

	private static Boolean matchTags(String systemTags, ArrayList<String> scenarioTags) {
		List<String> sys = Arrays.asList(systemTags.split(","));
		return CollectionUtils.containsAny(sys, scenarioTags);
	}

	public static String getScenarioSheet() throws RigInternalError {
		String scenarioSheet = null;

		if (dslConfigManager.useGherkinScenarios()) {
			try {
				scenarioSheet = resolveGherkinFeatureFilePath();
				logger.info("Scenario sheet (Gherkin): " + scenarioSheet);
			} catch (IOException e) {
				throw new RigInternalError("Gherkin feature file not found: " + e.getMessage());
			}
			registerGherkinStepDetails(scenarioSheet);
			return scenarioSheet;
		}

		if (dslConfigManager.useExternalScenarioSheet()) {

			scenarioSheet = dslConfigManager.getmountPathForScenario() + "/scenarios/" + "scenarios-"
					+ BaseTestCase.testLevel + "-" + BaseTestCase.environment + ".json";
			Path path = Paths.get(scenarioSheet);
			if (!Files.exists(path)) {
				logger.info("Scenario sheet path is: " + path);
				throw new RigInternalError("ScenarioSheet missing");
			}
			scenarioSheet = JsonToCsvConverter(scenarioSheet);
			if (scenarioSheet.isEmpty())
				throw new RigInternalError("Failed to generate CSV from JSON file, for internal processing");
		} else {
			throw new RigInternalError(
					"Bundled scenarios.json is no longer supported. Maintain config/"
							+ io.mosip.testrig.dslrig.ivv.parser.gherkin.GherkinFeatureParser.MASTER_FEATURE_FILE
							+ " or set useExternalScenarioSheet=yes with an external scenarios JSON file.");
		}
		return scenarioSheet;
	}

	/** Registers step metadata used by reports; also invoked when loading Gherkin feature files. */
	public static void registerStepDetails(String stepInput, String scenarioNumber, String description) {
		addAllStepDetails(stepInput, scenarioNumber, description);
		addUniqueStepDetails(stepInput, description);
	}

	private static String resolveGherkinFeatureFilePath() throws IOException {
		return io.mosip.testrig.dslrig.ivv.parser.gherkin.GherkinFeatureParser
				.resolveFeatureFile(resolveGherkinFeaturesDirectory()).getAbsolutePath();
	}

	private static void registerGherkinStepDetails(String featureFilePath) {
		try {
			new io.mosip.testrig.dslrig.ivv.parser.gherkin.GherkinFeatureParser()
					.forEachStep(new File(featureFilePath),
							(dsl, scenarioId, gherkinText) -> registerStepDetails(dsl, scenarioId, gherkinText));
		} catch (IOException e) {
			logger.warn("Could not register Gherkin step details for reports: " + e.getMessage());
		}
	}

	/**
	 * Resolves the Gherkin scenarios directory ({@code config/}). Prefers compiled classpath
	 * {@code classes/config} over a stale copy under MosipTemporaryTestResource from an earlier run.
	 */
	private static String resolveGherkinFeaturesDirectory() {
		String subPath = dslConfigManager.getGherkinFeaturesPath();
		String globalRoot = TestRunner.getGlobalResourcePath();
		String[] candidates = {
				TestRunner.getExternalResourcePath() + "/" + subPath,
				globalRoot.replace("/" + TestResources.resourceFolderName, "") + "/" + subPath,
				globalRoot + "/" + subPath
		};
		for (String candidate : candidates) {
			if (Files.exists(Paths.get(candidate))) {
				logger.info("Using Gherkin scenarios path: " + candidate);
				return candidate;
			}
		}
		return globalRoot + "/" + subPath;
	}

	public static String JsonToCsvConverter(String jsonFilePath) {
		String tempCSVPath = TestRunner.getGlobalResourcePath() + "/scenarios.csv";
		int maxSteps = 151;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(new File(jsonFilePath));
			FileWriter fileWriter = new FileWriter(tempCSVPath);

			List<String> headerList = new ArrayList<>();
			headerList.add("tc_no");
			headerList.add("tags");
			headerList.add("persona_class");
			headerList.add("persona");
			headerList.add("group_name");
			headerList.add("description");


			for (int i = 0; i < maxSteps; i++) {
				headerList.add("step" + i);
			}


			for (String string : headerList) {
				fileWriter.write(string + ",");
			}
			fileWriter.write("\r\n");


			for (JsonNode jsonNode : rootNode) {

				List<String> stepList = new ArrayList<>();
				stepList.add(jsonNode.get("Scenario").asText());
				stepList.add(jsonNode.get("Tag").asText());
				stepList.add(jsonNode.get("Persona").asText());
				stepList.add(jsonNode.get("Persona").asText());
				stepList.add(jsonNode.get("Group").asText());
				stepList.add(jsonNode.get("Description").asText());

				Pattern pattern = Pattern.compile("(.*?)\\((.*?),(.*)\\)");

				for (int stepIndex = 0; stepIndex < maxSteps; stepIndex++) {
					String stepDescription = "";
					String stepAction = "";
					JsonNode stepJsonNode = jsonNode.get("Step-" + stepIndex) == null ? null
							: jsonNode.get("Step-" + stepIndex);
					if (stepJsonNode != null) {
						stepAction = stepJsonNode.get("Action").asText();
						stepDescription = stepJsonNode.get("Description").asText();
					}
					Matcher matcher = pattern.matcher(stepAction);

					if (matcher.matches()) {
						stepList.add(stepAction == null ? "" : "\"" + stepAction + "\"");
					} else {
						stepList.add(stepAction == null ? "" : stepAction);
					}
					addAllStepDetails(stepAction, jsonNode.get("Scenario").asText(), stepDescription);
					addUniqueStepDetails(stepAction, stepDescription);
				}

				for (String string : stepList) {
					fileWriter.write(string + ",");
				}
				fileWriter.write("\r\n");
			}
			fileWriter.close();
		} catch (Exception e) {

			return "";
		}
		if (dslConfigManager.IsDebugEnabled()) {
			String keyValues = "";

			for (Map.Entry<String, String[]> entry : uniqueStepsMap.entrySet()) {
				keyValues += entry.getKey();
				String[] values = entry.getValue();
				for (int i = 0; i < values.length; i++) {
					keyValues += "," + values[i];
				}
				keyValues += "\r\n";
			}
			logger.info(keyValues);
		}
		return tempCSVPath;
	}

	private static final Map<String, String[]> uniqueStepsMap = new HashMap<>();

	private static void addUniqueStepDetails(String stepInput, String description) {
		if (stepInput.isEmpty() || description.isEmpty())
			return;

		int indexOfOpenParenthesis = stepInput.indexOf("(");
		if (indexOfOpenParenthesis != -1) {

			String step = stepInput.substring(stepInput.indexOf("e2e_"), indexOfOpenParenthesis);
			if (uniqueStepsMap.get(step) == null) {
				String[] descAndExample = new String[2];
				descAndExample[0] = description;
				descAndExample[1] = stepInput;
				uniqueStepsMap.put(step, descAndExample);
			}
		}
	}

	private static final Map<String, String[]> allStepsMap = new HashMap<>();

	private static void addAllStepDetails(String stepInput, String scenarioNumber, String description) {
		if (stepInput == null || stepInput.isEmpty()) {
			return;
		}

		String processedStepInput = stepInput.replaceAll("/\\*.*?\\*/", "");

		processedStepInput = processedStepInput.replace('(', '[').replace(')', ']');
		processedStepInput = trimSpaceWithinSquareBrackets(processedStepInput);

		if (allStepsMap.get("S_" + scenarioNumber + processedStepInput) == null) {
			String[] descAndExample = new String[2];
			descAndExample[0] = description;
			descAndExample[1] = stepInput;
			allStepsMap.put("S_" + scenarioNumber + processedStepInput, descAndExample);
		}
	}

	private static String[] getStepDetails(String stepName) {
		return allStepsMap.get(stepName);
	}

	private static String trimSpaceWithinSquareBrackets(String stringToTrim) {

		int openBracketIndex = stringToTrim.indexOf('[');
		int closeBracketIndex = stringToTrim.lastIndexOf(']');

		if (openBracketIndex != -1 && closeBracketIndex != -1 && openBracketIndex < closeBracketIndex) {

			String withinBrackets = stringToTrim.substring(openBracketIndex + 1, closeBracketIndex);


			withinBrackets = withinBrackets.replaceAll("\\s+", "");


			stringToTrim = stringToTrim.substring(0, openBracketIndex + 1) + withinBrackets
					+ stringToTrim.substring(closeBracketIndex);
		}
		return stringToTrim;
	}

	private Store executeScenarioStepsRange(Scenario scenario, Store store, ExtentTest extentTest,
			Properties properties, int fromStepIndex, int toStepIndex, boolean willRetry) throws Exception {
		String identifier = "";
		int jumpBackIndex = 0;
		int iterationCount = 0;
		for (int stepIndex = fromStepIndex; stepIndex <= toStepIndex && stepIndex < scenario.getSteps().size();
				stepIndex++) {
			Scenario.Step step = scenario.getSteps().get(stepIndex);

			identifier = "> #[Test Step: " + step.getName() + "] [Test Parameters: " + step.getParameters()
					+ "]  [Test outVarName: " + step.getOutVarName() + "] [module: " + step.getModule() + "] [variant: "
					+ step.getVariant() + "]";
			logger.info(identifier);

			extentTest.info(identifier + " - running");
			extentTest.info("parameters: " + step.getParameters().toString());
			StepInterface st = getInstanceOf(step);
			st.setExtentInstance(extentTest);
			st.setSystemProperties(properties);
			st.setState(store);
			st.setStep(step);
			String stepAction = "e2e_" + step.getName() + step.getParameters();
			stepAction = trimSpaceWithinSquareBrackets(stepAction);

			if (step.getOutVarName() != null) {
				stepAction = step.getOutVarName() + "=" + stepAction;
			}

			String stepParams[] = getStepDetails("S_" + step.getScenario().getId() + stepAction);
			if (stepParams == null && step.getScenario().getId().contains("_")) {
				String baseScenarioId = step.getScenario().getId().split("_")[0];
				stepParams = getStepDetails("S_" + baseScenarioId + stepAction);
			}

			if (!step.getName().contains("loopWindow")) {
				StringBuilder sb = new StringBuilder();
				sb.append(
						"<div style='padding: 0; margin: 0;'><textarea style='border: solid 1px gray; background-color: lightgray; width: 100%; padding: 0; margin: 0;' name='headers' rows='3' readonly='true'>");
				sb.append("Step Name: " + step.getName() + "\n");
				if (stepParams != null) {
					sb.append("Step Description: " + stepParams[0] + "\n");
					sb.append("Step Parameters: " + stepParams[1]);
				} else {
					sb.append("Step Description: [ERROR: stepParams is null]\n");
					sb.append("Step Parameters: [ERROR: stepParams is null]");
				}
				sb.append("</textarea></div>");
				Reporter.log(sb.toString());
			}

			if (step.getName().contains("loopWindow")) {
				if (step.getParameters().get(0).contains("START")) {
					jumpBackIndex = stepIndex + 1;
					iterationCount = 1;
				} else if (step.getParameters().size() > 1 && step.getParameters().get(0).contains("END")) {
					int loopCount = Integer.parseInt(step.getParameters().get(1));
					if (iterationCount < loopCount) {
						stepIndex = jumpBackIndex - 1;
						iterationCount++;
						logger.info("Repeating loop, iteration: " + iterationCount + " of " + loopCount);
						continue;
					} else {
						logger.info("Loop completed after " + iterationCount + " iterations.");
					}
				}
			}

			StepRunner.runLifecycle(st);
			if (StepRunner.lifecycleFailed(st)) {
				failStep(extentTest, identifier, willRetry, "Step reported error");
			}
			store = st.getState();
			extentTest.pass(identifier + " - passed");
		}
		return store;
	}

	private static void failStep(ExtentTest extentTest, String identifier, boolean willRetry, String message) {
		if (willRetry) {
			extentTest.warning(identifier + " - failed (will retry)");
		} else {
			extentTest.fail(identifier + " - failed");
		}
		throw new RuntimeException(message);
	}

	private Scenario copyScenarioForParallelTrack(Scenario source) {
		Scenario copy = new Scenario();
		copy.setId(source.getId());
		copy.setDescription(source.getDescription());
		copy.setTags(new ArrayList<>(source.getTags()));
		copy.setPersonaClass(source.getPersonaClass());
		copy.setGroupName(source.getGroupName());
		copy.setModules(new ArrayList<>(source.getModules()));
		copy.setVariables(new HashMap<>(source.getVariables()));
		copy.setObjectVariables(new HashMap<>(source.getObjectVariables()));
		copy.setResidentTemplatePaths(new LinkedHashMap<>(source.getResidentTemplatePaths()));
		copy.setResidentPathsPrid(new LinkedHashMap<>(source.getResidentPathsPrid()));
		copy.setTemplatePacketPath(new LinkedHashMap<>(source.getTemplatePacketPath()));
		copy.setManualVerificationRid(new LinkedHashMap<>(source.getManualVerificationRid()));
		copy.setResidentPersonaIdPro((Properties) source.getResidentPersonaIdPro().clone());
		copy.setPridsAndRids(new LinkedHashMap<>(source.getPridsAndRids()));
		copy.setUinReqIds(new LinkedHashMap<>(source.getUinReqIds()));
		copy.setGeneratedResidentData(new ArrayList<>(source.getGeneratedResidentData()));
		copy.setTemplatPath_updateResident(source.getTemplatPath_updateResident());
		copy.setRid_updateResident(source.getRid_updateResident());
		copy.setUin_updateResident(source.getUin_updateResident());
		copy.setPrid_updateResident(source.getPrid_updateResident());
		copy.setRidPersonaPath(new LinkedHashMap<>(source.getRidPersonaPath()));
		copy.setVidPersonaProp((Properties) source.getVidPersonaProp().clone());
		copy.setOidcPmsProp((Properties) source.getOidcPmsProp().clone());
		copy.setAppointmentDate((Properties) source.getAppointmentDate().clone());
		copy.setResidentPathGuardianRid(source.getResidentPathGuardianRid() == null ? null
				: new LinkedHashMap<>(source.getResidentPathGuardianRid()));
		copy.setCurrentStep(new HashMap<>(source.getCurrentStep()));
		copy.setUinPersonaProp((Properties) source.getUinPersonaProp().clone());
		copy.setHandlePersonaProp((Properties) source.getHandlePersonaProp().clone());
		copy.setOidcClientProp((Properties) source.getOidcClientProp().clone());
		copy.setPrid(source.getPrid());
		copy.setStatusCode(source.getStatusCode());
		copy.setPersona(source.getPersona());
		copy.setRegistrationUsers(source.getRegistrationUsers());
		copy.setPartners(source.getPartners());
		copy.setUser(source.getUser());
		ArrayList<Scenario.Step> steps = new ArrayList<>();
		for (Scenario.Step step : source.getSteps()) {
			steps.add(deepCopyStep(step, copy));
		}
		copy.setSteps(steps);
		return copy;
	}

	private static Scenario.Step deepCopyStep(Scenario.Step original, Scenario scenarioCopy) {
		Scenario.Step copy = new Scenario.Step();
		copy.setName(original.getName());
		copy.setVariant(original.getVariant());
		copy.setModule(original.getModule());
		copy.setAsserts(original.getAsserts() == null ? null : new ArrayList<>(original.getAsserts()));
		copy.setErrors(original.getErrors() == null ? null : new ArrayList<>(original.getErrors()));
		copy.setAssertionPolicy(original.getAssertionPolicy());
		copy.setFailExpected(original.isFailExpected());
		copy.setParameters(original.getParameters() == null ? null : new ArrayList<>(original.getParameters()));
		copy.setIndex(original.getIndex() == null ? null : new ArrayList<>(original.getIndex()));
		copy.setOutVarName(original.getOutVarName());
		copy.setScenario(scenarioCopy);
		return copy;
	}

	private static String ordinalWord(int number) {
		switch (number) {
		case 1: return "First";
		case 2: return "Second";
		case 3: return "Third";
		case 4: return "Fourth";
		case 5: return "Fifth";
		default:
			int mod10 = number % 10;
			int mod100 = number % 100;
			if (mod100 >= 11 && mod100 <= 13) {
				return number + "th";
			}
			switch (mod10) {
			case 1: return number + "st";
			case 2: return number + "nd";
			case 3: return number + "rd";
			default: return number + "th";
			}
		}
	}
}
