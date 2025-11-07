package de.jdufner.webscraper.crawler.data;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Date;

public record DownloadedImage(int id, @NonNull String state, @Nullable String fileName, @NonNull Date downloadStartedAt, @NonNull Date downloadFinishedAt, @Nullable Integer size, @Nullable Integer height, @Nullable Integer width) {
}
