package io.mosip.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;
import javax.ws.rs.core.MediaType;
import org.testng.Reporter;
import io.mosip.authentication.fw.util.RestClient;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

public class Packetsync extends BaseTestCaseUtil implements StepInterface {

	@Override
	public void run() throws RigInternalError {
		for (String packetPath : templatePacketPath.values())
			packetUtility.packetSync(packetPath, contextKey);
	}

	/*
	 * private void packetsync(String packetPath) throws RigInternalError { String
	 * url = baseUrl + props.getProperty("packetsyncUrl") + "?path=" + packetPath;
	 * Reporter.log("<pre> <b>PACKETSYNC: </b> <br/>"+url + "</pre>"); //Response
	 * apiResponse = RestClient.getRequest(url, MediaType.APPLICATION_JSON,
	 * MediaType.APPLICATION_JSON); Response apiResponse =
	 * given().contentType(ContentType.JSON).queryParams(contextKey)
	 * .log().all().when().get(url).then().log().all().extract().response();
	 * Reporter.log("<b><u>Actual Response Content: </u></b><pre>"+
	 * apiResponse.getBody().asString() + "</pre>");
	 * //assertTrue(apiResponse.getBody().asString().
	 * contains("Packet has reached Packet Receiver"
	 * ),"Unable to do sync packet from packet utility");
	 * if(!apiResponse.getBody().asString().
	 * contains("Packet has reached Packet Receiver")) throw new
	 * RigInternalError("Unable to do sync packet from packet utility"); }
	 */
}
