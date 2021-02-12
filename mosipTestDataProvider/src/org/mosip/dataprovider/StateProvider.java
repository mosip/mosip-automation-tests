package org.mosip.dataprovider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.mosip.dataprovider.models.CountryModel;
import org.mosip.dataprovider.models.StateModel;
import org.mosip.dataprovider.util.CommonUtil;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StateProvider extends LocationProviderBase{

	List<StateModel> stateDetail;
	
	public List<StateModel> getDetail() {
		return stateDetail;
	}
	public void dump() throws IOException {
		String strData = client.get("/Continentscountriescities_Subdivisions_States_Provinces?limit=100000" , null);
		ObjectMapper objectMapper = new ObjectMapper();
		
		JsonNode actualObj = objectMapper.readTree(strData);
		JsonNode values = actualObj.get("results");

		stateDetail = objectMapper.readValue(values.toString(), 
				objectMapper.getTypeFactory().constructCollectionType(List.class, StateModel.class));

		Hashtable<String, List<StateModel>> stateList = new Hashtable<String, List<StateModel>>();
		for(StateModel s: stateDetail) {
			 List<StateModel> states = stateList.get(s.getCountryCode());
			if(states == null) {
				states = new ArrayList<StateModel>();
				stateList.put(s.getCountryCode(), states);
			}
			states.add(s);
		}
		ObjectMapper Obj = new ObjectMapper();
		stateList.forEach( (countryCode, states) -> {
			String path = "resource/locations/" + countryCode + "/states.json";
			
			
			try {
				FileWriter myWriter = new FileWriter(path );
				myWriter.write( Obj.writeValueAsString(states));
				myWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
		});
	}
	public static List<StateModel> load(String countryIsoCode) throws JsonParseException, JsonMappingException, IOException{
	
		String strJson = CommonUtil.readFromJSONFile("resource/locations/"+ countryIsoCode + "/states.json");
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(strJson.toString(), 
				objectMapper.getTypeFactory().constructCollectionType(List.class, StateModel.class));
	
	}
	public void generate(CountryModel country) {
		try {
			String condn = "{" +
		            "    \"country\": {" +
		            "        \"__type\": \"Pointer\"," +
		            "        \"className\": \"Continentscountriescities_Country\"," +
		            "        \"objectId\": \"" + country.getObjectId() + "\","+
		            "    }" +
		            "}";
			
			String where = URLEncoder.encode(condn, "utf-8");
			
			String strData = client.get("/Continentscountriescities_Subdivisions_States_Provinces?limit=10&where="+ where , null);
			ObjectMapper objectMapper = new ObjectMapper();
			
			JsonNode actualObj = objectMapper.readTree(strData);
			JsonNode values = actualObj.get("results");

			stateDetail = objectMapper.readValue(values.toString(), 
					objectMapper.getTypeFactory().constructCollectionType(List.class, StateModel.class));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
