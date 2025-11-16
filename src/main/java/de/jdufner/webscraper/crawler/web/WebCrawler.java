package de.jdufner.webscraper.crawler.web;

import de.jdufner.webscraper.crawler.config.SiteConfigurationProperties;
import de.jdufner.webscraper.crawler.data.*;
import de.jdufner.webscraper.crawler.logger.JsonLogger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.lang.Math.min;

@Service
public class WebCrawler {

    enum LinkStatus {
        DOWNLOADED, SKIPPED, UNAVAILABLE, ERROR
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
            Link link = new Link(number.intValue(), uri, LinkState.INITIALIZED);
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
        try {
            if (siteConfigurationProperties.isNotBlocked(link.uri())) {
                return downloadAndSave(link);
            } else {
                return skip(link);
            }
        } catch (Exception e) {
            LOGGER.error("downloadLinkAndUpdateStatus", e);
            error(link);
            return LinkStatus.ERROR;
        }
    }

    @NonNull LinkStatus downloadAndSave(@NonNull Link link) {
        DownloadedDocument downloadedDocument = downloadAndSave(link.uri());
        Link downloadedLink = new Link(link.id(), link.uri(), LinkState.DOWNLOADED);
        crawlerRepository.setLinkState(downloadedLink);
        failsafeLog(downloadedLink, downloadedDocument);
        return LinkStatus.DOWNLOADED;
    }

    void failsafeLog(@NonNull Link link, @NonNull DownloadedDocument downloadedDocument) {
        record LinkDownloadedDocument(@NonNull Link link, @NonNull DownloadedDocument downloadedDocument) {}
        DownloadedDocument shortContent = cloneButShortenHtmlContent(downloadedDocument);
        jsonLogger.failsafeInfo(new LinkDownloadedDocument(link, shortContent));
    }

    @NonNull LinkStatus skip(@NonNull Link link) {
        LOGGER.debug("Skip Link {}", link.uri());
        Link skippedLink = link.skip();
        crawlerRepository.setLinkState(skippedLink);
        failsafeLog(skippedLink);
        return LinkStatus.SKIPPED;
    }

    @NonNull LinkStatus error(@NonNull Link link) {
        LOGGER.debug("Couldn't download Link {}", link.uri());
        Link errorLink = link.error();
        crawlerRepository.setLinkState(errorLink);
        failsafeLog(errorLink);
        return LinkStatus.ERROR;
    }

    void failsafeLog(@NonNull Link link) {
        jsonLogger.failsafeInfo(link);
    }

    @Transactional
    public void analyze() {
        // The next document is the document with the lowest ID in state DOWNLOADED
        Optional<DownloadedDocument> optionalDownloadedDocument = crawlerRepository.findNextDownloadedDocument();
        if (optionalDownloadedDocument.isPresent()) {
            LOGGER.info("analyze = {}", optionalDownloadedDocument.get().uri());
            AnalyzedDocument analyzedDocument = htmlAnalyzer.analyze(optionalDownloadedDocument.get());
            crawlerRepository.saveAnalyzedDocument(analyzedDocument);
            failsafeLog(optionalDownloadedDocument.get(), analyzedDocument);
        }
    }

    void failsafeLog(@NonNull DownloadedDocument downloadedDocument, @NonNull AnalyzedDocument analyzedDocument) {
        DownloadedDocument shortContent = cloneButShortenHtmlContent(downloadedDocument);
        record AnalyzedDocumentData(@Nullable String title, @Nullable Date createAt, @NonNull List<String> authors,
                                    @NonNull List<String> categories, @NonNull Integer numberLinks,
                                    @NonNull Integer numberImages, @NonNull Date analysisStartedAt,
                                    @NonNull Date analysisStoppedAt) { }
        AnalyzedDocumentData analyzedDocumentData = new AnalyzedDocumentData(analyzedDocument.title(),
                analyzedDocument.createdAt(), analyzedDocument.authors(), analyzedDocument.categories(),
                analyzedDocument.links().size(), analyzedDocument.images().size(),
                analyzedDocument.analysisStartedAt(), analyzedDocument.analysisStoppedAt());
        record DownloadedAnalyzedDocument(@NonNull DownloadedDocument downloadedDocument, @NonNull AnalyzedDocumentData analyzedDocumentData) {}
        jsonLogger.failsafeInfo(new DownloadedAnalyzedDocument(shortContent, analyzedDocumentData));
    }

    static @NonNull DownloadedDocument cloneButShortenHtmlContent(@NonNull DownloadedDocument downloadedDocument) {
        return new DownloadedDocument(downloadedDocument.id(), downloadedDocument.uri(),
                downloadedDocument.content().substring(0, min(downloadedDocument.content().length(), 200)),
                downloadedDocument.downloadStartedAt(), downloadedDocument.downloadStoppedAt(),
                downloadedDocument.documentState());
    }

}
