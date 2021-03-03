package org.mosip.dataprovider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mosip.dataprovider.models.DynamicFieldModel;

import org.mosip.dataprovider.models.MosipDocument;
import org.mosip.dataprovider.models.MosipIDSchema;
import org.mosip.dataprovider.models.MosipLocationModel;
import org.mosip.dataprovider.models.ResidentModel;
import org.mosip.dataprovider.preparation.MosipMasterData;
import org.mosip.dataprovider.util.CommonUtil;
import org.mosip.dataprovider.util.DataProviderConstants;
import org.mosip.dataprovider.util.Translator;


import io.cucumber.core.gherkin.messages.internal.gherkin.internal.com.eclipsesource.json.Json;
import variables.VariableManager;

/*
 * Generate Packet structure for a given Resident record
 */
public class PacketTemplateProvider {

	static HashMap<String, String[]> fileInfo = new HashMap<String, String[]>();
	
	public static String RID_FOLDER = "rid_id";
	public static String RID_EVIDENCE = "rid_evidence";
	public static String RID_OPTIONAL = "rid_optional";

	Hashtable<Double,List<MosipIDSchema>> allSchema = MosipMasterData.getIDSchemaLatestVersion();
	
	Double schemaVersion = allSchema.keys().nextElement();
	List<MosipIDSchema> schema = allSchema.get(schemaVersion  );
	
