package io.mosip.testrig.dslrig.ivv.orchestrator;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.testrig.apirig.kernel.util.ConfigManager;
import io.mosip.testrig.apirig.service.BaseTestCase;
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
	private static final Logger logger = LoggerFactory.getLogger(Orchestrator.class);
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

	static AtomicInteger counterLock = new AtomicInteger(0); // enable fairness policy

	private HashMap<String, String> packages = new HashMap<String, String>() {
		{
			put("e2e", "io.mosip.testrig.dslrig.ivv.e2e.methods");
		}
	};
	 

	/*
	 * HashMap<String, String> packages = (HashMap<String, String>)
	 * Collections.singletonMap("e2e", "io.mosip.testrig.dslrig.ivv.e2e.methods");
	 */

	@BeforeSuite
	public void beforeSuite() {
		this.properties = Utils.getProperties(TestRunner.getExternalResourcePath() + "/config/config.properties");
		Utils.setupLogger(System.getProperty("user.dir") + "/" + System.getProperty("testng.outpur.dir") + "/"
				+ this.properties.getProperty("ivv._path.auditlog"));
		htmlReporter = new ExtentHtmlReporter(

				System.getProperty("user.dir") + "/" + System.getProperty("testng.outpur.dir") + "/"
						+ this.properties.getProperty("ivv._path.reports"));
		extent = new ExtentReports();

		extent.attachReporter(htmlReporter);

		if (ConfigManager.getPushReportsToS3().equalsIgnoreCase("yes")) {
			// EXTENT REPORT
			File repotFile2 = new File(System.getProperty("user.dir") + "/" + System.getProperty("testng.outpur.dir")
					+ "/" + System.getProperty("emailable.report3.name"));
			logger.info("reportFile is::" + System.getProperty("user.dir") + "/"
					+ System.getProperty("testng.outpur.dir") + "/" + System.getProperty("emailable.report3.name"));

			S3Adapter s3Adapter = new S3Adapter();
			boolean isStoreSuccess = false;
			try {
				isStoreSuccess = s3Adapter.putObject(ConfigManager.getS3Account(), BaseTestCase.testLevel, null, null,
						System.getProperty("emailable.report3.name"), repotFile2);

				isStoreSuccess = s3Adapter.putObject(ConfigManager.getS3Account(), BaseTestCase.testLevel, null, null,
						System.getProperty("emailable.report3.name"), repotFile2);

				logger.info("isStoreSuccess:: " + isStoreSuccess);
			} catch (Exception e) {
				logger.info("error occured while pushing the object" + e.getLocalizedMessage());
				logger.error(e.getMessage());
			}
			if (isStoreSuccess) {
				logger.info("Pushed file to S3");
			} else {
				logger.info("Failed while pushing file to S3");
			}
		}
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

		scenarioSheet = ConfigManager.getmountPathForScenario() + "/scenarios/" + "scenarios-" + BaseTestCase.testLevel
				+ "-" + BaseTestCase.environment + ".csv";

		Path path = Paths.get(scenarioSheet);

		if (!Files.exists(path)) {
			scenarioSheet = ConfigManager.getmountPathForScenario() + "/default/" + "scenarios-"
					+ BaseTestCase.testLevel + "-" + "default" + ".csv";
		} else if (scenarioSheet == null || scenarioSheet.isEmpty()) {
			throw new RigInternalError("ScenarioSheet argument missing");
		}

		ParserInputDTO parserInputDTO = new ParserInputDTO();
		parserInputDTO.setConfigProperties(properties);
		parserInputDTO.setDocumentsFolder(
				TestRunner.getGlobalResourcePath() + "/" + properties.getProperty("ivv.path.documents.folder"));
		parserInputDTO.setBiometricsFolder(
				TestRunner.getGlobalResourcePath() + "/" + properties.getProperty("ivv.path.biometrics.folder"));
		parserInputDTO.setPersonaSheet(
				TestRunner.getGlobalResourcePath() + "/" + properties.getProperty("ivv.path.persona.sheet"));
		parserInputDTO.setScenarioSheet(scenarioSheet);

		parserInputDTO.setRcSheet(
				TestRunner.getGlobalResourcePath() + "/" + properties.getProperty("ivv.path.rcpersona.sheet"));
		parserInputDTO.setPartnerSheet(
				TestRunner.getGlobalResourcePath() + "/" + properties.getProperty("ivv.path.partner.sheet"));
		parserInputDTO.setIdObjectSchema(
				TestRunner.getGlobalResourcePath() + "/" + properties.getProperty("ivv.path.idobject"));
		parserInputDTO.setDocumentsSheet(
				TestRunner.getGlobalResourcePath() + "/" + properties.getProperty("ivv.path.documents.sheet"));
		parserInputDTO.setBiometricsSheet(
				TestRunner.getGlobalResourcePath() + "/" + properties.getProperty("ivv.path.biometrics.sheet"));
		parserInputDTO.setGlobalsSheet(
				TestRunner.getGlobalResourcePath() + "/" + properties.getProperty("ivv.path.globals.sheet"));
		parserInputDTO.setConfigsSheet(
				TestRunner.getGlobalResourcePath() + "/" + properties.getProperty("ivv.path.configs.sheet"));

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
		Object[][] dataArray = new Object[scenarios.size()][5];
		for (int i = 0; i < scenarios.size(); i++) {
			dataArray[i][0] = i;
			dataArray[i][1] = scenarios.get(i);
			dataArray[i][2] = configs;
			dataArray[i][3] = globals;
			dataArray[i][4] = properties;
		}
		return dataArray;
	}

	@BeforeMethod
	public void beforeMethod(Method method) {

	}

	private void updateRunStatistics(Scenario scenario)
			throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		logger.info(Thread.currentThread().getName() + ": " + counterLock.getAndIncrement());
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
		// Another scenario execution kicked-off before BEFORE_SUITE execution

		if (!scenario.getId().equalsIgnoreCase("0")) {

			// AFTER_SUITE scenario execution kicked-off before all execution
			if (scenario.getId().equalsIgnoreCase("AFTER_SUITE")) // || scenariosExeuted)
			{
				// Wait till all scenarios are executed
				while (counterLock.get() < totalScenario - 1) // executed excluding after suite
				{
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
		ObjectMapper mapper = new ObjectMapper();

		message = "Scenario_" + scenario.getId() + ": " + scenario.getDescription();
		logger.info("-- *** Scenario " + scenario.getId() + ": " + scenario.getDescription() + " *** --");
		ExtentTest extentTest = extent.createTest("Scenario_" + scenario.getId() + ": " + scenario.getDescription());
		Store store = new Store();
		store.setConfigs(configs);
		store.setGlobals(globals);
		store.setPersona(scenario.getPersona());
		store.setRegistrationUsers(scenario.getRegistrationUsers());
		store.setPartners(scenario.getPartners());
		store.setProperties(this.properties);
		Reporter.log("<b><u>" + "Scenario_" + scenario.getId() + ": " + scenario.getDescription() + "</u></b>");
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
				Reporter.log("\n\n\n\n==============" + "[Test Step: " + step.getName() + "] [Test Parameters: "
						+ step.getParameters() + "] " + "================ \n\n\n\n\n", true);
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
			} catch (ClassNotFoundException e) {
				extentTest.error(identifier + " - ClassNotFoundException --> " + e.toString());
				logger.error(e.getMessage());

				updateRunStatistics(scenario);
				Assert.assertTrue(false);

				return;
			} catch (IllegalAccessException e) {
				extentTest.error(identifier + " - IllegalAccessException --> " + e.toString());
				logger.error(e.getMessage());
				updateRunStatistics(scenario);
				Assert.assertTrue(false);
				return;
			} catch (InstantiationException e) {
				extentTest.error(identifier + " - InstantiationException --> " + e.toString());
				logger.error(e.getMessage());
				updateRunStatistics(scenario);
				Assert.assertTrue(false);
				return;

			} catch (RigInternalError e) {
				extentTest.error(identifier + " - RigInternalError --> " + e.getMessage());
				logger.error(e.getMessage());
				updateRunStatistics(scenario);
				Assert.assertTrue(false);
				return;

			} catch (RuntimeException e) {
				extentTest.error(identifier + " - RuntimeException --> " + e.toString());
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

}
