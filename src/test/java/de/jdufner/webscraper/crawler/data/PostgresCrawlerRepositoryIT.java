package de.jdufner.webscraper.crawler.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Profile("postgres")
class PostgresCrawlerRepositoryIT {

    @Autowired
    private PostgresCrawlerRepository postgresCrawlerRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private void deleteAllDataFromTables() {
        jdbcTemplate.update("delete from DOCUMENTS_TO_AUTHORS where id >= 0");
        jdbcTemplate.update("delete from AUTHORS where id >= 0");
        jdbcTemplate.update("delete from DOCUMENTS_TO_CATEGORIES where id >= 0");
        jdbcTemplate.update("delete from CATEGORIES where id >= 0");
        jdbcTemplate.update("delete from DOCUMENTS_TO_LINKS where id >= 0");
        jdbcTemplate.update("delete from LINKS where id >= 0");
        jdbcTemplate.update("delete from DOCUMENTS_TO_IMAGES where id >= 0");
        jdbcTemplate.update("delete from IMAGES where id >= 0");
        jdbcTemplate.update("delete from DOCUMENTS where id >= 0");
    }

    @Test
    public void when_analyzed_document_fully_populated_expect_everything_saved() {
        // arrange
        deleteAllDataFromTables();
        int documentId = insertDownloadedDocument();
        AnalyzedDocument analyzedDocument = new AnalyzedDocument(documentId, "title", new Date(),
                asList("vorname nachname", "first name surname"), asList("nice", "excellent", "fantastic"),
                asList(URI.create("https://www.google.com/"), URI.create("https://www.spiegel.de")),
                asList(URI.create("https://www.google.com/image1.jpg"), URI.create("https://www.spiegel.de/image2.png")),
                new Date(), new Date());

        // act
        postgresCrawlerRepository.saveAnalyzedDocument(analyzedDocument);

        // assert
    }

    @Test
    public void when_analyzed_document_almost_empty_expect_saved() {
        // arrange
        deleteAllDataFromTables();
        int documentId = insertDownloadedDocument();
        AnalyzedDocument analyzedDocument = new AnalyzedDocument(documentId, null, null,
                emptyList(), emptyList(), emptyList(), emptyList(), new Date(),  new Date());

        // act
        postgresCrawlerRepository.saveAnalyzedDocument(analyzedDocument);

        // assert
    }

