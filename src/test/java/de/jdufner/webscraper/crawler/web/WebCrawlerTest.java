package de.jdufner.webscraper.crawler.web;

import de.jdufner.webscraper.crawler.config.SiteConfiguration;
import de.jdufner.webscraper.crawler.dao.HsqldbRepository;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebCrawlerTest {

    @Mock
    private WebCrawlerConfiguration webCrawlerConfiguration;

    @Mock
    private SiteConfiguration siteConfiguration;

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
        when(webCrawlerConfiguration.startUrl()).thenReturn(url);
        when(webCrawlerConfiguration.numberPages()).thenReturn(3);
        HtmlPage htmlPageStart = new HtmlPage(URI.create(url), "", new Date(), "", null, emptyList(), emptyList(), emptyList(), emptyList());
        when(webFetcher.get(url)).thenReturn(htmlPageStart);
        Link link = new Link(1, URI.create("https://www.continue.com"));
        HtmlPage htmlPageContinue = new HtmlPage(link.uri(), "", new Date(), "", null, emptyList(), emptyList(), emptyList(), emptyList());
        when(webFetcher.get(link.uri().toString())).thenReturn(htmlPageContinue);
        when(repository.getNextLinkIfAvailable()).thenReturn(Optional.of(link));
        when(siteConfiguration.isValidAndNotBlocked(any())).thenReturn(true);

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

}
