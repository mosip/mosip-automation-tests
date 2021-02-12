package org.mosip.dataprovider.models;

import java.io.Serializable;

public class Contact  implements Serializable {
	 private static final long serialVersionUID = 1L;
	public String getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public String getResidenceNumber() {
		return residenceNumber;
	}
	public void setResidenceNumber(String residenceNumber) {
		this.residenceNumber = residenceNumber;
	}
	public String getEmailId() {
		return emailId;
	}
	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}
	private String mobileNumber;
	private String residenceNumber;
	private String emailId;
}