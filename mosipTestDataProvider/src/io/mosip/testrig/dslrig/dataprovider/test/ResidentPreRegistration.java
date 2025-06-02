package io.mosip.testrig.dslrig.dataprovider.test;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.mosip.testrig.dslrig.dataprovider.ResidentDataProvider;
import io.mosip.testrig.dslrig.dataprovider.models.AppointmentModel;
import io.mosip.testrig.dslrig.dataprovider.models.AppointmentTimeSlotModel;
import io.mosip.testrig.dslrig.dataprovider.models.CenterDetailsModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipLanguage;
import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.preparation.MosipMasterData;
import io.mosip.testrig.dslrig.dataprovider.test.prereg.PreRegistrationSteps;
import io.mosip.testrig.dslrig.dataprovider.util.Gender;
import io.mosip.testrig.dslrig.dataprovider.util.ReadEmail;
import io.mosip.testrig.dslrig.dataprovider.util.ResidentAttribute;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

public class ResidentPreRegistration {

	private static final Logger logger = LoggerFactory.getLogger(ResidentPreRegistration.class);
	String preRegID;
	ResidentModel person;
	String otpTarget;

	public ResidentPreRegistration() {

	}

	public ResidentPreRegistration(ResidentModel m) {
		person = m;
	}
	

	@Given("^Adult \"(.*)\" from \"(.*)\"$")
	public void createPersonaAdult(String gender, String country, String contextKey) {

		logger.info(String.format("createPersonaAdult %s from %s", gender, country));
		ResidentDataProvider provider = new ResidentDataProvider();
		Gender enumGender = Gender.valueOf(gender);

		provider.addCondition(ResidentAttribute.RA_Age, ResidentAttribute.RA_Adult);
		provider.addCondition(ResidentAttribute.RA_Gender, enumGender);
		List<MosipLanguage> langs = MosipMasterData.getConfiguredLanguages(contextKey);
		if (langs != null) {
			provider.addCondition(ResidentAttribute.RA_PRIMARAY_LANG, langs.get(0).getCode());
			if (langs.size() > 1)
				provider.addCondition(ResidentAttribute.RA_SECONDARY_LANG, langs.get(1).getCode());

		}
		List<ResidentModel> lst = provider.generate(contextKey);
		if (lst != null && !lst.isEmpty())
			person = lst.get(0);

		logger.info(String.format("createPersonaAdult %s{}", person.getName().getFirstName()));

	}

//	@When("^request otp for his/her \"(phone|email)\"$")
//	public void sendOtp(String to, String contextKey) {
//
//		sendOtpTo(to, contextKey);
//
//	}

	public String sendOtpTo(ResidentModel resident, String to, String contextKey) {
	    String emailTo = null;
	    logger.info(String.format("sendOtp %s {}", to));

	    String otpTarget = resident.getContact().getEmailId();

	    if (to.equals("phone")) {
	        otpTarget = resident.getContact().getMobileNumber();
	    }

	    // Override to otp email
	    String bRet = VariableManager.getVariableValue(contextKey, "usePreConfiguredOtp").toString();

	    if (bRet.contains("false")) {
	        emailTo = VariableManager.getVariableValue(contextKey, "otpTargetEmail").toString();
	        if (emailTo != null && !emailTo.trim().isEmpty()) {
	            otpTarget = emailTo;
	        }
	    }

	    emailTo = VariableManager.getVariableValue(contextKey, "usePreConfiguredEmail").toString();
	    if (emailTo != null && !emailTo.trim().isEmpty()) {
	        otpTarget = emailTo;
	    }

	    String result = CreatePersona.sendOtpTo(otpTarget, resident.getPrimaryLanguage(), contextKey);
	    logger.info(String.format("sendOtp Result %s {}", result));

	    // Add the otpTarget to the response
	    JSONObject jsonResult = new JSONObject(result);
	    jsonResult.put("emailId", otpTarget);  // Add emailId or phone used

	    return jsonResult.toString();
	}

