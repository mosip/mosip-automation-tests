package io.mosip.ivv.dg;

import java.util.ArrayList;

import io.mosip.ivv.core.dtos.Persona;
import io.mosip.ivv.core.dtos.Scenario;
import io.mosip.ivv.dg.Utils.MutationEngine;
import io.mosip.ivv.dg.exceptions.PersonaNotFoundException;

public class DataGenerator implements DataGeneratorInterface {

//    @Override
//    public ArrayList<Scenario> getScenarios() {
//        return this.generatedScenarios;
//    }
//
//    @Override
//    public HashMap<String, String> getConfigs() {
//        return this.configs;
//    }
//
//    @Override
//    public HashMap<String, String> getGlobals() {
//        return this.globals;
//    }
//
//    public Person getPerson() {
//        Parser parser = new Parser(this.user_dir, this.config_file);
//        ArrayList<Persona> ps = addDataToPersonas(parser.getPersonas());
//        if(ps.size()>0 && ps.get(0).getPersons().size()>0){
//            return ps.get(0).getPersons().get(0);
//        }
//        return null;
//    }
//
//    public RegistrationUser getRegistrationUser() {
//        Parser parser = new Parser(this.user_dir, this.config_file);
//        ArrayList<RegistrationUser> ps = addDataToRCUsers(parser.getRCUsers());
//        if(ps.size()>0){
//            return ps.get(0);
//        }
//        return null;
//    }

//    public Partner getPartner() {
//        Parser parser = new Parser(this.user_dir, this.config_file);
//        ArrayList<Partner> ps = addDataToPartners(parser.getPartners());
//        if(ps.size()>0){
//            return ps.get(0);
//        }
//        return null;
//    }

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
