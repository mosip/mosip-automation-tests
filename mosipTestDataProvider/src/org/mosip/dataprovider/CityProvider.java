package org.mosip.dataprovider;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.mosip.dataprovider.models.CityModel;
import org.mosip.dataprovider.util.CommonUtil;
import org.mosip.dataprovider.util.DataProviderConstants;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CityProvider extends LocationProviderBase {

	public CityProvider() {
		super();
		
	}
	List<CityModel> cityDetail;


	public List<CityModel> getDetail() {
		return cityDetail;
	}
	public void dump() {
		//https://parseapi.back4app.com/classes/Continentscountriescities_City?limit=10&excludeKeys=population,adminCode
		try {
			Hashtable<String,String> lookupTbl = CountryProvider.getCountryLookup();
			
			String strData = client.get("/Continentscountriescities_City?limit=100000&excludeKeys=population,adminCode", null);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			JsonNode actualObj = objectMapper.readTree(strData);
			JsonNode values = actualObj.get("results");

			cityDetail = objectMapper.readValue(values.toString(), 
					objectMapper.getTypeFactory().constructCollectionType(List.class, CityModel.class));
			
			Hashtable<String, List<CityModel>> cityList = new Hashtable<String, List<CityModel>>();
			
			for(CityModel s: cityDetail) {
				 List<CityModel> cities = cityList.get(s.getCountry().getObjectId());
				if(cities == null) {
					cities = new ArrayList<CityModel>();
					cityList.put(s.getCountry().getObjectId(), cities);
				}
				cities.add(s);
			}
			ObjectMapper Obj = new ObjectMapper();
			
			cityList.forEach( (countryObjectId, cities) -> {
				String countryCode = lookupTbl.get(countryObjectId);
				
				String path = DataProviderConstants.RESOURCE + "locations/" + countryCode + "/cities.json";
				
				
				try {
					FileWriter myWriter = new FileWriter(path );
					myWriter.write( Obj.writeValueAsString(cities));
					myWriter.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    
			});


		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	public static List<CityModel> load(String countryIsoCode) throws JsonParseException, JsonMappingException, IOException{
		
		String strJson = CommonUtil.readFromJSONFile(DataProviderConstants.RESOURCE +"locations/"+ countryIsoCode + "/cities.json");
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(strJson.toString(), 
				objectMapper.getTypeFactory().constructCollectionType(List.class, CityModel.class));
		
	
	}
	public void generate(String countryIsoCode, String stateIsoCode) {
		try {
			String strData = client.get("/countries/"+ countryIsoCode +"/states/" + stateIsoCode +"/cities", null);
			ObjectMapper objectMapper = new ObjectMapper();
			cityDetail = objectMapper.readValue(strData, 
					objectMapper.getTypeFactory().constructCollectionType(List.class, CityModel.class));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}


}
