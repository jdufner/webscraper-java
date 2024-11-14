package de.jdufner.webscraper.crawler.dao;

import de.jdufner.webscraper.crawler.data.HtmlPage;
import de.jdufner.webscraper.crawler.data.Image;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.URI;
import java.util.Date;
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
    public void when_get_next_image_expect_image() {
        // arrange
        jdbcTemplate.update("insert into IMAGES (URL) values (?)", "https://www.google.com/image.jpg");

        // act
        Optional<Image> image = hsqldbRepository.getNextImageIfAvailable();

        // assert
        assertThat(image.isPresent()).isTrue();
        assertThat(image.get().uri()).isEqualTo(URI.create("https://www.google.com/image.jpg"));
    }

    @Test
    public void when_get_no_images_available_expect_null() {
        // arrange
        jdbcTemplate.update("delete from IMAGES");

        // act
        Optional<Image> image = hsqldbRepository.getNextImageIfAvailable();

        // assert
        assertThat(image.isEmpty()).isTrue();
    }

}
