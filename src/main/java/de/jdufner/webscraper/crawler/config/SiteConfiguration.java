package de.jdufner.webscraper.crawler.config;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

import static java.util.Arrays.stream;

@ConfigurationProperties(prefix = "webscraper.crawler.sites")
public record SiteConfiguration(@NonNull String[] whiteList, @NonNull String[] blackList) {

    public boolean isValidAndNotBlocked(@NonNull URI uri) {
        return isInArray(whiteList, uri) && !isInArray(blackList, uri);
    }

    boolean isInArray(@NonNull String[] uris, @NonNull URI uri) {
        return stream(uris)
                .map(URI::create)
                .anyMatch(aUri -> aUri.getHost().equals(uri.getHost()) && removeTrailingSlash(aUri.getPath()).equals(removeTrailingSlash(uri.getPath())));
    }

    private static @NonNull String removeTrailingSlash(@NonNull String path) {
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

}
