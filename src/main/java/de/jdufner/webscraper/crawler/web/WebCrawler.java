package de.jdufner.webscraper.crawler.web;

import de.jdufner.webscraper.crawler.config.SiteConfigurationProperties;
import de.jdufner.webscraper.crawler.data.AnalyzedDocument;
import de.jdufner.webscraper.crawler.data.CrawlerRepository;
import de.jdufner.webscraper.crawler.data.DownloadedDocument;
import de.jdufner.webscraper.crawler.data.Link;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.Optional;

@Service
public class WebCrawler {

    enum LinkStatus {
        DOWNLOADED, SKIPPED, UNAVAILABLE
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(WebCrawler.class);

    @NonNull
    private final WebCrawlerConfigurationProperties webCrawlerConfigurationProperties;
    @NonNull
    private final SiteConfigurationProperties siteConfigurationProperties;
    @NonNull
    private final WebFetcher webFetcher;
    @NonNull
    private final HtmlAnalyzer htmlAnalyzer;
    @NonNull
    private final CrawlerRepository crawlerRepository;

    private boolean initialized = false;
    private int numberDownloadedLinks = 0;

    public WebCrawler(@NonNull WebCrawlerConfigurationProperties webCrawlerConfigurationProperties,
                      @NonNull SiteConfigurationProperties siteConfigurationProperties,
                      @NonNull WebFetcher webFetcher,
                      @NonNull HtmlAnalyzer htmlAnalyzer,
                      @NonNull CrawlerRepository crawlerRepository) {
        this.webCrawlerConfigurationProperties = webCrawlerConfigurationProperties;
        this.siteConfigurationProperties = siteConfigurationProperties;
        this.webFetcher = webFetcher;
        this.htmlAnalyzer = htmlAnalyzer;
        this.crawlerRepository = crawlerRepository;
    }

    @Transactional
    public void download() {
        if (!initialized) {
            LOGGER.info("startUrl = {}, numberPages = {}", webCrawlerConfigurationProperties.startUrl(), webCrawlerConfigurationProperties.numberPages());
            downloadInitialUrl();
            initialized = true;
        } else {
            findAndDownloadLinkUntilConfiguredNumberReached();
        }
    }

    void downloadInitialUrl() {
        LOGGER.info("downloadInitialUrl()");
        URI uri = URI.create(webCrawlerConfigurationProperties.startUrl());
        Optional<Number> linkId = crawlerRepository.saveUriAsLink(uri);
        downloadAndSave(uri);
        linkId.ifPresent(number -> crawlerRepository.setLinkDownloaded(new Link(number.intValue(), uri)));
    }

    private void downloadAndSave(URI uri) {
        DownloadedDocument downloadedDocument = webFetcher.downloadDocument(uri);
        crawlerRepository.saveDownloadedDocument(downloadedDocument);
    }

    void findAndDownloadLinkUntilConfiguredNumberReached() {
        LOGGER.debug("findAndDownloadLinkUntilConfiguredNumberReached()");
        if (numberDownloadedLinks < webCrawlerConfigurationProperties.numberPages()) {
            LinkStatus linkStatus = findAndDownloadNextLink();
            if (linkStatus == LinkStatus.DOWNLOADED) {
                numberDownloadedLinks++;
            }
        }
    }

    @NonNull LinkStatus findAndDownloadNextLink() {
        Optional<Link> optionalLink = crawlerRepository.getNextLinkIfAvailable();
        return optionalLink.map(this::downloadLinkAndUpdateStatus).orElse(LinkStatus.UNAVAILABLE);
    }

    @NonNull LinkStatus downloadLinkAndUpdateStatus(Link link) {
        if (siteConfigurationProperties.isNotBlocked(link.uri())) {
            return downloadAndSave(link);
        } else {
            return skip(link);
        }
    }

    @NonNull LinkStatus downloadAndSave(@NonNull Link link) {
        downloadAndSave(link.uri());
        crawlerRepository.setLinkDownloaded(link);
        return LinkStatus.DOWNLOADED;
    }

    @NonNull LinkStatus skip(@NonNull Link link) {
        LOGGER.debug("Skip Link {}", link.uri());
        crawlerRepository.setLinkSkip(link);
        return LinkStatus.SKIPPED;
    }

    @Transactional
    public void analyze() {
        // The next document is the document with the lowest ID in state DOWNLOADED
        Optional<DownloadedDocument> optionalDownloadedDocument = crawlerRepository.findNextDownloadedDocument();

        if (optionalDownloadedDocument.isPresent()) {
            LOGGER.info("analyze = {}", optionalDownloadedDocument.get().uri());
            AnalyzedDocument analyzedDocument = htmlAnalyzer.analyze(optionalDownloadedDocument.get());
            crawlerRepository.saveAnalyzedDocument(analyzedDocument);
        }
    }

}
