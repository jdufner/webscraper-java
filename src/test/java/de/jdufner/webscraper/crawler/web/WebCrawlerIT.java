package de.jdufner.webscraper.crawler.web;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(properties = { "webscraper.crawler.web.number-pages=1" })
@ConfigurationPropertiesScan
@DirtiesContext
class WebCrawlerIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebCrawlerIT.class);

    @Autowired
    private WebCrawler webCrawler;

    @Test
    public void given_setup_when_download_and_analyze_expect_done() {
        // arrange

        // act
        webCrawler.download();
        webCrawler.analyze();

        // assert
    }

}
