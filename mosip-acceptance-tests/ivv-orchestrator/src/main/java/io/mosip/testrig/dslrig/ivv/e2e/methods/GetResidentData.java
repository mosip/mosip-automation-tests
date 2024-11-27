package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.PacketUtility;
import io.mosip.testrig.dslrig.ivv.orchestrator.TestRunner;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.restassured.response.Response;

public class GetResidentData extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(GetResidentData.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {

		int nofResident = 1;
		String ageCategory = "";
		Boolean bSkipGuardian = false;
		String missFields = null;
		String[] bioFlag = null;
		HashMap<String, String> genderAndBioFlag = new HashMap<String, String>();
		if (!step.getParameters().isEmpty() && step.getParameters().size() > 3) {
			nofResident = Integer.parseInt(step.getParameters().get(0));
			ageCategory = step.getParameters().get(1);
			bSkipGuardian = Boolean.parseBoolean(step.getParameters().get(2));

			if (step.getParameters().get(3).contains("@@")) {
				bioFlag = step.getParameters().get(3).split("@@");
				genderAndBioFlag.put("Gender", bioFlag[0]);
				genderAndBioFlag.put("Iris", bioFlag[1]);
				genderAndBioFlag.put("Finger", bioFlag[2]);
				genderAndBioFlag.put("Face", bioFlag[3]);

			} else {

				genderAndBioFlag.put("Gender", step.getParameters().get(3));
				genderAndBioFlag.put("Iris", "true");
				genderAndBioFlag.put("Finger", "true");
				genderAndBioFlag.put("Face", "true");

			}

			// Get Miss attrobutes list

			if (step.getParameters().size() > 4)
				missFields = step.getParameters().get(4).replaceAll("@@", ",");

		} else {
			logger.warn("Input parameter missing [nofResident/bAdult/bSkipGuardian/gender]");
			this.hasError = true;
			throw new RigInternalError("Input parameter missing [nofResident/bAdult/bSkipGuardian/gender]");
		}

		// Generate Resident for all ages
		cleanData();
		Response response = packetUtility.generateResident(ageCategory, bSkipGuardian, missFields,
				genderAndBioFlag, step);
		JSONObject jsonObject = new JSONObject(response.getBody().asString());
		JSONArray resp = new JSONObject(response.getBody().asString()).getJSONArray("response");
		if (!jsonObject.getString("status").equalsIgnoreCase("success")) {
			this.hasError = true;
			throw new RigInternalError(response.getBody().asString());
		}
		for (int i = 0; i < resp.length(); i++) {
			JSONObject obj = resp.getJSONObject(i);
			String resFilePath = obj.get("path").toString();
			String id = obj.get("id").toString();
			if (step.getOutVarName() != null)
				step.getScenario().getVariables().put(step.getOutVarName(), resFilePath);

			step.getScenario().getResidentTemplatePaths().put(resFilePath, null);

			step.getScenario().getResidentPersonaIdPro().put(id, resFilePath);
		}
		if (!step.getScenario().getResidentPersonaIdPro().isEmpty())
			storeProp(step.getScenario().getResidentPersonaIdPro());

	}

	private static void storeProp(Properties prop) {
		String filePath = TestRunner.getExternalResourcePath() + props.getProperty("ivv.path.deviceinfo.folder")
				+ "step.getScenario().getResidentPersonaIdPro().properties";
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(filePath);
			prop.store(output, null);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			PacketUtility.closeOutputStream(output);
		}
	}

	private void cleanData() {
		step.getScenario().getPridsAndRids().clear();
		step.getScenario().getUinReqIds().clear();
		step.getScenario().getResidentTemplatePaths().clear();
		step.getScenario().getResidentPathsPrid().clear();
		step.getScenario().getTemplatePacketPath().clear();
	}
}
