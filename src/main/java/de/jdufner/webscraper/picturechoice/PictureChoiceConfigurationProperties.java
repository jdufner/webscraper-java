package de.jdufner.webscraper.picturechoice;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "webscraper.picturechoice")
public record PictureChoiceConfigurationProperties(@NonNull String directory, @NonNull String filenamePattern) {
}
