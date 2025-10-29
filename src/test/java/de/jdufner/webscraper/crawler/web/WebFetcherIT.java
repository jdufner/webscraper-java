package de.jdufner.webscraper.crawler.web;

import de.jdufner.webscraper.crawler.data.DownloadedDocument;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;

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
        DownloadedDocument downloadedDocument = webFetcher.downloadDocument(URI.create("https://www.heise.de"));

        // assert
        assertThat(downloadedDocument).isNotNull();
    }

}
