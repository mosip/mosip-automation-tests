package org.mosip.dataprovider.mds;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.mosip.dataprovider.models.IrisDataModel;
import org.mosip.dataprovider.models.ResidentModel;
import org.mosip.dataprovider.models.mds.MDSDataModel;
import org.mosip.dataprovider.models.mds.MDSDevice;
import org.mosip.dataprovider.models.mds.MDSDeviceCaptureModel;
import org.mosip.dataprovider.models.mds.MDSRCaptureModel;
import org.mosip.dataprovider.models.setup.MosipDeviceModel;
import org.mosip.dataprovider.preparation.MosipDataSetup;
import org.mosip.dataprovider.util.CommonUtil;
import org.mosip.dataprovider.util.DataProviderConstants;

import variables.VariableManager;

public class MDSClientNoMDS implements MDSClientInterface {

	Hashtable<String,MDSDataModel> profileData;
	MDSDataModel current;
	
	public MDSClientNoMDS() {
		profileData = new Hashtable<String,MDSDataModel>();
	}
	@Override
	public void createProfile(String profilePath, String profile, ResidentModel resident) {

		MDSDataModel data = new MDSDataModel();
		
		ISOConverter convert = new ISOConverter();
		try {
			byte[] face = resident.getBiometric().getRawFaceData();
			data.setFaceISO((convert.convertFace(face,null)));
			IrisDataModel iris = resident.getBiometric().getIris();
			if(iris != null) {
				
				if(iris.getRawLeft() != null)
					data.setIrisLeftISO (convert.convertIris(iris.getRawLeft(), null, "Left"));
				if(iris.getRawRight() != null)
					data.setIrisRightISO ( convert.convertIris(iris.getRawRight(), null, "Right"));
			}
			byte[] [] fingerData = resident.getBiometric().getFingerRaw();
			byte [][] fingersISO = new byte[10][];
			for(int i=0; i < 10; i++) {
				String fingerName = DataProviderConstants.displayFingerName[i];
				//String outFileName = DataProviderConstants.MDSProfileFingerNames[i];
				if(fingerData[i] != null) {
					fingersISO[i]=convert.convertFinger(fingerData[i], null , fingerName);
				}
			}
			data.setFingersISO(fingersISO);
			profileData.put(profile, data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
	}

	@Override
	public void removeProfile(String profilePath, String profile) {

		profileData.remove(profile);
		
	}

	@Override
	public void setProfile(String profile) {
	
		current = profileData.get(profile);
	}

	@Override
	public List<MDSDevice> getRegDeviceInfo(String type) {
		// TODO Auto-generated method stub
		//get configured Center ID  the devices belonging to that center
		String centerId = VariableManager.getVariableValue("centerId").toString();
		
		List<MosipDeviceModel> deviceModels = MosipDataSetup.getDevices(centerId);
		List<MDSDevice> lstRet = new ArrayList<MDSDevice>();
		
		for(MosipDeviceModel dm: deviceModels) {
			MDSDevice mdsDev = new MDSDevice();
			if(dm.getIsActive() != null && dm.getIsActive().equals("true") ) {
				mdsDev.setDeviceId( dm.getId());
				
				lstRet.add(mdsDev);
			}
		}
		return lstRet;
	}

	@Override
	public MDSRCaptureModel captureFromRegDevice(MDSDevice device, MDSRCaptureModel rCaptureModel, String bioType,
			String bioSubType, int reqScore, int deviceSubId) {

		List<String> lstSubtype = null;
		
		if(bioSubType != null)
			lstSubtype = Arrays.asList(bioSubType.split("\\s*,\\s*"));
		if(rCaptureModel == null)
			rCaptureModel = new MDSRCaptureModel();
		List<MDSDeviceCaptureModel> lstBiometrics  = rCaptureModel.getLstBiometrics().get(bioType);

		
		MDSDeviceCaptureModel model = new MDSDeviceCaptureModel();
		model.setBioType(bioType);
		
		if(bioType.equals("Face")) {
			model.setBioSubType(bioSubType);
			model.setBioValue( Base64.getUrlEncoder().encodeToString(current.getFaceISO())); 
			lstBiometrics.add(model);
			
		}
		else
		if(bioType.equals("Iris")) {
			for(String s: lstSubtype) {
				model = new MDSDeviceCaptureModel();
				model.setBioType(bioType);
				model.setBioSubType(s);
				
				if(s.contains("Left")) 
					model.setBioValue( Base64.getUrlEncoder().encodeToString(current.getIrisLeftISO())); 
				else
				if(s.contains("Right")) 
					model.setBioValue( Base64.getUrlEncoder().encodeToString(current.getIrisRightISO())); 
				
				lstBiometrics.add(model);			
			}
		}
		if(bioType.equals("Iris")) {
			for(String s: lstSubtype) {
				model = new MDSDeviceCaptureModel();
				model.setBioType(bioType);
				model.setBioSubType(s);
				int idx = ISOConverter.getFingerPos(s);
				model.setBioValue( Base64.getUrlEncoder().encodeToString(current.getFingersISO()[idx])) ;
				lstBiometrics.add(model);
			}
		}
		return rCaptureModel;
	}
	
	private void captureFingersModality(
			String deviceSubId, String[] bioSubType, 
			String[] bioException,List<String> list) {

		List<String> segmentsToCapture = null;

		switch (deviceSubId) {
		case "1": // left
			segmentsToCapture = getSegmentsToCapture(
					Arrays.asList("Left IndexFinger", "Left MiddleFinger", "Left RingFinger", "Left LittleFinger"),
					bioSubType == null ? null : Arrays.asList(bioSubType),
					bioException == null ? null : Arrays.asList(bioException));

			break;

		case "2": // right
			segmentsToCapture = getSegmentsToCapture(
					Arrays.asList("Right IndexFinger", "Right MiddleFinger", "Right RingFinger", "Right LittleFinger"),
					bioSubType == null ? null : Arrays.asList(bioSubType),
					bioException == null ? null : Arrays.asList(bioException));
			break;

		case "3": // thumbs
			segmentsToCapture = getSegmentsToCapture(Arrays.asList("Left Thumb", "Right Thumb"),
					bioSubType == null ? null : Arrays.asList(bioSubType),
					bioException == null ? null : Arrays.asList(bioException));
			break;

		case "0":
			break;
		}

		if (segmentsToCapture == null || segmentsToCapture.isEmpty()) {
			// Throw exception
		}

		if (segmentsToCapture.size() == bioSubType.length) {
			// TODO - validate requested Score, if deviceSubId is 3 then take the average of

			for (String segment : segmentsToCapture) {
				/*
				BioMetricsDataDto bioMetricsData = oB.readValue(
						Base64.getDecoder()
								.decode(new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir")
										+ "/files/MockMDS/registration/" + segment + ".txt")))),
						BioMetricsDataDto.class);
				list.add(bioMetricsData);
				*/
			}
		}

	}

	private List<String> getSegmentsToCapture(List<String> defaultSubTypes, List<String> bioSubTypes, List<String> exceptions) {
		List<String> localCopy = new ArrayList<>();
		localCopy.addAll(defaultSubTypes);
		if(exceptions != null) {
			localCopy.removeAll(exceptions);
		}
		
		List<String> segmentsToCapture = new ArrayList<>();
		
		if(bioSubTypes == null || bioSubTypes.isEmpty()) {
			segmentsToCapture.addAll(localCopy);			
			return segmentsToCapture;
		}
		else {
			Random rand = new Random();
			for(String bioSubType : bioSubTypes) {
				if(localCopy.contains(bioSubType)) {
					segmentsToCapture.add(bioSubType);
				}
				else if("UNKNOWN".equals(bioSubType)) {
					String randSubType = defaultSubTypes.get(rand.nextInt(defaultSubTypes.size()));
					while(bioSubTypes.contains(randSubType) && bioSubTypes.size() <= localCopy.size()) {
						randSubType = defaultSubTypes.get(rand.nextInt(defaultSubTypes.size()));
					}
					segmentsToCapture.add(randSubType);
				}
				else {
					//Throw exception
				}
			}
		}
		return segmentsToCapture;
	}
	
}	
