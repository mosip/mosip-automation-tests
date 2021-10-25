package org.mosip.dataprovider.models;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.json.JSONObject;
//import org.apache.commons.lang3.tuple.Pair;
import org.mosip.dataprovider.util.CommonUtil;
import org.mosip.dataprovider.util.Gender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.module.SimpleModule;

import lombok.Data;

@Data
public class ResidentModel  implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String id;
	private String primaryLanguage;
	private String secondaryLanguage;	
	private String thirdLanguage;
	//private String gender;
	//private String gender_seclang;
	//private MosipGenderModel gender;
	//private MosipGenderModel gender_seclang;
	//private MosipGenderModel gender_thirdlang;
	private Gender gender;
	private String dob;
	private boolean minor;
	private DynamicFieldValueModel bloodgroup;
	//private List<MosipLocationModel> location;
	private Hashtable<String, MosipLocationModel> location;
	
	//private List<MosipLocationModel> location_seclang;
	private Hashtable<String, MosipLocationModel> location_seclang;
	ApplicationConfigIdSchema appConfigIdSchema;
	ApplicationConfigIdSchema appConfigIdSchema_secLang;
	
	private Contact contact;
	private Name name;
	private Name name_seclang;
	private MosipIndividualTypeModel residentStatus;
	private MosipIndividualTypeModel residentStatus_seclang;

	private String[] address;
	private String[] address_seclang;
	
	//if minor set guardian
	private ResidentModel guardian;
	
	private BiometricDataModel biometric;
	
	private DynamicFieldValueModel maritalStatus;
	
	private Hashtable<String,List<DynamicFieldModel>> dynaFields;
	private List<MosipDocument> documents;
	private String UIN;
	private String RID;
	
	//Language specific genderTypes
	private Hashtable<String, List<MosipGenderModel>> genderTypes ;
	
	private List<String> missAttributes;
	private List<String> invalidAttributes;
	private MosipIdentity identity;
	private List<String> filteredBioAttribtures;
	private List<BioModality> bioExceptions;
	
	private String path;
	private Hashtable<String,Integer> docIndexes;
	//resident Metadata
	//private double schemaVersion;
	//private String targetCotext;
	private Hashtable<String,String> addtionalAttributes;
	
	private Boolean skipFinger;
	private Boolean skipFace;
	private Boolean skipIris;
	
	public ResidentModel() {
	
		//id = String.format("%04d", CommonUtil.generateRandomNumbers(1,99999, 1000)[0]);
	//ID must be atleast 12 characters
		//id = id + id + id;
		int [] r = CommonUtil.generateRandomNumbers(2, 99999, 11111);
		id = String.format("%d%d", r[0],r[1]);
		docIndexes = new Hashtable<String,Integer>();
		addtionalAttributes =new Hashtable<String,String>();
		genderTypes = new Hashtable<String, List<MosipGenderModel>>(); 
	}

	public String toJSONString() {
		
		ObjectMapper mapper = new ObjectMapper();
	//	mapper.getFactory().configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), true);
		
		String jsonStr ="";
		try {
				jsonStr = mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
				
				e.printStackTrace();
		}	
		return jsonStr;
	}
	public static ResidentModel readPersona(String filePath) throws IOException {
    	
    	ObjectMapper mapper = new ObjectMapper();
    	//mapper.registerModule(new SimpleModule().addDeserializer(Pair.class,new PairDeserializer()));
    //	mapper.registerModule(new SimpleModule().addSerializer(Pair.class, new PairSerializer()));
    	byte[] bytes = Files.readAllBytes(Paths.get(filePath));
    	ResidentModel model = mapper.readValue(bytes, ResidentModel.class);
		model.setPath(filePath);
		return model;
    }
	public void writePersona(String filePath) throws IOException {
		Files.write(Paths.get(filePath), this.toJSONString().getBytes());
	}
	public void save() throws IOException {
		Files.write(Paths.get(path), this.toJSONString().getBytes());
	}
	public static void main(String [] args) {
		
		ResidentModel model  = new ResidentModel();
		Name name = new Name();
		name.setFirstName("abcd ’'` efg");
		model.setName(name);
		System.out.println(model.toJSONString());

		try {
			Files.write(Paths.get("test.json"), model.toJSONString().getBytes());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
    	ObjectMapper mapper = new ObjectMapper();

		try {
			byte[] bytes = Files.readAllBytes(Paths.get("test.json"));
			ResidentModel m = mapper.readValue(model.toJSONString().getBytes(), ResidentModel.class);
			System.out.println(m.getName().getFirstName());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}			
    
	public JSONObject loadDemoData() {
		JSONObject demodata = new JSONObject();
		demodata.put("id", id);
		demodata.put("firstName", name.getFirstName());
		demodata.put("midName", name.getMidName());
		demodata.put("lastName", name.getSurName());
		demodata.put("dob", dob);
		demodata.put("gender", gender);
		demodata.put("UIN", UIN);
		demodata.put("RID", RID);
		demodata.put("emailId", contact.getEmailId());
		demodata.put("mobileNumber", contact.getMobileNumber());
		demodata.put("residenceNumber", contact.getResidenceNumber());
		
		return demodata;
	}

}
