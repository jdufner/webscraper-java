package de.jdufner.webscraper.crawler.data;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.Optional;

@Repository
public interface CrawlerRepository {

    int save(@NonNull HtmlPage htmlPage);

    @NonNull Optional<Image> getNextImageIfAvailable();

    void setImageDownloadedAndFilename(@NonNull Image image, @NonNull File file);

    @NonNull Optional<Link> getNextLinkIfAvailable();

    void setLinkDownloaded(@NonNull Link link);

    void setImageSkip(@NonNull Image image);

    void setLinkSkip(Link link);

}
