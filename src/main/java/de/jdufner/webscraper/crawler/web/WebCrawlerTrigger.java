package de.jdufner.webscraper.crawler.web;

import org.jspecify.annotations.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class WebCrawlerTrigger {

    private final @NonNull WebCrawler webCrawler;
    private final @NonNull WebCrawlerConfigurationProperties webCrawlerConfigurationProperties;

    public WebCrawlerTrigger(@NonNull WebCrawlerConfigurationProperties webCrawlerConfigurationProperties,
                             @NonNull WebCrawler webCrawler) {
        this.webCrawlerConfigurationProperties = webCrawlerConfigurationProperties;
        this.webCrawler = webCrawler;
    }

    //@EventListener(ApplicationReadyEvent.class)
    @Scheduled(fixedDelay = 100, timeUnit = TimeUnit.MILLISECONDS)
    public void doStartDownload() {
        if (webCrawlerConfigurationProperties.startAutomatically()) {
            webCrawler.download();
        }
    }

    @Scheduled(fixedDelay = 100, timeUnit = TimeUnit.MILLISECONDS)
    public void doStartAnalysis() {
        if (webCrawlerConfigurationProperties.startAutomatically()) {
            webCrawler.analyze();
        }
    }

}
