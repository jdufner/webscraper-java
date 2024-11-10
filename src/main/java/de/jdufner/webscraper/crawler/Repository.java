package de.jdufner.webscraper.crawler;

import org.jspecify.annotations.NonNull;

import java.net.URI;

public interface Repository {

    void save(@NonNull HtmlPage htmlPage);

    URI getNextImageUri();
}
