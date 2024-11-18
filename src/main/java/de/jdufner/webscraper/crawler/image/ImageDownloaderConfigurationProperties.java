package de.jdufner.webscraper.crawler.image;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "webscraper.crawler.image")
public record ImageDownloaderConfigurationProperties(int numberImages) {
}
