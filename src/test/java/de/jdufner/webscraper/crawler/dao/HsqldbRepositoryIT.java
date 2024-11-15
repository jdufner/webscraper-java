package de.jdufner.webscraper.crawler.dao;

import de.jdufner.webscraper.crawler.data.HtmlPage;
import de.jdufner.webscraper.crawler.data.Image;
import de.jdufner.webscraper.crawler.data.Link;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class HsqldbRepositoryIT {

    @Autowired
    private HsqldbRepository hsqldbRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    public void when_html_page_fully_populated_expect_everything_saved() {
        // arrange
        HtmlPage htmlPage = new HtmlPage(URI.create("https://localhost/"), "<html></html>", new Date(), "title", null,
                asList("vorname nachname", "first name surname"), asList("nice", "excellent", "fantastic"),
                asList(URI.create("https://www.google.com/"), URI.create("https://www.spiegel.de")),
                asList(URI.create("https://www.google.com/image1.jpg"), URI.create("https://www.spiegel.de/image2.png")));

        // act
        hsqldbRepository.save(htmlPage);

        // assert

    }

    @Test
    public void when_html_page_almost_empty_expect_saved() {
        // arrange
        HtmlPage htmlPage = new HtmlPage(URI.create("https://localhost/"), "<html></html>", new Date(), "title", null,
                emptyList(), emptyList(), emptyList(), emptyList());

        // act
        hsqldbRepository.save(htmlPage);

        // assert

    }

    @Test
    public void given_at_least_one_image_in_database_when_get_next_image_expect_image() {
        // arrange
        jdbcTemplate.update("insert into IMAGES (URL) values (?)", "https://www.google.com/image.jpg");

        // act
        Optional<Image> image = hsqldbRepository.getNextImageIfAvailable();

        // assert
        assertThat(image.isPresent()).isTrue();
        assertThat(image.get().uri()).isEqualTo(URI.create("https://www.google.com/image.jpg"));
    }

    @Test
    public void given_no_images_in_database_when_get_no_images_available_expect_empty() {
        // arrange
        jdbcTemplate.update("delete from IMAGES");

        // act
        Optional<Image> image = hsqldbRepository.getNextImageIfAvailable();

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
        hsqldbRepository.setImageDownloadedAndFilename(image, file);

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
        Optional<Link> link = hsqldbRepository.getNextLinkIfAvailable();

        // assert
        assertThat(link.isPresent()).isTrue();
        assertThat(link.get().uri()).isEqualTo(URI.create("https://www.google.com/"));
    }

    @Test
    public void given_no_link_in_database_when_get_next_link_expect_empty() {
        // arrange
        jdbcTemplate.update("delete from LINKS");

        // act
        Optional<Link> link = hsqldbRepository.getNextLinkIfAvailable();

        // assert
        assertThat(link.isEmpty()).isTrue();
    }

    @Test
    public void given_link_when_link_exists_expect_updated() {
        // arrange
        Link link = new Link(-1, URI.create("https://localhost/"));
        jdbcTemplate.update("insert into LINKS (ID, URL) values (?, ?)", link.id(), link.uri().toString());

        // act
        hsqldbRepository.setLinkDownloaded(link);

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
        Image image = new Image(-2, URI.create("https://localhost/"));
        jdbcTemplate.update("insert into IMAGES (ID, URL) values (?, ?)", image.id(), image.uri().toString());

        // act
        hsqldbRepository.setImageSkip(image);

        // assert
        Boolean skipped = jdbcTemplate.queryForObject(
                "select SKIP from IMAGES where id = ?",
                (rs, rowNum) -> rs.getBoolean("SKIP"),
                image.id()
        );
    }

}
