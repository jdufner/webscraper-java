package de.jdufner.webscraper.crawler.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.net.URI;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Profile("hsqldb")
class HsqldbCrawlerRepositoryIT {

    @Autowired
    private HsqldbCrawlerRepository hsqldbCrawlerRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    public void when_html_page_fully_populated_expect_everything_saved() {
        // arrange
        HtmlPage htmlPage = new HtmlPage(URI.create("https://localhost/"), "<html></html>", new Date(), null,
                asList("vorname nachname", "first name surname"), asList("nice", "excellent", "fantastic"),
                asList(URI.create("https://www.google.com/"), URI.create("https://www.spiegel.de")),
                asList(URI.create("https://www.google.com/image1.jpg"), URI.create("https://www.spiegel.de/image2.png")));

        // act
        hsqldbCrawlerRepository.saveDocument(htmlPage);

        // assert
    }

    @Test
    public void when_html_page_almost_empty_expect_saved() {
        // arrange
        HtmlPage htmlPage = new HtmlPage(URI.create("https://localhost/"), "<html></html>", new Date(), null,
                emptyList(), emptyList(), emptyList(), emptyList());

        // act
        int id = hsqldbCrawlerRepository.saveDocument(htmlPage);

        // assert
        assertThat(id).isGreaterThanOrEqualTo(0);
    }

    @Test
    public void given_at_least_one_image_in_database_when_get_next_image_expect_image() {
        // arrange
        jdbcTemplate.update("insert into IMAGES (URL) values (?)", "https://www.google.com/image.jpg");

        // act
        Optional<Image> image = hsqldbCrawlerRepository.getNextImageIfAvailable();

        // assert
        assertThat(image.isPresent()).isTrue();
        assertThat(image.get().uri()).isEqualTo(URI.create("https://www.google.com/image.jpg"));
    }

    @Test
    public void given_no_images_in_database_when_get_no_images_available_expect_empty() {
        // arrange
        jdbcTemplate.update("delete from IMAGES where id >= 0");

        // act
        Optional<Image> image = hsqldbCrawlerRepository.getNextImageIfAvailable();

        // assert
        assertThat(image.isEmpty()).isTrue();
    }

    @Test
    public void given_image_when_image_exists_expect_updated() {
        // arrange
        Image image = new Image(-1, URI.create("http://localhost/image.jpg"));
        jdbcTemplate.update("insert into IMAGES (ID, URL) values (?, ?)", image.id(), image.uri().toString());
        File file = new File("image.jpg");

        // act
        hsqldbCrawlerRepository.setImageDownloadedAndFilename(image, file);

        // assert
        Object[] data = Objects.requireNonNull(jdbcTemplate.queryForObject(
                "select filename, downloaded from IMAGES where id = ?",
                (rs, rowNum) ->  new Object[]{rs.getString("filename"), rs.getBoolean("downloaded")},
                image.id()
        ));
        assertThat(data[0]).isEqualTo(file.getPath());
        assertThat(data[1]).isEqualTo(Boolean.TRUE);
    }

    @Test
    public void given_at_least_one_link_in_database_when_get_next_link_expect_link() {
        // arrange
        jdbcTemplate.update("insert into LINKS (URL) values (?)", "https://www.google.com/");

        // act
        Optional<Link> link = hsqldbCrawlerRepository.getNextLinkIfAvailable();

        // assert
        assertThat(link.isPresent()).isTrue();
        assertThat(link.get().uri()).isEqualTo(URI.create("https://www.google.com/"));
    }

    @Test
    public void given_no_link_in_database_when_get_next_link_expect_empty() {
        // arrange
        jdbcTemplate.update("delete from LINKS where id >= 0");

        // act
        Optional<Link> link = hsqldbCrawlerRepository.getNextLinkIfAvailable();

        // assert
        assertThat(link.isEmpty()).isTrue();
    }

    @Test
    public void given_link_when_link_exists_expect_updated() {
        // arrange
        Link link = new Link(-1, URI.create("https://localhost/"));
        jdbcTemplate.update("insert into LINKS (ID, URL) values (?, ?)", link.id(), link.uri().toString());

        // act
        hsqldbCrawlerRepository.setLinkDownloaded(link);

        // assert
        Boolean downloaded = jdbcTemplate.queryForObject(
                "select DOWNLOADED from LINKS where id = ?",
                (rs, rowNum) -> rs.getBoolean("DOWNLOADED"),
                link.id()
        );
        assertThat(downloaded).isTrue();
    }

    @Test
    public void given_image_when_image_exists_expect_skip_updated() {
        // arrange
        jdbcTemplate.update("delete from IMAGES where id <= 0");
        Image image = new Image(-2, URI.create("https://localhost/"));
        jdbcTemplate.update("insert into IMAGES (ID, URL) values (?, ?)", image.id(), image.uri().toString());

        // act
        hsqldbCrawlerRepository.setImageSkip(image);

        // assert
        Boolean skipped = jdbcTemplate.queryForObject(
                "select SKIP from IMAGES where id = ?",
                (rs, rowNum) -> rs.getBoolean("SKIP"),
                image.id()
        );
        assertThat(skipped).isTrue();
    }

    @Test
    public void given_link_when_link_exists_expect_skip_updated() {
        // arrange
        jdbcTemplate.update("delete from LINKS where id <= 0");
        Link link = new Link(-2, URI.create("https://localhost/"));
        jdbcTemplate.update("insert into LINKS (ID, URL) values (?, ?)", link.id(), link.uri().toString());

        // act
        hsqldbCrawlerRepository.setLinkSkip(link);

        // assert
        Boolean skipped = jdbcTemplate.queryForObject(
                "select SKIP from LINKS where id = ?",
                (rs, rowNum) -> rs.getBoolean("SKIP"),
                link.id()
        );
        assertThat(skipped).isTrue();
    }

    @Test
    public void given_document_when_document_downloaded_expect_new_outbox_document() {
        // arrange
        jdbcTemplate.update("delete from DOCUMENTS where id >= 0");
        jdbcTemplate.update("delete from DOCUMENTS_OUTBOX where id >= 0");
        HtmlPage htmlPage = new HtmlPage(URI.create("https://localhost/"), "<html></html>", new Date(), null,
                emptyList(), emptyList(), emptyList(), emptyList());
        jdbcTemplate.update("insert into DOCUMENTS (ID, URL, CONTENT, DOWNLOADED_AT) values (?, ?, ?, ?)", 1, htmlPage.uri().toString(), htmlPage.html(), new Timestamp(htmlPage.downloadedAt().getTime()));
        DocumentOutbox documentOutbox = new DocumentOutbox(1, DocumentProcessState.DOWNLOADED);

        // act
        hsqldbCrawlerRepository.saveDocumentOutbox(documentOutbox);

        // assert
        Number documentOutboxId = jdbcTemplate.queryForObject("select ID from DOCUMENTS_OUTBOX where document_id = ?",
                (rs, rowNum) -> rs.getInt("ID"),
                1
        );
        assertThat(documentOutboxId).isNotNull();
    }

}
