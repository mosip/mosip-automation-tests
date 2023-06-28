package io.mosip.ivv.e2e.methods;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClonePersonaAndUpdate extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(ClonePersonaAndUpdate.class);

	@SuppressWarnings("unchecked")
	@Override
	public void run() throws RigInternalError {
		String id = null;
		String originalPersonaPath = null;
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Method Type[POST/GET/PUT/PATCH] parameter is  missing from DSL step");
			this.hasError=true;throw new RigInternalError(
					"Method Type[POST/GET/PUT/PATCH] parameter is  missing from DSL step: " + step.getName());
		}
		if (step.getParameters().size() >= 2 && step.getParameters().get(0).startsWith("$$")) {
			id = step.getParameters().get(0);
			if (id.startsWith("$$")) {
				originalPersonaPath = step.getScenario().getVariables().get(id);
				String[] arr = step.getParameters().get(1).split("@@");
				ObjectMapper mapper = new ObjectMapper();
				File source = new File(originalPersonaPath);
				try {
					Map<String,Object> root = mapper.readValue(source, Map.class);
					System.out.println(root);
					Map<String, String> jsonNamefirstlang = (Map<String, String>) root.get("name");
					
					Map<String, String> jsonNameseclang = (Map<String, String>) root.get("name_seclang");
					logger.info("Before jsonNamefirstlang " +jsonNamefirstlang);
					logger.info("Before jsonNameseclang " +jsonNameseclang);
				

					if(jsonNamefirstlang!=null) {
					for (String list : arr)
						jsonNamefirstlang.put(list, jsonNamefirstlang.get(list).toString().substring(0,
								jsonNamefirstlang.get(list).toString().length() - 2));
					}
					if(jsonNameseclang!=null) {
					for (String list : arr)
						jsonNameseclang.put(list, jsonNameseclang.get(list).toString().substring(0,
								jsonNameseclang.get(list).toString().length() - 2));
					}
					
					logger.info("After jsonNamefirstlang " +jsonNamefirstlang);
					logger.info("After jsonNameseclang " +jsonNameseclang);
				
					try (FileWriter file = new FileWriter(source,Charset.forName("utf-8"))) {
						String serializedJsonString = mapper.writeValueAsString(root);
						file.write(serializedJsonString);
						file.flush();
						System.out.println("Successfully updated json object to file...!!");
					}
				} catch (JsonParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonMappingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}
}