package de.jdufner.webscraper;

import de.jdufner.webscraper.crawler.SeleniumConfiguration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    private final SeleniumConfiguration seleniumConfiguration;

    public AppConfiguration(SeleniumConfiguration seleniumConfiguration) {
        this.seleniumConfiguration = seleniumConfiguration;
    }

    @Bean
    public WebDriver getChromeDriver() {
        ChromeOptions chromeOptions = new ChromeOptions();
        seleniumConfiguration.options().forEach(chromeOptions::addArguments);
        return new ChromeDriver(chromeOptions);
    }

}
