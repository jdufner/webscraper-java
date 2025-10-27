package de.jdufner.webscraper.crawler.web;

import de.jdufner.webscraper.crawler.config.SiteConfigurationProperties;
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
    private final CrawlerRepository crawlerRepository;

    private boolean initialized = false;
    private int numberDownloadedLinks = 0;

    public WebCrawler(@NonNull WebCrawlerConfigurationProperties webCrawlerConfigurationProperties,
                      @NonNull SiteConfigurationProperties siteConfigurationProperties,
                      @NonNull WebFetcher webFetcher,
                      @NonNull CrawlerRepository crawlerRepository) {
        this.webCrawlerConfigurationProperties = webCrawlerConfigurationProperties;
        this.siteConfigurationProperties = siteConfigurationProperties;
        this.webFetcher = webFetcher;
        this.crawlerRepository = crawlerRepository;
    }

    @Transactional
    public void download() {
        if (!initialized) {
            LOGGER.info("startUrl = {}, numberPages = {}", webCrawlerConfigurationProperties.startUrl(), webCrawlerConfigurationProperties.numberPages());
            downloadInitialUrl();
            initialized = true;
        } else {
            downloadLink();
        }
    }

    void downloadInitialUrl() {
        URI uri = URI.create(webCrawlerConfigurationProperties.startUrl());
        Optional<Number> linkId = crawlerRepository.saveUriAsLink(uri);
        int downloadedDocumentId = downloadAndSave(uri);
        linkId.ifPresent(number -> crawlerRepository.setLinkDownloaded(new Link(number.intValue(), uri)));
    }

    private int downloadAndSave(URI uri) {
        DownloadedDocument downloadedDocument = webFetcher.downloadDocument(uri);
        return crawlerRepository.saveDownloadedDocument(downloadedDocument);
    }

    void downloadLink() {
        while (numberDownloadedLinks < webCrawlerConfigurationProperties.numberPages()) {
            LinkStatus linkStatus = findAndDownloadNextLink();
            if (linkStatus == LinkStatus.DOWNLOADED) {
                numberDownloadedLinks++;
            }
            if (linkStatus == LinkStatus.UNAVAILABLE) {
                return;
            }
        }
    }

    @NonNull LinkStatus findAndDownloadNextLink() {
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
        int downloadedDocumentId = downloadAndSave(link.uri());
        crawlerRepository.setLinkDownloaded(link);
        return LinkStatus.DOWNLOADED;
    }

    @NonNull LinkStatus skip(@NonNull Link link) {
        LOGGER.info("Link skipped {}", link.uri());
        crawlerRepository.setLinkSkip(link);
        return LinkStatus.SKIPPED;
    }

    @Transactional
    public void analyze() {
        LOGGER.info("startUrl = {}", webCrawlerConfigurationProperties.startUrl());
        // The next document is the document with the lowest ID in state DOWNLOADED
        // DownloadedDocument downloadedDocument = crawlerRepository.findNextDownloadedDocument();
        // HtmlPage / AnalyzedDocument analyzedDocument = htmlAnalyzer.analyze(downloadedDocument)
        // crawlerRepository.saveAnalyzedDocument()
    }

}
