package de.jdufner.webscraper.crawler;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@ConfigurationPropertiesScan
@DirtiesContext
public class WebCrawlerIT {

    private static final Logger logger = LoggerFactory.getLogger(WebCrawler.class);

    @Autowired
    private WebCrawler webCrawler;

    @Test
    public void crawl() {
        // arrange

        // act
        webCrawler.startCrawling();

        // assert
    }

}
