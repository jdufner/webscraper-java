package de.jdufner.webscraper.crawler;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "webscraper")
public record WebCrawlerConfiguration(String startUrl, int numberPages, int numberImages) {
}
