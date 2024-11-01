package de.jdufner.webscraper.crawler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WebCrawlerTest {

    @Mock
    private WebCrawlerConfiguration webCrawlerConfiguration;

    @Mock
    private WebFetcher webFetcher;

    @InjectMocks
    private WebCrawler webCrawler;

    @Test
    public void given_webcrawler_when_crawl_expect_html_page() {
        // arrange
        String url = "https://www.google.com";
        when(webCrawlerConfiguration.startUrl()).thenReturn(url);
        when(webCrawlerConfiguration.numberPages()).thenReturn(100);
        when(webFetcher.get(url)).thenReturn(new HtmlPage(URI.create(url), "", "", null, emptyList(), emptyList(), emptyList(), emptyList()));

        // act
        webCrawler.crawl();

        // assert
    }

}
