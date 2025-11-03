package de.jdufner.webscraper.crawler.image;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ImageDownloaderTrigger {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageDownloaderTrigger.class);

    private final @NonNull ImageDownloaderConfigurationProperties imageDownloaderConfigurationProperties;
    private final @NonNull ImageDownloader imageDownloader;

    public ImageDownloaderTrigger(
            @NonNull ImageDownloaderConfigurationProperties imageDownloaderConfigurationProperties,
            @NonNull ImageDownloader imageDownloader) {
        this.imageDownloaderConfigurationProperties = imageDownloaderConfigurationProperties;
        this.imageDownloader = imageDownloader;
    }

    @Scheduled(fixedDelay = 100, timeUnit = TimeUnit.MILLISECONDS)
    public void doStartDownload() {
        LOGGER.info("Starting ImageDownloaderTrigger.doStartDownload()");
        if (imageDownloaderConfigurationProperties.startDownloadAutomatically()) {
            // imageDownloader.downloadAll();
        }
    }

}
