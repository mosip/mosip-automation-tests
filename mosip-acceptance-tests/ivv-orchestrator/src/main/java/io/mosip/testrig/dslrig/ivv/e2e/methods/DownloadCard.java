package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Reporter;
import io.mosip.testrig.apirig.dto.TestCaseDTO;

import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.testscripts.GetWithParamForDownloadCard;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.PacketUtility;
import io.mosip.testrig.dslrig.ivv.orchestrator.TestResources;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class DownloadCard extends BaseTestCaseUtil implements StepInterface {
	private static final String downLoadCard_YML = "preReg/downloadCard/downloadCard.yml";
	private static final String PDFFILEPATH = "preReg/downloadCard";
	static Logger logger = Logger.getLogger(DownloadCard.class);
	String fileNameValue = null;

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@SuppressWarnings("static-access")
	@Override
	public void run() throws RigInternalError {
		if (!step.getParameters().isEmpty() && step.getParameters().size() == 1) {
			String _requestId = step.getParameters().get(0);
			if (_requestId.startsWith("$$")) {
				_requestId = step.getScenario().getVariables().get(_requestId);
				step.getScenario().getUinReqIds().clear();
				step.getScenario().getUinReqIds().put("requestId", _requestId);
			}
		}
		String fileName = downLoadCard_YML;
		GetWithParamForDownloadCard getWithPathParam = new GetWithParamForDownloadCard();
		Object[] casesList = getWithPathParam.getYmlTestData(fileName);
		Object[] testCaseList = filterTestCases(casesList);
		logger.info("No. of TestCases in Yml file : " + testCaseList.length);

		for (Object object : testCaseList) {
			for (String requestid : this.step.getScenario().getUinReqIds().values()) {
				try {
					TestCaseDTO test = (TestCaseDTO) object;
					test.setInput(test.getInput().replace("$requestId$", requestid));
					test.setOutput(test.getOutput().replace("$requestId$", requestid));
					Reporter.log("<b><u>" + test.getTestCaseName() + "</u></b>");
					long startTime = System.currentTimeMillis();
					logger.info(this.getClass().getSimpleName() + " starts at..." + startTime + " MilliSec");
					getWithPathParam.test(test);
					long stopTime = System.currentTimeMillis();
					long elapsedTime = stopTime - startTime;
					logger.info("Time taken to execute " + this.getClass().getSimpleName() + ": " + elapsedTime
							+ " MilliSec");
					if (getWithPathParam.pdf.length > 0) {
						download(getWithPathParam.pdf, requestid);
					} else {
						this.hasError = true;
						throw new RigInternalError("downloaded pdf size is less than 0");
					}

				} catch (AdminTestException e) {
					logger.error("Failed at downloading card: " + e.getMessage());
					this.hasError = true;
					throw new RigInternalError("Failed at downloading card");
				} catch (Exception e) {
					logger.error("Failed at downloading card: " + e.getMessage());
					this.hasError = true;
					throw new RigInternalError("Failed at downloading card");
				}
			}
		}

	}

	private void download(byte[] pdfFile, String requestid) {
		FileOutputStream fos = null;
		try {

			fos = new FileOutputStream(TestResources.getResourcePath() + PDFFILEPATH + "/" + requestid + ".pdf");
			fos.write(pdfFile);
		} catch (IOException e) {
			logger.error("Failed to download the pdf Exception: " + e.getMessage());
		} finally {
			PacketUtility.closeOutputStream(fos);
		}
	}

}
