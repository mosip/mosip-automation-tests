package io.mosip.ivv.parser;

import java.util.ArrayList;
import java.util.HashMap;

import io.mosip.ivv.core.dtos.Partner;
import io.mosip.ivv.core.dtos.Persona;
import io.mosip.ivv.core.dtos.RegistrationUser;
import io.mosip.ivv.core.dtos.Scenario;
import io.mosip.ivv.core.exceptions.RigInternalError;

public interface ParserInterface {

    ArrayList<Persona> getPersonas() throws RigInternalError;

    ArrayList<Scenario> getScenarios() throws RigInternalError;

    ArrayList<RegistrationUser> getRCUsers() throws RigInternalError;

    ArrayList<Partner> getPartners() throws RigInternalError;

    HashMap<String, String> getGlobals() throws RigInternalError;

    HashMap<String, String> getConfigs() throws RigInternalError;

}
