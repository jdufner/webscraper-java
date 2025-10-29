package de.jdufner.webscraper.crawler.web;

import de.jdufner.webscraper.crawler.data.DownloadedDocument;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class WebFetcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebFetcher.class);
    static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

    private final WebdriverWrapper webdriverWrapper;

    public WebFetcher(@NonNull WebdriverWrapper webdriverWrapper) {
        this.webdriverWrapper = webdriverWrapper;
    }

//    public @NonNull HtmlPage get(@NonNull String url) {
//        LOGGER.info("get url = {}", url);
//        URI uri = URI.create(url);
//        String html = webdriverWrapper.getHtml(uri.toString());
//        Date downloadedAt = new Date();
//        Document document = Jsoup.parse(html);
//        Optional<String> title = extractTitle(document);
//        Optional<Date> createdAt = extractCreatedAt(document);
//        List<String> authors = extractAuthors(document);
//        List<String> categories = extractCategories(document);
//        List<URI> links = extractLinks(uri, document);
//        List<URI> images = extractImages(uri, document);
//        LOGGER.info("title = {}, createdAt = {}, authors = {}, categories = {}, links = {}, images = {}",
//                title, createdAt, authors, categories, links, images);
//        return new HtmlPage(uri, html, downloadedAt, title.orElse(null), createdAt.orElse(null), authors, categories, links, images);
//    }

    public @NonNull DownloadedDocument downloadDocument(@NonNull URI uri) {
        LOGGER.info("get url = {}", uri);
        String html = webdriverWrapper.getHtml(uri.toString());
        Date downloadedAt = new Date();
        return new DownloadedDocument(null, uri, html, downloadedAt);
    }

}
