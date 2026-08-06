package io.mosip.testrig.dslrig.dataprovider.biometric;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.mock.sbi.test.CentralizedMockSBI;
import io.mosip.testrig.dslrig.dataprovider.mds.MDSClient;
import io.mosip.testrig.dslrig.dataprovider.mds.MDSClientInterface;
import io.mosip.testrig.dslrig.dataprovider.mds.MDSClientNoMDS;
import io.mosip.testrig.dslrig.dataprovider.models.BioModality;
import io.mosip.testrig.dslrig.dataprovider.models.BiometricDataModel;
import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.models.mds.MDSDevice;
import io.mosip.testrig.dslrig.dataprovider.models.mds.MDSDeviceCaptureModel;
import io.mosip.testrig.dslrig.dataprovider.models.mds.MDSRCaptureModel;
import io.mosip.testrig.dslrig.dataprovider.test.registrationclient.RegistrationSteps;
import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

public final class MdsCaptureService {

	private static final Logger logger = LoggerFactory.getLogger(MdsCaptureService.class);

	private static final ConcurrentHashMap<String, Object> MDS_CONTEXT_LOCKS = new ConcurrentHashMap<>();

	// CentralizedMockSBI.localStore is a plain HashMap (not thread-safe). All
	// startSBI/stopSBI calls must be serialized through this global lock to prevent
	// concurrent HashMap corruption when multiple scenarios run in parallel.
	private static final Object SBI_MAP_LOCK = new Object();

	private static Object mdsLockFor(String contextKey) {
		String key = contextKey == null ? "default" : contextKey;
		return MDS_CONTEXT_LOCKS.computeIfAbsent(key, k -> new Object());
	}

	/**
	 * Removes the lock entry for a context so long-running deployments don't accumulate one
	 * entry per distinct contextKey forever. Call only after the context's MDS work has
	 * finished (e.g. from context reset/teardown), never while a capture may still be in flight.
	 */
	public static void releaseContextLock(String contextKey) {
		String key = contextKey == null ? "default" : contextKey;
		MDS_CONTEXT_LOCKS.remove(key);
	}

	private static final String FALSE = "false";
	private static final String LEFTEYE = "leftEye";
	private static final String RIGHTEYE = "rightEye";
	private static final String RIGHT = "Right";

	private MdsCaptureService() {
	}

	public static List<BioModality> getModalitiesByType(List<BioModality> bioExceptions, String type) {
		List<BioModality> lst = new ArrayList<BioModality>();

		for (BioModality m : bioExceptions) {
			if (m.getType().equalsIgnoreCase(type)) {
				lst.add(m);
			}
		}
		return lst;
	}

	public static MDSRCaptureModel regenBiometricViaMDS(ResidentModel resident, String contextKey, String purpose,
			String qualityScore, String process) throws Exception {
		synchronized (mdsLockFor(contextKey)) {
			return regenBiometricViaMDSUnderLock(resident, contextKey, purpose, qualityScore, process);
		}
	}

	private static MDSRCaptureModel regenBiometricViaMDSUnderLock(ResidentModel resident, String contextKey, String purpose,
			String qualityScore, String process) throws Exception {
		synchronized (SBI_MAP_LOCK) {
			CentralizedMockSBI.stopSBI(contextKey);
		}
		BiometricDataModel biodata = null;
		MDSRCaptureModel capture = null;

		MDSClientInterface mds = null;
		String val;
		boolean bNoMDS = true;
		String mdsprofilePath = null;
		String profileName = null;
		int port = 0;
		Object bypassValue = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "mdsbypass");
		val = bypassValue != null ? bypassValue.toString() : FALSE;
		List<String> filteredAttribs = resident.getFilteredBioAttribtures();
		List<BioModality> bioExceptions = resident.getBioExceptions();
		List<String> bioexceptionlist = new ArrayList<String>();
		try {
			try {
				if (val == null || val.equals("") || val.equals(FALSE)) {

					val = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "mdsport").toString();
					mdsprofilePath = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "mdsprofilepath")
							.toString();

					Path p12path = null;
					boolean invalidCertFlag = Boolean
							.parseBoolean(VariableManager.getVariableValue(contextKey, "invalidCertFlag").toString());

					if (invalidCertFlag)
						p12path = Paths.get(
								VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "invalidCertpath")
										.toString());
					else
						p12path = Paths.get(System.getProperty("java.io.tmpdir"),
								VariableManager.getVariableValue(contextKey, "db-server").toString());

					RestClient.logInfo(contextKey, "p12path" + p12path);

