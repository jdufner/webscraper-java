package de.jdufner.webscraper.crawler.web;

import de.jdufner.webscraper.crawler.config.SiteConfigurationProperties;
import de.jdufner.webscraper.crawler.data.AnalyzedDocument;
import de.jdufner.webscraper.crawler.data.CrawlerRepository;
import de.jdufner.webscraper.crawler.data.DownloadedDocument;
import de.jdufner.webscraper.crawler.data.Link;
import de.jdufner.webscraper.crawler.logger.JsonLogger;
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
    @NonNull
    private final JsonLogger jsonLogger;

    private boolean initialized = false;
    private int numberDownloadedLinks = 0;

    public WebCrawler(@NonNull WebCrawlerConfigurationProperties webCrawlerConfigurationProperties,
                      @NonNull SiteConfigurationProperties siteConfigurationProperties,
                      @NonNull WebFetcher webFetcher,
                      @NonNull HtmlAnalyzer htmlAnalyzer,
                      @NonNull CrawlerRepository crawlerRepository,
                      @NonNull JsonLogger jsonLogger) {
        this.webCrawlerConfigurationProperties = webCrawlerConfigurationProperties;
        this.siteConfigurationProperties = siteConfigurationProperties;
        this.webFetcher = webFetcher;
        this.htmlAnalyzer = htmlAnalyzer;
        this.crawlerRepository = crawlerRepository;
        this.jsonLogger = jsonLogger;
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
        LOGGER.debug("downloadInitialUrl()");
        URI uri = URI.create(webCrawlerConfigurationProperties.startUrl());
        Optional<Number> linkId = crawlerRepository.saveUriAsLink(uri);
        linkId.ifPresent(number -> {
            Link link = new Link(number.intValue(), uri);
            downloadLinkAndUpdateStatus(link);
        });
    }

    private @NonNull DownloadedDocument downloadAndSave(URI uri) {
        DownloadedDocument downloadedDocument = webFetcher.downloadDocument(uri);
        crawlerRepository.saveDownloadedDocument(downloadedDocument);
        return downloadedDocument;
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
        DownloadedDocument downloadedDocument = downloadAndSave(link.uri());
        crawlerRepository.setLinkDownloaded(link);
        record LinkDownloadedDocument(@NonNull Link link, @NonNull DownloadedDocument downloadedDocument) {}
        DownloadedDocument shortContent = new DownloadedDocument(downloadedDocument.id(), downloadedDocument.uri(),
                downloadedDocument.content().substring(0, Math.min(downloadedDocument.content().length(), 1000)),
                downloadedDocument.downloadStartedAt(), downloadedDocument.downloadStoppedAt(),
                downloadedDocument.documentState());
        jsonLogger.failsafeInfo(new LinkDownloadedDocument(link, shortContent));
        return LinkStatus.DOWNLOADED;
    }

    @NonNull LinkStatus skip(@NonNull Link link) {
        LOGGER.debug("Skip Link {}", link.uri());
        crawlerRepository.setLinkSkip(link);
        record LinkSkippedDocument(@NonNull Link link, @NonNull LinkStatus linkStatus) {}
        jsonLogger.failsafeInfo(new LinkSkippedDocument(link, LinkStatus.SKIPPED));
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
