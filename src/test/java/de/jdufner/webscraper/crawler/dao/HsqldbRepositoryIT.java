package de.jdufner.webscraper.crawler.dao;

import de.jdufner.webscraper.crawler.data.HtmlPage;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.util.Date;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

@SpringBootTest
class HsqldbRepositoryIT {

    @Autowired
    private HsqldbRepository hsqldbRepository;

    private static final Logger logger = LoggerFactory.getLogger(HsqldbRepositoryIT.class);

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

}
