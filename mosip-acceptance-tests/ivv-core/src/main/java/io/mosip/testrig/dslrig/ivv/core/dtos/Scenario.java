package io.mosip.testrig.dslrig.ivv.core.dtos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import io.mosip.testrig.dslrig.ivv.core.policies.AssertionPolicy;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Scenario {
    private String id = "";
    private String description = "";
    private ArrayList<String> tags = new ArrayList();
    private String personaClass, groupName;
    private ArrayList<Step.modules> modules = new ArrayList();
    
    private HashMap<String,String> variables = new HashMap<String,String>();
    private Map<String, Object> objectVariables = new HashMap<String, Object>();

    private HashMap<String, String> residentTemplatePaths = new LinkedHashMap<String, String>();
    private HashMap<String, String> residentPathsPrid = new LinkedHashMap<String, String>();
    private HashMap<String, String> templatePacketPath = new LinkedHashMap<String, String>();
    private HashMap<String, String> manualVerificationRid = new LinkedHashMap<String, String>();
    private Properties residentPersonaIdPro=new Properties();
    private HashMap<String, String> pridsAndRids=new LinkedHashMap<String, String>();
    private HashMap<String, String> uinReqIds = new LinkedHashMap<String, String>();
    private  List<String> generatedResidentData =new ArrayList<>();
    private  String templatPath_updateResident=null;
    private  String rid_updateResident=null;
    private  String uin_updateResident=null;
    private  String prid_updateResident=null;
   // private  List<String> resDataPathList= new LinkedList();

    private  HashMap<String, String> ridPersonaPath=new LinkedHashMap<String, String>();


    private  Properties vidPersonaProp=new Properties();

    private  Properties oidcPmsProp=new Properties();
    
    private  Properties appointmentDate=new Properties();
	
    private  HashMap<String, String> residentPathGuardianRid = null;
	
  //  private  HashMap<String, String> contextKey=new HashMap<String, String>();
    private  HashMap<String, String> currentStep=new HashMap<String, String>();
	
    private  Properties uinPersonaProp=new Properties();
    private  Properties handlePersonaProp=new Properties();
    private  Properties oidcClientProp=new Properties();
    private  String prid=null;
    private  String statusCode=null;
    @Getter
    @Setter
    public static class Step
    {
        public enum modules {
        	e2e,pr, rc, rp, ia, kr, mt
        }
        private String name = ""; // needs to be passed
        private String variant = "DEFAULT"; // default
        private modules module;
        private ArrayList<Assert> asserts;
        private ArrayList<Error> errors;
        private int AssertionPolicy = 0; // default
        private boolean FailExpected = false; //default
        private ArrayList<String> parameters;
        private ArrayList<Integer> index;
        private String outVarName=null;
        private Scenario scenario=null;

        
        
        
        public static class Error{
            public String code;
        }

        public static class Assert{
            public AssertionPolicy type;
            public ArrayList<String> parameters = new ArrayList<>();
        }

        public Step(){

        }

        public Step(String name, String variant, ArrayList<Assert> asserts, ArrayList<Error> errors, ArrayList<String> parameters, ArrayList<Integer> index)
        {
            this.name = name;
            this.variant = variant;
            this.asserts = asserts;
            this.errors = errors;
            this.parameters = parameters;
            this.index = index;
        }
    }

    private Persona persona;
    private ArrayList<RegistrationUser> registrationUsers;
    private ArrayList<Partner> partners;
    private Person user;
    private List<Step> steps = new ArrayList<Step>();
    private boolean continueOnFailure = false; // default
    private boolean isFailureExpected = false; // default

}
