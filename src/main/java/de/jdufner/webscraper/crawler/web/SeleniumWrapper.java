package de.jdufner.webscraper.crawler.web;

import org.jspecify.annotations.NonNull;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeleniumWrapper {

    private final WebDriver webDriver;

    private boolean cookieConsented = false;

    public SeleniumWrapper(@NonNull WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    public @NonNull String getHtml(@NonNull String url) {
        webDriver.get(url);
        waitUntilCookiesConsentedAndPageFullyLoaded();
        return webDriver.getPageSource();
    }

    private void waitUntilCookiesConsentedAndPageFullyLoaded() {
        sleep(1000);
        consentCookies();
        checkIfPageIsFullyLoaded();
    }

    private void sleep(@NonNull long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkIfPageIsFullyLoaded() {
        webDriver.switchTo().defaultContent();
        scrollDownPageByPage();
    }

    private void scrollDownPageByPage() {
        long innerHeight = getInnerHeight();
        long scrollHeight = getScrollHeight();
        int index = 1;
        while (getPixelsBelowWindow() >= 0.9 * innerHeight && index <= 100) {
            scrollVerticallyBy(innerHeight);
            index += 1;
            sleep(500);
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

    private void scrollVerticallyBy(@NonNull long verticalPixels) {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
        javascriptExecutor.executeScript("window.scrollBy(0, " + verticalPixels + ")");
    }

    private void scrollToEndOfPage() {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
        javascriptExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight)");
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
        if (!cookieConsented) {
            webDriver.switchTo().frame(webDriver.switchTo().activeElement());
            clickButtonWithTitle("Zustimmen");
            clickButtonWithTitle("Agree");
            webDriver.switchTo().defaultContent();
            cookieConsented = true;
        }
    }

    private void clickButtonWithTitle(@NonNull String title) {
        List<WebElement> buttons = webDriver.findElements(By.cssSelector("button[title='" + title + "']"));
        buttons.stream().filter(WebElement::isDisplayed).forEach(WebElement::click);
    }

}
