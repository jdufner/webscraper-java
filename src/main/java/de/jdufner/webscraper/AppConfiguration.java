package de.jdufner.webscraper;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.jspecify.annotations.NonNull;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    private final WebdriverConfiguration webdriverConfiguration;

    public AppConfiguration(@NonNull WebdriverConfiguration webdriverConfiguration) {
        this.webdriverConfiguration = webdriverConfiguration;
    }

    @Bean
    public @NonNull WebDriver getChromeDriver() {
        ChromeOptions chromeOptions = new ChromeOptions();
        webdriverConfiguration.options().forEach(chromeOptions::addArguments);
        return new ChromeDriver(chromeOptions);
    }

    @Bean
    public @NonNull AsyncHttpClient getAsyncHttpClient() {
        return Dsl.asyncHttpClient();
    }

}
