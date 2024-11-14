package de.jdufner.webscraper.crawler.data;

import org.jspecify.annotations.NonNull;

import java.net.URI;

public record Link(int id, @NonNull URI uri) {
}
