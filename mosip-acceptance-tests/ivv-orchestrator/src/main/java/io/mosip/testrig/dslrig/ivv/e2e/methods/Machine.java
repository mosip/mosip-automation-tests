package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.MachineHelper;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

@Scope("prototype")
@Component
public class Machine extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(Machine.class);
	MachineHelper machineHelper = new MachineHelper();
	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() throws RigInternalError {
		String id = null;
		String activecheck = "T";
		String calltype = null;
		int centerCount = 0;
		HashMap<String, String> machineDetailsmap = new HashMap<String, String>();
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Method Type[POST/GET/PUT/PATCH] parameter is  missing from DSL step");
			this.hasError = true;
			throw new RigInternalError(
					"Method Type[POST/GET/PUT/PATCH] parameter is  missing from DSL step: " + step.getName());
		} else {
			calltype = step.getParameters().get(0);

		}
		if (step.getParameters().size() >= 2 && step.getParameters().get(1).startsWith("$$")) {
			id = step.getParameters().get(1);
			if (id.startsWith("$$")) {
				machineDetailsmap = step.getScenario().getVariables();
			}
		}
		if (step.getParameters().size() >= 3) {
			centerCount = Integer.parseInt(step.getParameters().get(2));
		}
		if (step.getParameters().size() >= 4) {
			activecheck = step.getParameters().get(3);
		}
		switch (calltype) {
		case "CREATE":
			logger.info("Usage of this step :   $$machineDetails=e2e_Machine(CREATE,$$centerId1,T)");
			String machinetypecode = machineHelper.createMachineType();
			String machinetypestatus = machineHelper.activateMachineType(machinetypecode, activecheck);
			logger.info(machinetypecode + " " + machinetypestatus);
			String machinespecId = machineHelper.createMachineSpecification(machinetypecode);
			machineHelper.activateMachineSpecification(machinespecId, activecheck);

			machineDetailsmap = machineHelper.createMachine(machinespecId, machineDetailsmap, centerCount);
			machineHelper.activateMachine(machineDetailsmap.get("machineid"), activecheck);

			if (step.getOutVarName() != null)
				step.getScenario().getVariables().putAll(machineDetailsmap);
			break;
		case "ACTIVE_FLAG":
			machineHelper.activateMachine(machineDetailsmap.get("machineid"), activecheck);
			break;

		case "UPDATE":
			machineDetailsmap = machineHelper.updateMachine(machineDetailsmap, centerCount);
			if (step.getOutVarName() != null)
				step.getScenario().getVariables().putAll(machineDetailsmap);
			break;

		case "DCOM":

			machineDetailsmap = machineHelper.updateMachine(machineDetailsmap, 0);
			machineHelper.dcomMachine(machineDetailsmap.get("machineid"));
			break;
		case "REMOVE_CENTER":

			machineDetailsmap = machineHelper.updateMachine(machineDetailsmap, 0);
			break;

		default:
			break;
		}

	}

}
