package io.mosip.testrig.dslrig.packetcreator.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;


@Service
public class SimpleService {
    Logger logger = LoggerFactory.getLogger(SimpleService.class);

    public String getWelcomeMessage(){

        return VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "mosip.test.welcome").toString();
    }
}
