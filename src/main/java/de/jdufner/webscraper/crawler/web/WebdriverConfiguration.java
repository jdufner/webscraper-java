package de.jdufner.webscraper.crawler.web;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "webdriver.chrome")
public record WebdriverConfiguration(@NonNull List<String> options) {
}