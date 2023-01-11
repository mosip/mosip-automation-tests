package io.mosip.ivv.e2e.methods;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.testng.Reporter;

import io.mosip.admin.fw.util.AdminTestUtil;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.PacketUtility;
import io.mosip.ivv.orchestrator.TestRunner;
import io.mosip.kernel.util.ConfigManager;
import io.mosip.service.BaseTestCase;

public class WritePreReq extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(WritePreReq.class);

	@Override
	public void run() {

		String value = null;
		String appendedkey = null;
		HashMap<String, String> map = new HashMap<String, String>();
		Reporter.log("==========STEP ====== WritePreReq ", true);
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.warn("PreRequisite Arugemnt is  Missing : Please pass the argument from DSL sheet");
		} else if(step.getParameters().size() >= 1) {
			value = step.getParameters().get(0);

			if (value.startsWith("$$")) {
				map = step.getScenario().getVariables();
			}
		}
		if(step.getParameters().size() >= 2) {appendedkey=step.getParameters().get(1);
		map.put("appendedkey", appendedkey);
		}
		// Instantiating the properties file
		Properties props = new Properties();
		// Populating the properties file
		
		//take all properties from application .properties add to map2 props.putAll(map2);
		//Application.properties start
		//String fileName = (TestRunner.getExternalResourcePath() + "/config/kernel.properties");
	
//		FileInputStream fis = null;
//	      Properties prop = null;
//	      try {
//	         fis = new FileInputStream(fileName);
//	         prop = new Properties();
//	         prop.load(kernelprops);
//	      } catch(FileNotFoundException fnfe) {
//	         fnfe.printStackTrace();
//	      } catch(IOException ioe) {
//	         ioe.printStackTrace();
//	      } 
		
