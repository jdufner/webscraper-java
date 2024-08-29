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
        long pageYOffset = getPageYOffset();
        long scrollY = getScrollY();
        long scrollPosition = 0;
        int index = 1;
//        logger.info("innerHeight = {}, scrollHeight = {}, scrollPosition = {}", innerHeight, scrollHeight, scrollPosition);
        while (scrollPosition < (long)(.99 * (scrollHeight - innerHeight)) && index <= 100) {
            scrollToPosition(index * innerHeight);
            Thread.sleep(1000);
            scrollPosition = getScrollPosition();
            long newScrollHeight = getScrollHeight();
//            logger.info("scrollPosition = {}, newScrollHeight = {}", scrollPosition, newScrollHeight);
            if (newScrollHeight > scrollHeight) {
                index = -1;
                scrollHeight = newScrollHeight;
            }
            index += 1;

        }
        scrollToEndOfPage();
    }

    private long getScrollY() {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
        return (Long) javascriptExecutor.executeScript("return window.scrollY");
    }

    private long getPageYOffset() {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
        return (Long) javascriptExecutor.executeScript("return window.pageYOffset");
    }

    private void scrollToPosition(long position) throws Exception {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
        javascriptExecutor.executeScript("window.scrollBy(0, " + position + ")");
    }

    private void scrollToEndOfPage() {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
        javascriptExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    private long getScrollPosition() {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
        return (Long) javascriptExecutor.executeScript("return window.pageYOffset + window.innerHeight");
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
