package de.jdufner.webscraper.crawler.data;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.Date;
import java.util.List;

public record AnalyzedDocument(int documentId, @Nullable String title, @Nullable Date createdAt, @NonNull List<String> authors,
                               @NonNull List<String> categories, @NonNull List<URI> links, @NonNull List<URI> images,
                               @NonNull Date analysisStartedAt, @NonNull Date analysisStoppedAt,
                               @Nullable Integer contentLength) {
}
