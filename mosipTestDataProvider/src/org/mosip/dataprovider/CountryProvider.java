package org.mosip.dataprovider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.mosip.dataprovider.models.CountryLookup;
import org.mosip.dataprovider.models.CountryModel;
import org.mosip.dataprovider.util.CommonUtil;
import org.mosip.dataprovider.util.DataProviderConstants;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class CountryProvider extends LocationProviderBase{

	
	CountryModel countryDetail;
	
	public CountryModel getDetail() {
		return countryDetail;
	}

	public static Hashtable<String,String> getCountryLookup(){
		
		Hashtable<String,String> tbl = new Hashtable<String,String>() ;
		List<CountryLookup> list =null ;
		try {
			String strJson = CommonUtil.readFromJSONFile(DataProviderConstants.RESOURCE+ "locations/countries.json");
		
			ObjectMapper objectMapper = new ObjectMapper();
			list = objectMapper.readValue(strJson, 
					objectMapper.getTypeFactory().constructCollectionType(List.class, CountryLookup.class));
			list.forEach( (v) ->{
				tbl.put(v.getObjectId(), v.getIso2());
			});
			
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		return tbl;
	}
	public  void dump() throws IOException {
		String strData = client.get("/Continentscountriescities_Country?limit=100000" , null);
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode actualObj = objectMapper.readTree(strData);

		JsonNode values = actualObj.get("results");
		List<CountryModel> countries = objectMapper.readValue(values.toString(), 
				objectMapper.getTypeFactory().constructCollectionType(List.class, CountryModel.class));
		File file = new File("resource/locations");
		if(!file.exists())
			file.mkdir();

		List<CountryLookup> clList = new ArrayList<CountryLookup>();
		
		ObjectMapper Obj = new ObjectMapper();
		for(CountryModel c: countries) {
			CountryLookup cl = new CountryLookup();
			cl.setIso2( c.getIso2());
			cl.setObjectId(c.getObjectId());
			clList.add(cl);
			
			String path = "resource/locations/" + c.getIso2();
			file = new File(path);
			if(!file.exists())
				file.mkdir();
			  FileWriter myWriter = new FileWriter(path + "/country.json");
			  
		      myWriter.write( Obj.writeValueAsString(c));
		      myWriter.close();
		}
		FileWriter myWriter = new FileWriter(DataProviderConstants.RESOURCE +"locations/countries.json");
	    myWriter.write( Obj.writeValueAsString(clList));
	    myWriter.close();
	}
	public static CountryModel load(String isoCode) throws JsonParseException, JsonMappingException, IOException {
	
		String strJson = CommonUtil.readFromJSONFile(DataProviderConstants.RESOURCE+"locations/"+ isoCode + "/country.json");
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(strJson, CountryModel.class);
		
	}
	public void generate(String isoCode) {
		try {
			//String strData = client.get("/countries/"+ isoCode, null);
			//India?excludeKeys=emoji,continent,shape"
			//String strData = client.get("/"+ isoCode +"?excludeKeys=emoji,continent,shape", null);
			String strData = client.get("/Continentscountriescities_Country" , null);
			
			
		//	+ "Continentscountriescities_Subdivisions_States_Provinces"; 
	
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode actualObj = objectMapper.readTree(strData);

			JsonNode values = actualObj.get("results");
			
			
			//countryDetail = objectMapper.readValue(strData, CountryModel.class);


			List<CountryModel> countries = objectMapper.readValue(values.toString(), 
					objectMapper.getTypeFactory().constructCollectionType(List.class, CountryModel.class));
			
			for(CountryModel c: countries) {
				if(c.getIso2().equals(isoCode)) {
					countryDetail = c;
					break;
				}
					
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args) {
		
		CountryProvider c = new CountryProvider();
		StateProvider s = new StateProvider();
		CityProvider city = new CityProvider();
		//try {
		//	c.dump();
		//	s.dump();
			city.dump();
			
		//} catch (IOException e) {
		//	e.printStackTrace();
		//}
		/*
		c.load("IN");
		CountryModel cm = c.getDetail();
		System.out.println(cm.getObjectId() + " name:"+ cm.getName());
	
		StateProvider stateProvider = new StateProvider();
		stateProvider.load(cm);
		List<StateModel> states = stateProvider.getDetail();
		states.forEach((s) ->{
			System.out.println(s.getName());
		});
		*/
	}
}
