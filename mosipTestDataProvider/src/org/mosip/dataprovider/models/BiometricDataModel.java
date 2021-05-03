package org.mosip.dataprovider.models;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;

import org.mosip.dataprovider.models.mds.MDSDeviceCaptureModel;


import lombok.Data;

@Data
public class BiometricDataModel  implements Serializable {
	 private static final long serialVersionUID = 1L;
	//Indexed by Finger value
	private String [] fingerPrint;
	private String [] fingerHash;
	private byte[][] fingerRaw;

	//left, right
	private IrisDataModel iris;
	private String encodedPhoto;
	private String FaceHash;
	private byte[] rawFaceData;

	private Hashtable<String, List<MDSDeviceCaptureModel>> capture;
	
	private String cbeff;
}
