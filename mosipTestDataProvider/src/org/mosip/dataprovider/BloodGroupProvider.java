package org.mosip.dataprovider;

import java.util.ArrayList;

import java.util.List;
import java.util.Random;

import org.mosip.dataprovider.models.DynamicFieldModel;
import org.mosip.dataprovider.models.DynamicFieldValueModel;

public class BloodGroupProvider {
	static String [] bloodGroups = { "A+","A-","B+","B-","O+","O-","AB+","AB-"};
	
	public static List<DynamicFieldValueModel> generate(int count, List<DynamicFieldModel> dynaFields) {
		DynamicFieldModel bgModel = null;
		List<DynamicFieldValueModel> bgs =null;
		for(DynamicFieldModel fm: dynaFields) {
			if(fm.getName().contains("blood")) {
				bgModel = fm;
				break;
			}
				
		}
		if(bgModel !=null) {
			bgs = new ArrayList<DynamicFieldValueModel>();
			Random rand = new Random();
			for( int i=0; i < count; i++) {
				int idx = rand.nextInt(bgModel.getFieldVal().size());
				bgs.add( bgModel.getFieldVal().get(idx));
			}
		}
		return bgs;
	}
}
