package de.jdufner.webscraper.picturechoice;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class PictureAnalyzerTrigger {

    private static final Logger LOGGER = LoggerFactory.getLogger(PictureAnalyzerTrigger.class);

    @NonNull
    private final PictureAnalyzer pictureAnalyzer;

    public PictureAnalyzerTrigger(@NonNull PictureAnalyzer pictureAnalyzer) {
        this.pictureAnalyzer = pictureAnalyzer;
    }

    //@Scheduled(fixedDelay = 100, timeUnit = TimeUnit.MILLISECONDS)
    @Scheduled(initialDelay = 1, timeUnit = TimeUnit.SECONDS)
    public void doStartAnalysis() {
        LOGGER.debug("Starting PictureAnalyzer.doStartAnalysis()");
        //if (webCrawlerConfigurationProperties.startDownloadAutomatically()) {
            pictureAnalyzer.analyze();
        //}
    }

}
