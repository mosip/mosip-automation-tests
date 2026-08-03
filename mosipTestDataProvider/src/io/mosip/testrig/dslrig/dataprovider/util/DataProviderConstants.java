package io.mosip.testrig.dslrig.dataprovider.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.mosip.testrig.dslrig.dataprovider.models.BioModality;

public class DataProviderConstants {

	public static final int DEFAULT_ABIS_DELAY = 3;
	public static final Object INDIVIDUAL_TYPE = "residenceStatus";


	public static String email_format = "%s.%s.%d@mailinator.com";

	public static int mobNumber_prefix[] = {9,8,7,6};
	public static int mobNumber_maxlen = 10;

	public static int MAX_PHOTOS = 10;
	private static final ThreadLocal<String> RESOURCE_TL = ThreadLocal.withInitial(() -> "src/main/resource/");

	/** Thread-scoped resource root so parallel packet-creator requests do not clobber each other. */
	public static String getResource() {
		return RESOURCE_TL.get();
	}

	public static void setResource(String path) {
		RESOURCE_TL.set(path == null || path.isBlank() ? "src/main/resource/" : path);
	}

	/** Removes the thread-local override so a reused servlet worker thread does not leak it into the next request. */
	public static void clearResource() {
		RESOURCE_TL.remove();
	}

	/** @deprecated use {@link #getResource()} / {@link #setResource(String)} */
	public static String RESOURCE = "src/main/resource/";
	public static String COUNTRY_CODE ="IN";
	public static String DOC_TEMPLATE_PATH=  "documents/templates/";
	public static String LANG_CODE_ENGLISH = "eng";

	public static String [] schemaNames = {
			"leftThumb",
			"leftIndex",
			"leftMiddle",
			"leftRing",
			"leftLittle",
			"rightThumb",
			"rightIndex",
			"rightMiddle",
			"rightRing",
			"rightLittle",
			"leftEye",
			"rightEye",
			"face"
	};

	public static String [] displayFullName = {
			"Left Thumb",
			"Left IndexFinger",
			"Left MiddleFinger",
			"Left RingFinger",
			"Left LittleFinger",
			"Right Thumb",
			"Right IndexFinger",
			"Right MiddleFinger",
			"Right RingFinger",
			"Right LittleFinger",
			"Left",
			"Right",
			"Face"
	};

	public static String [] schemaFingerNames = {
			"leftThumb",
			"leftIndex",
			"leftMiddle",
			"leftRing",
			"leftLittle",
			"rightThumb",
			"rightIndex",
			"rightMiddle",
			"rightRing",
			"rightLittle"

	};
	public static String [] MDSProfileFingerNames = {
			"Left_Thumb",
			"Left_Index",
			"Left_Middle",
			"Left_Ring",
			"Left_Little",
			"Right_Thumb",
			"Right_Index",
			"Right_Middle",
			"Right_Ring",
			"Right_Little"

	};
	public static String [] displayFingerName = {
			"Left Thumb",
			"Left IndexFinger",
			"Left MiddleFinger",
			"Left RingFinger",
			"Left LittleFinger",
			"Right Thumb",
			"Right IndexFinger",
			"Right MiddleFinger",
			"Right RingFinger",
			"Right LittleFinger"
	};

	public static String MDS_DEVICE_TYPE_FINGER="Finger";
	public static String MDS_DEVICE_TYPE_IRIS="Iris";
	public static String MDS_DEVICE_TYPE_FACE="Face";
	public static String MDS_DEVICE_TYPE_EXCEPTION_PHOTO="ExceptionPhoto";
	public static int MAX_ADDRESS_LINES = 5;

	public static String getschemaName(String name)
	{

		for(int i=0; i < 13; i++) {
			String displayFingerName = DataProviderConstants.displayFullName[i];
			if (displayFingerName.equalsIgnoreCase(name) == true)
				return  DataProviderConstants.schemaNames[i];
		}


		return name;
	}

	public static List<String> getListWithoutExceptions(List<BioModality> exceptionlst,List<String> bioFilter)
	{
		List<String> listWithoutExceptions =bioFilter;

	if(exceptionlst!=null && !exceptionlst.isEmpty()) {
		List<String> exceptions = exceptionlst.stream().map(BioModality::getSubType).collect(Collectors.toList());
		List<String> schemaName=new ArrayList<String>();
		for(String ex: exceptions)
		{
			schemaName.add(getschemaName(ex));
		}
		listWithoutExceptions= bioFilter.stream().filter(bioAttribute -> !schemaName.contains(bioAttribute)).collect(Collectors.toList());
	}
	return listWithoutExceptions;
	}
}
