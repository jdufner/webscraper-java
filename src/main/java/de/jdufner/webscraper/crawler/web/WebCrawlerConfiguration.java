package de.jdufner.webscraper.crawler.web;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "webscraper")
public record WebCrawlerConfiguration(@NonNull String startUrl, int numberPages, int numberImages) {
}
