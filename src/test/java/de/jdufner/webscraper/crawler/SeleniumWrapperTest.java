package de.jdufner.webscraper.crawler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeleniumWrapperTest {

    private static final Logger logger = LoggerFactory.getLogger(SeleniumWrapperTest.class);

    @Mock
    private WebDriver webDriver;

    @InjectMocks
    private SeleniumWrapper seleniumWrapper;

    @Test
    void given_wrapper_when_get_url_expect_url_passed_through() throws Exception {
        // arrange

        // act
        seleniumWrapper.get("https://www.heise.de");

        // assert
        verify(webDriver).get("https://www.heise.de");
    }

    @Test
    void given_wrapper_when_get_page_content_expect_tag_present() throws Exception {
        // arrange
        when(webDriver.getPageSource()).thenReturn("<title>heise online - IT-News, Nachrichten und Hintergründe | heise online</title>");

        // act
        String htmlContent = seleniumWrapper.get("https://www.heise.de");

        // assert
        assertThat(htmlContent).isNotNull();
        assertThat(htmlContent).isNotEmpty();
        assertThat(htmlContent).contains("<title>heise online");
    }

}
