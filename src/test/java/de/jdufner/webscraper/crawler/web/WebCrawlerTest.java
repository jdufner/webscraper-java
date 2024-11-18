package de.jdufner.webscraper.crawler.web;

import de.jdufner.webscraper.crawler.config.SiteConfigurationProperties;
import de.jdufner.webscraper.crawler.data.HsqldbRepository;
import de.jdufner.webscraper.crawler.data.HtmlPage;
import de.jdufner.webscraper.crawler.data.Link;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.Date;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    private HsqldbRepository repository;

    @InjectMocks
    private WebCrawler webCrawler;

    @Test
    public void given_webcrawler_when_crawl_expect_html_page() {
        // arrange
        String url = "https://www.start.com";
        when(webCrawlerConfigurationProperties.startUrl()).thenReturn(url);
        when(webCrawlerConfigurationProperties.numberPages()).thenReturn(3);
        HtmlPage htmlPageStart = new HtmlPage(URI.create(url), "", new Date(), "", null, emptyList(), emptyList(), emptyList(), emptyList());
        when(webFetcher.get(url)).thenReturn(htmlPageStart);
        Link link = new Link(1, URI.create("https://www.continue.com"));
        HtmlPage htmlPageContinue = new HtmlPage(link.uri(), "", new Date(), "", null, emptyList(), emptyList(), emptyList(), emptyList());
        when(webFetcher.get(link.uri().toString())).thenReturn(htmlPageContinue);
        when(repository.getNextLinkIfAvailable()).thenReturn(Optional.of(link));
        when(siteConfigurationProperties.isEligibleAndNotBlocked(any())).thenReturn(true);

        // act
        webCrawler.crawl();

        // assert
        verify(webFetcher, times(1)).get(url);
        verify(repository, times(3)).getNextLinkIfAvailable();
        verify(webFetcher, times(3)).get(link.uri().toString());
        ArgumentCaptor<HtmlPage> savedHtmlPages = ArgumentCaptor.forClass(HtmlPage.class);
        verify(repository, times(4)).save(savedHtmlPages.capture());
        assertThat(savedHtmlPages.getAllValues()).containsExactly(htmlPageStart, htmlPageContinue, htmlPageContinue, htmlPageContinue);
        verify(repository, times(3)).setLinkDownloaded(link);
    }

    @Test
    public void given_webcrawler_when_initial_url_expect_downloaded() {
        // arrange
        URI uri = URI.create("https://localhost");
        when(webCrawlerConfigurationProperties.startUrl()).thenReturn(uri.toString());
        HtmlPage htmlPage = new HtmlPage(uri, "<html></hrml>", new Date(), "", null, emptyList(), emptyList(), emptyList(), emptyList());
        when(webFetcher.get(uri.toString())).thenReturn(htmlPage);
        when(repository.save(htmlPage)).thenReturn(1);

        // act
        webCrawler.downloadInitialUrl();

        // assert
        verify(repository).setLinkDownloaded(new Link(1, uri));
    }

    @Test
    public void given_webcrawler_when_no_links_available_expect_nothing_saved() {
        // arrange
        when(webCrawlerConfigurationProperties.numberPages()).thenReturn(100);
        when(repository.getNextLinkIfAvailable()).thenReturn(Optional.empty());

        // act
        webCrawler.downloadLinks();

        // assert
        verify(repository).getNextLinkIfAvailable();
        verify(repository, times(0)).save(any(HtmlPage.class));
    }

    @Test
    public void given_webcrawler_when_links_available_expect_html_page_saved() {
        // arrange
        when(webCrawlerConfigurationProperties.numberPages()).thenReturn(1);
        Link link = new Link(-1, URI.create("http://localhost"));
        when(repository.getNextLinkIfAvailable()).thenReturn(Optional.of(link));
        when(siteConfigurationProperties.isEligibleAndNotBlocked(any())).thenReturn(true);
        HtmlPage htmlPage = new HtmlPage(link.uri(), "<html></html>", new Date(), "test", null, emptyList(), emptyList(), emptyList(), emptyList());
        when(webFetcher.get(link.uri().toString())).thenReturn(htmlPage);

        // act
        webCrawler.downloadLinks();

        // assert
        verify(repository).getNextLinkIfAvailable();
        verify(repository).save(any(HtmlPage.class));
    }

    @Test
    public void given_webcrawler_when_next_link_available_and_eligible_expect_status_downloaded() {
        // arrange
        Link link = new Link(1, URI.create("https://localhost"));
        when(repository.getNextLinkIfAvailable()).thenReturn(Optional.of(link));
        when(siteConfigurationProperties.isEligibleAndNotBlocked(link.uri())).thenReturn(true);

        // act
        WebCrawler.LinkStatus linkStatus = webCrawler.downloadEligibleNextLink();

        // assert
        assertThat(linkStatus).isEqualTo(WebCrawler.LinkStatus.DOWNLOADED);
    }

    @Test
    public void given_webcrawler_when_next_link_available_but_blocked_expect_status_skipped() {
        // arrange
        Link link = new Link(1, URI.create("https://localhost"));
        when(repository.getNextLinkIfAvailable()).thenReturn(Optional.of(link));
        when(siteConfigurationProperties.isEligibleAndNotBlocked(link.uri())).thenReturn(false);

        // act
        WebCrawler.LinkStatus linkStatus = webCrawler.downloadEligibleNextLink();

        // assert
        assertThat(linkStatus).isEqualTo(WebCrawler.LinkStatus.SKIPPED);
    }

    @Test
    public void given_webcrawler_when_next_link_not_available_expect_status_unavailable() {
        // arrange
        Link link = new Link(1, URI.create("https://localhost"));
        when(repository.getNextLinkIfAvailable()).thenReturn(Optional.empty());

        // act
        WebCrawler.LinkStatus linkStatus = webCrawler.downloadEligibleNextLink();

        // assert
        assertThat(linkStatus).isEqualTo(WebCrawler.LinkStatus.UNAVAILABLE);
    }

    @Test
    public void given_webcrawler_when_download_and_save_expect_status_downloaded() {
        // arrange
        Link link = new Link(1, URI.create("https://localhost"));

        // act
        WebCrawler.LinkStatus linkStatus = webCrawler.downloadAndSave(link);

        // assert
        verify(repository).save(any());
        verify(repository).setLinkDownloaded(link);
        assertThat(linkStatus).isEqualTo(WebCrawler.LinkStatus.DOWNLOADED);
    }

    @Test
    public void given_webcrawler_when_skip_link_expect_status_skipped() {
        // arrange
        Link link = new Link(1, URI.create("https://localhost"));
        //when(repository.setLinkSkip(any())).

        // act
        WebCrawler.LinkStatus linkStatus = webCrawler.skip(link);

        // assert
        verify(repository).setLinkSkip(any());
        assertThat(linkStatus).isEqualTo(WebCrawler.LinkStatus.SKIPPED);
    }

}
