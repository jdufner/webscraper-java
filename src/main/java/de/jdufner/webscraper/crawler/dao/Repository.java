package de.jdufner.webscraper.crawler.dao;

import de.jdufner.webscraper.crawler.data.HtmlPage;
import de.jdufner.webscraper.crawler.data.Image;
import de.jdufner.webscraper.crawler.data.Link;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.Optional;

public interface Repository {

    void save(@NonNull HtmlPage htmlPage);

    @NonNull Optional<Image> getNextImageIfAvailable();

    void setImageDownloadedAndFilename(@NonNull Image image, @NonNull File file);

    @NonNull Optional<Link> getNextLinkIfAvailable();

    void setLinkDownloaded(@NonNull Link link);

    void setImageSkip(@NonNull Image image);

}
