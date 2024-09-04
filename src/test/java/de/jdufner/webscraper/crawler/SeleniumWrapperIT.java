package de.jdufner.webscraper.crawler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ConfigurationPropertiesScan
public class SeleniumWrapperIT {

    @Autowired
    private SeleniumWrapper wrapper;

    @Test
    public void given_integrated_wrapper_when_get_page_expect_cookie_consented_and_page_loaded() throws Exception {
        // arrange

        // act
        wrapper.get("https://www.heise.de/");

        // assert
    }

}
