package io.mosip.ivv.preregistration.methods;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.CallRecord;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.preregistration.utils.Helpers;
import io.mosip.testscripts.SimplePostForAutoGenId;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class SendOTP2 extends BaseStep implements StepInterface {

    @Override
    public void run() {
        //RequestDataDTO requestData = prepare();
        //ResponseDataDTO responseData = call(requestData);
        //process(responseData);
    	setUp();
    	
    }
    //@Test
    public void setUp() {
    	String fileName="src/main/resources/createPrereg/createPrereg.yml";
    	//Path path = Paths.get("src/test/resources/createPrereg/createPrereg.yml");
    	//SimplePostForAutoGenId post= new SimplePostForAutoGenId();
    //	Object[] testCaseList = post.getYmlTestData(fileName);
    	//System.out.println(testCaseList.length);
    	
    	
    	SendOTP2 app = new SendOTP2();

        // read all files from a resources folder
        try {

            // files from src/main/resources/json
            List<File> result = app.getAllFilesFromResource("createPrereg/createPrereg.yml");
            for (File file : result) {
                System.out.println("file : " + file);
                SimplePostForAutoGenId post= new SimplePostForAutoGenId();
               	Object[] testCaseList = post.getYmlTestData(file.getAbsolutePath());
                System.out.println(testCaseList.length);
               // printFile(file);
            }

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    	
    }
    
    
    private List<File> getAllFilesFromResource(String folder)
            throws URISyntaxException, IOException {

            ClassLoader classLoader = getClass().getClassLoader();

            URL resource = classLoader.getResource(folder);

            // dun walk the root path, we will walk all the classes
            List<File> collect = Files.walk(Paths.get(resource.toURI()))
                    .filter(Files::isRegularFile)
                    .map(x -> x.toFile())
                    .collect(Collectors.toList());

            return collect;
        }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public RequestDataDTO prepare(){
    	
        JSONObject request_json = new JSONObject();
        request_json.put("userId", store.getCurrentPerson().getUserid());

        JSONObject requestData = new JSONObject();
        requestData.put("id", "mosip.pre-registration.login.sendotp");
        requestData.put("version", "1.0");
        requestData.put("requesttime", Utils.getCurrentDateAndTimeForAPI());
        requestData.put("request", request_json);

        String url = "/preregistration/" + System.getProperty("ivv.prereg.version") + "/login/sendOtp";
        return new RequestDataDTO(url, requestData.toJSONString());
    }

    public ResponseDataDTO call(RequestDataDTO data){
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        Response responseData = (Response) given()
                .contentType(ContentType.JSON).body(data.getRequest()).post(data.getUrl());
        this.callRecord = new CallRecord(RestAssured.baseURI+data.getUrl(), "POST", data.getRequest(), responseData);
        Helpers.logCallRecord(this.callRecord);
        return new ResponseDataDTO(responseData.getStatusCode(), responseData.getBody().asString(), responseData.getCookies());
    }

    public void process(ResponseDataDTO res){

    }

}