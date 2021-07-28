package io.mosip.test.packetcreator.mosippacketcreator.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mosip.dataprovider.preparation.MosipMasterData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SchemaManagerService {
    
    private static final Logger logger = LoggerFactory.getLogger(SchemaManagerService.class);

    public String modifySchema(int version, String id){

        JSONObject wrapper;
        JSONArray schema;
        try{
            
            wrapper = new JSONObject(getSchema());
            schema = wrapper.getJSONArray("schema");
            return MosipMasterData.postSchema(id, version, schema);

            
        }
        catch(Exception e){

            logger.error("modifySchema", e);
            return "{\"Failed\"}";
        }
        
    }

    public String getSchema(){

        String schema_str;

        try{
            schema_str = MosipMasterData.getIDschemaStringLatest();
            return schema_str;
        }
        catch(Exception e){

            logger.error("getSchema", e);
            return "{\"Failed\"}";
        }
    }

}
