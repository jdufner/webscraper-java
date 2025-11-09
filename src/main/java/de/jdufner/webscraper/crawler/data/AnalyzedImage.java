package de.jdufner.webscraper.crawler.data;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record AnalyzedImage(@NonNull Long fileSize, @Nullable Integer dimensionWidth, @Nullable Integer dimensionHeight, @Nullable String hashValue) {
}
