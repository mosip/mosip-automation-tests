#!/bin/bash

## Run DSL Orchestrator
java --version
java -jar -Denv.user="$USER" -Denv.endpoint="$ENDPOINT" -Denv.testLevel="$TESTLEVEL" ivv-orchestrator-*-jar-with-dependencies.jar