//	      
		  Properties kernelprops=ConfigManager.propsKernel;
		
		//  {admin_zone_clientId=mosip-admin-client, FetchDeviceSpec_lang_URI=/v1/masterdata/devicespecifications/{langcode}, mosip.test.persona.locationsdatapath=../mountvolume/profile_resource/location_data, mosip.test.persona.datapath=../mountvolume/profile_resource/, partner_user_password=mosip123, getUserHistory=/v1/masterdata/users/{id}/{eff_dtimes}, FetchRejectionReason_URI=/v1/masterdata/packetrejectionreasons/{reasoncategorycode}/{langcode}, push-reports-to-s3=yes, FetchRegCentHolidays_URI=/v1/masterdata/getregistrationcenterholidays/{langcode}/{registrationcenterid}/{year}, decrypt_URI=/v1/keymanager/decrypt, mosip_resident_client_id=mosip-resident-client, s3-region=null, db-server=api-internal.dev2.mosip.net, mosip_pms_app_id=partner, syncConf=/v1/syncdata/configs, getRoles=/v1/syncdata/roles, admin_zone_clientSecret=blBWZRzbZKk1ztQG, FetchApplication_URI=/v1/masterdata/applicationtypes, fetchAllTemplate=/v1/masterdata/templates/templatetypecodes/{code}, s3-host=http://minio.minio:9000, FetchTitle_URI=/v1/masterdata/title/{langcode}, admin_zone_password=mosip123, FetchRegCent_id_lang_URI=/v1/masterdata/registrationcenters/{id}/{langcode}, ConfigParameters=mosip.kernel.rid.length,mosip.kernel.uin.length,mosip.kernel.sms.country.code,mosip.kernel.sms.number.length,mosip.kernel.otp.default-length,mosip.kernel.otp.expiry-time,mosip.kernel.otp.key-freeze-time,mosip.kernel.otp.validation-attempt-threshold,mosip.kernel.otp.min-key-length,mosip.kernel.otp.max-key-length,mosip.kernel.licensekey.length,mosip.supported-languages, fetchmapLicenseKey=/v1/keymanager/license/permission, getusersBasedOnRegCenter=/v1/syncdata/userdetails/{regid}, FetchLocationHierarchy_URI_hierarchyname=/v1/masterdata/locations/locationhierarchy/{hierarchyname}, audit_username=postgres, auditLog_URI=/v1/auditmanager/audits, admin_userName=dsl0, FetchRegcentMachUserMaping_URI=/v1/masterdata/getregistrationmachineusermappinghistory/{effdtimes}/{registrationcenterid}/{machineid}/{userid}, partner_userName=111997, FetchApplication_lang_URI=/v1/masterdata/applicationtypes/{langcode}, show_sql=true, hibernate.connection.driver_class=org.postgresql.Driver, pmsAuthInternal=true, FetchDevice_lang_URI=/v1/masterdata/devices/{languagecode}, FetchDocumentCategories_URI_withcodeAndLangCode=/v1/masterdata/documentcategories/{code}/{langcode}, usePreConfiguredEmail=sanath.test.mosip@gmail.com, pool_size=1, FetchMachineHistory_URI=/v1/masterdata/machineshistories/{id}/{langcode}/{effdatetimes}, mosip_reg_client_id=mosip-reg-client, new_Resident_User=111995, bulkUploadUrl=/v1/admin/bulkupload, email_otp=111111, mosip_idrepo_app_id=idrepo, keycloakAuthURL=/auth/realms/master/protocol/openid-connect/token, zoneNameUrl=/v1/masterdata/zones/zonename, FetchRegCent_URI=/v1/masterdata/registrationcenters, mosip.test.persona.facedatapath=../mountvolume/profile_resource/face_data, sendOtp=/v1/authmanager/authenticate/sendotp, dialect=org.hibernate.dialect.PostgreSQLDialect, partner_username=postgres, DB_PORT=, audit_url=jdbc:postgresql://api-internal.dev2.mosip.net:30090/mosip_audit, s3-account=automation, mosip.test.persona.templatesdatapath=../mountvolume/profile_resource/templates_data, FetchDevice_id_lang_URI=/v1/masterdata/devices/{languagecode}/{deviceType}, syncMdatawithRegCentIdKeyIndex=/v1/syncdata/clientsettings/{regcenterid}, updatePreRegStatus=preregistration/v1/applications/prereg/status/, FetchDocumentCategories_URI=/v1/masterdata/documentcategories/{langcode}, validateGenderByName=/v1/masterdata/gendertypes/validate/{gendername}, mosip_idrepo_client_id=mosip-idrepo-client, FetchHolidays_id_lang_URI=/v1/masterdata/holidays/{holidayid}/{langcode}, FetchGenderType_id_lang_URI=/v1/masterdata/gendertypes/{langcode}, mosip_resident_app_id=resident, getIndividualType=/v1/masterdata/individualtypes, getDocType_DocCatByAppID=/v1/masterdata/applicanttype/{applicantId}/languages, FetchRegCent_loc_lang_URI=/v1/masterdata/getlocspecificregistrationcenters/{langcode}/{locationcode}, FetchHolidays_id_URI=/v1/masterdata/holidays/{holidayid}, mosip_pms_client_id=mosip-pms-client, FetchApplication_id_lang_URI=/v1/masterdata/applicationtypes/{code}/{langcode}, mosip_testrig_client_id=mosip-testrig-client, mosip_hotlist_client_id=mosip-hotlist-client, FetchLocationHierarchy_URI_locationcode=/v1/masterdata/locations/{locationcode}/{langcode}, roles=GLOBAL_ADMIN,ID_AUTHENTICATION,REGISTRATION_ADMIN,REGISTRATION_SUPERVISOR,ZONAL_ADMIN,AUTH_PARTNER,PARTNER_ADMIN,PMS_ADMIN,POLICYMANAGER,REGISTRATION_SUPERVISOR, AuthAppID=resident, preregValidateOtp=/preregistration/v1/login/validateOtp, new_Resident_Password=mosip123, getApplicantType=/v1/masterdata/getApplicantType, mosip_resident_client_secret=iJHSOTIYkQlKPW1S, AuthClientSecret=PFDEzFrQo70EkfzS, FetchLocationHierarchy_URI_withlangCode=/v1/masterdata/locations/{langcode}, reportLogPath=automationLogAndReport, mosip_regclient_app_id=registrationclient, FetchBlackListedWord_URI=/v1/masterdata/blacklistedwords/{langcode}, mosip_idrepo_client_secret=gud7CZr4gSoa32zi, keycloak-realm-id=mosip, mosip.test.persona.irisdatapath=../mountvolume/profile_resource/iris_data/IITD Database, s3-user-secret=minioadmin, preregSendOtp=/preregistration/v1/login/sendOtp/langcode, mosip_admin_client_secret=blBWZRzbZKk1ztQG, attempt=10, fetchImmediateChildLocation=/v1/masterdata/locations/immediatechildren/{locationcode}/{langcode}, fetchIncrementalData=/v1/syncjob/syncjobdef, s3-user-key=minioadmin, uingenerator=/v1/idgenerator/uin, keycloak_Password=sluOKN5u9o, preconfiguredOtp=111111, mosip.test.persona.documentsdatapath=../mountvolume/profile_resource/documents_data/templates/, fetchmasterdata=/v1/syncdata/masterdata, FetchHolidays_URI=/v1/masterdata/holidays, RIDGenerator_URI=/v1/ridgenerator/generate/rid/{centerid}/{machineid}, FetchDeviceSpec_id_lang_URI=/v1/masterdata/devicespecifications/{langcode}/{devicetypecode}, audit_password=mosip123, mosip_hotlist_app_id=hotlist, zoneMappingUrl=/v1/masterdata/zoneuser, zoneMappingActivateUrl=/v1/masterdata/zoneuser, db-su-password=Kaa95EXBXB, new_Resident_Role=default-roles-mosip,PARTNER_ADMIN, db-su-user=postgres, keycloak-external-url=https://iam.dev2.mosip.net, fetchRegCenter=/v1/masterdata/registrationcenters/validate/{id}/{langCode}/{timestamp}, uploadpublickey=/v1/syncdata/tpm/publickey, admin_zone_userName=globaladmin, getRIDByUserId=/v1/authmanager/rid/{appid}/{userid}, master_db_schema=master, mosip_admin_client_id=mosip-admin-client, otpNotifier=/v1/otpnotifier/otp/send, SyncPublicKeyToRegClient_URI=/v1/keymanager/publickey/, validateLocationByName=/v1/masterdata/locations/validate/{locationname}, FetchRegCent_prox_lang_URI=/v1/masterdata/getcoordinatespecificregistrationcenters/{langcode}/{longitude}/{latitude}/{proximitydistance}, mosip_reg_client_secret=o68o4pO1eXKcWG4z, userCenterMappingUrl=/v1/masterdata/usercentermapping, hibernate.current_session_context_class=thread, FetchRegCentHistory_URI=/v1/masterdata/registrationcentershistory/{registrationCenterId}/{langcode}/{effectiveDate}, ida_db_schema=ida, partner_url=jdbc:postgresql://api-internal.dev2.mosip.net:30090/mosip_ida, licKeyGenerator=/v1/keymanager/license/generate, FetchGenderType_URI=/v1/masterdata/gendertypes, installation-domain=, FetchMachine_URI=/v1/masterdata/machines, mosip_hotlist_client_secret=bbNU08hMxtpPKVTE, iam-users-to-create=111997,111998,220005,111992,globaladmin, syncMdatawithKeyIndex=/v1/syncdata/clientsettings, FetchBiometricAttribute_URI=/v1/masterdata/getbiometricattributesbyauthtype/{langcode}/{biometrictypecode}, encrypt_URI=/v1/keymanager/encrypt, hibernate.connection.pool_size=1, partner_password=mosip123, hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect, OTPValidation=/v1/otpmanager/otp/validate, EmailNotification_URI=/v1/notifier/email/send, mosip.test.mockmds.p12.path=../mountvolume/mockmdscert/, roles.111995=PARTNER_ADMIN,default-roles-mosip, otpTargetEmail=sanath.test.mosip@gmail.com, roles.111997=AUTH_PARTNER,PARTNER_ADMIN,PMS_ADMIN,POLICYMANAGER,REGISTRATION_SUPERVISOR, current_session_context_class=thread, fetchDeviceHistory=/v1/masterdata/deviceshistories/{id}/{langcode}/{effdatetimes}, driver_class=org.postgresql.Driver, FetchTemplate_lang_URI=/v1/masterdata/templates/{langcode}, FetchMachine_id_lang_URI=/v1/masterdata/machines/{id}/{langcode}, audit_default_schema=audit, db-port=5432, pms_db_schema=pms, uploaddocument=preregistration/v1/documents/, admin_password=Techno@123, iam-users-password=mosip123,mosip123,mosip123,mosip123,mosip123, authDemoServicePort=8384, getDocTypeDocCatByLangCode=/v1/masterdata/validdocuments/{languagecode}, FetchIDlist_URI=/v1/masterdata/idtypes/{langcode}, audit_db_schema=audit, FetchMachine_lang_URI=/v1/masterdata/machines/{langcode}, CentetMachineUserMappingToMasterData_uri=/v1/masterdata/registrationmachineusermappings, fetchmasterdatawithRID=/v1/syncdata/masterdata/{regcenterId}, OTPGeneration=/v1/otpmanager/otp/generate, AuthClientID=mosip-resident-client, FetchRegCent_hir_name_lang_URI=/v1/masterdata/registrationcenters/{langcode}/{hierarchylevel}/{name}, mosip.test.temp=../mountvolume/packets, authenticationInternal=/v1/authmanager/authenticate/internal/useridPwd, km_db_schema=keymgr, FetchTemplate_id_lang_URI=/v1/masterdata/templates/{langcode}/{templatetypecode}, keycloak_UserName=admin, FetchTemplate_URI=/v1/masterdata/templates, SmsNotification_URI=/v1/notifier/sms/send, mosip_admin_app_id=admin, FetchBiometricAuthType_URI=/v1/masterdata/biometrictypes/{langcode}, fetchRegistrationCenterDeviceHistory=/v1/masterdata/registrationcenterdevicehistory/{regcenterid}/{deviceid}/{effdatetimes}, OTPTimeOut=181, hibernate.show_sql=true, mosip.test.persona.fingerprintdatapath=../mountvolume/profile_resource/fp_data, admin_zone_appid=admin, mosip_pms_client_secret=s0feRXeyqmPAzsKg, mosip.test.persona.namesdatapath=../mountvolume/profile_resource/names_data, mosip.test.prereg.centerid=10005, authentication=/v1/authmanager/authenticate/useridPwd, mosip_test...


	      props.putAll(kernelprops);
	      //{centerId1=10598, signPublicKey=MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnrhHlj/0cgKqTEi9ZA8czMT5OnlhGFhtutPR+QkjeyYywN8tCfIm/Ec7xX9w7PHRqF/fMV1Jqs9oNAdB7cLcluf9muqqkJgEC9EPA4yJ08dmSxM6VYjyhr+5zLhz71v0N8bPV+D9S50LZT+uo6eDVCBwfobroz9Y6fgkvz1ANeFRX0jKe7SDmg8PIHqkxGRC8K27SGWa1BltE/xjHR9lwv4vp8G98DnJEhNkpyTQm6KsT0YCva5BExoicatToxFEpFo+0zW9OmI61NqNcmifr28tAFvaIVxQ3Myi81xKLSeEc8nYW4QcDcb9p5BrDvv6XQ0Na6dEn8lh0Fy675c30wIDAQAB, machineid=10535, appendedkey=1, machineSpecId=57f16f77-6c62-4677-aa39-8a3aedd0ba03, publicKey=MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnrhHlj/0cgKqTEi9ZA8czMT5OnlhGFhtutPR+QkjeyYywN8tCfIm/Ec7xX9w7PHRqF/fMV1Jqs9oNAdB7cLcluf9muqqkJgEC9EPA4yJ08dmSxM6VYjyhr+5zLhz71v0N8bPV+D9S50LZT+uo6eDVCBwfobroz9Y6fgkvz1ANeFRX0jKe7SDmg8PIHqkxGRC8K27SGWa1BltE/xjHR9lwv4vp8G98DnJEhNkpyTQm6KsT0YCva5BExoicatToxFEpFo+0zW9OmI61NqNcmifr28tAFvaIVxQ3Myi81xKLSeEc8nYW4QcDcb9p5BrDvv6XQ0Na6dEn8lh0Fy675c30wIDAQAB, pwd=Techno@123, user=dsl2, userid=dsl1, zoneCode=CSB, machineName=DSL0105145039114, userpassword=Techno@123}
	      props.putAll(map);
			//Application.properties start
		// Instantiating the FileInputStream for output file
		try {
			
			String path = (TestRunner.getExternalResourcePath() + "/config/" + BaseTestCase.environment + "_prereqdata_"
					+ appendedkey + ".properties");
			  File file = new File(path);
			 if (file.createNewFile()) {
		            
				 Reporter.log("File has been created at this path : " +path,true);
		        } else {
		        
		        	Reporter.log("File already exists at the path : " + path,true);
		        }
			
			
			FileOutputStream outputStrem = new FileOutputStream(path);
			// Storing the properties file
			
			props.store(outputStrem, "This is path where file is created" + path);
			Reporter.log(props.toString(), true);
			
			Reporter.log("This is path where file is created" + path, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
String inputJson=null;
							
	}
}