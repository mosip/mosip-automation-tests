package org.mosip.dataprovider.models;
import java.io.Serializable;
import lombok.Data;
@Data
public class SchemaRule  implements Serializable {

	 private static final long serialVersionUID = 1L;
	 
	 String engine;
	 String expr;

}
