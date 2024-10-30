package de.jdufner.webscraper.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class WebCrawler {

    private static final Logger logger = LoggerFactory.getLogger(WebCrawler.class);

    private final WebCrawlerConfiguration webCrawlerConfiguration;
    private final WebFetcher webFetcher;

    public WebCrawler(WebCrawlerConfiguration webCrawlerConfiguration, WebFetcher webFetcher) {
        this.webCrawlerConfiguration = webCrawlerConfiguration;
        this.webFetcher = webFetcher;
    }

    public void crawl() {
        logger.info("startUrl = {}, numberPages = {}", webCrawlerConfiguration.startUrl(), webCrawlerConfiguration.numberPages());
        for (int i = 0; i < webCrawlerConfiguration.numberPages(); i++){
            String url = webCrawlerConfiguration.startUrl();
            HtmlPage htmlPage = webFetcher.get(url);
            // pageRepository.save(htmlPage);
        }
    }

    private URI getNextUri(int index) {
        if (index > 0) {
            // return pageRepository.getNextUri();
        }
        return URI.create(webCrawlerConfiguration.startUrl());
    }

}
