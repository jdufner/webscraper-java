package de.jdufner.webscraper.crawler.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebFetcherTest {

    @Mock
    private WebdriverWrapper webdriverWrapper;

    @InjectMocks
    private WebFetcher webFetcher;

    @Test
    public void given_webfetcher_when_get_html_then_expect_jsoup_document() {
        // arrange
        when(webdriverWrapper.getHtml("https://localhost")).thenReturn("""
                <html>
                  <head>
                    <title>This is a title</title>
                  </head>
                  <body>
                    <h1>A Header</>
                    <div class="a-publish-info">
                      <time datetime="2024-09-05T21:30:00+02:00">
                        <span>2024-09-05</span>
                        <span>21:30</span>
                      </time>
                    </div>
                    <div class="creator">
                      <ul>
                        <li>Vorname Nachname</li>
                      </ul>
                    </div>
                    <div class="content-categories">
                      <a>Category 1</a>
                      <a>Category 2</a>
                    </div>
                    <a href="./test.html">test.html</a>
                    <img src="./test.jpg">
                  </body>
                </html>""");

        // act
        webFetcher.downloadDocument(URI.create("https://localhost"));

        // assert

    }

}
