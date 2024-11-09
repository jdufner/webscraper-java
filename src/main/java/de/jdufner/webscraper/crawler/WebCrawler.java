package de.jdufner.webscraper.crawler;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class WebCrawler {

    private static final Logger logger = LoggerFactory.getLogger(WebCrawler.class);

    private final WebCrawlerConfiguration webCrawlerConfiguration;
    private final WebFetcher webFetcher;
    private final Repository repository;

    public WebCrawler(@NonNull WebCrawlerConfiguration webCrawlerConfiguration, @NonNull WebFetcher webFetcher, @NonNull HsqldbRepository repository) {
        this.webCrawlerConfiguration = webCrawlerConfiguration;
        this.webFetcher = webFetcher;
        this.repository = repository;
    }

    public void crawl() {
        logger.info("startUrl = {}, numberPages = {}", webCrawlerConfiguration.startUrl(), webCrawlerConfiguration.numberPages());
        for (int i = 0; i < webCrawlerConfiguration.numberPages(); i++) {
            URI uri = getNextUri(i);
            HtmlPage htmlPage = webFetcher.get(uri.toString());
            repository.save(htmlPage);
        }
    }

    private @NonNull URI getNextUri(int index) {
        if (index > 0) {
            // return pageRepository.getNextUri();
        }
        return URI.create(webCrawlerConfiguration.startUrl());
    }

}
