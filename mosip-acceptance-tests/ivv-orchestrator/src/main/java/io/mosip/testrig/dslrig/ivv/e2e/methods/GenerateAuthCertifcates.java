package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Reporter;
import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.utils.CertificateGenerationUtil;
import io.mosip.testrig.apirig.utils.PartnerRegistration;
import io.mosip.testrig.apirig.utils.RestAssuredPrettyLogger;
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
		setDslReportCapture(true);
		try {
			generateAuthenticationCertificates();
		} finally {
			setDslReportCapture(false);
		}
	}

	private void setDslReportCapture(boolean enabled) {
		String methodName = enabled ? "startDslReportCapture" : "stopDslReportCapture";
		try {
			RestAssuredPrettyLogger.class.getMethod(methodName).invoke(null);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(
					"apitest-commons 1.8.0-SNAPSHOT with DSL API report capture is required", e);
		}
	}

	private void generateAuthenticationCertificates() {
		runReportedOperation("Delete existing authentication certificates",
				PartnerRegistration::deleteCertificates);
		runReportedOperation("Generate authentication certificate thumbprints",
				CertificateGenerationUtil::getThumbprints);
		runReportedOperation("Create and publish authentication policy",
				AdminTestUtil::createAndPublishPolicy);

		runReportedOperation("Generate authentication partner key",
				PartnerRegistration::generateAndGetPartnerKeyUrl);

		runReportedOperation("Create and publish eKYC policy",
				AdminTestUtil::createAndPublishPolicyForKyc);
		runReportedOperation("Generate eKYC partner key",
				() -> kycPartnerKeyUrl = PartnerRegistration.generateAndGetEkycPartnerKeyUrl());

		String uriPartsforkyc[] = kycPartnerKeyUrl.split("/");
		kycPartnerId = uriPartsforkyc[uriPartsforkyc.length - 2];
		partnerKeyUrl = PartnerRegistration.partnerKeyUrl;
		String uriParts[] = PartnerRegistration.partnerKeyUrl.split("/");
		partnerId = uriParts[uriParts.length - 2];
		logger.info(partnerKeyUrl);
	}

	private void runReportedOperation(String operation, Runnable action) {
		report(operation + " - running", false);
		try {
			action.run();
			report(operation + " - passed", false);
		} catch (RuntimeException | Error e) {
			report(operation + " - failed: " + rootCauseMessage(e), true);
			throw e;
		}
	}

	private void report(String message, boolean failed) {
		Reporter.log((failed ? "<span style='color:red; font-weight:bold;'>" : "<span>")
				+ message + "</span><br>", true);
		if (extentInstance != null) {
			if (failed) {
				extentInstance.fail(message);
			} else {
				extentInstance.info(message);
			}
		}
	}

	private static String rootCauseMessage(Throwable throwable) {
		Throwable root = throwable;
		while (root.getCause() != null) {
			root = root.getCause();
		}
		return root.getMessage() != null ? root.getMessage() : root.getClass().getSimpleName();
	}
}
