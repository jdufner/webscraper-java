package de.jdufner.webscraper.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebCrawlerTest {

    @Mock
    private WebCrawlerConfiguration webCrawlerConfiguration;

    @Mock
    private SeleniumWrapper seleniumWrapper;

    @InjectMocks
    private WebCrawler webCrawler;

    @Test
    public void given_webcrawler_when_get_html_then_expect_jsoup_document() {
        // arrange
        when(webCrawlerConfiguration.startUrl()).thenReturn("https://www.spiegel.de");
        when(webCrawlerConfiguration.numberPages()).thenReturn(100);
        when(webCrawlerConfiguration.numberImages()).thenReturn(100);
        when(seleniumWrapper.getHtml("https://www.spiegel.de")).thenReturn("""
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
        webCrawler.startCrawling();

        // assert

    }

    @Test
    public void given_html_when_extract_title_then_expect_title() {
        // arrange
        Document document = Jsoup.parse("""
                <html>
                  <head>
                    <title>This is a title</title>
                  </head>
                  <body>
                    <h1>A Header</>
                  </body>
                </html>""");

        // act
        String title = webCrawler.extractTitle(document);

        // assert
        assertThat(title).isEqualTo("This is a title");
    }

    @Test
    public void given_html_when_extract_create_at_then_expect_datetime() {
        // arrange
        Document document = Jsoup.parse("""
                <html>
                  <body>
                    <h1>A Header</>
                    <div class="a-publish-info">
                      <time datetime="2024-09-05T21:30:00+02:00">
                        <span>2024-09-05</span>
                        <span>21:30</span>
                      </time>
                    </div>
                  </body>
                </html>""");

        // act
        String createdAt = webCrawler.extractCreatedAt(document);

        // assert
        assertThat(createdAt).isEqualTo("2024-09-05T21:30:00+02:00");
    }

    @Test
    public void given_html_when_extract_creator_then_expect_name() {
        // arrange
        Document document = Jsoup.parse("""
                <html>
                  <body>
                    <h1>A Header</>
                    <div class="creator">
                      <ul>
                        <li>Vorname Nachname</li>
                      </ul>
                    </div>
                  </body>
                </html>""");

        // act
        String creator = webCrawler.extractCreator(document);

        // assert
        assertThat(creator).isEqualTo("Vorname Nachname");
    }

    @Test
    public void given_html_when_extract_categories_then_expect_categories() {
        // arrange
        Document document = Jsoup.parse("""
                <html>
                  <body>
                    <h1>A Header</>
                    <div class="content-categories">
                      <a>Category 1</a>
                      <a>Category 2</a>
                    </div>
                  </body>
                </html>""");

        // act
        String categories = webCrawler.extractCategories(document);

        // assert
        assertThat(categories).isEqualTo("Category 1,Category 2");
    }

    @Test
    public void given_html_when_extract_links_then_expect_urls() throws Exception {
        // arrange
        Document document = Jsoup.parse("""
                <html>
                  <body>
                    <h1>A Header</>
                    <a href="./test.html">test.html</a>
                  </body>
                </html>""");

        // act
        List<URI> urls = webCrawler.extractLinks("https://www.spiegel.de", document);

        // assert
        assertThat(urls).containsExactly(new URI("https://www.spiegel.de/test.html"));
    }

    @Test
    public void given_html_when_extract_images_then_expect_urls() throws Exception {
        // arrange
        Document document = Jsoup.parse("""
                <html>
                  <body>
                    <h1>A Header</>
                    <img src="./test.jpg">
                  </body>
                </html>""");

        // act
        List<URI> urls = webCrawler.extractImages("https://www.spiegel.de", document);

        // assert
        assertThat(urls).containsExactly(new URI("https://www.spiegel.de/test.jpg"));
    }

}
