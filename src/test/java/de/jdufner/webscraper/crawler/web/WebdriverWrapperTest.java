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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebdriverWrapperTest {

    @Mock(extraInterfaces = JavascriptExecutor.class)
    private WebDriver webDriver;

    @InjectMocks
    private WebdriverWrapper webdriverWrapper;

    @Test
    void given_wrapper_when_get_html_url_expect_url_passed_through() {
        // arrange
        WebDriver.TargetLocator tl = mock(WebDriver.TargetLocator.class);
        when(webDriver.switchTo()).thenReturn(tl);
        when(tl.activeElement()).thenReturn(mock(WebElement.class));
        when(webDriver.findElements(any())).thenReturn(Collections.emptyList());

        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
        doReturn(100L).when(javascriptExecutor).executeScript("return window.innerHeight");
        doReturn(100L).when(javascriptExecutor).executeScript("return document.body.scrollHeight");
        doReturn(0L).when(javascriptExecutor).executeScript("return document.body.scrollHeight - window.scrollY - window.innerHeight");
        doReturn(0L).when(javascriptExecutor).executeScript("window.scrollTo(0, document.body.scrollHeight)");

        doReturn("<html></html>").when(webDriver).getPageSource();

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
        doReturn(100L).when(javascriptExecutor).executeScript("return window.innerHeight");
        doReturn(100L).when(javascriptExecutor).executeScript("return document.body.scrollHeight");
        doReturn(100L).doReturn(0L).when(javascriptExecutor).executeScript("return document.body.scrollHeight - window.scrollY - window.innerHeight");
        doReturn(0L).when(javascriptExecutor).executeScript("window.scrollBy(0, 100)");
        doReturn(0L).when(javascriptExecutor).executeScript("window.scrollTo(0, document.body.scrollHeight)");

        doReturn("<html></html>").when(webDriver).getPageSource();

        // act
        webdriverWrapper.getHtml("https://www.heise.de");

        // assert
        verify(javascriptExecutor).executeScript("window.scrollBy(0, 100)");
    }

    @Test
    void given_wrapper_when_get_html_page_content_expect_tag_present() {
        // arrange
        WebDriver.TargetLocator tl = mock(WebDriver.TargetLocator.class);
        when(webDriver.switchTo()).thenReturn(tl);
        when(tl.activeElement()).thenReturn(mock(WebElement.class));
        when(webDriver.findElements(any())).thenReturn(Collections.emptyList());

        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
        doReturn(100L).when(javascriptExecutor).executeScript("return window.innerHeight");
        doReturn(100L).when(javascriptExecutor).executeScript("return document.body.scrollHeight");
        doReturn(0L).when(javascriptExecutor).executeScript("return document.body.scrollHeight - window.scrollY - window.innerHeight");
        doReturn(0L).when(javascriptExecutor).executeScript("window.scrollTo(0, document.body.scrollHeight)");

        doReturn("<title>heise online - IT-News, Nachrichten und Hintergründe | heise online</title>").when(webDriver).getPageSource();

        // act
        String htmlContent = webdriverWrapper.getHtml("https://www.heise.de");

        // assert
        assertThat(htmlContent).isNotNull();
        assertThat(htmlContent).isNotEmpty();
        assertThat(htmlContent).contains("<title>heise online");
    }

}
