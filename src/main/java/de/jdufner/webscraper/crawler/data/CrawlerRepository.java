package de.jdufner.webscraper.crawler.data;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.net.URI;
import java.util.Optional;

@Repository
public interface CrawlerRepository {

    void saveAnalyzedDocument(@NonNull AnalyzedDocument analyzedDocument);

    @NonNull Optional<Image> getNextImageIfAvailable();

    void setImageDownloadedAndFilename(@NonNull Image image, @NonNull File file);

    @NonNull Optional<Link> getNextLinkIfAvailable();

    void setLinkDownloaded(@NonNull Link link);

    void setImageSkip(@NonNull Image image);

    void setLinkSkip(Link link);

    int saveDownloadedDocument(@NonNull DownloadedDocument downloadedDocument);

    @NonNull Optional<Number> saveUriAsLink(@NonNull URI uri);

    @NonNull Optional<DownloadedDocument> findNextDownloadedDocument();

    void saveDownloadedImage(@NonNull DownloadedImage downloadedImage);

}
