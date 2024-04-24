package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;

public class BookAppointment extends BaseTestCaseUtil implements StepInterface {
	public static Logger logger = Logger.getLogger(BookAppointment.class);
	boolean bookOnHolidays = Boolean.FALSE;
	
	static {
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		Boolean isForChildPacket = false;
		if (!step.getParameters().isEmpty() && step.getParameters().size() == 1) {
			bookOnHolidays = Boolean.parseBoolean(step.getParameters().get(0));
			
			for (String resDataPath : step.getScenario().getResidentPathsPrid().keySet()) {
				packetUtility.bookAppointment(step.getScenario().getResidentPathsPrid().get(resDataPath), 1, step.getScenario().getCurrentStep(), bookOnHolidays,step);
			}
		}else if(!step.getParameters().isEmpty() && step.getParameters().size() >= 2) {  //"$$var=e2e_bookAppointment(false,$$prid)"
			bookOnHolidays = Boolean.parseBoolean(step.getParameters().get(0));
			String prid = step.getParameters().get(1);
			int slotNumber= Integer.parseInt(step.getParameters().get(2));
			if (prid.startsWith("$$")) {
				prid = step.getScenario().getVariables().get(prid);
				packetUtility.bookAppointment(prid, slotNumber, step.getScenario().getCurrentStep(), bookOnHolidays,step);
			}
		}
		else if (!step.getParameters().isEmpty() && step.getParameters().size() == 2) { // used for child packet processing
			bookOnHolidays = Boolean.parseBoolean(step.getParameters().get(0));
			isForChildPacket = Boolean.parseBoolean(step.getParameters().get(1));
			int slotNumber= Integer.parseInt(step.getParameters().get(2));
			if (isForChildPacket && step.getScenario().getPrid_updateResident() != null)
				packetUtility.bookAppointment(step.getScenario().getPrid_updateResident(), slotNumber, step.getScenario().getCurrentStep(), bookOnHolidays,step);
		} else {
			this.hasError=true;
			throw new RigInternalError("Input parmeter is missiing [true/false]");
		}
	}
}
