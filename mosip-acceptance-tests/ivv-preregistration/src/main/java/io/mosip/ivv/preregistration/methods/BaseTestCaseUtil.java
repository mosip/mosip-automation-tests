package io.mosip.ivv.preregistration.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.service.BaseTestCase;
import io.mosip.util.PreRegistrationLibrary;

public class BaseTestCaseUtil extends BaseStep{

	public BaseTestCaseUtil() {
		BaseTestCase.initialize();
    	PreRegistrationLibrary prliberary= new PreRegistrationLibrary();
    	prliberary.PreRegistrationResourceIntialize();
	}
	
}
