package de.jdufner.webscraper;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "selenium.chrome")
public record SeleniumConfiguration(List<String> options) {
}
