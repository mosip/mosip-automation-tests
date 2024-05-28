package io.mosip.testrig.dslrig.ivv.orchestrator;

import java.io.File;
import java.io.FileWriter;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.testng.Assert;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
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

import io.mosip.testrig.apirig.authentication.fw.util.ReportUtil;
import io.mosip.testrig.apirig.kernel.util.ConfigManager;
import io.mosip.testrig.apirig.service.BaseTestCase;
import io.mosip.testrig.apirig.testrunner.MosipTestRunner;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.dtos.ParserInputDTO;
import io.mosip.testrig.dslrig.ivv.core.dtos.RegistrationUser;
import io.mosip.testrig.dslrig.ivv.core.dtos.Scenario;
import io.mosip.testrig.dslrig.ivv.core.dtos.Store;
import io.mosip.testrig.dslrig.ivv.core.exceptions.FeatureNotSupportedError;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.core.utils.Utils;
import io.mosip.testrig.dslrig.ivv.dg.DataGenerator;
import io.mosip.testrig.dslrig.ivv.e2e.methods.Center;
import io.mosip.testrig.dslrig.ivv.parser.Parser;
import com.sun.management.OperatingSystemMXBean;

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
	public static long suiteStartTime = System.currentTimeMillis();
	public static long suiteMaxTimeInMillis = 7200000; // 2 hour in milliseconds
	static AtomicInteger counterLock = new AtomicInteger(0); // enable fairness policy

	private HashMap<String, String> packages = new HashMap<String, String>() {
		{
			put("e2e", "io.mosip.testrig.dslrig.ivv.e2e.methods");
		}
	};

	static {
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@BeforeSuite
	public void beforeSuite() {

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

		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);

	}

	@BeforeTest
	public static void create_proxy_server() {

	}

	@AfterSuite
	public void afterSuite() {
		extent.flush();
	}

	@DataProvider(name = "ScenarioDataProvider", parallel = true)
	public static Object[][] dataProvider() throws RigInternalError {
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
		for (Scenario scenario : scenarios) {
			if (scenario.getId().equalsIgnoreCase("0") || scenario.getId().equalsIgnoreCase("AFTER_SUITE")
					|| ConfigManager.isInTobeExecuteList(scenario.getId())) {
				filteredScenarios.add(scenario);
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
		return dataArray;
	}

	@BeforeMethod
	public void beforeMethod(Method method) {

	}

	private synchronized void updateRunStatistics(Scenario scenario)
			throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		logger.info("Updating statistics for scenario: " + scenario.getId() + " -- updating the executed count to: "
				+ counterLock.getAndIncrement());
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
			IllegalAccessException, InstantiationException {

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
			} else {

				// Wait for before suite executed
				while (beforeSuiteExeuted == false) {
					Thread.sleep(10000); // Sleep for 10 sec

					logger.info(" Thread ID: " + Thread.currentThread().getId()
							+ " inside beforeSuiteExecuted == false " + counterLock.get() + "- " + scenario.getId());
				}
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
		String tags = System.getProperty("ivv.tags");
		String identifier = null;
		if (tags == null || tags.isEmpty()) {
			logger.info("Running Scenario #" + scenario.getId());
		} else if (!matchTags(tags, scenario.getTags())) {
			logger.info("Skipping Scenario #" + scenario.getId());
			throw new SkipException("Skipping Scenario #" + scenario.getId());
		}

		message = "Scenario_" + scenario.getId() + ": " + scenario.getDescription();
		logger.info("-- *** Scenario " + scenario.getId() + ": " + scenario.getDescription() + " *** --");
		ExtentTest extentTest = extent.createTest("Scenario_" + scenario.getId() + ": " + scenario.getDescription());

		// Check whether the scenario is in the defined skipped list
		if (ConfigManager.isInTobeSkippedList("S-" + scenario.getId())) {
			extentTest.skip("S-" + scenario.getId() + ": Skipping scenario due to known platform issue");
			updateRunStatistics(scenario);
			throw new SkipException("S-" + scenario.getId() + ": Skipping scenario due to known platform issue");
		}
		if (ConfigManager.isInTobeSkippedList("A-" + scenario.getId())) {
			extentTest.skip("A-" + scenario.getId() + ": Skipping scenario due to known Automation issue");
			updateRunStatistics(scenario);
			throw new SkipException("A-" + scenario.getId() + ": Skipping scenario due to known Automation issue");
		}

		Store store = new Store();
		store.setConfigs(configs);
		store.setGlobals(globals);
		store.setPersona(scenario.getPersona());
		store.setRegistrationUsers(scenario.getRegistrationUsers());
		store.setPartners(scenario.getPartners());
		store.setProperties(this.properties);
		Reporter.log("<div style='background-color: black; color: white; padding: 10px; border-radius: 5px;'><b><u>Scenario_" + scenario.getId() + ": " + scenario.getDescription() + "</u></b></div>");
		for (Scenario.Step step : scenario.getSteps()) {

			identifier = "> #[Test Step: " + step.getName() + "] [Test Parameters: " + step.getParameters()
					+ "]  [Test outVarName: " + step.getOutVarName() + "] [module: " + step.getModule() + "] [variant: "

					+ step.getVariant() + "]";
			logger.info(identifier);

			try {
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
				String stepParams[] = getStepDetails("S_" + step.getScenario().getId() + stepAction);

				StringBuilder sb = new StringBuilder();
				sb.append(
						"<div> <textarea style='border:solid 1px gray; background-color: darkgray;' name='headers' rows='3' cols='160' readonly='true'>");
				sb.append("Step Name: " + step.getName() + "\n");
				sb.append("Step Description:" + stepParams[0] + "\n");
				sb.append("Step Parameters: " + stepParams[1]);
				sb.append("</textarea> </div>");

				Reporter.log(sb.toString());

				st.run();

				st.assertHttpStatus();
				if (st.hasError()) {
					extentTest.fail(identifier + " - failed");
					Assert.assertFalse(st.hasError());
				}
				if (st.getErrorsForAssert().size() > 0) {
					st.errorHandler();
					if (st.hasError()) {
						extentTest.fail(identifier + " - failed");
						Assert.assertFalse(st.hasError());
					}
				} else {
					st.assertNoError();
					if (st.hasError()) {
						extentTest.fail(identifier + " - failed");
						Assert.assertFalse(st.hasError());
					}
				}
				store = st.getState();
				if (st.hasError()) {
					extentTest.fail(identifier + " - failed");
					Assert.assertFalse(st.hasError());
				} else {
					extentTest.pass(identifier + " - passed");
				}
			} catch (SkipException e) {
				extentTest.skip(identifier + " - skipped");
				updateRunStatistics(scenario);
				logger.error(e.getMessage());
				Reporter.log(e.getMessage());
				throw new SkipException(e.getMessage());
			} catch (ClassNotFoundException e) {
				extentTest.error(identifier + " - ClassNotFoundException --> " + e.getMessage());
				logger.error(e.getMessage());
				updateRunStatistics(scenario);
				Assert.assertTrue(false);
				return;
			} catch (IllegalAccessException e) {
				extentTest.error(identifier + " - IllegalAccessException --> " + e.getMessage());
				logger.error(e.getMessage());
				updateRunStatistics(scenario);
				Assert.assertTrue(false);
				return;
			} catch (InstantiationException e) {
				extentTest.error(identifier + " - InstantiationException --> " + e.getMessage());
				logger.error(e.getMessage());
				updateRunStatistics(scenario);
				Assert.assertTrue(false);
				return;
			} catch (RigInternalError e) {
				if (scenario.getId().equals("0")) {
					beforeSuiteFailed = true;
				}
				extentTest.error(identifier + " - RigInternalError --> " + e.getMessage());
				logger.error(e.getMessage());
				Reporter.log(e.getMessage());
				updateRunStatistics(scenario);
				Assert.assertTrue(false);
				return;
			} catch (RuntimeException e) {
				extentTest.error(identifier + " - RuntimeException --> " + e.getMessage());
				logger.error(e.getMessage());
				updateRunStatistics(scenario);
				Assert.assertTrue(false);
				return;
			} catch (FeatureNotSupportedError e) {
				logger.warn(e.getMessage());
				Reporter.log(e.getMessage());
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

	@SuppressWarnings("deprecation")
	public StepInterface getInstanceOf(Scenario.Step step)
			throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		String className = getPackage(step) + "." + step.getName().substring(0, 1).toUpperCase()
				+ step.getName().substring(1);
		return (StepInterface) Class.forName(className).newInstance();
	}

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
		if (ConfigManager.useExternalScenarioSheet()) {
			// Check first for the JSON file
			scenarioSheet = ConfigManager.getmountPathForScenario() + "/scenarios/" + "scenarios-"
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
			scenarioSheet = MosipTestRunner.getGlobalResourcePath() + "/config/scenarios.json";
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
		String tempCSVPath = MosipTestRunner.getGlobalResourcePath() + "/scenarios.csv";
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
		if (ConfigManager.IsDebugEnabled()) {
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
		// Extract the value part if there's an "=" delimiter
		String processedStepInput = stepInput;
		int equalsIndex = stepInput.indexOf('=');
		if (equalsIndex != -1) {
			processedStepInput = stepInput.substring(equalsIndex + 1);
		}
		// Remove parts enclosed in /*...*/
		processedStepInput = processedStepInput.replaceAll("/\\*.*?\\*/", "");
		// Replace ( with [ and ) with ]
		processedStepInput = processedStepInput.replace('(', '[').replace(')', ']');
		processedStepInput = trimSpaceWithinSquareBrackets(processedStepInput);
		String[] descAndExample = new String[2];
		descAndExample[0] = description;
		descAndExample[1] = stepInput;
		allStepsMap.put("S_" + scenarioNumber + processedStepInput, descAndExample);
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

}
