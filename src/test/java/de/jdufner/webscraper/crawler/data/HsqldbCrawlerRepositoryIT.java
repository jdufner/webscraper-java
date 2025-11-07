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

    private void deleteAllDataFromTables() {
        jdbcTemplate.update("delete from DOCUMENTS_TO_AUTHORS");
        jdbcTemplate.update("delete from AUTHORS");
        jdbcTemplate.update("delete from DOCUMENTS_TO_CATEGORIES");
        jdbcTemplate.update("delete from CATEGORIES");
        jdbcTemplate.update("delete from DOCUMENTS_TO_LINKS");
        jdbcTemplate.update("delete from LINKS");
        jdbcTemplate.update("delete from DOCUMENTS_TO_IMAGES");
        jdbcTemplate.update("delete from IMAGES");
        jdbcTemplate.update("delete from DOCUMENTS");
    }

    @Test
    public void when_analyzed_document_fully_populated_expect_everything_saved() {
        // arrange
        deleteAllDataFromTables();
        jdbcTemplate.update("insert into DOCUMENTS (ID, URL, CONTENT, DOWNLOAD_STARTED_AT, DOWNLOAD_STOPPED_AT, STATE) values (?, ?, ?, ?, ?, ?)",
                -1, "https://localhost", "<html></html>", new Timestamp((new Date()).getTime()),
                new Timestamp((new Date()).getTime()), DocumentState.DOWNLOADED.toString());
        AnalyzedDocument analyzedDocument = new AnalyzedDocument(-1, "title", new Date(),
                asList("vorname nachname", "first name surname"), asList("nice", "excellent", "fantastic"),
                asList(URI.create("https://www.google.com/"), URI.create("https://www.spiegel.de")),
                asList(URI.create("https://www.google.com/image1.jpg"), URI.create("https://www.spiegel.de/image2.png")),
                new Date(), new Date());

        // act
        hsqldbCrawlerRepository.saveAnalyzedDocument(analyzedDocument);

        // assert
        Timestamp timestamp = jdbcTemplate.queryForObject("select ANALYSIS_STARTED_AT from DOCUMENTS where ID = ?", Timestamp.class, -1);
        assertThat(timestamp).isNotNull();
    }

    @Test
    public void when_analyzed_date_almost_empty_expect_saved() {
        // arrange
        deleteAllDataFromTables();
        jdbcTemplate.update("insert into DOCUMENTS (ID, URL, CONTENT, DOWNLOAD_STARTED_AT, DOWNLOAD_STOPPED_AT, STATE) values (?, ?, ?, ?, ?, ?)",
                -2, "https://localhost/", "<html></html>", new Timestamp((new Date()).getTime()),
                new Timestamp((new Date()).getTime()), DocumentState.DOWNLOADED.toString());
        AnalyzedDocument analyzedDocument = new AnalyzedDocument(-2, null, null,
                emptyList(), emptyList(), emptyList(), emptyList(), new Date(), new Date());

        // act
        hsqldbCrawlerRepository.saveAnalyzedDocument(analyzedDocument);

        // assert

    }

    @Test
    public void given_at_least_one_image_in_database_when_get_next_image_expect_image() {
        // arrange
        deleteAllDataFromTables();
        jdbcTemplate.update("insert into IMAGES (URL, STATE) values (?, ?)", "https://www.google.com/image.jpg", ImageState.INITIALIZED.toString());

        // act
        Optional<Image> image = hsqldbCrawlerRepository.getNextImageIfAvailable();

        // assert
        assertThat(image.isPresent()).isTrue();
        assertThat(image.get().uri()).isEqualTo(URI.create("https://www.google.com/image.jpg"));
    }

    @Test
    public void given_no_images_in_database_when_get_no_images_available_expect_empty() {
        // arrange
        jdbcTemplate.update("delete from DOCUMENTS_TO_IMAGES where IMAGE_ID >= 0");
        jdbcTemplate.update("delete from IMAGES where ID >= 0");

        // act
        Optional<Image> image = hsqldbCrawlerRepository.getNextImageIfAvailable();

        // assert
        assertThat(image.isEmpty()).isTrue();
    }

    @Test
    public void given_image_when_image_exists_expect_updated() {
        // arrange
        Image image = new Image(-1, URI.create("http://localhost/image.jpg"));
        jdbcTemplate.update("insert into IMAGES (ID, URL, STATE) values (?, ?, ?)", image.id(), image.uri().toString(), ImageState.INITIALIZED.toString());
        File file = new File("image.jpg");

        // act
        hsqldbCrawlerRepository.setImageDownloadedAndFilename(image, file);

        // assert
        Object[] data = Objects.requireNonNull(jdbcTemplate.queryForObject(
                "select FILENAME, STATE from IMAGES where ID = ?",
                (rs, rowNum) ->  new Object[]{rs.getString("FILENAME"), ImageState.valueOf(rs.getString("STATE"))},
                image.id()
        ));
        assertThat(data[0]).isEqualTo(file.getPath());
        assertThat(data[1]).isEqualTo(ImageState.DOWNLOADED);
    }

    @Test
    public void given_at_least_one_link_in_database_when_get_next_link_expect_link() {
        // arrange
        jdbcTemplate.update("insert into LINKS (URL, STATE) values (?, ?)", "https://www.google.com", LinkState.INITIALIZED.toString());

        // act
        Optional<Link> link = hsqldbCrawlerRepository.getNextLinkIfAvailable();

        // assert
        assertThat(link.isPresent()).isTrue();
        assertThat(link.get().uri()).isEqualTo(URI.create("https://www.google.com"));
    }

    @Test
    public void given_no_link_in_database_when_get_next_link_expect_empty() {
        // arrange
        jdbcTemplate.update("delete from DOCUMENTS_TO_LINKS where LINK_ID >= 0");
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
        jdbcTemplate.update("insert into LINKS (ID, URL, STATE) values (?, ?, ?)", link.id(), link.uri().toString(), LinkState.INITIALIZED.toString());

        // act
        hsqldbCrawlerRepository.setLinkDownloaded(link);

        // assert
        LinkState linkSTate = jdbcTemplate.queryForObject(
                "select STATE from LINKS where id = ?",
                (rs, rowNum) -> LinkState.valueOf(rs.getString("STATE")),
                link.id()
        );
        assertThat(linkSTate).isEqualTo(LinkState.DOWNLOADED);
    }

    @Test
    public void given_image_when_image_exists_expect_skip_updated() {
        // arrange
        jdbcTemplate.update("delete from DOCUMENTS_TO_IMAGES where IMAGE_ID <= 0");
        jdbcTemplate.update("delete from IMAGES where id <= 0");
        Image image = new Image(-2, URI.create("https://localhost/"));
        jdbcTemplate.update("insert into IMAGES (ID, URL, STATE) values (?, ?, ?)", image.id(), image.uri().toString(), ImageState.INITIALIZED.toString());

        // act
        hsqldbCrawlerRepository.setImageSkip(image);

        // assert
        ImageState processState = jdbcTemplate.queryForObject(
                "select STATE from IMAGES where id = ?",
                (rs, rowNum) -> ImageState.valueOf(rs.getString(1)),
                image.id()
        );
        assertThat(processState).isEqualTo(ImageState.SKIPPED);
    }

    @Test
    public void given_link_when_link_exists_expect_skip_updated() {
        // arrange
        jdbcTemplate.update("delete from DOCUMENTS_TO_LINKS where LINK_ID <= 0");
        jdbcTemplate.update("delete from LINKS where id <= 0");
        Link link = new Link(-2, URI.create("https://localhost/"));
        jdbcTemplate.update("insert into LINKS (ID, URL, STATE) values (?, ?, ?)", link.id(), link.uri().toString(),  LinkState.INITIALIZED.toString());

        // act
        hsqldbCrawlerRepository.setLinkSkip(link);

        // assert
        LinkState linkState = jdbcTemplate.queryForObject(
                "select STATE from LINKS where id = ?",
                (rs, rowNum) -> LinkState.valueOf(rs.getString("STATE")),
                link.id()
        );
        assertThat(linkState).isEqualTo(LinkState.SKIPPED);
    }

    @Test
    public void given_downloaded_document_when_save_document_then_id() {
        // arrange
        DownloadedDocument downloadedDocument = new DownloadedDocument(null, URI.create("https://localhost/"), "<html></html>", new Date(), new Date());

        // act
        int i = hsqldbCrawlerRepository.saveDownloadedDocument(downloadedDocument);

        // assert
        assertThat(i).isGreaterThanOrEqualTo(0);
    }

    @Test
    public void given_saved_downloaded_document_when_load_document_then_downloaded_document_with_id() {
        // arrange
        deleteAllDataFromTables();
        jdbcTemplate.update("insert into DOCUMENTS (ID, URL, CONTENT, DOWNLOAD_STARTED_AT, DOWNLOAD_STOPPED_AT, STATE) values (?, ?, ?, ?, ?, ?)", 1, "https://localhost/", "<html></html>", new Date(), new Date(), DocumentState.DOWNLOADED.toString());

        // act
        Optional<DownloadedDocument> downloadedDocument = hsqldbCrawlerRepository.findNextDownloadedDocument();

        // assert
        assertThat(downloadedDocument.isPresent()).isTrue();
    }

    @Test
    public void given_not_yet_saved_uri_when_save_expect_new_id() {
        try {
            // arrange
            jdbcTemplate.update("delete from DOCUMENTS_TO_LINKS");
            jdbcTemplate.update("delete from LINKS");

            // act
            Optional<Number> linkId = hsqldbCrawlerRepository.saveUriAsLink(URI.create("https://localhost/"));

            // assert
            assertThat(linkId.isPresent()).isTrue();
        }  finally {
            jdbcTemplate.update("delete from LINKS");
        }
    }

    @Test
    public void given_already_saved_uri_when_save_expect_existing_id() {
        try {
            // arrange
            jdbcTemplate.update("delete from LINKS");
            jdbcTemplate.update("insert into LINKS (ID, URL, STATE) values (?, ?, ?)", 1, "https://localhost", LinkState.INITIALIZED.toString());

            // act
            Optional<Number> linkId = hsqldbCrawlerRepository.saveUriAsLink(URI.create("https://localhost"));

            // assert
            assertThat(linkId.isPresent()).isTrue();
            assertThat(linkId.get().intValue()).isEqualTo(1);
        }  finally {
            jdbcTemplate.update("delete from LINKS");
        }
    }

}
