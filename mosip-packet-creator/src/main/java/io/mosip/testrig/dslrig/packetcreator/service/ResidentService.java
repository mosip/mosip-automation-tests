package io.mosip.testrig.dslrig.packetcreator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.test.registrationclient.RegistrationSteps;
import io.restassured.response.Response;

@Component
public class ResidentService {

	private static final Logger logger = LoggerFactory.getLogger(ResidentService.class);

	public String downloadCard(String personaPath, String uin, String context) throws Exception {
		ResidentModel resident = ResidentModel.readPersona(personaPath);
		RegistrationSteps steps = new RegistrationSteps();
		return steps.downloadCard(resident, uin, context);
	}

	public String getRIDStatus(String rid, String context) {
		RegistrationSteps steps = new RegistrationSteps();
		try {
			return steps.getRIDStatus(rid, context);
		} catch (Exception e) {
			logger.error("getRIDStatus", e);
		}
		return "{Failed}";
	}

	public String getUINByRID(String rid, String context) throws Exception {
		RegistrationSteps steps = new RegistrationSteps();
		return steps.getUINByRID(rid, context);

	}

	public Response getStagesByRID(String rid, String context) throws Exception {
		RegistrationSteps steps = new RegistrationSteps();
		return steps.getStagesByRID(rid, context);
	}
}
