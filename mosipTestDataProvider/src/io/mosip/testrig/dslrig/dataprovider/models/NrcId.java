package io.mosip.testrig.dslrig.dataprovider.models;

import java.io.Serializable;

public class NrcId implements Serializable {
	
	 private static final long serialVersionUID = 1L;
	
	public String getNrcId() {
		return nrcId;
	}
	public void setNrcId(String nrcId) {
		this.nrcId = nrcId;
	}
	
	private String nrcId;

}
