package de.jdufner.webscraper.crawler.web;

import de.jdufner.webscraper.crawler.data.HtmlPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ConfigurationPropertiesScan
@DirtiesContext
class WebFetcherIT {

    @Autowired
    private WebFetcher webFetcher;

    @Test
    public void fetch() {
        // arrange

        // act
        HtmlPage htmlPage = webFetcher.get("https://www.heise.de");

        // assert
        assertThat(htmlPage).isNotNull();
    }

}
