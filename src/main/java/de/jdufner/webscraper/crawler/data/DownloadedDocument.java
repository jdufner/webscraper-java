package de.jdufner.webscraper.crawler.data;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.Date;

public record DownloadedDocument(@Nullable Integer id, @NonNull URI uri, @NonNull String content,
                                 Date downloadStartedAt, @NonNull Date downloadStoppedAt) {
}
