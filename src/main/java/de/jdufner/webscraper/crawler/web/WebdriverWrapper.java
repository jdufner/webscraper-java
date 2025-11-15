package de.jdufner.webscraper.crawler.web;

import org.jspecify.annotations.NonNull;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static java.util.Arrays.stream;

@Service
public class WebdriverWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebdriverWrapper.class);

    @NonNull
    private final WebCrawlerConfigurationProperties config;

    @NonNull
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
        sleep(500);
        consentCookies();
        sleep(500);
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
        long scrollY = executeScriptAndReturnLong("return window.scrollY");
        long scrollHeight = executeScriptAndReturnLong("return document.body.scrollHeight");
        double scrollYPercent = 1d * scrollY / scrollHeight;
        long remainingHeight = scrollHeight - scrollY - innerHeight;
        LOGGER.debug("scrollDownPageByPage innerHeight: {}, scrollY: {}, scrollHeight: {}, remainingHeight: {}, scrollY (%): {}}", innerHeight, scrollY, scrollHeight, remainingHeight, scrollYPercent);
        int index = 1;
        while (remainingHeight > 0.1 * innerHeight && index <= 100) {
            LOGGER.debug("scrollDownPageByPage index={}", index);
            executeScript("window.scrollBy(0, " + innerHeight + ")");
            long newScrollY = executeScriptAndReturnLong("return window.scrollY");
            long newScrollHeight = executeScriptAndReturnLong("return document.body.scrollHeight");
            double newScrollYPercent = 1d * newScrollY / newScrollHeight;
            long newRemainingHeight = newScrollHeight - newScrollY - innerHeight;
            LOGGER.debug("scrollDownPageByPage innerHeight: {}, scrollY: {}, scrollHeight: {}, remainingHeight: {}, scrollY (%): {}", innerHeight, newScrollY, newScrollHeight, newRemainingHeight, newScrollYPercent);
            sleep(250);
            index += 1;
            if (newScrollHeight != scrollHeight) {
                index = 1;
            }
            scrollHeight = newScrollHeight;
            remainingHeight = newRemainingHeight;
        }
        executeScript("window.scrollTo(0, document.body.scrollHeight)");
    }

    private long executeScriptAndReturnLong(@NonNull String script) {
        LOGGER.debug("enter executeScriptAndReturnLong({})", script);
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
        long l = ((Number) Objects.requireNonNull(javascriptExecutor.executeScript(script))).longValue();
        LOGGER.debug("exit executeScriptAndReturnLong(..): {}", l);
        return l;
    }

    private void executeScript(@NonNull String script) {
        LOGGER.debug("enter executeScript({})", script);
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
        javascriptExecutor.executeScript(script);
        LOGGER.debug("exit executeScript()");
    }

    private void consentCookies() {
        if (config.consentCookies() && !cookieConsented) {
            webDriver.switchTo().frame(webDriver.switchTo().activeElement());
            clickButtonWithTitle("Zustimmen");
            clickButtonWithTitle("Agree");
            // Alternative approach
            // executeScript("document.querySelectorAll('button[title=\"Zustimmen\"]').forEach(button => {button.click();})");
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
