package de.jdufner.webscraper.crawler.web;

import de.jdufner.webscraper.crawler.config.SiteConfigurationProperties;
import de.jdufner.webscraper.crawler.data.CrawlerRepository;
import de.jdufner.webscraper.crawler.data.HtmlPage;
import de.jdufner.webscraper.crawler.data.Link;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Optional;

@Service
public class WebCrawler {

    enum LinkStatus {
        DOWNLOADED, SKIPPED, UNAVAILABLE
    }

    private static final Logger logger = LoggerFactory.getLogger(WebCrawler.class);

    @NonNull
    private final WebCrawlerConfigurationProperties webCrawlerConfigurationProperties;
    @NonNull
    private final SiteConfigurationProperties siteConfigurationProperties;
    @NonNull
    private final WebFetcher webFetcher;
    @NonNull
    private final CrawlerRepository crawlerRepository;

    public WebCrawler(@NonNull WebCrawlerConfigurationProperties webCrawlerConfigurationProperties,
                      @NonNull SiteConfigurationProperties siteConfigurationProperties,
                      @NonNull WebFetcher webFetcher,
                      @NonNull CrawlerRepository crawlerRepository) {
        this.webCrawlerConfigurationProperties = webCrawlerConfigurationProperties;
        this.siteConfigurationProperties = siteConfigurationProperties;
        this.webFetcher = webFetcher;
        this.crawlerRepository = crawlerRepository;
    }

    public void crawl() {
        logger.info("startUrl = {}, numberPages = {}", webCrawlerConfigurationProperties.startUrl(), webCrawlerConfigurationProperties.numberPages());
        downloadInitialUrl();
        downloadLinks();
    }

    void downloadInitialUrl() {
        URI uri = URI.create(webCrawlerConfigurationProperties.startUrl());
        HtmlPage htmlPage = webFetcher.get(uri.toString());
        int documentId = crawlerRepository.save(htmlPage);
        crawlerRepository.setLinkDownloaded(new Link(documentId, uri));
    }

    void downloadLinks() {
        int i = 0;
        while (i < webCrawlerConfigurationProperties.numberPages()) {
            LinkStatus linkStatus = downloadEligibleNextLink();
            if (linkStatus == LinkStatus.DOWNLOADED) {
                i++;
            }
            if (linkStatus == LinkStatus.UNAVAILABLE) {
                return;
            }
        }
    }

    @NonNull LinkStatus downloadEligibleNextLink() {
        Optional<Link> optionalLink = crawlerRepository.getNextLinkIfAvailable();
        if (optionalLink.isPresent()) {
            Link link = optionalLink.get();
            if (siteConfigurationProperties.isEligibleAndNotBlocked(link.uri())) {
                return downloadAndSave(link);
            } else {
                return skip(link);
            }
        }
        return LinkStatus.UNAVAILABLE;
    }

    @NonNull LinkStatus downloadAndSave(@NonNull Link link) {
        HtmlPage htmlPage = webFetcher.get(link.uri().toString());
        crawlerRepository.save(htmlPage);
        crawlerRepository.setLinkDownloaded(link);
        return LinkStatus.DOWNLOADED;
    }

    @NonNull LinkStatus skip(@NonNull Link link) {
        logger.info("Link skipped {}", link.uri());
        crawlerRepository.setLinkSkip(link);
        return LinkStatus.SKIPPED;
    }

}
