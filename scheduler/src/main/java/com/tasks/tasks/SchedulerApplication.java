package com.tasks.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
public class SchedulerApplication {

	static Logger logger = LoggerFactory.getLogger(SchedulerApplication.class);

	public static void main(String[] args) {
		String mode = System.getenv("MODE");

		SpringApplicationBuilder builder = new SpringApplicationBuilder(SchedulerApplication.class);
		logger.info("Booting in mode: " + mode);
		if (mode == "worker") {
			builder = builder.web(WebApplicationType.NONE);
		}
		builder.run(args);
	}

}
