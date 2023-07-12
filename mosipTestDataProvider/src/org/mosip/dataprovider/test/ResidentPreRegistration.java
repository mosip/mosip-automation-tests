package org.mosip.dataprovider.test;
import java.util.List;


import org.json.JSONException;
import org.mosip.dataprovider.ResidentDataProvider;
import org.mosip.dataprovider.models.AppointmentModel;
import org.mosip.dataprovider.models.AppointmentTimeSlotModel;
import org.mosip.dataprovider.models.CenterDetailsModel;
import org.mosip.dataprovider.models.MosipDocument;
import org.mosip.dataprovider.models.MosipLanguage;
import org.mosip.dataprovider.models.ResidentModel;
import org.mosip.dataprovider.preparation.MosipMasterData;
import org.mosip.dataprovider.test.prereg.PreRegistrationSteps;
import org.mosip.dataprovider.util.Gender;
import org.mosip.dataprovider.util.ReadEmail;
import org.mosip.dataprovider.util.ResidentAttribute;
//import org.mosip.dataprovider.util.RestClient;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import variables.VariableManager;

public class ResidentPreRegistration {


	String preRegID;
	ResidentModel person;
	String otpTarget;
	public ResidentPreRegistration() {
		
	}
	public ResidentPreRegistration(ResidentModel m) {
		person = m;
	}
	/*public static RestClient getRestClient() {
		return restClient;
	}*/
	
	@Given("^Adult \"(.*)\" from \"(.*)\"$")
	public void createPersonaAdult(String gender, String country,String contextKey) {
	
		System.out.println(String.format("createPersonaAdult %s from %s", gender, country));
		ResidentDataProvider provider = new ResidentDataProvider();
		Gender enumGender = Gender.valueOf(gender);
		
		provider.addCondition(ResidentAttribute.RA_Age, ResidentAttribute.RA_Adult);
		provider.addCondition(ResidentAttribute.RA_Gender, enumGender);
		List<MosipLanguage> langs = MosipMasterData.getConfiguredLanguages(contextKey);
		if(langs != null ) {
			provider.addCondition(ResidentAttribute.RA_PRIMARAY_LANG, langs.get(0).getCode());
			if(langs.size() > 1)
			provider.addCondition(ResidentAttribute.RA_SECONDARY_LANG, langs.get(1).getCode());
			
		}
		List<ResidentModel> lst = provider.generate(contextKey);
		if(lst != null && !lst.isEmpty())
			person  = lst.get(0);
		
		System.out.println(String.format("createPersonaAdult %s", person.getName().getFirstName()));
			
	}
	
	@When("^request otp for his/her \"(phone|email)\"$")
	public void sendOtp(String to, String contextKey)  {
		
		sendOtpTo(to,contextKey); 
		
	}
	public String sendOtpTo(String to,String contextKey) {

		System.out.println(String.format("sendOtp %s ",to));
		
		if(to.equals("phone"))
			otpTarget = person.getContact().getMobileNumber();
		else
			otpTarget = person.getContact().getEmailId();
		
		//Override to otp email
		String bRet  = VariableManager.getVariableValue(contextKey, "usePreConfiguredOtp").toString();
		
		if(bRet.contains("false")){
		
			otpTarget = VariableManager.getVariableValue(contextKey, "otpTargetEmail").toString();
		}
		String emailTo  = VariableManager.getVariableValue(contextKey, "usePreConfiguredEmail").toString();
		if(emailTo != null && !emailTo.equals(""))
			otpTarget = emailTo;
		
		String result = CreatePersona.sendOtpTo(otpTarget,person.getPrimaryLanguage(),contextKey);
		System.out.println(String.format("sendOtp Result %s ",result));
		return result;
	}
	@And("^fetch otp$")
	public void fetchOtp(String contextKey) {
		
		String otp ="111111";
		
		String bRet  = VariableManager.getVariableValue(contextKey, "usePreConfiguredOtp").toString();
		
		if(bRet.contains("false")){
			//do a wait
			Boolean bFound = false;
			int nRepeat = 5;
			while(!bFound && nRepeat >= 0) {
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				List<String> otps = ReadEmail.getOtps();
				//get last one
				otp = otps.get( otps.size()-1);
				nRepeat--;
				if(otp != null) bFound = true;
			}
		}
		else
			otp = VariableManager.getVariableValue(contextKey, "preconfiguredOtp").toString();
		
		VariableManager.setVariableValue( "email_otp", otp,contextKey);
	}
	
