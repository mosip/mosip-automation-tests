package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.restassured.response.Response;

public class CheckRIDStage extends BaseTestCaseUtil implements StepInterface {
    Logger logger = Logger.getLogger(CheckRIDStage.class);

    @Override
    public void run() throws RigInternalError {
        JSONObject myJSONObject = null;
        String ridStage = null;
        Boolean flag = false;
        String transactionTypeCode = null;
        String statusCode = null;
        String subStatusCode = null;
        String waitTime = props.getProperty("waitTime");
        int counter = 0;
        JSONObject res = null;
        JSONArray arr = null;

        if (step.getParameters().size() >= 3) {
            ridStage = step.getScenario().getVariables().get(step.getParameters().get(0));
            transactionTypeCode = step.getParameters().get(1);
            statusCode = step.getParameters().get(2);

            if (step.getParameters().size() == 4) {
                subStatusCode = step.getParameters().get(3);
            }
        }

        while (counter < Integer.parseInt(props.getProperty("loopCount"))) {
            Response response = getRequest(baseUrl + props.getProperty("ridStatus") + ridStage, "Get Stages by rid", step);

            // Check these two keys statusCode, transactionTypeCode

            res = new JSONObject(response.getBody().asString());
            arr = res.getJSONObject("response").getJSONArray("packetStatusUpdateList");
            for (Object myObject : arr) {
                myJSONObject = (JSONObject) myObject;

                if (transactionTypeCode.equalsIgnoreCase(myJSONObject.getString("transactionTypeCode"))) {
                    if (statusCode.equalsIgnoreCase(myJSONObject.getString("statusCode"))) {
                        System.out.println("matching statusCode");
                        flag = true;
                        break;
                    }
                     else if (subStatusCode != null && subStatusCode.equalsIgnoreCase(myJSONObject.getString("subStatusCode"))) {
                        flag = true;
                        break;
                    } else {
                        flag = false;
                    }
                }
                
            }

            if (!myJSONObject.getString("transactionTypeCode").equalsIgnoreCase(transactionTypeCode) || myJSONObject.getString("subStatusCode").equalsIgnoreCase(subStatusCode)) {
            	logger.info("Waiting for " +Long.parseLong(waitTime)/1000+ " sec to get desired response");
                counter++;
                try {
                    Thread.sleep(Long.parseLong(waitTime));
                } catch (NumberFormatException | InterruptedException e) {
                	logger.error(e.getMessage());
                    Thread.currentThread().interrupt();
                }
            } else {
                // Exit the loop if the desired result is achieved
                break;
            }
        }

        logger.info(res.toString());
        if (flag.equals(true)) {
            logger.info("RESPONSE= contains" + transactionTypeCode + statusCode);
            logger.info("subStatusCode= " + myJSONObject.getString("subStatusCode"));
        } else {
            logger.error("RESPONSE= doesn't contain" + arr);
            this.hasError=true;
            throw new RuntimeException("RESPONSE= doesn't contain" + transactionTypeCode + statusCode);
        }
    }
}
