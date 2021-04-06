package io.mosip.ivv.e2e.methods;

import org.apache.log4j.Logger;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class BookAppointment extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(BookAppointment.class);
	boolean bookOnHolidays=Boolean.FALSE;
	
	@Override
	public void run() throws RigInternalError {
		if (step.getParameters() != null && !step.getParameters().isEmpty())
			bookOnHolidays =Boolean.parseBoolean(step.getParameters().get(0));
		
		for (String resDataPath : residentPathsPrid.keySet()) {
			packetUtility.bookAppointment(residentPathsPrid.get(resDataPath), 1,contextKey,bookOnHolidays);
		}
	}

}
