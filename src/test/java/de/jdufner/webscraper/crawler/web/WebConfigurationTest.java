package de.jdufner.webscraper.crawler.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebConfigurationTest {

    @Mock
    private WebdriverConfigurationProperties webdriverConfigurationProperties;

    @InjectMocks
    private WebConfiguration webConfiguration;

    @Test
    void when_get_chrome_driver_given_selenium_configuration_expect_arguments_added() {
        WebDriver webDriver = null;
        try {
            // arrange
            when(webdriverConfigurationProperties.options()).thenReturn(Arrays.asList("--disable-search-engine-choice-screen", "--disable-notifications", "--disable-infobars", "--disable-extensions"));

            // act
            webDriver = webConfiguration.chromeDriver();

            // assert
            verify(webdriverConfigurationProperties, times(1)).options();
        } finally {
            if (webDriver != null) {
                webDriver.quit();
            }
        }
    }

}
