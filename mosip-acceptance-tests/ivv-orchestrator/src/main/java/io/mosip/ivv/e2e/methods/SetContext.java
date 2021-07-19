package io.mosip.ivv.e2e.methods;

import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import io.mosip.admin.fw.util.AdminTestUtil;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.PacketUtility;
import io.mosip.ivv.orchestrator.TestRunner;
import io.mosip.service.BaseTestCase;

public class SetContext extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(SetContext.class);

	@Override
	public void run() throws RigInternalError {
		constantIntializer();
		String contextKeyValue = "dev_context";
		String userAndMachineDetailParam = null;
		String mosipVersion = null;
		Properties machinePrivateKeyProp = null;
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.warn("SetContext Arugemnt is  Missing : Please pass the argument from DSL sheet");
		} else {
			contextKeyValue = step.getParameters().get(0);
			// contextKey.put("contextKey",contextKeyValue );
			contextKey.put(contextKeyValue, "true");
			contextInuse.clear();
			contextInuse.put("contextKey", contextKeyValue);
			if (step.getParameters().size() > 1) {
				String value = step.getParameters().get(1);
				if (!(value.equalsIgnoreCase("-1")))
					userAndMachineDetailParam = value;
			}
			if (step.getParameters().size() > 2) {
				List<String> version = PacketUtility.getParamsArg(step.getParameters().get(2), "@@");
				if (!(version.contains("-1")))
					mosipVersion = version.get(0) + "." + version.get(1);
			}
			if (step.getParameters().size() > 3) {
				String machinePrivaeKeyFileName = step.getParameters().get(3);
				if (!StringUtils.isBlank(machinePrivaeKeyFileName)) {
					String machinePrivaeteKeyFilePath = TestRunner.getExeternalResourcePath()
							+ props.getProperty("ivv.path.deviceinfo.folder") + machinePrivaeKeyFileName
							+ ".properties";
					machinePrivateKeyProp = AdminTestUtil.getproperty(machinePrivaeteKeyFilePath);
				}
			}
			// packetUtility.createContext(contextKeyValue,BaseTestCase.ApplnURI+"/");
			packetUtility.createContexts(contextKeyValue, userAndMachineDetailParam, mosipVersion,
					machinePrivateKeyProp, BaseTestCase.ApplnURI + "/");

		}
	}
}
