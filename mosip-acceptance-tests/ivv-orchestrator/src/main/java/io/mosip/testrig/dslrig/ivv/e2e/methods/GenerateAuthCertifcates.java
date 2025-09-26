package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.utils.CertificateGenerationUtil;
import io.mosip.testrig.apirig.utils.PartnerRegistration;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class GenerateAuthCertifcates extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(GenerateAuthCertifcates.class);
	PartnerRegistration partnerRegistration = new PartnerRegistration();

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		// AuthPartnerProcessor.startProcess();
		PartnerRegistration.deleteCertificates();
		CertificateGenerationUtil.getThumbprints();
		AdminTestUtil.createAndPublishPolicy();

		PartnerRegistration.generateAndGetPartnerKeyUrl();
		// Genrating Kyc Certificate
		AdminTestUtil.createAndPublishPolicyForKyc();
		kycPartnerKeyUrl = PartnerRegistration.generateAndGetEkycPartnerKeyUrl();
		logger.info("kycPartnerKeyUrl = " + kycPartnerKeyUrl);
		
		String uriPartsforkyc[] = kycPartnerKeyUrl.split("/");
		kycPartnerId = uriPartsforkyc[uriPartsforkyc.length - 2];

		partnerKeyUrl = PartnerRegistration.partnerKeyUrl;
		String uriParts[] = PartnerRegistration.partnerKeyUrl.split("/");
		partnerId = uriParts[uriParts.length - 2];
		logger.info("partnerKeyUrl = " + partnerKeyUrl);

	}
}
