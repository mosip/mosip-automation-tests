package io.mosip.testrig.dslrig.dataprovider.mds;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cucumber.messages.internal.com.google.common.io.Files;
import io.mosip.testrig.dslrig.dataprovider.models.IrisDataModel;
import io.mosip.testrig.dslrig.dataprovider.models.JWTTokenModel;
import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.models.mds.MDSDevice;
import io.mosip.testrig.dslrig.dataprovider.models.mds.MDSDeviceCaptureModel;
import io.mosip.testrig.dslrig.dataprovider.models.mds.MDSRCaptureModel;
import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class MDSClient implements MDSClientInterface {

	private static final Logger logger = LoggerFactory.getLogger(MDSClient.class);
	public  int port;
	public static String MDSURL = "http://127.0.0.1:";

	public MDSClient(int port) {
		if(port == 0)
			this.port = 4501;
		else
			this.port = port;
	}


	//create profile folder and create all ISO images as per resident data

	public void createProfileold(String profilePath,String profile, ResidentModel resident,String contextKey,String purpose) throws Exception {
		//		File profDir = new File(profilePath + "/"+ profile);
		//		if(!profDir.exists())
		//			profDir.mkdir();
		File profDir1 = new File(profilePath + "/"+ profile);
		File profDir = new File(profilePath + "/"+ profile+ "/" + purpose);
		if(!profDir1.exists())
			profDir1.mkdir();
		if(!profDir.exists())
			profDir.mkdir();
		//copy from default profile
		File defProfile = new File( profilePath +"/"+ "Default"+"/"+purpose);

		File []defFiles = defProfile.listFiles();
		for(File f: defFiles) {
			try {
				Files.copy(f, new File(profDir.getAbsolutePath() +"\\"+ f.getName()));
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
		ISOConverter convert = new ISOConverter();
		try {
			if(!resident.getSkipFace()) {
				byte[] face = resident.getBiometric().getRawFaceData();
				convert.convertFace(face,profDir + "/" + "Face.iso");
			}
			if(!resident.getSkipIris()) {

				IrisDataModel iris = resident.getBiometric().getIris();
				if(iris != null) {

					if(iris.getRawLeft() != null)
						convert.convertIris(iris.getRawLeft(), profDir + "/"+ "Left_Iris.iso", "Left");
					if(iris.getRawRight() != null)
						convert.convertIris(iris.getRawRight(), profDir + "/"+ "Right_Iris.iso", "Right");
				}
			}
			if(!resident.getSkipFinger()) {
				byte[] [] fingerData = resident.getBiometric().getFingerRaw();
				for(int i=0; i < 10; i++) {
					String fingerName = DataProviderConstants.displayFingerName[i];
					String outFileName = DataProviderConstants.MDSProfileFingerNames[i];
					if(fingerData[i] != null) {
						convert.convertFinger(fingerData[i], profDir + "/" + outFileName + ".iso" , fingerName,purpose);
					}
				}
			}

		} catch (IOException e) {
			logger.error(e.getMessage());
		}


	}




	//create profile folder and create all ISO images as per resident data

	public void createProfile(String profilePath,String profile, ResidentModel resident,String contextKey,String purpose) throws Exception {
		//		File profDir = new File(profilePath + "/"+ profile+ "/" + purpose);
		File profDir1 = new File(profilePath + "/"+ profile);
		File profDir = new File(profilePath + "/"+ profile+ "/" + purpose);
		if(!profDir1.exists())
			profDir1.mkdir();
		if(!profDir.exists())
			profDir.mkdir();
		//copy from default profile


		/////////
		//reach cached finger prints from folder
		String dirPath = VariableManager.getVariableValue(contextKey,"mountPath").toString()+VariableManager.getVariableValue(contextKey,"mosip.test.persona.fingerprintdatapath").toString();
		logger.info("createProfile dirPath " + dirPath);
		Hashtable<Integer, List<File>> tblFiles = new Hashtable<Integer, List<File>>();
		File dir = new File(dirPath);

		File listDir[] = dir.listFiles();
		int numberOfSubfolders = listDir.length;

		int min=1;
		int max=numberOfSubfolders ;
		int randomNumber = (int) (Math.random()*(max-min)) + min;
		String beforescenario=VariableManager.getVariableValue(contextKey,"scenario").toString();
		String afterscenario=beforescenario.substring(0, beforescenario.indexOf(':'));

		int currentScenarioNumber = Integer.valueOf(afterscenario);


		// If the available impressions are less than scenario number, pick the random one

		// otherwise pick the impression of same of scenario number
		int impressionToPick = (currentScenarioNumber < numberOfSubfolders) ? currentScenarioNumber : randomNumber ;

		logger.info("createProfile currentScenarioNumber=" + currentScenarioNumber +" numberOfSubfolders=" + numberOfSubfolders + " impressionToPick=" + impressionToPick );
		List<File> lst=new LinkedList<File>();
		for(int i=min; i <= max; i++) {

			lst = CommonUtil.listFiles(dirPath +
					String.format("/Impression_%d/fp_1/", i));
			tblFiles.put(i,lst);
		}

		List<File> firstSet = tblFiles.get(impressionToPick);
		logger.info("createProfile Impression used "+ impressionToPick);

		///////////////


		//File defProfile = new File( profilePath +"/"+ "Automatic");

		//File []defFiles = defProfile.listFiles();
		for(File f: firstSet) {
			try {
				Files.copy(f, new File(profDir.getAbsolutePath() +"/"+ f.getName()));
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
		ISOConverter convert = new ISOConverter();
		try {
			if(!resident.getSkipFace()) {
				byte[] face = resident.getBiometric().getRawFaceData();
				convert.convertFace(face,profDir + "/" + "Face.iso");
			}
			if(!resident.getSkipIris()) {

				IrisDataModel iris = resident.getBiometric().getIris();
				if(iris != null) {

					//					if(iris.getRawLeft() != null)
					convert.convertIris(iris.getRawLeft(), profDir + "/"+ "Left_Iris.iso", "Left");
					//					if(iris.getRawRight() != null)
					convert.convertIris(iris.getRawRight(), profDir + "/"+ "Right_Iris.iso", "Right");
				}
			}
			if(!resident.getSkipFinger()) {
				byte[] [] fingerData = resident.getBiometric().getFingerRaw();
				for(int i=0; i < 10; i++) {
					String fingerName = DataProviderConstants.displayFingerName[i];
					String outFileName = DataProviderConstants.MDSProfileFingerNames[i];
					if(fingerData[i] != null) {
						convert.convertFinger(fingerData[i], profDir + "/" + outFileName + ".iso" , fingerName,purpose);
					}
				}
			}

		} catch (IOException e) {
			logger.error(e.getMessage());
		}


	}


	public void removeProfile(String profilePath,String profile,int port,String contextKey) {
		setProfile("Default",port,contextKey);
		File profDir = new File(profilePath + "/"+ profile);
		boolean isFileDeleted = false;
		boolean isProfDirDeleted = false;
		if(profDir.exists()) {
			// list all the files in an array
			File[] files = profDir.listFiles();

			// delete each file from the directory
			for(File file : files) {
				boolean isDeleted = file.delete();
				if (!isDeleted) {
					logger.info("File Deleted successfully");

				}
				isFileDeleted=file.delete();
				if(!isFileDeleted) {
					logger.info("File Deleted successfully");	
				}
			}
			isProfDirDeleted=profDir.delete();
			if(!isProfDirDeleted) {
				logger.info("File Deleted successfully");	
			}
		}

	}
	public  void setProfile(String profile,int port,String contextKey) {

		String url =  MDSURL +port + "/admin/profile";
		JSONObject body = new JSONObject();
		body.put("profileId", profile);
		body.put("type", "Biometric Device");

		try {
			logger.info("Inside Setprofile");
			HttpRCapture capture = new HttpRCapture(url);
			capture.setMethod("POST");
			String response = RestClient.rawHttp(capture, body.toString(),contextKey);
			JSONObject respObject = new JSONObject(response);

		}catch(Exception ex) {
			logger.error(ex.getMessage());
		}

	}
	//Type ->"Finger", "Iris", "Face"
	public  List<MDSDevice> getRegDeviceInfo(String type) {

		List<MDSDevice> devices = null;

		String url =  MDSURL + port;
		JSONObject body = new JSONObject();
		body.put("type", type);
		Response response = given()
				.contentType(ContentType.JSON)
				.body(body.toString())
				.post(url );
		if(response.getStatusCode() == 200) {
			String resp = response.getBody().asString();

			if(resp != null) {
				JSONArray deviceArray = new JSONArray(resp);
				ObjectMapper objectMapper = new ObjectMapper();

				try {
					devices = objectMapper.readValue(deviceArray.toString(), 
							objectMapper.getTypeFactory().constructCollectionType(List.class, MDSDevice.class));

				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}
		}
		return devices;
	}


	//	capture = mds.captureFromRegDevice(exceptionDevice, capture, DataProviderConstants.MDS_DEVICE_TYPE_EXCEPTION_PHOTO,
	//			null, 60, exceptionDevice.getDeviceSubId().get(0), port,contextKey,bioexceptionlist);

	public  MDSRCaptureModel captureFromRegDevice(MDSDevice device, 
			MDSRCaptureModel rCaptureModel,
			String type,
			String bioSubType, int reqScore,String deviceSubId,int port,String contextKey,List<String> listbioexception) {
		String mosipVersion=null;;
		try {
			mosipVersion=VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"mosip.version").toString();
		}catch(Exception e) {

		}

		if(rCaptureModel == null)
			rCaptureModel = new MDSRCaptureModel();

		String url =  MDSURL +port + "/capture";
		JSONObject jsonReq = new JSONObject();
		jsonReq.put("env", "Developer");
		jsonReq.put("purpose", "Registration");
		jsonReq.put("specVersion", "0.9.5");
		jsonReq.put("timeout", "120000");
		jsonReq.put("captureTime", CommonUtil.getUTCDateTime(null));
		jsonReq.put("domainUri", "automated");
		jsonReq.put("transactionId", "123456789123");
		JSONObject bio = new JSONObject();
		bio.put("type", type);



		bio.put("count", 1);
		bio.put("deviceSubId", deviceSubId);

		if(type.equalsIgnoreCase("finger")) {

			switch(deviceSubId)
			{
			case "1":
				bio.put("count", 4);

				break;
			case "2":
				bio.put("count", 4);

				break;
			case "3": 
				bio.put("count", 2);
				break;
			}

		}

		bio.put("requestedScore", reqScore);
		//bio.put("deviceId", Integer.valueOf(device.getDeviceId()));
		bio.put("deviceId", device.getDeviceId());
		if(listbioexception!=null && !listbioexception.isEmpty())
			bio.put("exception",listbioexception );

		JSONArray arr = new JSONArray();
		arr.put(bio);
		jsonReq.put("bio", arr);
		/*
		Response response = given()
				.contentType(ContentType.JSON)
				.body(jsonReq.toString())
				.post(url );
		 */
		try {
			HttpRCapture capture = new HttpRCapture(url);
			capture.setMethod("RCAPTURE");
			String response = RestClient.rawHttp(capture, jsonReq.toString(),contextKey);
			//logger.info("MDS RESPONSE :"+  response);
			JSONObject respObject = new JSONObject(response);
			JSONArray bioArray = respObject.getJSONArray("biometrics");
			List<MDSDeviceCaptureModel> lstBiometrics  = rCaptureModel.getLstBiometrics().get(type);
			if(lstBiometrics == null)
				lstBiometrics = new ArrayList<MDSDeviceCaptureModel>();

			if(!CollectionUtils.isEmpty(listbioexception) && type.equalsIgnoreCase("face"))
				rCaptureModel.getLstBiometrics().put("exception", lstBiometrics);
			else
				rCaptureModel.getLstBiometrics().put(type, lstBiometrics);



			List<String> retriableErrorCodes=new  ArrayList<String>();
			retriableErrorCodes.add("703");
			retriableErrorCodes.add("710");

			// Check if Rcapture returns an error response if on error, retry based on Error ;code. 
			while(bioArray.length()==1 &&  retriableErrorCodes.contains( bioArray.getJSONObject(0).getJSONObject("error").getString("errorCode") ))
			{
				logger.info("Check if Rcapture returns an error response if on error, retry based on Error ;code. ");
				response = RestClient.rawHttp(capture, jsonReq.toString(),contextKey);

				respObject = new JSONObject(response);
				bioArray = respObject.getJSONArray("biometrics");
			}


			for(int i=0; i < bioArray.length(); i++) {
				JSONObject bioObject = bioArray.getJSONObject(i);
				String data = bioObject.getString("data");

				String hash = bioObject.getString("hash");
				JWTTokenModel jwtTok = new JWTTokenModel(data);
				JSONObject jsonPayload = new JSONObject(jwtTok.getJwtPayload());
				String jwtSign = jwtTok.getJwtSign();
				MDSDeviceCaptureModel model = new MDSDeviceCaptureModel();
				model.setBioType( CommonUtil.getJSONObjectAttribute(jsonPayload, "bioType",""));
				model.setBioSubType( CommonUtil.getJSONObjectAttribute(jsonPayload, "bioSubType",""));
				model.setQualityScore(CommonUtil.getJSONObjectAttribute(jsonPayload, "qualityScore",""));
				model.setBioValue ( CommonUtil.getJSONObjectAttribute(jsonPayload,"bioValue",""));
				model.setDeviceServiceVersion ( CommonUtil.getJSONObjectAttribute(jsonPayload,"deviceServiceVersion",""));
				model.setDeviceCode( CommonUtil.getJSONObjectAttribute(jsonPayload,"deviceCode",""));
				model.setHash(hash);
				if(mosipVersion!=null && mosipVersion.startsWith("1.2")) {
					model.setSb(jwtSign); // SB is signature block (header..signature)
					//String temp=jwtTok.getJwtPayload().replace(model.getBioValue(),);

					String BIOVALUE_KEY = "bioValue";
					String BIOVALUE_PLACEHOLDER = "\"<bioValue>\"";
					int bioValueKeyIndex = jwtTok.getJwtPayload().indexOf(BIOVALUE_KEY) + (BIOVALUE_KEY.length() + 1);
					int bioValueStartIndex = jwtTok.getJwtPayload().indexOf('"', bioValueKeyIndex);
					int bioValueEndIndex = jwtTok.getJwtPayload().indexOf('"', (bioValueStartIndex + 1));
					String bioValue = jwtTok.getJwtPayload().substring(bioValueStartIndex, (bioValueEndIndex + 1));
					String payload = jwtTok.getJwtPayload().replace(bioValue, BIOVALUE_PLACEHOLDER);
					model.setPayload(payload);
				}
				lstBiometrics.add(model);

			}

		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		return rCaptureModel;
	}


	public void setThresholdValue(String qualityScore) {

		String url = MDSURL + port + "/admin/score";
		JSONObject body = new JSONObject();
		body.put("type", "Biometric Device");
		body.put("qualityScore", qualityScore);
		body.put("fromIso", false);

		try {
			/*
			 * HttpRCapture capture = new HttpRCapture(url);
			 * capture.setMethod("SETTHRESHOLVALUE"); String response =
			 * RestClient.rawHttp(capture, body.toString()); JSONObject respObject = new
			 * JSONObject(response);
			 */

			Response response = given()
					.contentType(ContentType.JSON)
					.body(body.toString())
					.post(url );
			String resp = response.getBody().asString();
			logger.info(resp);

		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}

	}



	public static void main(String[] args) {

		MDSClient client = new MDSClient(0);
		//client.setProfile("res643726437264372");
		//client.setProfile("Default",port);
		List<MDSDevice> d= client.getRegDeviceInfo("Iris");
		d.forEach( dv-> {
			logger.info(dv.toJSONString());	
		});


		List<MDSDevice> f= client.getRegDeviceInfo("Finger");


		f.forEach( dv-> {
			logger.info(dv.toJSONString());	

			//			MDSRCaptureModel r =  client.captureFromRegDevice(dv, null, "Finger",null,60,"1",0);
			//MDSRCaptureModel r =  client.captureFromRegDevice(d.get(0),null, "Iris",null,60,2);

			//			logger.info( r.toJSONString());

		});

		//r = client.captureFromRegDevice(d.get(0),r, "Face",null,60,1);

		//logger.info( r.toJSONString());
	}

	@Override
	public List<MDSDevice> getRegDeviceInfo(String type, String contextKey) {
		// TODO Auto-generated method stub
		return null;
	}




}
