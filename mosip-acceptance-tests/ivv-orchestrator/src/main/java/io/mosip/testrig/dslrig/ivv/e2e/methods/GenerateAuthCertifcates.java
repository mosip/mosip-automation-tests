package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Logger;

import io.mosip.testrig.apirig.admin.fw.util.AdminTestUtil;
import io.mosip.testrig.apirig.ida.certificate.CertificateGenerationUtil;
import io.mosip.testrig.apirig.ida.certificate.PartnerRegistration;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;


public class GenerateAuthCertifcates extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(GenerateAuthCertifcates.class);
	PartnerRegistration partnerRegistration=new PartnerRegistration();

	@Override
	public void run() throws RigInternalError {
		//AuthPartnerProcessor.startProcess();
		PartnerRegistration.deleteCertificates();
		CertificateGenerationUtil.getThumbprints();
		AdminTestUtil.createAndPublishPolicy();
		
		PartnerRegistration.generateAndGetPartnerKeyUrl();
		String partnerKeyUrl= PartnerRegistration.partnerKeyUrl;
		String uriParts[] = PartnerRegistration.partnerKeyUrl.split("/");
		partnerId = uriParts[uriParts.length - 2];
		System.out.println(partnerKeyUrl);
		BaseTestCaseUtil.partnerKeyUrl=partnerKeyUrl;
		System.out.println(partnerKeyUrl);
		
		
	}
}
