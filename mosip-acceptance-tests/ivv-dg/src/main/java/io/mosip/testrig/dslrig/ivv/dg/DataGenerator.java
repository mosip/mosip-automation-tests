package io.mosip.testrig.dslrig.ivv.dg;

import java.util.ArrayList;

import io.mosip.testrig.dslrig.ivv.core.dtos.Persona;
import io.mosip.testrig.dslrig.ivv.core.dtos.Scenario;
import io.mosip.testrig.dslrig.ivv.dg.Utils.MutationEngine;
import io.mosip.testrig.dslrig.ivv.dg.exceptions.PersonaNotFoundException;

public class DataGenerator implements DataGeneratorInterface {


    public ArrayList<Scenario> prepareScenarios(ArrayList<Scenario> scenarios, ArrayList<Persona> personas) {
        ArrayList<Scenario> generatedScenarios = new ArrayList<>();
        for (Scenario scenario : scenarios){
            scenario.setPersona(addPersonaData(scenario, personas));
            generatedScenarios.add(scenario);
        }
        return generatedScenarios;
    }

    private Persona addPersonaData(Scenario scenario, ArrayList<Persona> personas){
        for (Persona persona : personas) {
            if(scenario.getGroupName() != null && !scenario.getGroupName().isEmpty()
                    && persona.getPersonaClass().equals(scenario.getPersonaClass())
                    && persona.getGroupName().equals(scenario.getGroupName())){
                return persona;
            }
        }
        for (Persona persona : personas) {
            if(persona.getPersonaClass().equals(scenario.getPersonaClass())){
                return new MutationEngine().mutatePersona(persona);
            }
        }
        throw new PersonaNotFoundException("Persona not found");
    }

}