    private int insertDownloadedDocument() {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator psc = con -> {
            PreparedStatement ps = con.prepareStatement("insert into DOCUMENTS (URL, CONTENT, DOWNLOAD_STARTED_AT, DOWNLOAD_STOPPED_AT, STATE) values (?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, "https://localhost");
            ps.setString(2, "<html></html>");
            ps.setTimestamp(3, new Timestamp((new Date()).getTime()));
            ps.setTimestamp(4, new Timestamp((new Date()).getTime()));
            ps.setString(5, DocumentState.DOWNLOADED.toString());
            return ps;
        };
        jdbcTemplate.update(psc, keyHolder);
        return ((Number) Objects.requireNonNull(keyHolder.getKeys()).get("ID")).intValue();
    }

    @Test
    public void given_at_least_one_image_in_database_when_get_next_image_expect_image() {
        // arrange
        jdbcTemplate.update("delete from DOCUMENTS_TO_IMAGES where ID > -1000");
        jdbcTemplate.update("delete from IMAGES where ID > -1000");
        jdbcTemplate.update("insert into IMAGES (URL, STATE) values (?, ?)", "https://localhost/image_hgjyRxggitggoNEm38Ds.jpg", ImageState.INITIALIZED.toString());

        // act
        Optional<Image> image = postgresCrawlerRepository.getNextImageIfAvailable();

        // assert
        assertThat(image.isPresent()).isTrue();
        assertThat(image.get().uri()).isEqualTo(URI.create("https://localhost/image_hgjyRxggitggoNEm38Ds.jpg"));
    }

    @Test
    public void given_no_images_in_database_when_get_no_images_available_expect_empty() {
        // arrange
        jdbcTemplate.update("delete from DOCUMENTS_TO_IMAGES where ID > -1000");
        jdbcTemplate.update("delete from IMAGES where ID > -1000");

        // act
        Optional<Image> image = postgresCrawlerRepository.getNextImageIfAvailable();

        // assert
        assertThat(image.isEmpty()).isTrue();
    }

    @Test
    public void given_at_least_one_link_in_database_when_get_next_link_expect_link() {
        // arrange
        jdbcTemplate.update("insert into LINKS (URL, STATE) values (?, ?)", "https://www.google.com", LinkState.INITIALIZED.toString());

        // act
        Optional<Link> link = postgresCrawlerRepository.getNextLinkIfAvailable();

        // assert
        assertThat(link.isPresent()).isTrue();
        assertThat(link.get().uri()).isEqualTo(URI.create("https://www.google.com"));
    }

    @Test
    public void given_no_link_in_database_when_get_next_link_expect_empty() {
        // arrange
        jdbcTemplate.update("delete from LINKS where id >= 0");

        // act
        Optional<Link> link = postgresCrawlerRepository.getNextLinkIfAvailable();

        // assert
        assertThat(link.isEmpty()).isTrue();
    }

    @Test
    public void given_link_when_link_exists_expect_updated() {
        // arrange
        jdbcTemplate.update("delete from DOCUMENTS_TO_LINKS where ID > -1000");
        jdbcTemplate.update("delete from LINKS where id > -1000");
        Link link = new Link(-1, URI.create("https://localhost"), LinkState.INITIALIZED);
        jdbcTemplate.update("insert into LINKS (URL, STATE) values (?, ?)", link.uri().toString(), link.state().toString());
        // TODO read more columns from table to Object[]
        Integer id = jdbcTemplate.queryForObject("select ID from LINKS where URL = ?", new Object[]{link.uri().toString(), }, new int[]{Types.VARCHAR}, Integer.class);
        link = new Link(Optional.ofNullable(id).orElse(0), link.uri(), LinkState.INITIALIZED);

        // act
        postgresCrawlerRepository.setLinkState(link);

        // assert
        LinkState linkState = jdbcTemplate.queryForObject(
                "select STATE from LINKS where id = ?",
                (rs, rowNum) -> LinkState.valueOf(rs.getString("STATE")),
                link.id()
        );
        assertThat(linkState).isEqualTo(LinkState.DOWNLOADED);
    }

    @Test
    public void given_image_when_image_exists_expect_skip_updated() {
        // arrange
        jdbcTemplate.update("delete from DOCUMENTS_TO_IMAGES where ID > -1000");
        jdbcTemplate.update("delete from IMAGES where id > -1000");
        Image image = new Image(-1, URI.create("https://localhost/image_p9W5QuCf2kgagJc5ViKu.jpg"), ImageState.INITIALIZED);
        jdbcTemplate.update("insert into IMAGES (URL, STATE) values (?, ?)", image.uri().toString(), image.state().toString());
        Integer id = jdbcTemplate.queryForObject("select ID from IMAGES where URL = ?", new Object[]{image.uri().toString()}, new int[]{Types.VARCHAR}, Integer.class);
        image = new Image(Optional.ofNullable(id).orElse(0), image.uri(), ImageState.INITIALIZED);
        Image skippedImage = image.skip();

        // act
        postgresCrawlerRepository.setImageState(skippedImage);

        // assert
        ImageState state = jdbcTemplate.queryForObject(
                "select STATE from IMAGES where URL = ?",
                (rs, rowNum) -> ImageState.valueOf(rs.getString("STATE")),
                image.uri().toString()
        );
        assertThat(state).isEqualTo(skippedImage.state());
    }

    @Test
    public void given_link_when_link_exists_expect_skip_updated() {
        // arrange
        jdbcTemplate.update("delete from DOCUMENTS_TO_LINKS where ID > -1000");
        jdbcTemplate.update("delete from LINKS where id > -1000");
        Link link = new Link(-2, URI.create("https://localhost/"), LinkState.INITIALIZED);
        jdbcTemplate.update("insert into LINKS (URL, STATE) values (?, ?)", link.uri().toString(), link.state().toString());
        // TODO read more columns from LINKS to Object[]
        Integer id = jdbcTemplate.queryForObject("select ID from LINKS where URL = ?", new Object[]{link.uri().toString()}, new int[]{Types.VARCHAR}, Integer.class);
        link = new Link(Optional.ofNullable(id).orElse(0), link.uri(), LinkState.INITIALIZED);

        // act
        postgresCrawlerRepository.setLinkState(link);

        // assert
        LinkState linkState = jdbcTemplate.queryForObject(
                "select STATE from LINKS where id = ?",
                (rs, rowNum) -> LinkState.valueOf(rs.getString("STATE")),
                link.id()
        );
        assertThat(linkState).isEqualTo(LinkState.SKIPPED);
    }

}
