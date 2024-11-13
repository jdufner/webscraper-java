package de.jdufner.webscraper.crawler.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Collections;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebdriverWrapperTest {

    @Mock(extraInterfaces = JavascriptExecutor.class)
    private WebDriver webDriver;

    @InjectMocks
    private WebdriverWrapper webdriverWrapper;

    @Test
    void given_wrapper_when_get_Html_url_expect_url_passed_through() {
        // arrange
        WebDriver.TargetLocator tl = mock(WebDriver.TargetLocator.class);
        when(webDriver.switchTo()).thenReturn(tl);
        when(tl.activeElement()).thenReturn(mock(WebElement.class));
        when(webDriver.findElements(any())).thenReturn(Collections.emptyList());

        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
        when(Objects.requireNonNull(javascriptExecutor.executeScript("return window.innerHeight"))).thenReturn(100L);
        when(Objects.requireNonNull(javascriptExecutor.executeScript("return document.body.scrollHeight"))).thenReturn(100L);
        when(Objects.requireNonNull(javascriptExecutor.executeScript("return document.body.scrollHeight - window.scrollY - window.innerHeight"))).thenReturn(0L);
        when(Objects.requireNonNull(javascriptExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight)"))).thenReturn(0L);

        // act
        webdriverWrapper.getHtml("https://www.heise.de");

        // assert
        verify(webDriver).get("https://www.heise.de");
    }

    @Test
    void given_wrapper_when_not_yet_scrolled_to_end_of_page_expect_scroll_down() {
        // arrange
        WebDriver.TargetLocator tl = mock(WebDriver.TargetLocator.class);
        when(webDriver.switchTo()).thenReturn(tl);
        when(tl.activeElement()).thenReturn(mock(WebElement.class));
        when(webDriver.findElements(any())).thenReturn(Collections.emptyList());

        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
        when(Objects.requireNonNull(javascriptExecutor.executeScript("return window.innerHeight"))).thenReturn(100L);
        when(Objects.requireNonNull(javascriptExecutor.executeScript("return document.body.scrollHeight"))).thenReturn(100L);
        when(Objects.requireNonNull(javascriptExecutor.executeScript("return document.body.scrollHeight - window.scrollY - window.innerHeight"))).thenReturn(100L).thenReturn(0L);
        when(Objects.requireNonNull(javascriptExecutor.executeScript("window.scrollBy(0, 100)"))).thenReturn(0L);
        when(Objects.requireNonNull(javascriptExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight)"))).thenReturn(0L);

        // act
        webdriverWrapper.getHtml("https://www.heise.de");

        // assert
        verify(javascriptExecutor).executeScript("window.scrollBy(0, 100)");
    }

    @Test
    void given_wrapper_when_get_Html_page_content_expect_tag_present() {
        // arrange
        WebDriver.TargetLocator tl = mock(WebDriver.TargetLocator.class);
        when(webDriver.switchTo()).thenReturn(tl);
        when(tl.activeElement()).thenReturn(mock(WebElement.class));
        when(webDriver.findElements(any())).thenReturn(Collections.emptyList());

        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
        when(Objects.requireNonNull(javascriptExecutor.executeScript("return window.innerHeight"))).thenReturn(100L);
        when(Objects.requireNonNull(javascriptExecutor.executeScript("return document.body.scrollHeight"))).thenReturn(100L);
        when(Objects.requireNonNull(javascriptExecutor.executeScript("return document.body.scrollHeight - window.scrollY - window.innerHeight"))).thenReturn(0L);
        when(Objects.requireNonNull(javascriptExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight)"))).thenReturn(0L);

        when(Objects.requireNonNull(webDriver.getPageSource())).thenReturn("<title>heise online - IT-News, Nachrichten und Hintergründe | heise online</title>");

        // act
        String htmlContent = webdriverWrapper.getHtml("https://www.heise.de");

        // assert
        assertThat(htmlContent).isNotNull();
        assertThat(htmlContent).isNotEmpty();
        assertThat(htmlContent).contains("<title>heise online");
    }

}
