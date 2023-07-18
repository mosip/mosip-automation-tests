package io.mosip.testrig.dslrig.ivv.dg;

import java.util.ArrayList;

import io.mosip.testrig.dslrig.ivv.core.dtos.Persona;
import io.mosip.testrig.dslrig.ivv.core.dtos.Scenario;

public interface DataGeneratorInterface {
    ArrayList<Scenario> prepareScenarios(ArrayList<Scenario> scenarios, ArrayList<Persona> personas);
}
