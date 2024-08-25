package de.jdufner.webscraper.crawler;

import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Service;

@Service
public class SeleniumWrapper {

    private final WebDriver webDriver;

    SeleniumWrapper(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    public String get(String url) {
        webDriver.get(url);
        return webDriver.getPageSource();
    }

}
