package io.mosip.testrig.dslrig.dataprovider.packet;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.testrig.dslrig.dataprovider.BiometricDataProvider;
import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.models.mds.MDSRCaptureModel;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

/**
 * CBEFF file generation for packet biometrics (extracted from
 * {@code PacketTemplateProvider}).
 */
public final class PacketCbeffGenerator {

	private static final Logger logger = LoggerFactory.getLogger(PacketCbeffGenerator.class);

	private PacketCbeffGenerator() {
	}

	public static Boolean generateCBEFF(ResidentModel resident, List<String> bioAttrib, String outFile,
			String contextKey, String purpose, String qualityScore, List<String> missAttribs,
			boolean genarateValidCbeff, String process) throws Exception {

		String strVal = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "usemds").toString();
		boolean bMDS = Boolean.valueOf(strVal);
		String cbeff = null;

		if (resident.getBioExceptions() == null || resident.getBioExceptions().isEmpty())
			cbeff = resident.getBiometric().getCbeff();

		if (bMDS) {
			// Regenerate MDS capture when missing even if CBEFF was cached (face_encrypted needs capture).
			if (cbeff == null || resident.getBiometric().getCapture() == null) {
				MDSRCaptureModel capture = BiometricDataProvider.regenBiometricViaMDS(resident, contextKey, purpose,
						qualityScore, process);

				if (capture == null) {
					logger.error("Failed to generate biometric via mds");
					return false;
				}

				resident.getBiometric().setCapture(capture.getLstBiometrics());
				String strCBeff = BiometricDataProvider.toCBEFFFromCapture(bioAttrib, capture, outFile, missAttribs,
						genarateValidCbeff, resident.getBioExceptions(), contextKey);

				resident.getBiometric().setCbeff(strCBeff);

			} else {
			try(FileOutputStream fos = new FileOutputStream(outFile)){
				BufferedOutputStream bos = new BufferedOutputStream(fos);

				PrintWriter writer = new PrintWriter(new BufferedOutputStream(new FileOutputStream(outFile)));
				writer.print(cbeff);
				writer.close();
				fos.close();
				bos.close();
			}catch(Exception e) {
				logger.error("Error while writing CBEFF to file", e);
				return false;
			}
		}

		} else {

			if (cbeff == null) {

				String strCBeff = BiometricDataProvider.toCBEFF(bioAttrib, resident.getBiometric(), outFile,
						genarateValidCbeff, contextKey);
				resident.getBiometric().setCbeff(strCBeff);

			} else {
				try(FileOutputStream fos = new FileOutputStream(outFile)){
				BufferedOutputStream bos = new BufferedOutputStream(fos);

				PrintWriter writer = new PrintWriter(new BufferedOutputStream(new FileOutputStream(outFile)));
				writer.print(cbeff);
				writer.close();
				fos.close();
				bos.close();
				}catch(Exception e) {
					logger.error("Error while writing CBEFF to file", e);
					return false;
			}
		}
		}
		resident.save();
		return true;
	}
}
