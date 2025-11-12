package io.mosip.testrig.dslrig.ivv.orchestrator;

import java.io.File;
import java.io.FileWriter;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
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
import io.mosip.testrig.dslrig.ivv.core.base.BaseStep;
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

	public static Boolean beforeSuiteFailed = false;
	public static Boolean beforeSuiteExeuted = false;
	public static final Object lock = new Object();
	public static long suiteStartTime = 0;
	public static long suiteMaxTimeInMillis = 7200000; // 2 hour in milliseconds
	static AtomicInteger counterLock = new AtomicInteger(0); // enable fairness policy
	private static AtomicInteger totalFailedScenarios = new AtomicInteger(0);
	private static final int MAX_FAILED_SCENARIOS_BEFORE_STOP_RETRY = 20;

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
		extent.flush();
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
										// Deep copy steps
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
		totalScenario = filteredScenarios.size();
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

	private synchronized void updateRunStatistics(Scenario scenario) throws ClassNotFoundException,
			IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
		logger.info("Updating statistics for scenario: " + scenario.getId() + " -- updating the executed count to: "
				+ counterLock.getAndIncrement());

		long endTime = System.nanoTime();
		BaseTestCaseUtil.sceanrioExecutionStatistics.put("Scenario_" + scenario.getId() + "_endTime",
				String.valueOf(endTime));

		if (scenario.getId().equalsIgnoreCase("0")) {
			/// Check if all steps in Before are passed or not
			for (Scenario.Step step : scenario.getSteps()) {
				StepInterface st = getInstanceOf(step);
				if (st.hasError() == true) {
					beforeSuiteFailed = true;
					logger.info("Before suite failed");
					break;

				}
			}
			beforeSuiteExeuted = true;
			logger.info("Before Suite executed");
		}

		logger.info(" Thread ID: " + Thread.currentThread().getId() + " scenarios Executed : " + counterLock.get());

	}

	@Test(dataProvider = "ScenarioDataProvider")
	private void run(int i, Scenario scenario, HashMap<String, String> configs, HashMap<String, String> globals,
			Properties properties) throws SQLException, InterruptedException, ClassNotFoundException,
			IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

		// Capture the start time of the scenario execution
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

			// AFTER_SUITE scenario execution kicked-off before all execution
			if (scenario.getId().equalsIgnoreCase("AFTER_SUITE")) // || scenariosExeuted)
			{
				// Wait till all scenarios are executed
				while (counterLock.get() < totalScenario - 1) // executed excluding after suite
				{
					long currentTime = System.currentTimeMillis();
					if (currentTime - suiteStartTime >= suiteMaxTimeInMillis) {
						logger.error("Exhausted the maximum suite execution time.Hence, terminating the execution");
						break;
					}

					logger.info(" Thread ID: " + Thread.currentThread().getId() + " inside scenariosExecuted "
							+ counterLock.get() + "- " + scenario.getId());
					Thread.sleep(10000); // Sleep for 10 sec
				}
				startTime = System.nanoTime();
				BaseTestCaseUtil.sceanrioExecutionStatistics.put("Scenario_" + scenario.getId() + "_startTime",
						String.valueOf(startTime));
			} else {

				// Wait for before suite executed
				while (beforeSuiteExeuted == false) {
					Thread.sleep(10000); // Sleep for 10 sec

					logger.info(" Thread ID: " + Thread.currentThread().getId()
							+ " inside beforeSuiteExecuted == false " + counterLock.get() + "- " + scenario.getId());
				}
				startTime = System.nanoTime();
				BaseTestCaseUtil.sceanrioExecutionStatistics.put("Scenario_" + scenario.getId() + "_startTime",
						String.valueOf(startTime));

				// Check if the beforeSuite is successful. If not skip the scenario execution
				if (beforeSuiteFailed == true) {
					updateRunStatistics(scenario);
					throw new SkipException((" Thread ID: " + Thread.currentThread().getId()
							+ " Skipping scenarios execution - " + scenario.getId()));
				}

			}
		}

		logger.info(" Thread ID: " + Thread.currentThread().getId() + " scenario :- " + counterLock.get()
				+ scenario.getId());

		extent.flush();
		String testLevel = BaseTestCase.testLevel;
		String identifier = null;
		ExtentTest extentTest = extent.createTest("Scenario_" + scenario.getId() + ": " + scenario.getDescription());
		if (testLevel == null || testLevel.isEmpty() || testLevel.equalsIgnoreCase("regression")) {
			logger.info("Running Scenario #" + scenario.getId());
		} else if (matchTags("Negative_Test", scenario.getTags()) && testLevel.equalsIgnoreCase("smoke")) {
			extentTest.skip(
					"S-" + scenario.getId() + "Ignoring scenario since it is classified as a negative test case.");
			updateRunStatistics(scenario);
			throw new SkipException(
					"S-" + scenario.getId() + "Ignoring scenario since it is classified as a negative test case.");
		}

		message = "Scenario_" + scenario.getId() + ": " + scenario.getDescription();
		logger.info("-- *** Scenario " + scenario.getId() + ": " + scenario.getDescription() + " *** --");

		// Check whether the scenario is in the defined skipped list
		if (dslConfigManager.isInTobeSkippedList("I-" + scenario.getId())) {
			extentTest.skip("I-" + scenario.getId()
					+ "Ignoring scenario as it is marked to be excluded in the current environment due to unsupported feature or undeployed service.");
			updateRunStatistics(scenario);
			throw new SkipException("I-" + scenario.getId()
					+ "Ignoring scenario as it is marked to be excluded in the current environment due to unsupported feature or undeployed service.");
		}
		if (dslConfigManager.isInTobeBugList("S-" + scenario.getId())) {
			extentTest.skip("S-" + scenario.getId() + ": Skipping scenario due to known platform issue");
			updateRunStatistics(scenario);
			throw new SkipException("S-" + scenario.getId() + ": Skipping scenario due to platform known issue");
		}
		if (dslConfigManager.isInTobeSkippedList("A-" + scenario.getId())) {
			extentTest.skip("A-" + scenario.getId()
					+ ": Ignoring scenario as it is marked to be excluded due to a known automation issue");
			updateRunStatistics(scenario);
			throw new SkipException("A-" + scenario.getId()
					+ ": Ignoring scenario as it is marked to be excluded due to a known automation issue");
		}

		Reporter.log(
				"<div class='box black-bg left-aligned' style='max-width: 100%; word-wrap: break-word;'><b><u>Scenario_"
						+ scenario.getId() + ": " + scenario.getDescription() + "</u></b></div>");

		// Prepare a base store (used to reset state between retry attempts)
		Store store = new Store();
		store.setConfigs(configs);
		store.setGlobals(globals);
		store.setPersona(scenario.getPersona());
		store.setRegistrationUsers(scenario.getRegistrationUsers());
		store.setPartners(scenario.getPartners());
		store.setProperties(this.properties);

		int maxAttempts = 2; // Run each scenario up to 2 times on failure
		boolean scenarioSucceeded = false;
		Exception finalException = null;

		for (int attempt = 1; attempt <= maxAttempts && !scenarioSucceeded; attempt++) {
			// Determine whether this attempt will be retried on failure
			boolean willRetry = (attempt < maxAttempts);
			// Reset store to initial values before each attempt
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
				for (int stepIndex = 0; stepIndex < scenario.getSteps().size(); stepIndex++) {
					Scenario.Step step = scenario.getSteps().get(stepIndex);

					identifier = "> #[Test Step: " + step.getName() + "] [Test Parameters: " + step.getParameters()
							+ "]  [Test outVarName: " + step.getOutVarName() + "] [module: " + step.getModule()
							+ "] [variant: "

							+ step.getVariant() + "]";
					logger.info(identifier);

					extentTest.info(identifier + " - running"); //
					extentTest.info("parameters: " + step.getParameters().toString());
					StepInterface st = getInstanceOf(step);
					st.setExtentInstance(extentTest);
					st.setSystemProperties(properties);
					st.setState(store);
					st.setStep(step);
					st.setup();
					st.validateStep();

					String stepAction = "e2e_" + step.getName() + step.getParameters();
					stepAction = trimSpaceWithinSquareBrackets(stepAction);

					if (step.getOutVarName() != null)
						stepAction = step.getOutVarName() + "=" + stepAction;

					String stepParams[] = getStepDetails("S_" + step.getScenario().getId() + stepAction);
					if (stepParams == null && step.getScenario().getId().contains("_")) {
						// Try with the base scenario ID (before the underscore)
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

					// loopWindow handling
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

					// Execute step
					st.run();
					st.assertHttpStatus();
					if (st.hasError()) {
						if (willRetry) {
							extentTest.warning(identifier + " - failed (will retry)");
						} else {
							extentTest.fail(identifier + " - failed");
						}
						throw new RuntimeException("Step reported error");
					}
					if (st.getErrorsForAssert().size() > 0) {
						st.errorHandler();
						if (st.hasError()) {
							if (willRetry) {
								extentTest.warning(identifier + " - failed after errorHandler (will retry)");
							} else {
								extentTest.fail(identifier + " - failed after errorHandler");
							}
							throw new RuntimeException("Step reported error after errorHandler");
						}
					} else {
						st.assertNoError();
						if (st.hasError()) {
							if (willRetry) {
								extentTest.warning(identifier + " - failed after assertNoError (will retry)");
							} else {
								extentTest.fail(identifier + " - failed after assertNoError");
							}
							throw new RuntimeException("Step reported error after assertNoError");
						}
					}
					store = st.getState();
					// If no error, mark step passed
					extentTest.pass(identifier + " - passed");
				}

				// If we reach here without exception, scenario passed for this attempt
				scenarioSucceeded = true;
			} catch (SkipException e) {
				extentTest.skip(identifier + " - skipped");
				updateRunStatistics(scenario);
				logger.error(e.getMessage());
				Reporter.log(e.getMessage());
				throw new SkipException(e.getMessage());
			} catch (FeatureNotSupportedError e) {
				logger.warn(e.getMessage());
				Reporter.log(e.getMessage());
				// Feature not supported - keep behavior as before (treat as not fatal)
			} catch (Exception e) {
				finalException = e;
				String failMessage = "Attempt " + attempt + " failed for scenario " + scenario.getId() + " : "
						+ e.getMessage();
				logger.error(failMessage, e);

				// HTML red text in TestNG report
				String redFailMessage = "<span style='color:red; font-weight:bold;'>" + failMessage + "</span>";
				Reporter.log(redFailMessage);

				// Increment total failure counter
				totalFailedScenarios.incrementAndGet();

				// ✅ Check global threshold first
				if (totalFailedScenarios.get() >= MAX_FAILED_SCENARIOS_BEFORE_STOP_RETRY) {
					String thresholdMessage = "Global retry threshold (" + MAX_FAILED_SCENARIOS_BEFORE_STOP_RETRY
							+ ") reached. Marking scenario " + scenario.getId() + " as failed.";

					logger.error(thresholdMessage);
					extentTest.fail(thresholdMessage);
					Reporter.log("<span style='color:red; font-weight:bold;'>" + thresholdMessage + "</span>");
					updateRunStatistics(scenario);

					// Explicitly fail (so TestNG marks it as FAILED, not SKIPPED)
					throw new RuntimeException(thresholdMessage);
				}

				// ✅ If this was not the last retry
				if (attempt < maxAttempts) {
					String humanRetryMessage = ordinalWord(attempt) + " try for scenario " + scenario.getId()
							+ " failed; trying for " + ordinalWord(attempt + 1) + " time.";

					extentTest.info("Scenario failed on attempt " + attempt + ". Retrying attempt " + (attempt + 1));
					String yellowRetryMessage = "<span style='color:orange;'>" + humanRetryMessage + "</span>";
					Reporter.log(yellowRetryMessage);

					logger.info(humanRetryMessage);
					Thread.sleep(2000);
					continue; // retry next attempt
				}
				// ✅ Final failure (after last attempt)
				else {
					String finalFail = "<span style='color:red; font-weight:bold;'>Scenario failed after " + maxAttempts
							+ " attempts: " + e.getMessage() + "</span>";

					extentTest.fail("Scenario failed after " + maxAttempts + " attempts: " + e.getMessage());
					Reporter.log(finalFail);
					updateRunStatistics(scenario);

					if (e instanceof RuntimeException)
						throw (RuntimeException) e;
					else
						throw new RuntimeException(e);
				}
			}
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
		String className = getPackage(step) + "." + step.getName().substring(0, 1).toUpperCase()
				+ step.getName().substring(1);
		// Load the class
		Class<?> clazz = Class.forName(className);
		// Use the new approach to create an instance
		return (StepInterface) clazz.getDeclaredConstructor().newInstance();
	}

	/*
	 * @SuppressWarnings("deprecation") public StepInterface
	 * getInstanceOf(Scenario.Step step) throws ClassNotFoundException,
	 * NoSuchMethodException, InvocationTargetException, InstantiationException,
	 * IllegalAccessException { String className = getPackage(step) + "." +
	 * step.getName().substring(0, 1).toUpperCase() + step.getName().substring(1);
	 * // Load the class Class<?> clazz = Class.forName(className); // Retrieve the
	 * bean from the Spring application context return (StepInterface)
	 * context.getBean(clazz); }
	 */

	private void configToSystemProperties() {
		Set<String> keys = this.properties.stringPropertyNames();
		for (String key : keys) {
			System.setProperty(key, this.properties.getProperty(key));
		}
	}

	private static Boolean matchTags(String systemTags, ArrayList<String> scenarioTags) {
		List<String> sys = Arrays.asList(systemTags.split(","));
		return CollectionUtils.containsAny(sys, scenarioTags);
	}

	public static String getScenarioSheet() throws RigInternalError {
		String scenarioSheet = null;
		// Use external Scenario sheet
		if (dslConfigManager.useExternalScenarioSheet()) {
			// Check first for the JSON file
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
		} else { // Use the scenario sheet bundled with jar
			scenarioSheet = TestRunner.getGlobalResourcePath() + "/config/scenarios.json";
			logger.info("Scenario sheet path is: " + scenarioSheet);
			Path path = Paths.get(scenarioSheet);
			if (!Files.exists(path)) {
				logger.info("Scenario sheet path is: " + path);
				throw new RigInternalError("ScenarioSheet missing");
			}
			scenarioSheet = JsonToCsvConverter(scenarioSheet);
			if (scenarioSheet.isEmpty())
				throw new RigInternalError("Failed to generate CSV from JSON file, for internal processing");
		}
		return scenarioSheet;
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

			// Add steps to the header list
			for (int i = 0; i < maxSteps; i++) {
				headerList.add("step" + i);
			}

			// Write header line
			for (String string : headerList) {
				fileWriter.write(string + ",");
			}
			fileWriter.write("\r\n");

			// Write scenarios
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
			// Log the error
			return "";
		}
		if (dslConfigManager.IsDebugEnabled()) {
			String keyValues = "";
			// Iterate through the map and print its contents
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
		// Find the index of the first "(" character
		int indexOfOpenParenthesis = stepInput.indexOf("(");
		if (indexOfOpenParenthesis != -1) {
			// Extract the substring "e2e_" up to the first "("
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
		// Remove parts enclosed in /*...*/
		String processedStepInput = stepInput.replaceAll("/\\*.*?\\*/", "");
		// Replace ( with [ and ) with ]
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
		// Find the part within square brackets
		int openBracketIndex = stringToTrim.indexOf('[');
		int closeBracketIndex = stringToTrim.lastIndexOf(']');

		if (openBracketIndex != -1 && closeBracketIndex != -1 && openBracketIndex < closeBracketIndex) {
			// Extract the content within the square brackets
			String withinBrackets = stringToTrim.substring(openBracketIndex + 1, closeBracketIndex);

			// Remove spaces within the square brackets
			withinBrackets = withinBrackets.replaceAll("\\s+", "");

			// Reconstruct the stepAction without spaces within brackets
			stringToTrim = stringToTrim.substring(0, openBracketIndex + 1) + withinBrackets
					+ stringToTrim.substring(closeBracketIndex);
		}
		return stringToTrim;
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
		case 1:
			return "First";
		case 2:
			return "Second";
		case 3:
			return "Third";
		case 4:
			return "Fourth";
		case 5:
			return "Fifth";
		default:
			return number + "th";
		}
	}
}
