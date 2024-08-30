package de.jdufner.webscraper.crawler;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeleniumWrapper {

    private static final Logger logger = LoggerFactory.getLogger(SeleniumWrapper.class);

    private final WebDriver webDriver;

    SeleniumWrapper(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    public String get(String url) throws Exception {
        webDriver.get(url);
        waitUntilCookiesConsentedAndPageFullyLoaded();
        return webDriver.getPageSource();
    }

    private void waitUntilCookiesConsentedAndPageFullyLoaded() throws Exception {
        Thread.sleep(1000);
        consentCookies();
        checkIfPageIsFullyLoaded();
    }

    private void checkIfPageIsFullyLoaded() throws Exception {
        webDriver.switchTo().defaultContent();
        scrollDownPageByPage();
    }

    private void scrollDownPageByPage() throws Exception {
        long innerHeight = getInnerHeight();
        long scrollHeight = getScrollHeight();
        int index = 1;
        while (getPixelsBelowWindow() >= 0.9 * innerHeight && index <= 100) {
            logger.debug("innerHeight = {}, scrollHeight = {}, index = {}, scrollY = {}", innerHeight, scrollHeight, index, getScrollY());
            scrollVerticallyBy(innerHeight);
            index += 1;
            Thread.sleep(500);
            long newScrollHeight = getScrollHeight();
            if (newScrollHeight > scrollHeight) {
                index = 1;
                scrollHeight = newScrollHeight;
            }

        }
        scrollToEndOfPage();
    }

    private long getPixelsBelowWindow() {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
        return ((Number) javascriptExecutor.executeScript("return document.body.scrollHeight - window.scrollY - window.innerHeight")).longValue();
    }

    private long getScrollY() {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
        return (Long) javascriptExecutor.executeScript("return window.scrollY");
    }

    private void scrollVerticallyBy(long verticalPixels) throws Exception {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
        javascriptExecutor.executeScript("window.scrollBy(0, " + verticalPixels + ")");
    }

    private void scrollToEndOfPage() {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
        javascriptExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    private long getScrollHeight() {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
        return (Long) javascriptExecutor.executeScript("return document.body.scrollHeight");
    }

    private long getInnerHeight() {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
        return (Long) javascriptExecutor.executeScript("return window.innerHeight");
    }

    private void consentCookies() {
        webDriver.switchTo().frame(webDriver.switchTo().activeElement());
        clickButtonWithTitle("Zustimmen");
        clickButtonWithTitle("Agree");
        webDriver.switchTo().defaultContent();
    }

    private void clickButtonWithTitle(String title) {
        List<WebElement> buttons = webDriver.findElements(By.cssSelector("button[title='" + title + "']"));
        buttons.stream().filter(WebElement::isDisplayed).forEach(WebElement::click);
    }

}
