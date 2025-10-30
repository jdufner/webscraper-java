package de.jdufner.webscraper;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class WebscraperApplication {

	public static void main(@NonNull String[] args) {
		SpringApplication.run(WebscraperApplication.class, args);
	}

}
