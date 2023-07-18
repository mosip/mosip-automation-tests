package io.mosip.testrig.dslrig.dataprovider.models;

import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import lombok.Data;
@Data
public class ContextSchemaDetail {

	Hashtable<Double, Properties> allSchema ;
	Double schemaVersion;
	List<MosipIDSchema> schema ;
	List<String> requiredAttribs;
	
	
}
