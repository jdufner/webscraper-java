package de.jdufner.webscraper.crawler;

import org.jspecify.annotations.NonNull;

public interface Repository {

    void save(@NonNull HtmlPage htmlPage);

}
