package de.jdufner.webscraper.crawler.web;

import de.jdufner.webscraper.crawler.config.SiteConfigurationProperties;
import de.jdufner.webscraper.crawler.data.CrawlerRepository;
import de.jdufner.webscraper.crawler.data.DocumentState;
import de.jdufner.webscraper.crawler.data.DownloadedDocument;
import de.jdufner.webscraper.crawler.data.Link;
import de.jdufner.webscraper.crawler.logger.JsonLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.util.Date;
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
    public void given_webcrawler_when_download_and_not_initialized_expect_download_initial_url() {
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

    @Test void given_webcrawler_when_download_and_initialized_expect_download_link() {
        // arrange
        URI uri = URI.create("https://localhost");
        when(webCrawlerConfigurationProperties.numberPages()).thenReturn(1);
        ReflectionTestUtils.setField(webCrawler, "initialized", true);
        Link link = new Link(1, uri);
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
    public void given_webcrawler_when_download_initial_url_expect_downloaded_and_saved() {
        // arrange
        URI uri = URI.create("https://localhost");
        when(webCrawlerConfigurationProperties.startUrl()).thenReturn(uri.toString());
        when(crawlerRepository.saveUriAsLink(uri)).thenReturn(Optional.of(1));
        DownloadedDocument downloadedDocument = new DownloadedDocument(null, uri, "<html></hml>", new Date(), new Date(), DocumentState.INITIALIZED);
        when(webFetcher.downloadDocument(uri)).thenReturn(downloadedDocument);
        when(crawlerRepository.saveDownloadedDocument(downloadedDocument)).thenReturn(1);
        when(siteConfigurationProperties.isNotBlocked(uri)).thenReturn(true);
        doNothing().when(crawlerRepository).setLinkDownloaded(any(Link.class));

        // act
        webCrawler.downloadInitialUrl();

        // assert
        verify(crawlerRepository).saveUriAsLink(uri);
        verify(webFetcher).downloadDocument(uri);
        verify(crawlerRepository).saveDownloadedDocument(downloadedDocument);
        verify(crawlerRepository).setLinkDownloaded(new Link(1, uri));
    }

    @Test
    public void given_webcrawler_when_no_links_available_expect_nothing_saved() {
        // arrange
        when(webCrawlerConfigurationProperties.numberPages()).thenReturn(100);
        when(crawlerRepository.getNextLinkIfAvailable()).thenReturn(Optional.empty());

        // act
        webCrawler.findAndDownloadLinkUntilConfiguredNumberReached();

        // assert
        verify(crawlerRepository).getNextLinkIfAvailable();
    }

    @Test
    public void given_webcrawler_when_links_available_expect_downloaded_and_saved() {
        // arrange
        when(webCrawlerConfigurationProperties.numberPages()).thenReturn(1);
        Link link = new Link(1, URI.create("http://localhost"));
        when(crawlerRepository.getNextLinkIfAvailable()).thenReturn(Optional.of(link));
        when(siteConfigurationProperties.isNotBlocked(any())).thenReturn(true);
        DownloadedDocument downloadedDocument = new DownloadedDocument(null, link.uri(), "<html></hml>", new Date(), new Date(), DocumentState.INITIALIZED);
        when(webFetcher.downloadDocument(link.uri())).thenReturn(downloadedDocument);
        when(crawlerRepository.saveDownloadedDocument(downloadedDocument)).thenReturn(1);
        doNothing().when(crawlerRepository).setLinkDownloaded(link);

        // act
        webCrawler.findAndDownloadLinkUntilConfiguredNumberReached();

        // assert
        verify(crawlerRepository).getNextLinkIfAvailable();
        verify(webFetcher).downloadDocument(link.uri());
        verify(crawlerRepository).saveDownloadedDocument(downloadedDocument);
        verify(crawlerRepository).setLinkDownloaded(link);
    }

    @Test
    public void given_webcrawler_when_next_link_available_and_not_blocked_expect_status_downloaded() {
        // arrange
        Link link = new Link(1, URI.create("https://localhost"));
        when(crawlerRepository.getNextLinkIfAvailable()).thenReturn(Optional.of(link));
        when(siteConfigurationProperties.isNotBlocked(link.uri())).thenReturn(true);
        when(webFetcher.downloadDocument(link.uri())).thenReturn(new DownloadedDocument(1, link.uri(), "<html></html>", new Date(), new Date(), DocumentState.INITIALIZED));

        // act
        WebCrawler.LinkStatus linkStatus = webCrawler.findAndDownloadNextLink();

        // assert
        assertThat(linkStatus).isEqualTo(WebCrawler.LinkStatus.DOWNLOADED);
    }

    @Test
    public void given_webcrawler_when_next_link_available_but_blocked_expect_status_skipped() {
        // arrange
        Link link = new Link(1, URI.create("https://localhost"));
        when(crawlerRepository.getNextLinkIfAvailable()).thenReturn(Optional.of(link));
        when(siteConfigurationProperties.isNotBlocked(link.uri())).thenReturn(false);

        // act
        WebCrawler.LinkStatus linkStatus = webCrawler.findAndDownloadNextLink();

        // assert
        assertThat(linkStatus).isEqualTo(WebCrawler.LinkStatus.SKIPPED);
    }

    @Test
    public void given_webcrawler_when_next_link_not_available_expect_status_unavailable() {
        // arrange
        when(crawlerRepository.getNextLinkIfAvailable()).thenReturn(Optional.empty());

        // act
        WebCrawler.LinkStatus linkStatus = webCrawler.findAndDownloadNextLink();

        // assert
        assertThat(linkStatus).isEqualTo(WebCrawler.LinkStatus.UNAVAILABLE);
    }

    @Test
    public void given_webcrawler_when_download_link_and_update_status_is_not_blocked_expect_downloaded() {
        // arrange
        Link link = new Link(1, URI.create("https://localhost"));
        when(siteConfigurationProperties.isNotBlocked(link.uri())).thenReturn(true);
        when(webFetcher.downloadDocument(link.uri())).thenReturn(new DownloadedDocument(1, link.uri(), "<html></html>", new Date(), new Date(), DocumentState.INITIALIZED));

        // act
        WebCrawler.LinkStatus linkStatus = webCrawler.downloadLinkAndUpdateStatus(link);

        // assert
        verify(crawlerRepository).setLinkDownloaded(link);
        assertThat(linkStatus).isEqualTo(WebCrawler.LinkStatus.DOWNLOADED);
    }

    @Test
    public void given_webcrawler_when_download_link_and_update_status_is_blocked_expect_skipped() {
        // arrange
        Link link = new Link(1, URI.create("https://localhost"));
        when(siteConfigurationProperties.isNotBlocked(link.uri())).thenReturn(false);

        // act
        WebCrawler.LinkStatus linkStatus = webCrawler.downloadLinkAndUpdateStatus(link);

        // assert
        verify(crawlerRepository).setLinkSkip(link);
        assertThat(linkStatus).isEqualTo(WebCrawler.LinkStatus.SKIPPED);
    }

    @Test
    public void given_webcrawler_when_download_and_save_expect_status_downloaded() {
        // arrange
        Link link = new Link(1, URI.create("https://localhost"));
        DownloadedDocument downloadedDocument = new DownloadedDocument(null, link.uri(), "<html></html>", new Date(), new Date(), DocumentState.INITIALIZED);
        when(webFetcher.downloadDocument(link.uri())).thenReturn(downloadedDocument);
        when(crawlerRepository.saveDownloadedDocument(downloadedDocument)).thenReturn(1);
        doNothing().when(crawlerRepository).setLinkDownloaded(link);

        // act
        WebCrawler.LinkStatus linkStatus = webCrawler.downloadAndSave(link);

        // assert
        verify(webFetcher).downloadDocument(link.uri());
        verify(crawlerRepository).saveDownloadedDocument(downloadedDocument);
        verify(crawlerRepository).setLinkDownloaded(link);
        assertThat(linkStatus).isEqualTo(WebCrawler.LinkStatus.DOWNLOADED);
    }

    @Test
    public void given_webcrawler_when_skip_link_expect_status_skipped() {
        // arrange
        Link link = new Link(1, URI.create("https://localhost"));
        doNothing().when(crawlerRepository).setLinkSkip(any());

        // act
        WebCrawler.LinkStatus linkStatus = webCrawler.skip(link);

        // assert
        verify(crawlerRepository).setLinkSkip(any());
        assertThat(linkStatus).isEqualTo(WebCrawler.LinkStatus.SKIPPED);
    }

}