	@And("^fetch otp$")
	public void fetchOtp(String contextKey) {

		String otp = "111111";

		String bRet = VariableManager.getVariableValue(contextKey, "usePreConfiguredOtp").toString();

		if (bRet.contains("false")) {
			// do a wait
			Boolean bFound = false;
			int nRepeat = 5;
			while (!bFound && nRepeat >= 0) {
				try {
					Thread.sleep(30000);
				} catch (Exception e) {
					logger.error(e.getMessage());
					Thread.currentThread().interrupt();
				}

				List<String> otps = ReadEmail.getOtps();
				// get last one
				otp = otps.get(otps.size() - 1);
				nRepeat--;
				if (otp != null)
					bFound = true;
			}
		} else
			otp = VariableManager.getVariableValue(contextKey, "preconfiguredOtp").toString();

		VariableManager.setVariableValue("email_otp", otp, contextKey);
	}

	public void fetchAdditionalInfoReqId(long waitTimeInMillis, String contextKey) {
		String additionalInfoReqId = null;
		Boolean bFound = false;
		int nRepeat = 5;
		while (!bFound && nRepeat >= 0) {
			try {
				Thread.sleep(waitTimeInMillis);
			} catch (Exception e) {
				logger.error(e.getMessage());
				Thread.currentThread().interrupt();
			}
			List<String> additionalInfoReqIds = ReadEmail.getadditionalInfoReqIds();
			// get last one
			additionalInfoReqId = additionalInfoReqIds.get(additionalInfoReqIds.size() - 1);
			nRepeat--;
			if (additionalInfoReqId != null)
				bFound = true;
		}
		VariableManager.setVariableValue("email_additionalInfoReqId", additionalInfoReqId, contextKey);
	}

	@Then("^verify otp \"(.*)\"$")
	public String VerifyOtp(String otp, String contextKey) {
		if (otp == null || otp.equals(""))
			otp = VariableManager.getVariableValue(contextKey, "email_otp").toString();

		return CreatePersona.validateOTP(otp, otpTarget, contextKey);
	}

	public String verifyOtp(String to, String otp, String contextKey) {
		if (otp == null || otp.equals(""))
			otp = VariableManager.getVariableValue(contextKey, "email_otp").toString();

		if (to.equals("phone"))
			otpTarget = person.getContact().getMobileNumber();
		else
			otpTarget = person.getContact().getEmailId();

		String emailTo = VariableManager.getVariableValue(contextKey, "usePreConfiguredEmail").toString();
		if (emailTo != null && !emailTo.equals(""))
			otpTarget = emailTo;

		// Override to otp email
		String bRet = VariableManager.getVariableValue(contextKey, "usePreConfiguredOtp").toString();

		if (bRet.contains("false")) {
			emailTo = VariableManager.getVariableValue(contextKey, "otpTargetEmail").toString();
			if (emailTo != null && !emailTo.trim().isEmpty()) {
				otpTarget = emailTo;
			}
		}
		return CreatePersona.validateOTP(otp, otpTarget, contextKey);
	}

	@And("^PreRegister him$")
	public void PreRegisterAdultMale(String contextKey) throws JSONException {
		logger.info("PreRegisterAdultMale");

		String result = PreRegistrationSteps.postApplication(person, null, contextKey);
		preRegID = result;
		logger.info(String.format("PreRegisterAdultMale Result %s {}", result));

	}

	

	@And("^book first available appointment$")
	public void bookAppointment(String contextKey) throws JSONException {
		Boolean bBooked = false;
		AppointmentModel res = PreRegistrationSteps.getAppointments(contextKey);

		for (CenterDetailsModel a : res.getAvailableDates()) {
			if (!a.getHoliday()) {
				for (AppointmentTimeSlotModel ts : a.getTimeslots()) {
					if (ts.getAvailability() > 0) {
						PreRegistrationSteps.bookAppointment(preRegID, a.getDate(), res.getRegCenterId(), ts,
								contextKey);
						bBooked = true;
						VariableManager.setVariableValue(contextKey, "PRID", preRegID);
						break;
					}
				}
			}
			if (bBooked)
				break;
		}
	}

	

}
