package org.mosip.dataprovider.util;

public class DataProviderConstants {

	public static int Age_Adult_Min_Age = 18;
	public static int Age_Minor_Max_Age = 5;
	public static int Age_Senior_Citizen_Min_Age = 60;

	//contacts formatting firstname.surname.{randomnumber}@mailinator.com
	public static String email_format = "%s.%s.%d@mailinator.com";
	//mob number - excluding contry code
	public static int mobNumber_prefix[] = {9,8,7,6};
	public static int mobNumber_maxlen = 10;
	
	public static int MAX_PHOTOS = 10;
	public static String RESOURCE="src/main/resource/";
	public static String COUNTRY_CODE ="IN";
	public static String DOC_TEMPLATE_PATH=  "documents/templates/";
	public static String LANG_CODE_ENGLISH = "eng";
	public static String ANGULI_PATH ="C:\\Mosip.io\\gitrepos\\biometric-data\\anguli"	;
}
