package io.mosip.ivv.e2e.constant;

public class E2EConstants{
	private E2EConstants() {}
	public static final String APPROVED_SUPERVISOR_STATUS = "APPROVED";
	public static final String REJECTED_SUPERVISOR_STATUS = "REJECTED";
	public static final String LOST_PROCESS = "LOST";
	public static final String NEW_PROCESS = "NEW";
	public static final String SOURCE = "REGISTRATION_CLIENT";
	//Ridsync
	public static final String RIDSYNC = "Ridsync";
	public static final String RIDSYNCURL = "ridsyncUrl";
	public static final String REGISTRATIONID = "registrationId";
	//UploadDocuments
	public static final String PERSONAFILEPATH = "personaFilePath";
	public static final String UPLOAD_DOCUMENTS = "Upload Documents";
	//PreRegister
	public static final String SENDOTPURL = "sendOtpUrl";
	public static final String SEND_OTP = "Send Otp";
	public static final String VERIFYOTPURL = "verifyOtpUrl";
	public static final String PREREGISTERURL = "preregisterUrl";
	public static final String ADDAPPLICATION = "AddApplication";
	//Packetsync
	public static final String PACKETSYNCURL = "packetsyncUrl";
	//Packetcreator
	public static final String PACKETCRETORURL = "packetCretorUrl";
	public static final String CREATEPACKET = "CreatePacket";
	public static final String REPLACETO = "\\\\";
	public static final String REPLACEFROM = "\\\\\\\\";

	public static String MACHINE_ID= "machine_id";
	public static String CENTER_ID= "center_id";
	public static String USER_ID= "user_id";
	public static String USER_PASSWD = "user_passwd";
	public static String SUPERVISOR_ID = "supervisor_id";
	public static String PRECONFIGURED_OTP="preconfigured_otp";

	//BioMetric Constant
	public static final String LEFT_EYE = "iris_encrypted.left";
	public static final String RIGHT_EYE = "iris_encrypted.right";

	public static final String RIGHT_INDEX = "Right IndexFinger";
	public static final String RIGHT_LITTLE = "Right LittleFinger";
	public static final String RIGHT_RING = "Right RingFinger";
	public static final String RIGHT_MIDDLE = "Right MiddleFinger";
	public static final String LEFT_INDEX = "Left IndexFinger";
	public static final String LEFT_LITTLE = "Left LittleFinger";
	public static final String LEFT_RING = "Left RingFinger";
	public static final String LEFT_MIDDLE = "Left MiddleFinger";
	public static final String LEFT_THUMB = "Left Thumb";
	public static final String RIGHT_THUMB = "Right Thumb";

	public static final String FACEFETCH = "face_encrypted";
	public static final String IRISFETCH = "iris_encrypted";
	public static final String FINGERFETCH = "finger_encrypted";

	public static final String DEMOFETCH = "demodata";
	public static final String FACEBIOTYPE = "FACE";
	public static final String IRISBIOTYPE = "Iris";
	public static final String IRISBIOTYPE_LEFT = "Left";
	public static final String IRISBIOTYPE_RIGHT = "Right";
	public static final String FINGERBIOTYPE = "Finger";
	public static final String LEFT_RINGFINGER = "Left RingFinger";
	public static final String RIGHT_RINGFINGER = "Right RingFinger";

	public static final String DEMONAME="name";
	public static final String DEMOFNAME="firstName";
	public static final String DEMOMNAME="midName";
	public static final String DEMOLNAME="lastName";
	public static final String DEMOGENDER="gender";
	public static final String DEMODOB="dob";
	public static final String DEMOYMLPHONE="phone";

	public static final String DEMOPHONE="mobileNumber";
	public static final String DEMOEMAIL="emailId";
	public static final String DEMOAGE="age";
	public static final String DEMOADDRESSFETCH="address";

	public static final String DEMOFULLADDRESS="fullAddress";
	public static final String DEMOADDRESSLINE1="addressLine1";
	public static final String DEMOADDRESSLINE2="addressLine2";
	public static final String DEMOADDRESSLINE3="addressLine3";
	public static final String CONTEXTKEY="contextKey";

	//Pre-Registration booking details
	public static final String APPOINTMENT_DATE = "appointment_date";
	public static final String PRE_REGISTRATION_ID = "pre_registration_id";
	public static final String REGISTRATION_CENTER_ID = "registration_center_id";
	public static final String TIME_SLOT_FROM = "time_slot_from";
	public static final String TIME_SLOT_TO = "time_slot_to";
	
	// server concurrency check
	public static final String FREE = "free";
	public static final String INUSE = "inUse";
	public static final String ALLOCATE = "allocate";
	public static final String DE_ALLOCATE = "deallocate";
	
	
	//Activate/DeActivate [machine/operator/regCenter/device/partner]
	public static final String MACHINE = "machine";
	public static final String OPERATOR = "operator";
	public static final String PARTNER = "partner";
	public static final String REGCENTER = "regcenter";
	public static final String DEVICE = "device";
	public static final String DEVICElTS = "devicelts";
	
	//to fetch hashValue from personapath
	public static final String FACEHASHFETCH = "face_hash";
	public static final String IRISHASHFETCH = "iris_hash";
	public static final String FINGERHASHFETCH = "finger_hash";
	
	
	
	public static final String LEFT_EYE_HASH = "iris_hash.left";
	public static final String RIGHT_EYE_HASH = "iris_hash.right";
	
	//check status api   processed
	public static final String PROCESSED = "processed";
	public static final String REJECTED = "rejected";
	public static final String REJECTED_MSG = "REJECTED - PLEASE VISIT THE NEAREST CENTER FOR DETAILS.";
	public static final String FAILED = "failed";
	public static final String FAILED_MSG = "FAILED - PLEASE VISIT THE NEAREST CENTER FOR DETAILS.";
	public static final String UNDER_PROCESSING_MSG = "UNDER PROCESSING - PLEASE CHECK BACK AGAIN LATER.";
	
	


}
