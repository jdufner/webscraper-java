package de.jdufner.webscraper.crawler.web;

import de.jdufner.webscraper.crawler.config.SiteConfigurationProperties;
import de.jdufner.webscraper.crawler.data.*;
import de.jdufner.webscraper.crawler.logger.JsonLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebCrawlerTest {

    @Mock
    private WebCrawlerConfigurationProperties webCrawlerConfigurationProperties;

    @Mock
    private SiteConfigurationProperties siteConfigurationProperties;

    @Mock
    private WebFetcher webFetcher;

    @Mock
    private CrawlerRepository crawlerRepository;

    @Mock
    private JsonLogger jsonLogger;

    @InjectMocks
    private WebCrawler webCrawler;

    @Test
    void given_webcrawler_when_download_and_not_initialized_expect_download_initial_url() {
        // arrange
        URI uri = URI.create("https://localhost");
        when(webCrawlerConfigurationProperties.startUrl()).thenReturn(uri.toString());
        when(webCrawlerConfigurationProperties.numberPages()).thenReturn(1);

        // act
        webCrawler.download();

        // assert
        assertThat((Boolean) ReflectionTestUtils.getField(webCrawler, "initialized")).isTrue();
        verify(crawlerRepository).saveUriAsLink(uri);
    }

    @Test
    void given_webcrawler_when_download_and_initialized_expect_download_link() {
        // arrange
        URI uri = URI.create("https://localhost");
        when(webCrawlerConfigurationProperties.numberPages()).thenReturn(1);
        ReflectionTestUtils.setField(webCrawler, "initialized", true);
        Link link = new Link(1, uri, LinkState.INITIALIZED);
        when(crawlerRepository.getNextLinkIfAvailable()).thenReturn(Optional.of(link));
        when(siteConfigurationProperties.isNotBlocked(uri)).thenReturn(true);
        when(webFetcher.downloadDocument(uri)).thenReturn(new DownloadedDocument(1, uri, "<html></html>", new Date(), new Date(), DocumentState.INITIALIZED));

        // act
        webCrawler.download();

        // assert
        assertThat((Integer) ReflectionTestUtils.getField(webCrawler, "numberDownloadedLinks")).isEqualTo(1);
        verify(crawlerRepository).getNextLinkIfAvailable();
    }

    @Test
    void given_webcrawler_when_download_initial_url_expect_downloaded_and_saved() {
        // arrange
        URI uri = URI.create("https://localhost");
        when(webCrawlerConfigurationProperties.startUrl()).thenReturn(uri.toString());
        when(crawlerRepository.saveUriAsLink(uri)).thenReturn(Optional.of(1));
        DownloadedDocument downloadedDocument = new DownloadedDocument(null, uri, "<html></hml>", new Date(), new Date(), DocumentState.INITIALIZED);
        when(webFetcher.downloadDocument(uri)).thenReturn(downloadedDocument);
        when(crawlerRepository.saveDownloadedDocument(downloadedDocument)).thenReturn(1);
        when(siteConfigurationProperties.isNotBlocked(uri)).thenReturn(true);
        doNothing().when(crawlerRepository).setLinkState(any(Link.class));

        // act
        webCrawler.downloadInitialUrl();

        // assert
        verify(crawlerRepository).saveUriAsLink(uri);
        verify(webFetcher).downloadDocument(uri);
        verify(crawlerRepository).saveDownloadedDocument(downloadedDocument);
        verify(crawlerRepository).setLinkState(new Link(1, uri, LinkState.DOWNLOADED));
    }

    @Test
    void given_webcrawler_when_no_links_available_expect_nothing_saved() {
        // arrange
        when(webCrawlerConfigurationProperties.numberPages()).thenReturn(100);
        when(crawlerRepository.getNextLinkIfAvailable()).thenReturn(Optional.empty());

        // act
        webCrawler.findAndDownloadLinkUntilConfiguredNumberReached();

        // assert
        verify(crawlerRepository).getNextLinkIfAvailable();
    }

    @Test
    void given_webcrawler_when_links_available_expect_downloaded_and_saved() {
        // arrange
        when(webCrawlerConfigurationProperties.numberPages()).thenReturn(1);
        Link link = new Link(1, URI.create("http://localhost"), LinkState.INITIALIZED);
        when(crawlerRepository.getNextLinkIfAvailable()).thenReturn(Optional.of(link));
        when(siteConfigurationProperties.isNotBlocked(any())).thenReturn(true);
        DownloadedDocument downloadedDocument = new DownloadedDocument(null, link.uri(), "<html></hml>", new Date(), new Date(), DocumentState.INITIALIZED);
        when(webFetcher.downloadDocument(link.uri())).thenReturn(downloadedDocument);
        when(crawlerRepository.saveDownloadedDocument(downloadedDocument)).thenReturn(1);
        Link downloadedLink = link.download();
        doNothing().when(crawlerRepository).setLinkState(downloadedLink);

        // act
        webCrawler.findAndDownloadLinkUntilConfiguredNumberReached();

        // assert
        verify(crawlerRepository).getNextLinkIfAvailable();
        verify(webFetcher).downloadDocument(link.uri());
        verify(crawlerRepository).saveDownloadedDocument(downloadedDocument);
        verify(crawlerRepository).setLinkState(downloadedLink);
    }

    @Test
    void given_webcrawler_when_next_link_available_and_not_blocked_expect_status_downloaded() {
        // arrange
        Link link = new Link(1, URI.create("https://localhost"), LinkState.INITIALIZED);
        when(crawlerRepository.getNextLinkIfAvailable()).thenReturn(Optional.of(link));
        when(siteConfigurationProperties.isNotBlocked(link.uri())).thenReturn(true);
        when(webFetcher.downloadDocument(link.uri())).thenReturn(new DownloadedDocument(1, link.uri(), "<html></html>", new Date(), new Date(), DocumentState.INITIALIZED));

        // act
        WebCrawler.LinkStatus linkStatus = webCrawler.findAndDownloadNextLink();

        // assert
        assertThat(linkStatus).isEqualTo(WebCrawler.LinkStatus.DOWNLOADED);
    }

    @Test
    void given_webcrawler_when_next_link_available_but_blocked_expect_status_skipped() {
        // arrange
        Link link = new Link(1, URI.create("https://localhost"), LinkState.INITIALIZED);
        when(crawlerRepository.getNextLinkIfAvailable()).thenReturn(Optional.of(link));
        when(siteConfigurationProperties.isNotBlocked(link.uri())).thenReturn(false);

        // act
        WebCrawler.LinkStatus linkStatus = webCrawler.findAndDownloadNextLink();

        // assert
        assertThat(linkStatus).isEqualTo(WebCrawler.LinkStatus.SKIPPED);
    }

    @Test
    void given_webcrawler_when_next_link_not_available_expect_status_unavailable() {
        // arrange
        when(crawlerRepository.getNextLinkIfAvailable()).thenReturn(Optional.empty());

        // act
        WebCrawler.LinkStatus linkStatus = webCrawler.findAndDownloadNextLink();

        // assert
        assertThat(linkStatus).isEqualTo(WebCrawler.LinkStatus.UNAVAILABLE);
    }

    @Test
    void given_webcrawler_when_download_link_and_update_status_is_not_blocked_expect_downloaded() {
        // arrange
        Link link = new Link(1, URI.create("https://localhost"), LinkState.INITIALIZED);
        Link downloadedLink = link.download();
        when(siteConfigurationProperties.isNotBlocked(link.uri())).thenReturn(true);
        when(webFetcher.downloadDocument(link.uri())).thenReturn(new DownloadedDocument(1, link.uri(), "<html></html>", new Date(), new Date(), DocumentState.INITIALIZED));

        // act
        WebCrawler.LinkStatus linkStatus = webCrawler.downloadLinkAndUpdateStatus(link);

        // assert
        verify(crawlerRepository).setLinkState(downloadedLink);
        assertThat(linkStatus).isEqualTo(WebCrawler.LinkStatus.DOWNLOADED);
    }

    @Test
    void given_webcrawler_when_download_link_and_update_status_is_blocked_expect_skipped() {
        // arrange
        Link link = new Link(1, URI.create("https://localhost"), LinkState.INITIALIZED);
        Link skippedLink = link.skip();
        when(siteConfigurationProperties.isNotBlocked(link.uri())).thenReturn(false);

        // act
        WebCrawler.LinkStatus linkStatus = webCrawler.downloadLinkAndUpdateStatus(link);

        // assert
        verify(crawlerRepository).setLinkState(skippedLink);
        assertThat(linkStatus).isEqualTo(WebCrawler.LinkStatus.SKIPPED);
    }

    @Test
    void given_webcrawler_when_download_and_save_expect_status_downloaded() {
        // arrange
        Link link = new Link(1, URI.create("https://localhost"), LinkState.INITIALIZED);
        Link downloadedLink =  link.download();
        DownloadedDocument downloadedDocument = new DownloadedDocument(null, link.uri(), "<html></html>", new Date(), new Date(), DocumentState.INITIALIZED);
        when(webFetcher.downloadDocument(link.uri())).thenReturn(downloadedDocument);
        when(crawlerRepository.saveDownloadedDocument(downloadedDocument)).thenReturn(1);
        doNothing().when(crawlerRepository).setLinkState(downloadedLink);

        // act
        WebCrawler.LinkStatus linkStatus = webCrawler.downloadAndSave(link);

        // assert
        verify(webFetcher).downloadDocument(link.uri());
        verify(crawlerRepository).saveDownloadedDocument(downloadedDocument);
        verify(crawlerRepository).setLinkState(downloadedLink);
        assertThat(linkStatus).isEqualTo(WebCrawler.LinkStatus.DOWNLOADED);
    }

    @Test
    void given_webcrawler_when_skip_link_expect_status_skipped() {
        // arrange
        Link link = new Link(1, URI.create("https://localhost"), LinkState.INITIALIZED);
        Link skippedLink = link.skip();
        doNothing().when(crawlerRepository).setLinkState(skippedLink);

        // act
        WebCrawler.LinkStatus linkStatus = webCrawler.skip(link);

        // assert
        verify(crawlerRepository).setLinkState(skippedLink);
        assertThat(linkStatus).isEqualTo(WebCrawler.LinkStatus.SKIPPED);
    }

    @Test
    void given_webcrawler_when_clone_and_shorten_html_content_is_empty_expect_shortened_html_content() {
        // arrange
        DownloadedDocument downloadedDocument = new DownloadedDocument(null,URI.create("https://localhost"), "", new Date(), new Date(), DocumentState.INITIALIZED);

        // act
        DownloadedDocument downloadedDocumentWithShortenedHtmlContent = WebCrawler.cloneButShortenHtmlContent(downloadedDocument);

        // assert
        assertThat(downloadedDocumentWithShortenedHtmlContent).isNotNull();
    }

    @Test
    void given_webcrawler_when_clone_and_shorten_html_content_is_large_expect_shortened_html_content() {
        // arrange
        DownloadedDocument downloadedDocument = new DownloadedDocument(1, URI.create("https://localhost"),
                "012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",
                new Date(), new Date(), DocumentState.INITIALIZED);

        // act
        DownloadedDocument downloadedDocumentWithShortenedHtmlContent = WebCrawler.cloneButShortenHtmlContent(downloadedDocument);

        // assert
        assertThat(downloadedDocumentWithShortenedHtmlContent.content().length()).isEqualTo(200);
    }

    @Test
    void given_webcrawler_when_log_downloaded_document_and_analyzed_document_expect_log() {
        // arrange
        DownloadedDocument downloadedDocument = new DownloadedDocument(1, URI.create("https://localhost"),
                "<html></html>", new Date(), new Date(), DocumentState.INITIALIZED);
        AnalyzedDocument analyzedDocument = new AnalyzedDocument(1, "title", null,
                List.of(), List.of(), List.of(), List.of(), new Date(), new Date(), null);

        // act
        webCrawler.failsafeLog(downloadedDocument, analyzedDocument);

        // assert
        verify(jsonLogger).failsafeInfo(any());
    }

    @Test
    void given_webcrawler_when_log_link_and_downloaded_document_expect_log() {
        // arrange
        Link link = new Link(1, URI.create("https://localhost"), LinkState.INITIALIZED);
        DownloadedDocument downloadedDocument = new DownloadedDocument(1, URI.create("https://localhost"),
                "<html></html>", new Date(), new Date(), DocumentState.INITIALIZED);

        // act
        webCrawler.failsafeLog(link, downloadedDocument);

        // assert
        verify(jsonLogger).failsafeInfo(any());
    }

    @Test
    void given_webcrawler_when_log_link_and_link_state_expect_log() {
        // arrange
        Link link = new Link(1, URI.create("https://localhost"), LinkState.INITIALIZED);

        // act
        webCrawler.failsafeLog(link);

        // assert
        verify(jsonLogger).failsafeInfo(any());
    }

}
