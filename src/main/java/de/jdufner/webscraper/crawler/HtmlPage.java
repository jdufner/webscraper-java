package de.jdufner.webscraper.crawler;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.Date;

public record HtmlPage(@NonNull URI uri, @NonNull String html, @Nullable String title, @Nullable Date createdAt, String creator, String categories,
                       java.util.List<URI> links, java.util.List<URI> images) {
}
