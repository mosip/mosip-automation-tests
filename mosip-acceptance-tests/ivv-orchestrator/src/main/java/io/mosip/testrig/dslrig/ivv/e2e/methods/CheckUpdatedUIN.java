package io.mosip.testrig.dslrig.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Reporter;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class CheckUpdatedUIN extends BaseTestCaseUtil implements StepInterface {

    private static final Logger logger = Logger.getLogger(CheckUpdatedUIN.class);

    static {
        if (dslConfigManager.IsDebugEnabled()) {
            logger.setLevel(Level.ALL);
        } else {
            logger.setLevel(Level.ERROR);
        }
    }

    @Override
    public void run() throws RigInternalError {
        String uin1 = null;
        String uin2 = null;

        try {
            // Validate step parameters
            if (step.getParameters() == null || step.getParameters().isEmpty()) {
                logger.error("Parameters are missing from the DSL step.");
                assertTrue(false, "Parameters are missing in step: " + step.getName());
            } else if (step.getParameters().size() == 2) {
                uin1 = step.getScenario().getVariables().get(step.getParameters().get(0));
                uin2 = step.getScenario().getVariables().get(step.getParameters().get(1));
            } else {
                logger.error("Incorrect number of parameters provided. Expected 2, found: " 
                        + step.getParameters().size());
                assertTrue(false, "Expected 2 parameters but found: " + step.getParameters().size());
            }

            // Compare UINs
            if (uin1 == null || uin2 == null) {
                logger.error("One or both UINs are null. UIN1: " + uin1 + ", UIN2: " + uin2);
                throw new RigInternalError("One or both UINs are null.");
            }

            if (uin1.equals(uin2)) {
                logger.info("Updated UIN is the same as the previous UIN. UIN: " + uin1);
                Reporter.log("<b style=\"background-color: #0A0;\">Marking test case as passed. As Updated UIN is the same as the previous UIN. UIN: </b><br>\n" + uin1);
            } else {
                logger.error("Updated UIN is different from the previous UIN. Previous UIN: " 
                        + uin1 + ", Updated UIN: " + uin2);
                this.hasError = true;
                throw new RigInternalError("Updated UIN is different from the previous UIN.");
            }
        } catch (RigInternalError e) {
            logger.error("RigInternalError occurred: " + e.getMessage(), e);
            throw e; // Re-throw after logging
        } catch (Exception e) {
            logger.error("An unexpected error occurred: " + e.getMessage(), e);
            throw new RigInternalError("Unexpected error occurred: " + e.getMessage());
        }
    }
}
