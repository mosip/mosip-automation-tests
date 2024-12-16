package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.apirig.dbaccess.DBManager;
import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.TestRunner;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class DeleteCertificatesAndOnboardingPartners extends BaseTestCaseUtil implements StepInterface {
	public static Logger logger = Logger.getLogger(CheckRIDStage.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		DBManager.executeDBQueries(dslConfigManager.getKMDbUrl(), dslConfigManager.getKMDbUser(),
				dslConfigManager.getKMDbPass(), dslConfigManager.getKMDbSchema(),
				TestRunner.getGlobalResourcePath() + "/" + "config/keyManagerCertDataDeleteQueries.txt");
		DBManager.executeDBQueries(dslConfigManager.getIdaDbUrl(), dslConfigManager.getIdaDbUser(),
				dslConfigManager.getPMSDbPass(), dslConfigManager.getIdaDbSchema(),
				TestRunner.getGlobalResourcePath() + "/" + "config/idaCertDataDeleteQueries.txt");
		DBManager.executeDBQueries(dslConfigManager.getMASTERDbUrl(), dslConfigManager.getMasterDbUser(),
				dslConfigManager.getMasterDbPass(), dslConfigManager.getMasterDbSchema(),
				TestRunner.getGlobalResourcePath() + "/" + "config/masterDataCertDataDeleteQueries.txt");
	}

}
