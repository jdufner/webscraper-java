package de.jdufner.webscraper.crawler.web;

import de.jdufner.webscraper.crawler.config.SiteConfiguration;
import de.jdufner.webscraper.crawler.data.HsqldbRepository;
import de.jdufner.webscraper.crawler.data.HtmlPage;
import de.jdufner.webscraper.crawler.data.Link;
import de.jdufner.webscraper.crawler.data.Repository;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Optional;

@Service
public class WebCrawler {

    private enum LinkStatus {
        DOWNLOADED, SKIPPED, UNAVAILABLE
    }

    private static final Logger logger = LoggerFactory.getLogger(WebCrawler.class);

    @NonNull
    private final WebCrawlerConfiguration webCrawlerConfiguration;
    @NonNull
    private final SiteConfiguration siteConfiguration;
    @NonNull
    private final WebFetcher webFetcher;
    @NonNull
    private final Repository repository;

    public WebCrawler(@NonNull WebCrawlerConfiguration webCrawlerConfiguration,
                      @NonNull SiteConfiguration siteConfiguration,
                      @NonNull WebFetcher webFetcher,
                      @NonNull HsqldbRepository repository) {
        this.webCrawlerConfiguration = webCrawlerConfiguration;
        this.siteConfiguration = siteConfiguration;
        this.webFetcher = webFetcher;
        this.repository = repository;
    }

    public void crawl() {
        logger.info("startUrl = {}, numberPages = {}", webCrawlerConfiguration.startUrl(), webCrawlerConfiguration.numberPages());
        downloadInitialUrl();
        downloadLinks();
    }

    private void downloadInitialUrl() {
        URI uri = URI.create(webCrawlerConfiguration.startUrl());
        HtmlPage htmlPage = webFetcher.get(uri.toString());
        int documentId = repository.save(htmlPage);
        repository.setLinkDownloaded(new Link(documentId, uri));
    }

    private void downloadLinks() {
        int i = 0;
        while (i < webCrawlerConfiguration.numberPages()) {
            LinkStatus linkStatus = downloadNextLink();
            if (linkStatus == LinkStatus.DOWNLOADED) {
                i++;
            }
            if (linkStatus == LinkStatus.UNAVAILABLE) {
                return;
            }
        }
    }

    private LinkStatus downloadNextLink() {
        Optional<Link> link = repository.getNextLinkIfAvailable();
        if (link.isPresent()) {
            if (siteConfiguration.isValidAndNotBlocked(link.get().uri())) {
                HtmlPage htmlPage = webFetcher.get(link.get().uri().toString());
                repository.save(htmlPage);
                repository.setLinkDownloaded(link.get());
                return LinkStatus.DOWNLOADED;
            } else {
                logger.info("Link skipped {}", link.get().uri());
                repository.setLinkSkip(link.get());
                return LinkStatus.SKIPPED;
            }
        }
        return LinkStatus.UNAVAILABLE;
    }

}
