package de.jdufner.webscraper.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebFetcherTest {

    @Mock
    private SeleniumWrapper seleniumWrapper;

    @InjectMocks
    private WebFetcher webFetcher;

    @Test
    public void given_webfetcher_when_get_html_then_expect_jsoup_document() {
        // arrange
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
        webFetcher.get("https://www.spiegel.de");

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
        String title = webFetcher.extractTitle(document);

        // assert
        assertThat(title).isEqualTo("This is a title");
    }

    @Test
    public void given_html_when_extract_create_at_then_expect_datetime() {
        // arrange
        String timestamp = "2024-09-05T21:30:00+02:00";
        Date date = null;
        try {
            date = WebFetcher.DATE_FORMAT.parse(timestamp);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Document document = Jsoup.parse("""
                <html>
                  <body>
                    <h1>A Header</>
                    <div class="a-publish-info">"""
                + "        <time datetime=\"" + timestamp + "\">" + """
                        <span>2024-09-05</span>
                        <span>21:30</span>
                      </time>
                    </div>
                  </body>
                </html>""");

        // act
        Date createdAt = webFetcher.extractCreatedAt(document);

        // assert
        assertThat(createdAt).isEqualTo(date);
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
        String creator = webFetcher.extractCreator(document);

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
        String categories = webFetcher.extractCategories(document);

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
        List<URI> urls = webFetcher.extractLinks("https://www.spiegel.de", document);

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
        List<URI> urls = webFetcher.extractImages("https://www.spiegel.de", document);

        // assert
        assertThat(urls).containsExactly(new URI("https://www.spiegel.de/test.jpg"));
    }

}
