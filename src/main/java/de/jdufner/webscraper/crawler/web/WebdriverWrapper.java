package de.jdufner.webscraper.crawler.web;

import org.jspecify.annotations.NonNull;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static java.util.Arrays.stream;

@Service
public class WebdriverWrapper {

    @NonNull
    private final WebCrawlerConfigurationProperties config;
    private final WebDriver webDriver;

    private boolean cookieConsented = false;

    public WebdriverWrapper(@NonNull WebCrawlerConfigurationProperties config, @NonNull WebDriver webDriver) {
        this.config = config;
        this.webDriver = webDriver;
    }

    public @NonNull String getHtml(@NonNull String url) {
        webDriver.get(url);
        waitUntilCookiesConsentedAndPageFullyLoaded();
        return Objects.requireNonNull(webDriver.getPageSource());
    }

    private void waitUntilCookiesConsentedAndPageFullyLoaded() {
        sleep(1000);
        consentCookies();
        executeScriptFromConfig();
        checkIfPageIsFullyLoaded();
    }

    private void sleep(long millis) {
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
        long innerHeight = executeScriptAndReturnLong("return window.innerHeight");
        long scrollHeight = executeScriptAndReturnLong("return document.body.scrollHeight");
        int index = 1;
        while (executeScriptAndReturnLong("return document.body.scrollHeight - window.scrollY - window.innerHeight") >= 0.9 * innerHeight && index <= 100) {
            executeScript("window.scrollBy(0, " + innerHeight + ")");
            index += 1;
            sleep(500);
            long newScrollHeight = executeScriptAndReturnLong("return document.body.scrollHeight");
            if (newScrollHeight > scrollHeight) {
                index = 1;
                scrollHeight = newScrollHeight;
            }

        }
        executeScript("window.scrollTo(0, document.body.scrollHeight)");
    }

    private long executeScriptAndReturnLong(@NonNull String script) {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
        return ((Number) Objects.requireNonNull(javascriptExecutor.executeScript(script))).longValue();
    }

    private void executeScript(@NonNull String script) {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
        javascriptExecutor.executeScript(script);
    }

    private void consentCookies() {
        if (config.consentCookies() && !cookieConsented) {
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

    private void executeScriptFromConfig() {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
        if (config.javascript() != null) {
            stream(config.javascript()).forEach(javascriptExecutor::executeScript);
        }
    }

}
