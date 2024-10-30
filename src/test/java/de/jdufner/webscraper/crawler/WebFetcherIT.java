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
public class WebFetcherIT {

    private static final Logger logger = LoggerFactory.getLogger(WebFetcherIT.class);

    @Autowired
    private WebFetcher webFetcher;

    @Test
    public void fetch() {
        // arrange

        // act
        webFetcher.get("https://www.heise.de");

        // assert
    }

}
