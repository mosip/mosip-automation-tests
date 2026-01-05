package io.mosip.testrig.dslrig.packetcreator.config;

import org.jobrunr.configuration.JobRunr;
import org.jobrunr.jobs.mappers.JobMapper;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.storage.InMemoryStorageProvider;
import org.jobrunr.storage.StorageProvider;
import org.jobrunr.utils.mapper.jackson.JacksonJsonMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;

@Configuration
public class PacketCreatorConfig {

	@Bean
	public JobMapper jobMapper() {
		return new JobMapper(new JacksonJsonMapper());
	}

	@Bean
	@DependsOn("jobMapper")
	public StorageProvider storageProvider(JobMapper jobMapper) {
		InMemoryStorageProvider storageProvider = new InMemoryStorageProvider();
		storageProvider.setJobMapper(jobMapper);
		return storageProvider;
	}

	@Bean
	@DependsOn("storageProvider")
	public JobScheduler jobScheduler(StorageProvider storageProvider, ApplicationContext applicationContext, Environment env) {
		// Make dashboard optional to avoid Address already in use errors when the default
		// JobRunr dashboard port is occupied. Enable by setting 'jobrunr.dashboard.enabled=true'
		var builder = JobRunr.configure()
			.useStorageProvider(storageProvider)
			.useJobActivator(applicationContext::getBean)
			.useDefaultBackgroundJobServer();

		boolean enableDashboard = Boolean.parseBoolean(env.getProperty("jobrunr.dashboard.enabled", "false"));
		if (enableDashboard) {
			builder.useDashboard();
		}

		builder.useJmxExtensions();
		return builder.initialize();
	}

}