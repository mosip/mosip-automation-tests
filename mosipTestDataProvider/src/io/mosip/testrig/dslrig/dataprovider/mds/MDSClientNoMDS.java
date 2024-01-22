package io.mosip.testrig.dslrig.dataprovider.mds;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.testrig.dslrig.dataprovider.models.IrisDataModel;
import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.models.mds.MDSDataModel;
import io.mosip.testrig.dslrig.dataprovider.models.mds.MDSDevice;
import io.mosip.testrig.dslrig.dataprovider.models.mds.MDSDeviceCaptureModel;
import io.mosip.testrig.dslrig.dataprovider.models.mds.MDSRCaptureModel;
import io.mosip.testrig.dslrig.dataprovider.models.setup.MosipDeviceModel;
import io.mosip.testrig.dslrig.dataprovider.preparation.MosipDataSetup;
import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

public class MDSClientNoMDS implements MDSClientInterface {
	private static SecureRandom  rand = new SecureRandom ();
	private static final Logger logger = LoggerFactory.getLogger(MDSClientNoMDS.class);

	Hashtable<String,MDSDataModel> profileData;
	MDSDataModel current;
	
	public MDSClientNoMDS() {
		profileData = new Hashtable<String,MDSDataModel>();
	}
	@Override
	public void createProfile(String profilePath, String profile, ResidentModel resident,String contextKey,String purpose) throws Exception {

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
					fingersISO[i]=convert.convertFinger(fingerData[i], null , fingerName,purpose);
				}
			}
			data.setFingersISO(fingersISO);
			profileData.put(profile, data);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		
	
	}

	@Override
	public void removeProfile(String profilePath, String profile,int port,String contextKey) {

		profileData.remove(profile);
		
	}

	@Override
	public void setProfile(String profile,int port,String contextKey) {
	
		current = profileData.get(profile);
	}

	@Override
	public List<MDSDevice> getRegDeviceInfo(String type,String contextKey) {
		// TODO Auto-generated method stub
		//get configured Center ID  the devices belonging to that center
		String centerId = VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"centerId").toString();
		
		List<MosipDeviceModel> deviceModels = MosipDataSetup.getDevices(centerId,contextKey);
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
			String bioSubType[], int reqScore, String deviceSubId,int port,String contextKey,List<String> bioException) {

		List<String> lstSubtype = null;
		
		if(bioSubType != null)
			lstSubtype = Arrays.asList(bioSubType);//.split("\\s*,\\s*"));
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
			String[] myArrayIris = new String[2];
			for(String s: lstSubtype) {
				model = new MDSDeviceCaptureModel();

				 myArrayIris = addElement(myArrayIris, s);
				model.setBioType(bioType);
				model.setBioSubType(myArrayIris);//Jana sir help
				
				if(s.contains("Left")) 
					model.setBioValue( Base64.getUrlEncoder().encodeToString(current.getIrisLeftISO())); 
				else
				if(s.contains("Right")) 
					model.setBioValue( Base64.getUrlEncoder().encodeToString(current.getIrisRightISO())); 
				
				lstBiometrics.add(model);			
			}
		}
		
		if(bioType.equals("Iris")) {
			String[] myArrayIris = new String[2];
			
			for(String s: lstSubtype) {
				model = new MDSDeviceCaptureModel();
				model.setBioType(bioType);
				 myArrayIris = addElement(myArrayIris, s);
				model.setBioSubType(myArrayIris);//Jana sir help
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

		if (bioSubType != null && segmentsToCapture!= null && segmentsToCapture.size() == 2 * bioSubType.length) {
			// TODO - validate requested Score, if deviceSubId is 3 then take the average of

			for (String segment : segmentsToCapture) {
				/*
				BioMetricsDataDto bioMetricsData = oB.readValue(
						Base64.getDecoder()
								.decode(new String(ComonUtil.read(System.getProperty("user.dir")
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
	@Override
	public List<MDSDevice> getRegDeviceInfo(String type) {
		// TODO Auto-generated method stub
		return null;
	}
	// Utility method to add an element to an array
   public String[] addElement(String[] array, String element) {
        // Create a new array with a larger size
        String[] newArray = Arrays.copyOf(array, array.length + 1);

        // Add the new element to the new array
        newArray[array.length] = element;

        // Return the new array
        return newArray;
    }
}	
