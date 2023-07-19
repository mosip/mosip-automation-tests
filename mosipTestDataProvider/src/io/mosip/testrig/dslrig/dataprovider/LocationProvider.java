package io.mosip.testrig.dslrig.dataprovider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.testrig.dslrig.dataprovider.models.ApplicationConfigIdSchema;
import io.mosip.testrig.dslrig.dataprovider.models.CityModel;
import io.mosip.testrig.dslrig.dataprovider.models.CountryModel;
import io.mosip.testrig.dslrig.dataprovider.models.Location;
import io.mosip.testrig.dslrig.dataprovider.models.LocationHierarchyModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipLocationModel;
import io.mosip.testrig.dslrig.dataprovider.models.StateModel;
import io.mosip.testrig.dslrig.dataprovider.preparation.MosipMasterData;
import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;

public class LocationProvider {
	private static Random rand = new Random();
	private static final Logger logger = LoggerFactory.getLogger(LocationProvider.class);

	public static ApplicationConfigIdSchema generate( String langCode, int count,String contextKey) {

		ApplicationConfigIdSchema ret = null;
		try {
			ret = MosipMasterData.getPreregLocHierarchy(langCode,count,contextKey);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return ret;
	}
	//modified version to get Location details as per configured Masterdata 
	public static Hashtable<String,List<MosipLocationModel>> generate_old(int count, String country,String contextKey) {
		
		Hashtable<String,List<MosipLocationModel>> tbl = new Hashtable<String,List<MosipLocationModel>>();

		HashMap<String,LocationHierarchyModel[]> locHi = MosipMasterData.getAllLocationHierarchies(contextKey);
		Set<String> langSet = locHi.keySet();
		langSet.forEach( (langcode) ->{
			List<MosipLocationModel> locations = new ArrayList<MosipLocationModel>();
			tbl.put(langcode, locations);
			int nLoop = 0;
			LocationHierarchyModel[] locHierachies =null;
			List<MosipLocationModel> rootlist = null;
		
			locHierachies = locHi.get(langcode);
			if(country != null) {
				for(LocationHierarchyModel m: locHierachies) {
					if(m.getIsActive()  && m.getHierarchyLevelName().toLowerCase().equals("country")) {
						rootlist = MosipMasterData.getLocationsByLevel(locHierachies[nLoop].getHierarchyLevelName(),contextKey);
						break;
					}
					nLoop++;	
				}
			}
			else
			do {
				if(locHierachies[nLoop].getIsActive())
					rootlist = MosipMasterData.getLocationsByLevel(locHierachies[nLoop].getHierarchyLevelName(),contextKey);
				nLoop++;
			}while(nLoop < locHierachies.length  && (rootlist == null || rootlist.isEmpty() ));
			
			if(rootlist != null && ! rootlist.isEmpty()) {
				MosipLocationModel rootLoc =null;
				List<MosipLocationModel> list =null;
				for( MosipLocationModel rlm: rootlist) {
					list = MosipMasterData.getImmedeateChildren(rlm.getCode(), langcode,contextKey );
					if(list != null && !list.isEmpty()) {
						rootLoc = rlm;	
						locations.add( rootLoc);
						break;
					}

				}
				if(rootLoc != null) {
					
					for(int i=1; i < locHierachies.length; i++) {
						//List<MosipLocationModel> list = MosipMasterData.getLocationsByLevel(locHierachies[i].getHierarchyLevelName());
						list = MosipMasterData.getImmedeateChildren(rootLoc.getCode(), langcode ,contextKey );
				
						if(list != null && !list.isEmpty()) {
							int pos = 0;//CommonUtil.generateRandomNumbers(1, list.size()-1, 0)[0];
							rootLoc = list.get(pos);
							locations.add( rootLoc );
						}
			
					}
				}
			}
		});
		
		return tbl;
	}
	public static List<Location> generateFromFile(String countryIsoCode,int count,String contextKey) {
		
		List<Location> locations = new ArrayList<Location>();
		CountryModel country;
		try {
			country = CountryProvider.load(countryIsoCode,contextKey);
			List<StateModel> states =StateProvider.load(country.getIso2());
			List<CityModel> cities = CityProvider.load(countryIsoCode,contextKey);

	
			int [] zipcode = CommonUtil.generateRandomNumbers(count, 99999, 1111);
					
			for(int i=0; i < count; i++) {
			
				Location location = new Location();
				location.setCountry(country.getName());
				int index =  rand.nextInt( states.size());
				location.setState(states.get(index).getName() );
			
				index =  rand.nextInt( cities.size());
				location.setCity( cities.get(index).getName());
				location.setZipcode(String.format("%05d",zipcode[i]));
				locations.add(location);
			}	
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		
		return locations;
	}
}
