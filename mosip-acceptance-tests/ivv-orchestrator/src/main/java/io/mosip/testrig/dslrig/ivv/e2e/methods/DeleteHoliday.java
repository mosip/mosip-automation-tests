package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Logger;

import io.mosip.testrig.apirig.dbaccess.AuditDBManager;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;

public class DeleteHoliday extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(DeleteHoliday.class);
	String holidayId ="";
	
	@Override
	public void run() throws RigInternalError {
		
		if (step.getParameters().size() == 1) {
			String holidayId = step.getParameters().get(0);
			if (holidayId.startsWith("$$"))
				holidayId = step.getScenario().getVariables().get(holidayId);
			String deleteQuery = "delete from master.loc_holiday where id = '" + holidayId + "'";
			logger.info(deleteQuery);
			AuditDBManager.executeQueryAndDeleteRecord("master", deleteQuery);
		} 
	}	
}