					int maxLoopCount = Integer.parseInt(
							VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "mdsPortLoopCount").toString());

					while (maxLoopCount > 0) {
						try {
							synchronized (SBI_MAP_LOCK) {
								port = CentralizedMockSBI.startSBI(contextKey, "Registration", "Biometric Device",
										p12path.toString());
							}
						} catch (Exception e) {
							logger.error("Exception occured during startSBI " + contextKey, e);
						}
						if (port != 0) {
							RestClient.logInfo(contextKey, "Found the port " + contextKey + " port number is: " + port);
							break;
						}

						maxLoopCount--;
					}

					if (port == 0) {
						logger.error("Unable to find the port " + contextKey + " port number is: " + port);
						return null;
					}

					MdsPortRegistry.put(contextKey, port);

					mds = new MDSClient(port);

					profileName = "res" + resident.getId();

					if (resident.getBioExceptions() != null && !resident.getBioExceptions().isEmpty()) {
						copyExceptionPhoto(mdsprofilePath, "Default", profileName);
					}

					if (process.equalsIgnoreCase("Update")) {

						mds.updateProfile(mdsprofilePath, profileName, resident, contextKey, purpose);

					} else {

						mds.createProfile(mdsprofilePath, profileName, resident, contextKey, purpose);
					}
					mds.setProfile(profileName, port, contextKey);

				} else {
					mds = new MDSClientNoMDS();
					bNoMDS = false;
					profileName = "res" + resident.getId();
					mds.createProfile(mdsprofilePath, profileName, resident, contextKey, purpose);
					mds.setProfile(profileName, port, contextKey);
				}

				Integer registeredPort = MdsPortRegistry.get(contextKey);
				RegistrationSteps steps = new RegistrationSteps();
				steps.setMDSscore(registeredPort != null ? registeredPort : port, "Biometric Device", qualityScore,
						contextKey);
				RestClient.logInfo(contextKey, "mds score is changed to : " + qualityScore);
				biodata = resident.getBiometric();

				if (biodata.getFaceHash() == null && biodata.getFingerHash() == null && biodata.getIris() == null)
					return new MDSRCaptureModel();
			} catch (Throwable t) {
				logger.error(" Port issue " + contextKey, t);
				return null;
			}

			if (bioExceptions != null && !bioExceptions.isEmpty()) {
				for (int modalityCount = 0; modalityCount < bioExceptions.size(); modalityCount++)
					bioexceptionlist.add(bioExceptions.get(modalityCount).getSubType().toString());
			}

			try {
				if ((filteredAttribs != null && filteredAttribs.contains("face")) && biodata.getRawFaceData() != null) {

					List<MDSDevice> faceDevices = mds.getRegDeviceInfo(DataProviderConstants.MDS_DEVICE_TYPE_FACE);
					MDSDevice faceDevice = faceDevices.get(0);

					capture = mds.captureFromRegDevice(faceDevice, capture, DataProviderConstants.MDS_DEVICE_TYPE_FACE,
							null, 60, faceDevice.getDeviceSubId().get(0), port, contextKey, null);
				}
			}

			catch (Throwable t) {
				logger.error(" Face get capture   fail" + contextKey, t);
				return null;
			}

			try {
				if (biodata.getIris() != null) {
					List<BioModality> irisExceptions = null;
					List<String> listexceptionBio = new ArrayList<String>();

					if (bioExceptions != null && !bioExceptions.isEmpty()) {
						irisExceptions = getModalitiesByType(bioExceptions, "Iris");
						for (BioModality modality : bioExceptions) {
							listexceptionBio.add(modality.getSubType());

						}
					}

					List<MDSDevice> irisDevices = mds.getRegDeviceInfo(DataProviderConstants.MDS_DEVICE_TYPE_IRIS);
					MDSDevice irisDevice = irisDevices.get(0);

					if (irisExceptions == null || irisExceptions.isEmpty()) {
						if (filteredAttribs != null && filteredAttribs.contains(LEFTEYE)) {
							capture = mds.captureFromRegDevice(irisDevice, capture,
									DataProviderConstants.MDS_DEVICE_TYPE_IRIS, null, 60,
									irisDevice.getDeviceSubId().get(0), port, contextKey, null);
						}

						if (irisDevice.getDeviceSubId().size() > 1) {
							if (filteredAttribs != null && filteredAttribs.contains(RIGHTEYE)) {

								capture = mds.captureFromRegDevice(irisDevice, capture,
										DataProviderConstants.MDS_DEVICE_TYPE_IRIS, null, 60,
										irisDevice.getDeviceSubId().get(1), port, contextKey, null);
							}
						}
					} else {
						String[] irisSubTypes = new String[irisExceptions.size()];
						int i = 0;
						for (BioModality bm : irisExceptions) {
							irisSubTypes[i] = bm.getSubType();
							i++;
						}
						for (String f : irisSubTypes) {

							if (f.equalsIgnoreCase(RIGHT)
									&& (filteredAttribs != null && filteredAttribs.contains(LEFTEYE))) {
								capture = mds.captureFromRegDevice(irisDevice, capture,
										DataProviderConstants.MDS_DEVICE_TYPE_IRIS, null, 60,
										irisDevice.getDeviceSubId().get(0), port, contextKey, null);
							} else if (f.equalsIgnoreCase("left")
									&& (filteredAttribs != null && filteredAttribs.contains(RIGHTEYE))) {

								if (irisDevice.getDeviceSubId().size() > 1)
									capture = mds.captureFromRegDevice(irisDevice, capture,
											DataProviderConstants.MDS_DEVICE_TYPE_IRIS, null, 60,
											irisDevice.getDeviceSubId().get(1), port, contextKey, null);
							}
						}
					}
				}

			}

			catch (Throwable t) {
				logger.error(" IRIS get capture  fail" + contextKey, t);
				return null;
			}

			try {
				if (biodata.getFingerPrint() != null) {
					List<BioModality> fingerExceptions = null;
					List<MDSDeviceCaptureModel> lstToRemove = new ArrayList<MDSDeviceCaptureModel>();
					List<String> listFingerExceptionBio = new ArrayList<String>();

					if (bioExceptions != null && !bioExceptions.isEmpty()) {

						fingerExceptions = getModalitiesByType(bioExceptions, "Finger");

						for (BioModality modality : bioExceptions) {
							listFingerExceptionBio.add(modality.getSubType());

						}
					}

					List<MDSDevice> fingerDevices = mds.getRegDeviceInfo(DataProviderConstants.MDS_DEVICE_TYPE_FINGER);
					MDSDevice fingerDevice = fingerDevices.get(0);

					for (int i = 0; i < fingerDevice.getDeviceSubId().size(); i++) {
						capture = mds.captureFromRegDevice(fingerDevice, capture,
								DataProviderConstants.MDS_DEVICE_TYPE_FINGER, null, 60,
								fingerDevice.getDeviceSubId().get(i), port, contextKey, null);
					}
					List<MDSDeviceCaptureModel> lstFingers = capture.getLstBiometrics()
							.get(DataProviderConstants.MDS_DEVICE_TYPE_FINGER);
					if (filteredAttribs != null) {

						String attr = null;

						for (MDSDeviceCaptureModel mdc : lstFingers) {
							int indx = 0;
							boolean bFound = false;
							for (indx = 0; indx < DataProviderConstants.schemaNames.length; indx++) {
								if (DataProviderConstants.displayFingerName[indx].equals(mdc.getBioSubType())) {
									attr = DataProviderConstants.schemaNames[indx];
									break;
								}
							}
							if (attr != null) {
								for (String a : filteredAttribs) {
									if (a.equals(attr)) {
										bFound = true;
										break;
									}
								}
							}
							if (!bFound)
								lstToRemove.add(mdc);
						}
						lstFingers.removeAll(lstToRemove);
					}

				}
			}

			catch (Throwable t) {
				logger.error("Finger get capture fail" + contextKey, t);
				return null;
			}

			try {

				if (bioExceptions != null && !bioExceptions.isEmpty()) {

					List<MDSDevice> exceptionfaceDevices = mds
							.getRegDeviceInfo(DataProviderConstants.MDS_DEVICE_TYPE_FACE);
					MDSDevice exceptionDevice = exceptionfaceDevices.get(0);
					try {
						capture = mds.captureFromRegDevice(exceptionDevice, capture,
								DataProviderConstants.MDS_DEVICE_TYPE_FACE, null, 60,
								exceptionDevice.getDeviceSubId().get(0), port, contextKey, bioexceptionlist);

					} catch (Throwable t) {
						logger.error("Exception photo capture failure" + contextKey, t);
						return null;
					}

				}

			}

			catch (Throwable t) {
				logger.error("Exceptionphoto face capture", t);
				return null;
			}
			return capture;
		} finally {
			cleanupMdsResources(mds, port, contextKey);
		}
	}

	private static void cleanupMdsResources(MDSClientInterface mds, int port, String contextKey) {
		try {
			if (mds != null && port > 0) {
				mds.setProfile("Default", port, contextKey);
			}
		} catch (Throwable t) {
			logger.warn("Failed to reset MDS profile for context {}", contextKey, t);
		} finally {
			MdsPortRegistry.remove(contextKey);
			try {
				synchronized (SBI_MAP_LOCK) {
					CentralizedMockSBI.stopSBI(contextKey);
				}
			} catch (Throwable t) {
				logger.warn("Failed to stop SBI for context {}", contextKey, t);
			}
		}
	}

	static void copyExceptionPhoto(
			String basePath,
			String sourceFolderName,
			String targetFolderName) throws IOException {

		Path sourceFile = Paths.get(
				basePath,
				sourceFolderName + "/Registration/",
				"Exception_Photo.iso");

		Path targetDir = Paths.get(basePath, targetFolderName + "/Registration/");
		Path targetFile = targetDir.resolve("Exception_Photo.iso");

		if (!Files.exists(sourceFile)) {
			throw new IllegalArgumentException(
					"Exception_Photo.iso not found in source folder: " + sourceFile);
		}

		if (!Files.exists(targetDir)) {
			Files.createDirectories(targetDir);
		}

		Files.copy(
				sourceFile,
				targetFile,
				StandardCopyOption.REPLACE_EXISTING);
	}
}
