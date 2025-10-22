package de.jdufner.webscraper.crawler.data;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.Date;
import java.util.List;

public record HtmlPage(@NonNull URI uri, @NonNull String html, @NonNull Date downloadedAt,
                       @Nullable Date createdAt, @NonNull List<String> authors, @NonNull List<String> categories,
                       @NonNull List<URI> links, @NonNull List<URI> images) {
}
