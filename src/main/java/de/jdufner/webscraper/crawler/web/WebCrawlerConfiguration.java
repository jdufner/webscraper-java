package de.jdufner.webscraper.crawler.web;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "webscraper.crawler.web")
public record WebCrawlerConfiguration(@NonNull String startUrl, int numberPages, @Nullable String[] javascript) {
}
