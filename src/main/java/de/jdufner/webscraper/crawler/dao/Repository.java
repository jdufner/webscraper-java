package de.jdufner.webscraper.crawler.dao;

import de.jdufner.webscraper.crawler.data.HtmlPage;
import org.jspecify.annotations.NonNull;

import java.net.URI;

public interface Repository {

    void save(@NonNull HtmlPage htmlPage);

    @NonNull URI getNextImageUri();
}