	public void fetchAdditionalInfoReqId(long waitTimeInMillis,String contextKey) {
		String additionalInfoReqId = null;
		Boolean bFound = false;
		int nRepeat = 5;
		while (!bFound && nRepeat >= 0) {
			try {
				Thread.sleep(waitTimeInMillis);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			List<String> additionalInfoReqIds = ReadEmail.getadditionalInfoReqIds();
			// get last one
			additionalInfoReqId = additionalInfoReqIds.get(additionalInfoReqIds.size() - 1);
			nRepeat--;
			if (additionalInfoReqId != null)
				bFound = true;
		}
		VariableManager.setVariableValue("email_additionalInfoReqId", additionalInfoReqId,contextKey);
	}
	@Then("^verify otp \"(.*)\"$") 
	public String VerifyOtp(String otp, String contextKey)  {
		if(otp == null || otp.equals(""))
			otp =VariableManager.getVariableValue(contextKey, "email_otp").toString();
	
	
		return CreatePersona.validateOTP(otp, otpTarget,contextKey);
	}
	public String verifyOtp(String to, String otp, String contextKey) {

		if(otp == null || otp.equals(""))
			otp =VariableManager.getVariableValue(contextKey, "email_otp").toString();

		if(to.equals("phone"))
			otpTarget = person.getContact().getMobileNumber();
		else
			otpTarget = person.getContact().getEmailId();
		
		String emailTo  = VariableManager.getVariableValue(contextKey, "usePreConfiguredEmail").toString();
		if(emailTo != null && !emailTo.equals(""))
			otpTarget = emailTo;
		
		//Override to otp email
		String bRet  = VariableManager.getVariableValue( contextKey,"usePreConfiguredOtp").toString();
		
		if(bRet.contains("false")){
		
			otpTarget = VariableManager.getVariableValue(contextKey, "otpTargetEmail").toString();
		}
		return CreatePersona.validateOTP(otp, otpTarget,contextKey);
	}
	@And("^PreRegister him$") 
	public void PreRegisterAdultMale(String contextKey) throws JSONException { 
		System.out.println("PreRegisterAdultMale");
	
		String result = PreRegistrationSteps.postApplication(person,null,contextKey);
		preRegID = result;
		System.out.println(String.format("PreRegisterAdultMale Result %s ",result));
		
		//assert(1 == 2);
	} 
	 @Then("^upload \"(POI|POD)\" document$") 
	 public void uploadProof(String docCategory, String contextKey) throws JSONException {
		
		 System.out.println("uploadProof " + docCategory);
		 int i=0;
		 for(MosipDocument a:person.getDocuments()) {
			 PreRegistrationSteps.UploadDocument(a.getDocCategoryCode(),
					 a.getType().get(i).getCode(),
					 a.getDocCategoryLang(), a.getDocs().get(i) ,preRegID,contextKey);
			 break;
		 }
		 
	 }

	 @And("^book first available appointment$") 
	 public void bookAppointment(String contextKey) throws JSONException {
		 Boolean bBooked = false;
		 AppointmentModel res = PreRegistrationSteps.getAppointments(contextKey);
			
			for( CenterDetailsModel a: res.getAvailableDates()) {
				if(!a.getHoliday()) {
					for(AppointmentTimeSlotModel ts: a.getTimeslots()) {
						if(ts.getAvailability() > 0) {
							PreRegistrationSteps.bookAppointment(preRegID,a.getDate(),res.getRegCenterId(),ts,contextKey);
							bBooked = true;
							VariableManager.setVariableValue(contextKey,"PRID",preRegID);
							break;
						}
					}
				}
				if(bBooked) break;
			}
	 }
	 public static void main(String[] args) {
		 /*
		 ResidentPreRegistration a =  new ResidentPreRegistration();
		 a.createPersonaAdult("Male", "IN");
		 
		 a.sendOtp("email");
		 */
	 }
	
}
