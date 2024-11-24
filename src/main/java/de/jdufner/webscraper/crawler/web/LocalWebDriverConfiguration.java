package de.jdufner.webscraper.crawler.web;

import org.jspecify.annotations.NonNull;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("localWebDriver")
public class LocalWebDriverConfiguration {

    private final WebdriverConfigurationProperties webdriverConfigurationProperties;

    public LocalWebDriverConfiguration(@NonNull WebdriverConfigurationProperties webdriverConfigurationProperties) {
        this.webdriverConfigurationProperties = webdriverConfigurationProperties;
    }

    @Bean
    public @NonNull WebDriver webDriver() {
        ChromeOptions chromeOptions = new ChromeOptions();
        webdriverConfigurationProperties.options().forEach(chromeOptions::addArguments);
        return new ChromeDriver(chromeOptions);
    }

}
