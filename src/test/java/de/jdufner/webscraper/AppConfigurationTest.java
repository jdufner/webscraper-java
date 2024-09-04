package de.jdufner.webscraper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppConfigurationTest {

    @Mock
    private SeleniumConfiguration seleniumConfiguration;

    @InjectMocks
    private AppConfiguration appConfiguration;

    @Test
    public void when_get_chrome_driver_given_selenium_configuration_expect_arguments_added() {
        WebDriver webDriver = null;
        try {
            // arrange
            when(seleniumConfiguration.options()).thenReturn(Arrays.asList("--disable-search-engine-choice-screen", "--disable-notifications", "--disable-infobars", "--disable-extensions"));

            // act
            webDriver = appConfiguration.getChromeDriver();

            // assert
            verify(seleniumConfiguration, times(1)).options();
        } finally {
            if (webDriver != null) {
                webDriver.quit();
            }
        }

    }

}