package de.jdufner.webscraper.crawler.web;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class WebCrawlerTrigger {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebCrawlerTrigger.class);

    private final @NonNull WebCrawler webCrawler;
    private final @NonNull WebCrawlerConfigurationProperties webCrawlerConfigurationProperties;

    public WebCrawlerTrigger(@NonNull WebCrawlerConfigurationProperties webCrawlerConfigurationProperties,
                             @NonNull WebCrawler webCrawler) {
        this.webCrawlerConfigurationProperties = webCrawlerConfigurationProperties;
        this.webCrawler = webCrawler;
    }

    @Scheduled(fixedDelay = 100, timeUnit = TimeUnit.MILLISECONDS)
    public void doStartDownload() {
        LOGGER.info("Starting WebCrawler.doStartDownload()");
        if (webCrawlerConfigurationProperties.startAutomatically()) {
            webCrawler.download();
        }
    }

    @Scheduled(fixedDelay = 100, timeUnit = TimeUnit.MILLISECONDS)
    public void doStartAnalysis() {
        LOGGER.info("Starting WebCrawler.doStartAnalysis()");
        if (webCrawlerConfigurationProperties.startAutomatically()) {
            webCrawler.analyze();
        }
    }

}
