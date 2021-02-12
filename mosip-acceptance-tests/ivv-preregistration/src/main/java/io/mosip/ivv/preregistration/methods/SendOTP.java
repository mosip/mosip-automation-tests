package io.mosip.ivv.preregistration.methods;

import io.mosip.admin.fw.util.AdminTestException;
import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.authentication.fw.util.AuthenticationTestException;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.mosip.testscripts.SimplePostForAutoGenId;

public class SendOTP extends BaseTestCaseUtil implements StepInterface {

    @Override
    public void run() {
    	test();
    	
    }
    
    public void test() {
    	String fileName="preReg/createPrereg/createPrereg.yml";
    	SimplePostForAutoGenId post= new SimplePostForAutoGenId();
        Object[] testCaseList = post.getYmlTestData(fileName);
    	System.out.println(testCaseList.length);
    	try {
			post.test((TestCaseDTO)testCaseList[0]);
		} catch (AuthenticationTestException e) {
			e.printStackTrace();
		} catch (AdminTestException e) {
			e.printStackTrace();
		}
    	
    }
    
    
    
	@Override
	public RequestDataDTO prepare() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public ResponseDataDTO call(RequestDataDTO requestData) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void process(ResponseDataDTO res) {
		// TODO Auto-generated method stub
		
	}
    
    
    
    
    
    
    
    
    
    
    
    
    
    


}