package de.jdufner.webscraper.crawler.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.net.URI;
import java.sql.Types;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Profile("postgres")
//@EnabledIf(value = "#{environment.getActiveProfiles()[0] == 'postgres'}")
class PostgresCrawlerRepositoryIT {

    @Autowired
    private PostgresCrawlerRepository postgresCrawlerRepository;

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
        postgresCrawlerRepository.saveDocument(htmlPage);

        // assert
    }

    @Test
    public void when_html_page_almost_empty_expect_saved() {
        // arrange
        HtmlPage htmlPage = new HtmlPage(URI.create("https://localhost/"), "<html></html>", new Date(), null,
                emptyList(), emptyList(), emptyList(), emptyList());

        // act
        int id = postgresCrawlerRepository.saveDocument(htmlPage);

        // assert
        assertThat(id).isGreaterThanOrEqualTo(0);
    }

    @Test
    public void given_at_least_one_image_in_database_when_get_next_image_expect_image() {
        // arrange
        jdbcTemplate.update("delete from DOCUMENTS_TO_IMAGES where ID > -1000");
        jdbcTemplate.update("delete from IMAGES where ID > -1000");
        jdbcTemplate.update("insert into IMAGES (URL) values (?)", "https://localhost/image_hgjyRxggitggoNEm38Ds.jpg");

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
    public void given_image_when_image_exists_expect_updated() {
        // arrange
        jdbcTemplate.update("delete from DOCUMENTS_TO_IMAGES where ID > -1000");
        jdbcTemplate.update("delete from IMAGES where ID > -1000");
        var filename = "image_p9W5QuCf2kgagJc5ViKu.jpg";
        Image image = new Image(-1, URI.create("http://localhost/" + filename));
        jdbcTemplate.update("insert into IMAGES (URL) values (?)", image.uri().toString());
        Integer id = jdbcTemplate.queryForObject("select ID from IMAGES where URL = ?", new Object[]{image.uri().toString()}, new int[]{Types.VARCHAR}, Integer.class);
        image = new Image(id, image.uri());
        File file = new File(filename);

        // act
        postgresCrawlerRepository.setImageDownloadedAndFilename(image, file);

        // assert
        Object[] data = Objects.requireNonNull(jdbcTemplate.queryForObject(
                "select filename, downloaded from IMAGES where URL = ?",
                (rs, rowNum) ->  new Object[]{rs.getString("filename"), rs.getBoolean("downloaded")},
                image.uri().toString()
        ));
        assertThat(data[0]).isEqualTo(file.getPath());
        assertThat(data[1]).isEqualTo(Boolean.TRUE);
    }

    @Test
    public void given_at_least_one_link_in_database_when_get_next_link_expect_link() {
        // arrange
        jdbcTemplate.update("insert into LINKS (URL) values (?)", "https://www.google.com/");

        // act
        Optional<Link> link = postgresCrawlerRepository.getNextLinkIfAvailable();

        // assert
        assertThat(link.isPresent()).isTrue();
        assertThat(link.get().uri()).isEqualTo(URI.create("https://www.google.com/"));
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
        Link link = new Link(-1, URI.create("https://localhost/"));
        jdbcTemplate.update("insert into LINKS (URL) values (?)", link.uri().toString());
        Integer id = jdbcTemplate.queryForObject("select ID from LINKS where URL = ?", new Object[]{link.uri().toString()}, new int[]{Types.VARCHAR}, Integer.class);
        link = new Link(id, link.uri());

        // act
        postgresCrawlerRepository.setLinkDownloaded(link);

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
        jdbcTemplate.update("delete from DOCUMENTS_TO_IMAGES where ID > -1000");
        jdbcTemplate.update("delete from IMAGES where id > -1000");
        Image image = new Image(-1, URI.create("https://localhost/image_p9W5QuCf2kgagJc5ViKu.jpg"));
        jdbcTemplate.update("insert into IMAGES (URL) values (?)", image.uri().toString());
        Integer id = jdbcTemplate.queryForObject("select ID from IMAGES where URL = ?", new Object[]{image.uri().toString()}, new int[]{Types.VARCHAR}, Integer.class);
        image = new Image(id, image.uri());

        // act
        postgresCrawlerRepository.setImageSkip(image);

        // assert
        Boolean skipped = jdbcTemplate.queryForObject(
                "select SKIP from IMAGES where URL = ?",
                (rs, rowNum) -> rs.getBoolean("SKIP"),
                image.uri().toString()
        );
        assertThat(skipped).isTrue();
    }

    @Test
    public void given_link_when_link_exists_expect_skip_updated() {
        // arrange
        jdbcTemplate.update("delete from DOCUMENTS_TO_LINKS where ID > -1000");
        jdbcTemplate.update("delete from LINKS where id > -1000");
        Link link = new Link(-2, URI.create("https://localhost/"));
        jdbcTemplate.update("insert into LINKS (URL) values (?)", link.uri().toString());
        Integer id = jdbcTemplate.queryForObject("select ID from LINKS where URL = ?", new Object[]{link.uri().toString()}, new int[]{Types.VARCHAR}, Integer.class);
        link = new Link(id, link.uri());

        // act
        postgresCrawlerRepository.setLinkSkip(link);

        // assert
        Boolean skipped = jdbcTemplate.queryForObject(
                "select SKIP from LINKS where id = ?",
                (rs, rowNum) -> rs.getBoolean("SKIP"),
                link.id()
        );
        assertThat(skipped).isTrue();
    }

}
