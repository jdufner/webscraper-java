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
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
        long innerHeight = (Long) javascriptExecutor.executeScript("return window.innerHeight");
        long scrollHeight = (Long) javascriptExecutor.executeScript("return document.body.scrollHeight");
        long scrollPosition = 0;
        logger.debug("innerHeight = {}, scrollHeight = {}", innerHeight, scrollHeight);
        int index = 1;
        while (scrollPosition < (long)(.99 * (scrollHeight - innerHeight)) && index <= 100) {
            javascriptExecutor.executeScript("window.scrollBy(0," + index * innerHeight + ")");
            Thread.sleep(1000);
            scrollPosition = (Long) javascriptExecutor.executeScript("return window.pageYOffset + window.innerHeight");
            long newScrollHeight = (Long) javascriptExecutor.executeScript("return document.body.scrollHeight");
            if (newScrollHeight > scrollHeight) {
                index = -1;
                scrollHeight = newScrollHeight;
            }
            index += 1;
        }
        javascriptExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    private void consentCookies() {
        webDriver.switchTo().frame(webDriver.switchTo().activeElement());
        List<WebElement> buttons = webDriver.findElements(By.cssSelector("button[title='Zustimmen']"));
        buttons.stream().filter(WebElement::isDisplayed).forEach(WebElement::click);
        webDriver.switchTo().defaultContent();
    }

}
