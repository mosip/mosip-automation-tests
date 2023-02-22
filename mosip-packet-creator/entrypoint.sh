#!/bin/bash

java --version
java -Dfile.encoding=UTF-8  -jar mosip-packet-creator-*-SNAPSHOT.jar --spring.config.location=./config/application.properties
