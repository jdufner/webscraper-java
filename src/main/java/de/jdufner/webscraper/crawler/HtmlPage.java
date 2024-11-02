package de.jdufner.webscraper.crawler;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.Date;
import java.util.List;

public record HtmlPage(@NonNull URI uri, @NonNull String html, @Nullable String title, @Nullable Date createdAt,
                       @NonNull List<String> creator, @NonNull List<String> categories,
                       @NonNull List<URI> links, @NonNull List<URI> images) {
}