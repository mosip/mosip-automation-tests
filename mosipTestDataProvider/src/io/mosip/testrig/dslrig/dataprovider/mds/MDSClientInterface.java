package io.mosip.testrig.dslrig.dataprovider.mds;

import java.util.List;

import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.models.mds.MDSDevice;
import io.mosip.testrig.dslrig.dataprovider.models.mds.MDSRCaptureModel;

public interface MDSClientInterface {
	public void createProfile(String profilePath,String profile, ResidentModel resident,String contextKey,String purpose ) throws Exception ;
	public void removeProfile(String profilePath,String profile,int port,String contextKey) ;
	public  void setProfile(String profile,int port,String contextKey) ;
	//Type ->"Finger", "Iris", "Face"
	public  List<MDSDevice> getRegDeviceInfo(String type) ;
	public  MDSRCaptureModel captureFromRegDevice(MDSDevice device, 
			MDSRCaptureModel rCaptureModel,
			String type,
			String bioSubType, int reqScore,String deviceSubId,int port,String contextKey,List<String> exceptionlist) ;
	
	List<MDSDevice> getRegDeviceInfo(String type, String contextKey);
	
}
