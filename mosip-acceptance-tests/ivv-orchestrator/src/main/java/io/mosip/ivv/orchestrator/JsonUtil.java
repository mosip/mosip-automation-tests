package io.mosip.ivv.orchestrator;


import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
    private static final org.slf4j.Logger logger= org.slf4j.LoggerFactory.getLogger(JsonUtil.class);

    private static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
    }

    public static String convertJavaToJson(Object object) {

        String jsonResult = "";
        try {
            jsonResult = mapper.writeValueAsString(object);
        } catch (JsonParseException e) {
            logger.error("", e);
        } catch (JsonMappingException e) {
            logger.error("", e);
        } catch (IOException e) {
            logger.error("", e);
        }
        return jsonResult;
    }

    public static <T> T convertJsonintoJava(String jsonString, Class<T> cls) {
        T payload = null;
        try {
            payload = mapper.readValue(jsonString, cls);
        } catch (JsonParseException e) {
            logger.error("", e);
        } catch (JsonMappingException e) {
            logger.error("", e);
        } catch (IOException e) {
            logger.error("", e);
        }
        return payload;
    }

}
