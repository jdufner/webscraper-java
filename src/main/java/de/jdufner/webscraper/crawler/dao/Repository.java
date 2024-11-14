package de.jdufner.webscraper.crawler.dao;

import de.jdufner.webscraper.crawler.data.HtmlPage;
import de.jdufner.webscraper.crawler.data.Image;
import de.jdufner.webscraper.crawler.data.Link;
import org.jspecify.annotations.NonNull;

import java.io.File;

public interface Repository {

    void save(@NonNull HtmlPage htmlPage);

    @NonNull Image getNextImage();

    void setImageDownloadedAndFilename(@NonNull Image image, @NonNull File file);

    @NonNull Link getNextLink();

    void setLinkDownloaded(@NonNull Link link);

}
