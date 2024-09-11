package io.mosip.testrig.dslrig.ivv.core.base;

import java.util.ArrayList;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.aventstack.extentreports.ExtentTest;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;

import io.mosip.testrig.dslrig.ivv.core.dtos.CallRecord;
import io.mosip.testrig.dslrig.ivv.core.dtos.RequestDataDTO;
import io.mosip.testrig.dslrig.ivv.core.dtos.ResponseDataDTO;
import io.mosip.testrig.dslrig.ivv.core.dtos.Scenario;
import io.mosip.testrig.dslrig.ivv.core.dtos.Store;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.core.utils.ErrorMiddleware;

public class BaseStep {
	private static final Logger logger = LoggerFactory.getLogger(BaseStep.class);
    public Boolean hasError = false;
    public Properties properties;
    public Store store = null;
    public int index = 0;
    public CallRecord callRecord;
    public ExtentTest extentInstance;
    public Scenario.Step step;

    public Boolean hasError() {
        return hasError;
    }

    public void setSystemProperties(Properties props){
        properties = props;
    }

    public ArrayList<Scenario.Step.Error> getErrorsForAssert(){
        return step.getErrors();
    }

    public void setStep(Scenario.Step s) {
        this.step = s;
    }

    public void setState(Store s) {
        this.store = s;
    }

    public Store getState() {
        return this.store;
    }

    public CallRecord getCallRecord() {
        return this.callRecord;
    }

    public void setExtentInstance(ExtentTest e){
        this.extentInstance = e;
    }

    public void setup() throws RigInternalError {
        try {
            if(store.getCurrentPerson() == null){
                store.setCurrentPerson(store.getPersona().getPersons().get(0));
            }
            if(store.getCurrentRegistrationUSer() == null){
                store.setCurrentRegistrationUSer(store.getRegistrationUsers().get(0));
            }
            if(store.getCurrentPartner() == null){
                store.setCurrentPartner(store.getPartners().get(0));
            }
        } catch (RuntimeException e){
            throw new RigInternalError("Error during setup "+e.getMessage());
        }
    }

    public void validateStep() throws RigInternalError {return;}

    public void logInfo(String msg){
    	logger.info(msg);
        extentInstance.info(msg);
    }

    public void logWarning(String msg){
        logger.info(msg);
        extentInstance.warning(msg);
    }

    public void logFail(String msg){
        logger.error(msg);
        extentInstance.fail(msg);
    }

    public void logSevere(String msg){
        logger.error(msg);
        extentInstance.info(msg);
    }

    public void errorHandler(){
        if(callRecord != null){
            if (step.getErrors() != null && step.getErrors().size() > 0) {
                ErrorMiddleware.MiddlewareResponse emr = new ErrorMiddleware(step.getErrors(), callRecord.getResponse().body().asString(), extentInstance).inject();
                if (!emr.getStatus()) {
                    this.hasError = true;
                    return;
                }
            }
        }
    }

    public void assertHttpStatus(){
        if (callRecord != null && callRecord.getResponse().getStatusCode() == 200) {
            logInfo("Assert [passed]: HTTP status code assert passed - expected [200], actual [" + callRecord.getResponse().getStatusCode()+"]");
        } else if (callRecord != null && callRecord.getResponse().getStatusCode() != 200){
            logSevere("Assert [failed]: HTTP status code assert failed - expected [200], actual [" + callRecord.getResponse().getStatusCode()+"]");
            this.hasError = true;
            return;
        }
    }

    public void assertNoError() {
        if(callRecord != null){
            ReadContext ctx = JsonPath.parse(callRecord.getResponse().getBody().asString());
            try {
                if(ctx.read("$['errors']") != null){
                    logSevere("Assert [failed]: Response error object - expected [null], actual ["+ctx.read("$['errors']")+"]");
                    this.hasError=true;
                    return;
                }
                if(ctx.read("$['response']") == null){
                    logSevere("Assert [failed]: Response status - response expected [not null], actual ["+ctx.read("$['response']")+"]");
                    this.hasError=true;
                    return;
                }
            } catch (PathNotFoundException e) {
                logSevere("Assert [failed]: Response status - "+e.getStackTrace());
                this.hasError=true;
                return;
            }
            logInfo("Assert [passed]: Response object - error object is null and response object is not null");
        }
    }

	public void process(ResponseDataDTO res) {
		// TODO Auto-generated method stub
		
	}

	public RequestDataDTO prepare() {
		// TODO Auto-generated method stub
		return null;
	}

}
