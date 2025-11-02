package de.jdufner.webscraper.crawler.web;

import de.jdufner.webscraper.crawler.data.DownloadedDocument;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Date;

@Service
public class WebFetcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebFetcher.class);

    private final WebdriverWrapper webdriverWrapper;

    public WebFetcher(@NonNull WebdriverWrapper webdriverWrapper) {
        this.webdriverWrapper = webdriverWrapper;
    }

    public @NonNull DownloadedDocument downloadDocument(@NonNull URI uri) {
        LOGGER.info("get url = {}", uri);
        Date downloadStartedAt = new Date();
        String html = webdriverWrapper.getHtml(uri.toString());
        Date downloadStoppedAt = new Date();
        return new DownloadedDocument(null, uri, html, downloadStartedAt, downloadStoppedAt);
    }

}