	public  void getSchema() {
		allSchema = MosipMasterData.getIDSchemaLatestVersion();
		schemaVersion = allSchema.keys().nextElement();
		schema = allSchema.get(schemaVersion  );
	}
	//generate un encrypted template
	public  void generate(String source, String process, ResidentModel resident, String packetFilePath) throws IOException {
		
		String rootFolder = packetFilePath;
		String ridFolder ="";
		Path path = Paths.get(rootFolder);

		getSchema();

        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }
        String sourceFolder = rootFolder + File.separator + source.toUpperCase(); 
        path = Paths.get(sourceFolder );
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }
        String processFolder = sourceFolder + File.separator + process.toUpperCase();
        path = Paths.get(processFolder );
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }
        String nextFolder = processFolder + File.separator + RID_FOLDER;
        ridFolder = nextFolder;
		fileInfo.put(RID_FOLDER, (new String [] {ridFolder,"",""}));

        path = Paths.get(nextFolder );
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }
        nextFolder = processFolder + File.separator + RID_EVIDENCE;
        String rid_evidence_folder = nextFolder;
        fileInfo.put(RID_EVIDENCE, (new String [] {rid_evidence_folder,"",""}));

        path = Paths.get(nextFolder );
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }
        nextFolder = processFolder + File.separator + RID_OPTIONAL;
        String rid_optional_folder  = nextFolder;
        path = Paths.get(nextFolder );
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }
        String idJson = generateIDJson(resident, fileInfo);
        Files.write(Paths.get(ridFolder+"/ID.json"), idJson.getBytes() );
        String metadataJson = generateMetaDataJson(resident, fileInfo);
        Files.write(Paths.get(ridFolder+"/packet_meta_info.json"), metadataJson.getBytes() );
        
        //Generate evidence json
       
        String evidenceJson = generateEvidenceJson(resident, fileInfo);
        Files.write(Paths.get(rid_evidence_folder+"/ID.json"), evidenceJson.getBytes() );
        Files.write(Paths.get(rid_evidence_folder+"/packet_meta_info.json"), metadataJson.getBytes() );
        
        //copy the dummy jsons to optional also
        
        Files.write(Paths.get(rid_optional_folder+"/ID.json"), evidenceJson.getBytes() );
        Files.write(Paths.get(rid_optional_folder+"/packet_meta_info.json"), metadataJson.getBytes() );
        
       idJson =  genRID_PacketTypeJson(source,process, "id");
       Files.write(Paths.get(processFolder + File.separator +"/rid_id.json"), idJson.getBytes() );
       idJson =  genRID_PacketTypeJson(source,process, "evidence");
       Files.write(Paths.get(processFolder + File.separator +"/rid_evidence.json"), idJson.getBytes() );
       idJson =  genRID_PacketTypeJson(source,process, "optional");
       Files.write(Paths.get(processFolder + File.separator +"/rid_optional.json"), idJson.getBytes() );
       
       
	}
	 String generateEvidenceJson(ResidentModel resident, HashMap<String, String[]> fileInfo) {
		
		JSONObject identity = new JSONObject();
		for(MosipIDSchema s: schema) {
			if(s.getFieldCategory().equals("evidence") && ( s.getInputRequired() || s.getRequired()) ) {
				
				if(s.getType().equals("documentType") ) {
					
					//	String docType = s.getSubType();
						int index = 0;
						for(MosipDocument doc: resident.getDocuments()) {
							index = 0;
							if(doc.getDocCategoryCode().toLowerCase().equals(s.getSubType().toLowerCase())) {
								index = CommonUtil.generateRandomNumbers(1, doc.getDocs().size()-1, 0)[0];
								String docFile = doc.getDocs().get(index);
								System.out.println("docFIle=" + docFile + " dType="+ s.getSubType() + " cat=" + s.getId());
								
								JSONObject o = new JSONObject();
								o.put("format", "pdf");
								o.put("type", doc.getType().get(index).getCode());
								String [] v = fileInfo.get(RID_EVIDENCE);
								v[1] =s.getId() +".pdf";
								fileInfo.put(RID_EVIDENCE, v);
								o.put("value",s.getId());
								
								identity.put(s.getId(), o);
								
								String outFile = fileInfo.get(RID_EVIDENCE)[0] +"/" + fileInfo.get(RID_EVIDENCE)[1];
					        	try {
					        		Files.copy(Paths.get(docFile), Paths.get(outFile));
									
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} 
								break;	
							}
							
						}
						continue;
				}
				else
				if(s.getId().toLowerCase().equals("uin") ) {
					if(resident.getUIN() == null || resident.getUIN().equals(""))
						identity.put(s.getId(), JSONObject.NULL);
					else
						identity.put(s.getId(), resident.getUIN());
					continue;
				}
				else
				if(s.getGroup().equals("GuardianDetails") && s.getId().toLowerCase().matches(".*guard.*uin") ) {
					if(resident.isMinor()) {
						if(resident.getGuardian().getUIN() == null || resident.getGuardian().getUIN().equals("")) {
							//skip if null
							//	identity.put(s.getId(), JSONObject.NULL);
						}
						else
							identity.put(s.getId(),resident.getGuardian().getUIN());
					}
					continue;
				}
				else
				if(s.getGroup().equals("GuardianDetails") && s.getId().toLowerCase().matches(".*guard.*rid") ) {
						if(resident.isMinor()) {
							if(resident.getGuardian() != null && (resident.getGuardian().getRID() == null || resident.getGuardian().getRID().equals(""))) {
								//As per MONO, skip
								//identity.put(s.getId(), JSONObject.NULL);
							}
							else
								identity.put(s.getId(),resident.getGuardian().getRID());
						}
						continue;
				}
				else
				if(s.getGroup().equals("GuardianDetails") && s.getId().toLowerCase().matches(".*guard.*name") ) {
					if(resident.isMinor()) {
						String primValue = "";
						String secValue = "";
						JSONArray arr = new JSONArray();
						JSONObject o = new JSONObject();
						o.put("language", resident.getPrimaryLanguage());
						String value = null;
						if(resident.getGuardian() != null)
							primValue = resident.getGuardian().getName().getFirstName();
						
						if(resident.getSecondaryLanguage() != null) {
							if(resident.getGuardian() != null && resident.getGuardian().getName_seclang() != null)
								secValue = resident.getGuardian().getName_seclang().getFirstName();
						}

						updateSimpleType(s.getId(),identity,primValue, secValue, resident.getPrimaryLanguage(),
								resident.getSecondaryLanguage(), resident.getThirdLanguage());

					}
					continue;
				}
				if(s.getType().equals("simpleType")) {
					
					updateSimpleType(s.getId(),identity,"", "", resident.getPrimaryLanguage(),
							resident.getSecondaryLanguage(), resident.getThirdLanguage());

				}
				else
				{
						identity.put(s.getId(), JSONObject.NULL);
				}
			}
		}
		JSONObject retObject = new JSONObject();
		retObject.put("identity", identity);
		return retObject.toString();
		
	}
	 JSONObject constructExceptnNode(String type, String missingBio) {
		JSONObject	node = new JSONObject();
		node.put("type", type);
		node.put("missingBiometric", missingBio);
		node.put("reason", "Temporary");
		node.put("exceptionType", "Temporary");
		node.put("individualType", "applicant");
		return node;
	 }
	 JSONObject constructBioMetaNode() {
		 JSONObject	node = new JSONObject();
		 node.put("numRetry", 1);
		 node.put("forceCaptured", false);
		 node.put("birindex", "4c099c1f-4fb2-4de3-8a2f-928f79430e9b");
		return node;
	 }
	 JSONObject constructBioMetaData(ResidentModel resident, JSONObject identity) {
		
		 List<String> lstAttr = resident.getFilteredBioAttribtures();
		 if(lstAttr != null) {
			JSONObject biometrics= new JSONObject();
			JSONObject applicant = new JSONObject();

			for(String n: DataProviderConstants.schemaNames) {					
				if(lstAttr.contains(n)) {
					applicant.put(n, constructBioMetaNode());
				}	
			}
		
			biometrics.put("introducer",new JSONObject());
			biometrics.put("applicant-auth",new JSONObject());
				
			biometrics.put("applicant",applicant);
			identity.put("biometrics", biometrics);
		
		 }
		 return identity;
	 }
	 JSONObject constructBioException(ResidentModel resident, JSONObject identity) {
			//update biometric exceptions
		List<String> missedAttrib =  resident.getMissAttributes();
		if(missedAttrib != null) {
			JSONObject exceptionBiometrics= new JSONObject();
			JSONObject applicant = new JSONObject();

	if(missedAttrib.contains("leftEye")) {
				applicant.put("leftEye", constructExceptnNode("Iris","leftEye"));
			}
			if(missedAttrib.contains("rightEye")) {
				applicant.put("rightEye", constructExceptnNode("Iris","rightEye"));
			}
			for(String n: DataProviderConstants.schemaNames) {
				if(n.contains("Eye") )
					continue;
				if( n.equals("face"))
					applicant.put(n, constructExceptnNode("Face",n));
					
				if(missedAttrib.contains(n)) {
					applicant.put(n, constructExceptnNode("Finger",n));
				}	
			}
			exceptionBiometrics.put("introducer",new JSONObject());
			exceptionBiometrics.put("applicant-auth",new JSONObject());
				
			exceptionBiometrics.put("applicant",applicant);
			identity.put("exceptionBiometrics", exceptionBiometrics);
				
		}
		return identity;
	 }
	 JSONObject updateSimpleType(String id,JSONObject identity, String primValue, String secValue, String primLang, String secLang, String thirdLang) {
		 
		 if( (secValue == null || secValue.equals("")) && secLang != null && !secLang.equals(""))
			 secValue =  Translator.translate(secLang,  primValue);
		 
		 String thirdValue = "";
		 if(  thirdLang != null && !thirdLang.equals(""))
			 thirdValue =  Translator.translate(thirdLang,  primValue);
		 	
			//array
			JSONArray ar = new JSONArray();
			JSONObject o = new JSONObject();
			o.put("language",primLang);
			if(primValue.equals(""))
				o.put("value", Json.NULL);
			else
			o.put("value", primValue); 
			ar.put(o);
			if(secLang != null) {
				o = new JSONObject();
				o.put("language",secLang);
				if(secValue.equals(""))
					o.put("value", Json.NULL);
				else
					o.put("value", secValue); 
				ar.put(o);
			}
			if(thirdLang != null) {
				o = new JSONObject();
				o.put("language",thirdLang);
				if(thirdValue.equals(""))
					o.put("value", Json.NULL);
				else
					o.put("value", thirdValue); 
				ar.put(o);
			}
			
			identity.put(id, ar);
			return identity;
	 }
	/*
	 * HashMap<FolderType, [(in)folderPath][(out)biofilename]> fileInfo
	 */
	String generateIDJson(ResidentModel resident, HashMap<String, String[]> fileInfo) {

		String idjson="";

		JSONObject identity = new JSONObject();
		
		String primaryLanguage = resident.getPrimaryLanguage();
		String secLanguage = resident.getSecondaryLanguage();
		Hashtable<String, MosipLocationModel> locations =   resident.getLocation();
		Hashtable<String, MosipLocationModel> locations_seclang =   resident.getLocation_seclang();
		
		Set<String> locationSet =  locations.keySet();
		
		Set<String> locationSet_sec =  null;
		if(locations_seclang != null)
			locationSet_sec = locations_seclang.keySet();
		List<String> lstMissedAttributes = resident.getMissAttributes();
		
		for(MosipIDSchema s: schema) {
			System.out.println(s.toJSONString());
			if(lstMissedAttributes != null && lstMissedAttributes.stream().anyMatch( v -> v.equalsIgnoreCase(s.getId()))) {
				continue;
			}
			if(s.getId().equalsIgnoreCase("idschemaVersion")) {
				identity.put(s.getId(), schemaVersion);
				continue;
			}
			if(!s.getRequired() && !s.getInputRequired()) {
				continue;
			}
		
			if(s.getFieldCategory().equals("pvt") || s.getFieldCategory().equals("kyc"))
			{
				String primaryValue = "";
				String secValue  ="";
				//System.out.println(s.toJSONString());
				if(s.getRequired() || s.getInputRequired()) {
				/*	if(s.getId().equalsIgnoreCase("idschemaVersion")) {
						
						primaryValue = schemaVersion.toString();	
					}
					else */
					if(s.getId().toLowerCase().equals("fullname")) {
						primaryValue = resident.getName().getFirstName() +" " + resident.getName().getMidName()+ " "+ resident.getName().getSurName();
						if(secLanguage != null)
							secValue = resident.getName_seclang().getFirstName() +" "+ resident.getName_seclang().getMidName() + " " + resident.getName_seclang().getSurName();
					}
					else
					if(s.getId().toLowerCase().equals("firstname")) {
						primaryValue = resident.getName().getFirstName() ;
						if(secLanguage != null)
							secValue = resident.getName_seclang().getFirstName();
					}
					else
					if(s.getId().toLowerCase().equals("lastname") || s.getId().toLowerCase().equals("surname")) {
						primaryValue = resident.getName().getSurName();
						if(secLanguage != null)
							secValue = resident.getName_seclang().getSurName();
					}
					else
					if(s.getId().toLowerCase().equals("middlename") || s.getId().toLowerCase().equals("midname")) {
						primaryValue = resident.getName().getMidName();
						if(secLanguage != null)
							secValue = resident.getName_seclang().getMidName();
					}
					else
					if(s.getId().toLowerCase().equals("dateofbirth") ||s.getId().toLowerCase().equals("dob") || s.getId().toLowerCase().equals("birthdate") ) {
						primaryValue = resident.getDob();
						secValue = primaryValue;
					}
					else
					if(s.getId().toLowerCase().equals("gender")){
						primaryValue ="Female";
						if(resident.getGender().equals("Male"))
							primaryValue = "Male";
						secValue = primaryValue;
					}
					else
					if(s.getId().toLowerCase().contains("address") && !s.getGroup().toLowerCase().equals("documents")) {
						primaryValue = "#111, 127th Main, " + s.getId();
						if(secLanguage != null)
							secValue = Translator.translate(secLanguage,primaryValue);
						
					}
					else
					if(s.getId().toLowerCase().contains("residen")  ) {
						primaryValue = resident.getResidentStatus().getCode() ;
						secValue = primaryValue;
					}
					else
					if(s.getId().toLowerCase().contains("phone") || s.getId().toLowerCase().contains("mobile") ) {
						primaryValue =  resident.getContact().getMobileNumber();
					}
					else
					if(s.getId().toLowerCase().contains("email") || s.getId().toLowerCase().contains("mail") ) {
						primaryValue =  resident.getContact().getEmailId();
					}
					else
					if(s.getId().toLowerCase().contains("referenceIdentity") ) {
						primaryValue = resident.getId();
					}
					else
					if(s.getId().toLowerCase().contains("blood") ) {
						primaryValue = resident.getBloodgroup().getCode();
						secValue = primaryValue;	
					}	
					else
					if(s.getType().equals("biometricsType") && ( s.getGroup() !=null && s.getGroup().equals("Biometrics")) &&
							s.getId().toLowerCase().contains("individual") ) {
						JSONObject o = new JSONObject();
						o.put("format", "cbeff");
						o.put("version", 1.0f);
						String [] v = fileInfo.get(RID_FOLDER);
						v[1] =s.getId() +"_bio_CBEFF.xml";
						fileInfo.put(RID_FOLDER, v);
						o.put("value",s.getId() +"_bio_CBEFF");
						//o.put("value","individualBiometrics_bio_CBEFF");
						identity.put(s.getId(), o);
						
						String outFile = fileInfo.get(RID_FOLDER)[0] +"/" + fileInfo.get(RID_FOLDER)[1];
			        	try {
			        		List<String> missAttribs =  resident.getMissAttributes();
			        		List<String> bioAttrib = s.getBioAttributes();
			        		if(missAttribs != null && !missAttribs.isEmpty())
			        			bioAttrib.removeAll(missAttribs);
			        		if(resident.getFilteredBioAttribtures() == null)
			        			resident.setFilteredBioAttribtures(bioAttrib);
			        		
							BiometricDataProvider.toCBEFF(bioAttrib, resident.getBiometric(), outFile);
							//test code- hard coded cbeff file
						//	Files.copy(Paths.get("C:\\temp\\test\\individualBiometrics_bio_CBEFF.xml"),Paths.get(outFile));
						//	Files.copy(Paths.get("C:\\temp\\test\\ID.json"),Paths.get(RID_FOLDER + "ID.json"));
							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 
						continue;
					}
					else
					if(s.getType().equals("biometricsType") &&
							( s.getGroup() !=null && s.getGroup().equals("Biometrics"))
							&& s.getSubType().equals("introducer") ) {
					
						if(resident.isMinor() && resident.getGuardian() != null) {
							JSONObject o = new JSONObject();
							o.put("format", "cbeff");
							o.put("version", 1.0f);
							String [] v = fileInfo.get(RID_FOLDER);
							v[2] =s.getId() +"_bio_CBEFF.xml";
							fileInfo.put(RID_FOLDER, v);
							o.put("value",s.getId() +"_bio_CBEFF");
							//o.put("value","individualBiometrics_bio_CBEFF");
							identity.put(s.getId(), o);
							
							String outFile = fileInfo.get(RID_FOLDER)[0] +"/" + v[2];
				        	try {
				        		//Implement excetpions by parsing 'Miss' list
				        		List<String> missAttribs =  resident.getMissAttributes();
				        		List<String> bioAttrib = s.getBioAttributes();
				        		if(missAttribs != null && !missAttribs.isEmpty())
				        			bioAttrib.removeAll(missAttribs);
								BiometricDataProvider.toCBEFF(bioAttrib, resident.getGuardian().getBiometric(), outFile);

							
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} 
						
						}
						continue;
					}
					else
					if(s.getType().equals("biometricsType") 
							//&& ( s.getGroup() !=null && s.getGroup().equals("Biometrics")) 
							//&& s.getId().toLowerCase().contains("parent")
							) {
						//identity.put(s.getId(), JSONObject.NULL);
						continue;
					}
					else
					if(s.getType().equals("documentType") ) {
							
					//	String docType = s.getSubType();
						int index = 0;
						for(MosipDocument doc: resident.getDocuments()) {
							index = 0;
							if(doc.getDocCategoryCode().toLowerCase().equals(s.getSubType().toLowerCase())) {
								index = CommonUtil.generateRandomNumbers(1, doc.getDocs().size()-1, 0)[0];
								String docFile = doc.getDocs().get(index);
								System.out.println("docFIle=" + docFile + " dType="+ s.getSubType() + " cat=" + s.getId());
								
								JSONObject o = new JSONObject();
								o.put("format", "pdf");
								o.put("type", doc.getType().get(index).getCode());
								String [] v = fileInfo.get(RID_FOLDER);
								v[1] =s.getId() +".pdf";
								fileInfo.put(RID_FOLDER, v);
								o.put("value",s.getId());
								
								identity.put(s.getId(), o);
								
								String outFile = fileInfo.get(RID_FOLDER)[0] +"/" + fileInfo.get(RID_FOLDER)[1];
					        	try {
					        		Files.copy(Paths.get(docFile), Paths.get(outFile));
									
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} 
								break;	
							}
							
						}
						continue;
					}
					else
					if(s.getSubType().equals("parentOrGuardianName") || s.getSubType().equals("introducer") ||
							(s.getGroup() != null && s.getGroup().equals("GuardianDetails")) ){
						if(resident.getGuardian() != null) {

							//(JSONObject identity, String primValue, String secValue, String primLang, String secLang, String thirdLang) {
							String primValue = null;
							String secGValue = null;
							if(resident.getGuardian() != null)
								primValue = resident.getGuardian().getName().getFirstName();
							if(resident.getGuardian() != null && resident.getGuardian().getName_seclang() != null)
								secGValue = resident.getGuardian().getName_seclang().getFirstName();
									 
							updateSimpleType(s.getId(),identity, primValue, secGValue, resident.getPrimaryLanguage(),
									resident.getSecondaryLanguage(), resident.getThirdLanguage());


						}
					}
					else
					if(s.getInputRequired() && s.getId().contains("IdentityNumber") ) {

						int []r = CommonUtil.generateRandomNumbers(2, 99999, 11111);
						
						primaryValue = String.format("%d%d", r[0],r[1]);
						identity.put(s.getId(), primaryValue);

					}
					else
					{
						//System.out.println("SchemaID:"+ s.getId());
						for(DynamicFieldModel dfm: resident.getDynaFields()) {
							//System.out.println("dfm:"+ dfm.getName());
							if(s.getId().toLowerCase().endsWith(dfm.getName().toLowerCase())){
								primaryValue = dfm.getFieldVal().get(0).getValue();
								break;
							}
						}	
					}
					for(String locKey:locationSet) {
						MosipLocationModel locModel = locations.get(locKey);
		
						if(s.getId().toLowerCase().endsWith(locModel.getHierarchyName().toLowerCase())  ) {
							primaryValue = locModel.getName();

							break;
						}
					}
					if(locations_seclang != null)			
					for(String locKey: locationSet_sec) {
						MosipLocationModel locModel = locations_seclang.get(locKey);
							
						if(s.getId().toLowerCase().endsWith(locModel.getHierarchyName().toLowerCase())  ) {
							secValue = locModel.getName();
							break;
						}
					}
					
					if(s.getType().equals("simpleType") 
							//&& !s.getId().toLowerCase().equals("postalcode")
							) {

						updateSimpleType(s.getId(),identity, primaryValue, secValue, primaryLanguage,
								secLanguage, resident.getThirdLanguage());

					}
					else
					{
						if(s.getRequired() && (primaryValue == null || primaryValue.equals(""))) {
							primaryValue =  new Random().nextLong() +"";
						}
						if(primaryValue.equals("") ) //&& !s.getType().equals("string"))
							identity.put(s.getId(), JSONObject.NULL);
						else
							identity.put(s.getId(), primaryValue);
					}
				}
			}
		}
		JSONObject retObject = new JSONObject();
		retObject.put("identity", identity);
		idjson = retObject.toString();
		return idjson;
	}
	String generateMetaDataJson(ResidentModel resident, HashMap<String, String[]> fileInfo) {


		String templateMetaJsonPath = VariableManager.getVariableValue("templateIDMeta").toString().trim();
		
		String templateIdentityStr = CommonUtil.readFromJSONFile(templateMetaJsonPath);
		JSONObject templateIdentity = new JSONObject(templateIdentityStr).getJSONObject("identity");
		JSONObject identity = new JSONObject();
		JSONArray docArray = new JSONArray();

		for(MosipIDSchema s: schema) {
			if(s.getType().equals("documentType") ) {
				int index = 0;
				for(MosipDocument doc: resident.getDocuments()) {
					index = 0;
					if(doc.getDocCategoryCode().toLowerCase().equals(s.getSubType().toLowerCase())) {
						index = CommonUtil.generateRandomNumbers(1, doc.getDocs().size()-1, 0)[0];
						String docFile = doc.getDocs().get(index);
						System.out.println("docFIle=" + docFile + " dType="+ s.getSubType() + " cat=" + s.getId());
							
						JSONObject o = new JSONObject();
						o.put("documentCategory", doc.getDocCategoryCode());
						o.put("documentType", doc.getType().get(index).getCode());
						o.put("documentName",s.getId());
						o.put("documentOwner", "Applicant");
						o.put("refNumber" , JSONObject.NULL);
						docArray.put(o);		
							 
						break;	
					}
						
				}
				continue;
			}
		}
		identity.put("documents", docArray);
		identity.put("metaData", new JSONArray());
		identity.put("capturedRegisteredDevices", new JSONArray());
		identity.put("exceptionBiometrics", new JSONArray());
		identity.put("creationDate", CommonUtil.getUTCDateTime(null));
		//identity.put("capturedRegisteredDevices",templateIdentity.getJSONArray("capturedRegisteredDevices") );
		
		constructBioException(resident,identity);
		constructBioMetaData(resident, identity);
		identity.put("operationsData",templateIdentity.getJSONArray("operationsData") );
		
		/*
		JSONArray opData = new JSONArray();
		JSONObject o = new JSONObject();
		o.put("label", "officerId");
		o.put("value", "");
		opData.put(o);
		o = new JSONObject();
		o.put("label", "officerBiometricFileName");
		o.put("value", "");
		opData.put(o);
	
		o = new JSONObject();
		o.put("label", "supervisorId");
		o.put("value", "");
		opData.put(o);
		
		o = new JSONObject();
		o.put("label", "supervisorBiometricFileName");
		o.put("value", "");
		opData.put(o);
		
		o = new JSONObject();
		o.put("label", "supervisorBiometricFileName");
		o.put("value", "");
		opData.put(o);
		
		o = new JSONObject();
		o.put("label", "supervisorPassword");
		o.put("value", true);
		opData.put(o);

		o = new JSONObject();
		o.put("label", "officerPassword");
		o.put("value", true);
		opData.put(o);

		o = new JSONObject();
		o.put("label", "supervisorPIN");
		o.put("value", "");
		opData.put(o);
		
		o = new JSONObject();
		o.put("label", "officerPIN");
		o.put("value", "");
		opData.put(o);
		o = new JSONObject();
		o.put("label", "supervisorOTPAuthentication");
		o.put("value", "");
		opData.put(o);
		
		o = new JSONObject();
		o.put("label", "officerOTPAuthentication");
		o.put("value", "");
		opData.put(o);
		
		identity.put("operationsData", opData);
		*/
		JSONObject retObject = new JSONObject();
		retObject.put("identity", identity);
		return retObject.toString();
	}
	
	String genRID_PacketTypeJson(String src, String process, String packetType) {
	
		 
		JSONObject retObject = new JSONObject();
		retObject.put("process", process.toUpperCase());
		retObject.put("source", src.toUpperCase());
		retObject.put("creationdate",  CommonUtil.getUTCDateTime(null));
		retObject.put("providerversion",  "v1.0");
		retObject.put("schemaversion",  schemaVersion);
		retObject.put("encryptedhash",  "");
		retObject.put("signature",  "");
		retObject.put("id",  "");
		retObject.put("packetname",  "id_"+ packetType);
		retObject.put("signature",  "");
		retObject.put("providername",  "PacketWriterImpl");
			
		return retObject.toString();
		
		
	}
	public static void main(String[] args) {

		ResidentDataProvider provider = new ResidentDataProvider();
		List<ResidentModel> residents = provider.generate();
		try {
			new PacketTemplateProvider().generate("registration_client","new", residents.get(0), "/temp//newpacket");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(generateIDJson(residents.get(0)));

	}

}
