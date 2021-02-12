package org.mosip.dataprovider.models;

import java.io.Serializable;



import lombok.Data;

@Data
public class BiometricDataModel  implements Serializable {
	 private static final long serialVersionUID = 1L;
	//Indexed by Finger value
	private String [] fingerPrint;
	//left, right
	private IrisDataModel iris;
	private String encodedPhoto;
}
