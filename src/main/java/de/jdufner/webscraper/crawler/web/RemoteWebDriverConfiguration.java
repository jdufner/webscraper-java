package de.jdufner.webscraper.crawler.web;

import org.jspecify.annotations.NonNull;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.net.MalformedURLException;
import java.net.URI;

@Configuration
@Profile("remoteWebDriver")
public class RemoteWebDriverConfiguration {

    private final WebdriverConfigurationProperties webdriverConfigurationProperties;

    public RemoteWebDriverConfiguration(@NonNull WebdriverConfigurationProperties webdriverConfigurationProperties) {
        this.webdriverConfigurationProperties = webdriverConfigurationProperties;
    }

    @Bean
    public @NonNull WebDriver webDriver() {
        try {
            ChromeOptions chromeOptions = new ChromeOptions();
            webdriverConfigurationProperties.options().forEach(chromeOptions::addArguments);
            return new RemoteWebDriver(URI.create("http://localhost:4444").toURL(), chromeOptions);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
