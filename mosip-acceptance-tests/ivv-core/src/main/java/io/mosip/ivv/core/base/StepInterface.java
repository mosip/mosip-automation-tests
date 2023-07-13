package io.mosip.ivv.core.base;


import java.util.ArrayList;
import java.util.Properties;

import com.aventstack.extentreports.ExtentTest;

import io.mosip.ivv.core.dtos.CallRecord;
import io.mosip.ivv.core.dtos.Scenario;
import io.mosip.ivv.core.dtos.Store;
import io.mosip.ivv.core.exceptions.RigInternalError;

public interface StepInterface {
    Boolean hasError();

    void setState(Store s);

    Store getState();

    void setSystemProperties(Properties props);

    ArrayList<Scenario.Step.Error> getErrorsForAssert();

    void errorHandler();

    void validateStep() throws RigInternalError;

    void assertNoError();

    void assertHttpStatus();

    void setExtentInstance(ExtentTest e);

    void setStep(Scenario.Step s);

    void setup() throws RigInternalError;

    CallRecord getCallRecord();
    void run() throws RigInternalError;



   // RequestDataDTO prepare();
    //ResponseDataDTO call(RequestDataDTO requestData);
   // void process(ResponseDataDTO res);
}
