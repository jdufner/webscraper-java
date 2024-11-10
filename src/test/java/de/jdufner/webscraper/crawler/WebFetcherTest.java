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
import java.util.Optional;

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
    public void given_html_when_title_present_then_expect_title() {
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
        Optional<String> title = webFetcher.extractTitle(document);

        // assert
        assertThat(title.orElse(null)).isEqualTo("This is a title");
    }

    @Test
    public void given_html_when_title_not_present_then_expect_null() {
        // arrange
        Document document = Jsoup.parse("""
                <html>
                  <head></head>
                  <body>
                    <h1>A Header</>
                  </body>
                </html>""");

        // act
        Optional<String> title = webFetcher.extractTitle(document);

        // assert
        assertThat(title.orElse(null)).isNull();
    }

    @Test
    public void given_html_when_create_at_present_then_expect_datetime() {
        // arrange
        String timestamp = "2024-09-05T21:30:00+02:00";
        Date date;
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
        Optional<Date> createdAt = webFetcher.extractCreatedAt(document);

        // assert
        assertThat(createdAt.orElse(null)).isEqualTo(date);
    }

    @Test
    public void given_html_when_create_at_present_in_wrong_format_then_expect_null() {
        // arrange
        String timestamp = "05.09.2024 21:30:00";
        Date date = null;
        try {
            date = WebFetcher.DATE_FORMAT.parse(timestamp);
        } catch (ParseException ignored) {
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
        Optional<Date> createdAt = webFetcher.extractCreatedAt(document);

        // assert
        assertThat(createdAt.orElse(null)).isEqualTo(date);
    }

    @Test
    public void given_html_when_create_at_not_present_then_expect_null() {
        // arrange
        Document document = Jsoup.parse("""
                <html>
                  <body>
                    <h1>A Header</h1>
                    <div class="a-publish-info">
                      <time>
                        <span>2024-09-05</span>
                        <span>21:30</span>
                      </time>
                    </div>
                  </body>
                </html>""");

        // act
        Optional<Date> createdAt = webFetcher.extractCreatedAt(document);

        // assert
        assertThat(createdAt.orElse(null)).isNull();
    }

    @Test
    public void given_html_when_creator_present_then_expect_list_of_names() {
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
        List<String> authors = webFetcher.extractAuthors(document);

        // assert
        assertThat(authors).containsExactly("Vorname Nachname");
    }

    @Test
    public void given_html_when_multiple_creators_present_then_expect_list_of_names() {
        // arrange
        Document document = Jsoup.parse("""
                <html>
                  <body>
                    <h1>A Header</>
                    <div class="creator">
                      <ul>
                        <li>Vorname1 Nachname1</li>
                        <li>Vorname2 Nachname2</li>
                      </ul>
                    </div>
                  </body>
                </html>""");

        // act
        List<String> authors = webFetcher.extractAuthors(document);

        // assert
        assertThat(authors).containsExactly("Vorname1 Nachname1","Vorname2 Nachname2");
    }

    @Test
    public void given_html_when_creator_not_present_then_expect_empty_list() {
        // arrange
        Document document = Jsoup.parse("""
                <html>
                  <body>
                    <h1>A Header</>
                    </div>
                  </body>
                </html>""");

        // act
        List<String> authors = webFetcher.extractAuthors(document);

        // assert
        assertThat(authors).isEmpty();
    }

    @Test
    public void given_html_when_categories_present_then_expect_list_of_categories() {
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
        List<String> categories = webFetcher.extractCategories(document);

        // assert
        assertThat(categories).containsExactly("Category 1", "Category 2");
    }

    @Test
    public void given_html_when_one_category_present_then_expect_list_of_categories() {
        // arrange
        Document document = Jsoup.parse("""
                <html>
                  <body>
                    <h1>A Header</>
                    <div class="content-categories">
                      <a>Category 1</a>
                    </div>
                  </body>
                </html>""");

        // act
        List<String> categories = webFetcher.extractCategories(document);

        // assert
        assertThat(categories).containsExactly("Category 1");
    }

    @Test
    public void given_html_when_no_category_present_then_expect_empty_list() {
        // arrange
        Document document = Jsoup.parse("""
                <html>
                  <body>
                    <h1>A Header</>
                  </body>
                </html>""");

        // act
        List<String> categories = webFetcher.extractCategories(document);

        // assert
        assertThat(categories).isEmpty();
    }

    @Test
    public void given_html_when_relative_link_present_then_expect_url() {
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
        assertThat(urls).containsExactly(URI.create("https://www.spiegel.de/test.html"));
    }

    @Test
    public void given_html_when_absolute_link_present_then_expect_url() {
        // arrange
        Document document = Jsoup.parse("""
                <html>
                  <body>
                    <h1>A Header</>
                    <a href="https://www.google.com/">Google</a>
                  </body>
                </html>""");

        // act
        List<URI> urls = webFetcher.extractLinks("https://www.spiegel.de", document);

        // assert
        assertThat(urls).containsExactly(URI.create("https://www.google.com/"));
    }

    @Test
    public void given_html_when_absolute_links_present_then_expect_urls() {
        // arrange
        Document document = Jsoup.parse("""
                <html>
                  <body>
                    <h1>A Header</>
                    <a href="https://www.google.com/">Google</a>
                    <a href="https://www.heise.de/">Heise Medien</a>
                  </body>
                </html>""");

        // act
        List<URI> urls = webFetcher.extractLinks("https://www.spiegel.de", document);

        // assert
        assertThat(urls).containsExactly(URI.create("https://www.google.com/"),URI.create("https://www.heise.de/"));
    }

    @Test
    public void given_html_when_no_links_present_then_expect_no_urls() {
        // arrange
        Document document = Jsoup.parse("""
                <html>
                  <body>
                    <h1>A Header</>
                  </body>
                </html>""");

        // act
        List<URI> urls = webFetcher.extractLinks("https://www.spiegel.de", document);

        // assert
        assertThat(urls).isEmpty();
    }

    @Test
    public void given_html_when_relative_image_then_expect_url() {
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
        assertThat(urls).containsExactly(URI.create("https://www.spiegel.de/test.jpg"));
    }

    @Test
    public void given_html_when_absolute_image_then_expect_url() {
        // arrange
        Document document = Jsoup.parse("""
                <html>
                  <body>
                    <h1>A Header</>
                    <img src="https://www.google.com/test.jpg">
                  </body>
                </html>""");

        // act
        List<URI> urls = webFetcher.extractImages("https://www.spiegel.de", document);

        // assert
        assertThat(urls).containsExactly(URI.create("https://www.google.com/test.jpg"));
    }

}
