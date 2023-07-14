package io.mosip.ivv.dg;

import java.util.ArrayList;

import io.mosip.ivv.core.dtos.Persona;
import io.mosip.ivv.core.dtos.Scenario;

public interface DataGeneratorInterface {
    ArrayList<Scenario> prepareScenarios(ArrayList<Scenario> scenarios, ArrayList<Persona> personas);
}
