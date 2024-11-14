package de.jdufner.webscraper.crawler.dao;

import de.jdufner.webscraper.crawler.data.HtmlPage;
import de.jdufner.webscraper.crawler.data.Image;
import org.jspecify.annotations.NonNull;

import java.io.File;

public interface Repository {

    void save(@NonNull HtmlPage htmlPage);

    @NonNull Image getNextImageUri();

    void setDownloadedFilename(@NonNull Image image, @NonNull File file);
}
