package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import io.mosip.testrig.apirig.dbaccess.DBManager;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.TestRunner;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class MasterdataDelete extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(MasterdataDelete.class);

	@Override
	public void run() throws RigInternalError {
		Session session = null;
		try {
			session = DBManager.getDataBaseConnection(dslConfigManager.getMASTERDbUrl(), dslConfigManager.getMasterDbUser(),
					dslConfigManager.getMasterDbPass(), dslConfigManager.getMasterDbSchema());
			DBManager.executeQueries(session,
					TestRunner.getGlobalResourcePath() + "/" + "config/masterDataDeleteQueries.txt");
		} catch (Exception e) {
			logger.error("Error:: While executing MASTER DB Quiries." + e.getMessage());
		} finally {
			if (session != null) {
				session.close();
			}
		}

	}
}
