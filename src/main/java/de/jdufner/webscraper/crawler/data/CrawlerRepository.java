package de.jdufner.webscraper.crawler.data;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.Optional;

@Repository
public interface CrawlerRepository {

    void saveAnalyzedDocument(@NonNull AnalyzedDocument analyzedDocument);

    @NonNull Optional<Image> getNextImageIfAvailable();

    @NonNull Optional<Link> getNextLinkIfAvailable();

    void setLinkState(@NonNull Link link, @NonNull LinkState linkState);

    void setImageState(@NonNull Image image, @NonNull ImageState state);

    int saveDownloadedDocument(@NonNull DownloadedDocument downloadedDocument);

    @NonNull Optional<Number> saveUriAsLink(@NonNull URI uri);

    @NonNull Optional<DownloadedDocument> findNextDownloadedDocument();

    void saveDownloadedImage(@NonNull DownloadedImage downloadedImage);

}
