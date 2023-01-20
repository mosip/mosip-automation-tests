package org.mosip.dataprovider.mds;

import java.util.List;

import org.mosip.dataprovider.models.ResidentModel;
import org.mosip.dataprovider.models.mds.MDSDevice;
import org.mosip.dataprovider.models.mds.MDSRCaptureModel;

public interface MDSClientInterface {
	public void createProfile(String profilePath,String profile, ResidentModel resident,String contextKey,String purpose ) throws Exception ;
	public void removeProfile(String profilePath,String profile,int port) ;
	public  void setProfile(String profile,int port) ;
	//Type ->"Finger", "Iris", "Face"
	public  List<MDSDevice> getRegDeviceInfo(String type) ;
	public  MDSRCaptureModel captureFromRegDevice(MDSDevice device, 
			MDSRCaptureModel rCaptureModel,
			String type,
			String bioSubType, int reqScore,String deviceSubId,int port) ;
	
	List<MDSDevice> getRegDeviceInfo(String type, String contextKey);
	
}
