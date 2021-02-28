package org.mosip.dataprovider.mds;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mosip.dataprovider.models.IrisDataModel;
import org.mosip.dataprovider.models.JWTTokenModel;
import org.mosip.dataprovider.models.ResidentModel;
import org.mosip.dataprovider.models.mds.MDSDevice;
import org.mosip.dataprovider.models.mds.MDSDeviceCaptureModel;
import org.mosip.dataprovider.models.mds.MDSRCaptureModel;
import org.mosip.dataprovider.util.CommonUtil;
import org.mosip.dataprovider.util.DataProviderConstants;
import org.mosip.dataprovider.util.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cucumber.messages.internal.com.google.common.io.Files;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class MDSClient {

	int port;
	public static String MDSURL = "http://127.0.0.1:";
	
	public MDSClient(int port) {
		if(port == 0)
			this.port = 4501;
		else
			this.port = port;
	}
	//create profile folder and create all ISO images as per resident data
	public void createProfile(String profilePath,String profile, ResidentModel resident) {
		File profDir = new File(profilePath + profile);
		if(!profDir.exists())
			profDir.mkdir();
		//copy from default profile
		File defProfile = new File( profilePath + "Default");
		File []defFiles = defProfile.listFiles();
		for(File f: defFiles) {
			try {
				Files.copy(f, new File(profDir.getAbsolutePath() +"\\"+ f.getName()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ISOConverter convert = new ISOConverter();
		try {
			String face = resident.getBiometric().getEncodedPhoto();
			convert.convertFace(face,profDir + "\\Face.iso");
			IrisDataModel iris = resident.getBiometric().getIris();
			if(iris != null) {
				if(iris.getLeft() != null)
					convert.convertIris(iris.getLeft(), profDir + "Left_Iris.iso", "Left");
				if(iris.getRight() != null)
					convert.convertIris(iris.getRight(), profDir + "Right_Iris.iso", "Right");
			}
			String [] fingerData = resident.getBiometric().getFingerPrint();
			for(int i=0; i < 10; i++) {
				String fingerName = DataProviderConstants.displayFingerName[i];
				String outFileName = DataProviderConstants.MDSProfileFingerNames[i];
				if(fingerData[i] != null) {
					convert.convertFinger(fingerData[i], profDir + outFileName + ".iso" , fingerName);
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
			
	}
	public  void setProfile(String profile) {
		
		String url =  MDSURL +port + "/profile";
		JSONObject body = new JSONObject();
		body.put("profileId", profile);

		try {
			HttpRCapture capture = new HttpRCapture(url);
			capture.setMethod("SETPROFILE");
			String response = RestClient.rawHttp(capture, body.toString());
			JSONObject respObject = new JSONObject(response);

		}catch(Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
    	return devices;
	}
	public  MDSRCaptureModel captureFromRegDevice(MDSDevice device, 
			MDSRCaptureModel rCaptureModel,
			String type,
			String bioSubType, int reqScore,int deviceId,int deviceSubId) {

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
		List<String> lstSubtype = null;
		
		if(bioSubType != null)
			lstSubtype = Arrays.asList(bioSubType.split("\\s*,\\s*"));
		
		bio.put("bioSubType", lstSubtype);
		int count = lstSubtype == null ? 0 : lstSubtype.size();
		
		bio.put("count", count);
		bio.put("requestedScore", reqScore);
		bio.put("deviceId", deviceId);
		bio.put("deviceSubId", deviceSubId);
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
			String response = RestClient.rawHttp(capture, jsonReq.toString());
			JSONObject respObject = new JSONObject(response);
			JSONArray bioArray = respObject.getJSONArray("biometrics");
			List<MDSDeviceCaptureModel> lstBiometrics  = new ArrayList<MDSDeviceCaptureModel>();
			rCaptureModel.getLstBiometrics().put(type, lstBiometrics);
			
			for(int i=0; i < bioArray.length(); i++) {
				JSONObject bioObject = bioArray.getJSONObject(i);
				String data = bioObject.getString("data");
				String hash = bioObject.getString("hash");
				JWTTokenModel jwtTok = new JWTTokenModel(data);
				
				MDSDeviceCaptureModel model = new MDSDeviceCaptureModel();
				model.setBioType( jwtTok.getJwtPayload().getString("bioType"));
				model.setBioSubType( jwtTok.getJwtPayload().getString("bioSubType"));
				model.setQualityScore( jwtTok.getJwtPayload().getString("qualityScore"));
				model.setBioValue ( jwtTok.getJwtPayload().getString("bioValue"));
				model.setDeviceServiceVersion ( jwtTok.getJwtPayload().getString("deviceServiceVersion"));
				model.setDeviceCode( jwtTok.getJwtPayload().getString("deviceCode"));
				model.setHash(hash);
				lstBiometrics.add(model);
				
			}
				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rCaptureModel;
	}
		
	public static void main(String[] args) {
		
		MDSClient client = new MDSClient(0);
		client.setProfile("automated");
		List<MDSDevice> d= client.getRegDeviceInfo("Finger");
		d.forEach( dv-> {
			System.out.println(dv.toJSONString());	
		});
		
		MDSRCaptureModel r =  client.captureFromRegDevice(d.get(0),null, "Finger",null,60,2,1);
		
		System.out.println( r.toJSONString());
		
		r = client.captureFromRegDevice(d.get(0),r, "Face",null,60,1,1);
		
		System.out.println( r.toJSONString());
	}
}
