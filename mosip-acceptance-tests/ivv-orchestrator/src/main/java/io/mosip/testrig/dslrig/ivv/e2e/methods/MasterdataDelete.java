package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import io.mosip.testrig.apirig.dbaccess.DBManager;
import io.mosip.testrig.apirig.kernel.util.ConfigManager;
import io.mosip.testrig.apirig.testrunner.MosipTestRunner;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;

public class MasterdataDelete extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(MasterdataDelete.class);
	

	@Override
	public void run() throws RigInternalError {
		Session session = null;
		try {
			session = DBManager.getDataBaseConnection(ConfigManager.getMASTERDbUrl(), ConfigManager.getMasterDbUser(),
					ConfigManager.getMasterDbPass(), ConfigManager.getMasterDbSchema());
			DBManager.executeQueries(session,  MosipTestRunner.getGlobalResourcePath() + "/"	+ "config/masterDataDeleteQueries.txt");
			} catch (Exception e) {
				logger.error("Error:: While executing MASTER DB Quiries." + e.getMessage());
			} finally {
				if (session != null) {
					session.close();
				}
			}

	}
}
