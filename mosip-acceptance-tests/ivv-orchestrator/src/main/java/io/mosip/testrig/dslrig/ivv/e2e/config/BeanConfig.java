package io.mosip.testrig.dslrig.ivv.e2e.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@Configuration
@ComponentScan(basePackages = { "io.mosip.testrig.apirig", "io.mosip.testrig.dslrig"})
public class BeanConfig {
	int i = 0;
}
