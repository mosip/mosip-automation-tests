package io.mosip.testrig.dslrig.packetcreator.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SchemaUtil {

    private static final ConcurrentHashMap<String, String> SCHEMA_MEMORY_CACHE = new ConcurrentHashMap<>();

    /** Fixed-size lock striping keyed by schema file path hash; avoids FILE_LOCKS growing per distinct path. */
    private static final int FILE_LOCK_STRIPES = 32;
    private static final Object[] FILE_LOCKS = new Object[FILE_LOCK_STRIPES];
    static {
        for (int i = 0; i < FILE_LOCK_STRIPES; i++) {
            FILE_LOCKS[i] = new Object();
        }
    }

    private static Object fileLockFor(String path) {
        return FILE_LOCKS[Math.floorMod(path.hashCode(), FILE_LOCK_STRIPES)];
    }

    /** Drops cached schema JSON scoped to {@code contextKey}; call on context reset so long-running or
     *  repeatedly reset environments don't retain schema JSON for contexts that no longer exist. */
    public static void invalidateContext(String contextKey) {
        if (contextKey == null) {
            return;
        }
        String prefix = contextKey + "|";
        SCHEMA_MEMORY_CACHE.keySet().removeIf(key -> key.startsWith(prefix));
    }

    public static final String PROPERTIES = "properties";
    public static final String IDENTITY = "identity";
    public static final String SCHEMA_CATEGORY = "fieldCategory";
    public static final String SCHEMA_VERSION_QUERY_PARAM = "schemaVersion";
    public static final String ID_VERSION = "IDSchemaVersion";

    @Value("${mosip.test.masterdata.idschemaapi}")
    private String schemaUrl;

    @Autowired
    private APIRequestUtil apiRequestUtil;

    @Value("#{${mosip.test.packet.mapping.id}}")
    private List<String> id_categories;

    @Value("#{${mosip.test.packet.mapping.evidence}}")
    private List<String> evidence_categories;

    @Value("#{${mosip.test.packet.mapping.optional}}")
    private List<String> optional_categories;

    @Value("${mosip.test.baseurl}")
    private String baseUrl;

    @Autowired
    private ContextUtils contextUtils;

    public String getAndSaveSchema(String version, String workFolder, String contextKey) throws Exception{

    	String effectiveBaseUrl = baseUrl;
    	String effectiveSchemaUrl = schemaUrl == null ? "" : schemaUrl.trim();
    	if(contextKey != null && !contextKey.equals("")) {

    		Properties props = contextUtils.loadServerContext(contextKey);
    		String ctxBaseUrl = props.getProperty("mosip.test.baseurl");
    		if (ctxBaseUrl != null && !ctxBaseUrl.isBlank()) {
    			effectiveBaseUrl = ctxBaseUrl.trim();
    		}
    	}
        Path schemaFileLocation = Path.of(workFolder, "v"+version+".json");
        String memoryKey = contextKey + "|v" + version;
        Object fileLock = fileLockFor(schemaFileLocation.toString());
        final String baseUrlForLoad = effectiveBaseUrl;
        final String schemaUrlForLoad = effectiveSchemaUrl;
        try {
            return SCHEMA_MEMORY_CACHE.computeIfAbsent(memoryKey, k -> {
                try {
                    synchronized (fileLock) {
                        if (schemaFileLocation.toFile().exists()) {
                            return readSchemaAsString(schemaFileLocation.toFile().getAbsolutePath());
                        }
                        JSONObject queryParams = new JSONObject();
                        queryParams.put(SCHEMA_VERSION_QUERY_PARAM, Double.valueOf(version));
                        JSONObject response = apiRequestUtil.get(baseUrlForLoad, baseUrlForLoad + schemaUrlForLoad,
                                queryParams, new JSONObject(), contextKey);
                        try (FileOutputStream fos = new FileOutputStream(schemaFileLocation.toString())) {
                            String schemaData = response.getString("schemaJson");
                            fos.write(schemaData.getBytes());
                            fos.flush();
                        }
                        return readSchemaAsString(schemaFileLocation.toFile().getAbsolutePath());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw e;
        }
    }

    public String readSchemaAsString(String file) throws IOException{
        return Files.readString(Path.of(file));
    }

    public String getIdSchemaVersion(String dataFile) {
        JSONObject data = new JSONObject(dataFile);
        data = data.getJSONObject(IDENTITY);
        return data.getString(ID_VERSION);
    }


    public JSONObject getPacketIDData(String schemaJson, String data, String category) {
        switch (category.toLowerCase()) {
            case "id":
                return getPacketIDData(schemaJson, id_categories, data);
            case "evidence":
                return getPacketIDData(schemaJson, evidence_categories, data);
            case "optional":
                return getPacketIDData(schemaJson, optional_categories, data);
            default:
                return getPacketIDData(schemaJson, id_categories, data);
        }
    }

    private JSONObject getPacketIDData(String schemaJson, List<String> categories, String dataFile) {
        JSONObject identity = new JSONObject();

        JSONObject data = new JSONObject(dataFile);
        data = data.getJSONObject(IDENTITY);

        schemaJson =schemaJson.replaceAll(Pattern.quote("\\\""), "\"");
        JSONObject schema = new JSONObject(schemaJson);
        schema = schema.getJSONObject(PROPERTIES);
        schema = schema.getJSONObject(IDENTITY);
        schema = schema.getJSONObject(PROPERTIES);

        JSONArray fieldNames = data.names();
        for (int i = 0; i < fieldNames.length(); i++) {
            String fieldName = fieldNames.getString(i);
            JSONObject fieldDetail = schema.has(fieldName) ? schema.getJSONObject(fieldName) : new JSONObject();
            String fieldCategory = fieldDetail.has(SCHEMA_CATEGORY) ? fieldDetail.getString(SCHEMA_CATEGORY) : "none";
            if(categories.contains(fieldCategory.toLowerCase())){
                identity.put(fieldName, data.get(fieldName));
            }
        }
        JSONObject identityWrapper = new JSONObject();
        identityWrapper.put("identity", identity);
        return identityWrapper;
    }
}
