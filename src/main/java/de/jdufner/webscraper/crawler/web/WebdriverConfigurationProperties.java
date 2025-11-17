package de.jdufner.webscraper.crawler.web;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.openqa.selenium.PageLoadStrategy;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "webdriver.chrome")
public record WebdriverConfigurationProperties(@NonNull List<String> options, @Nullable String pageLoadStrategy) {

    public @NonNull PageLoadStrategy getPageLoadStrategy() {
        if (pageLoadStrategy() == null) {
            return PageLoadStrategy.NORMAL;
        }
        return switch (pageLoadStrategy()) {
            case "eager" -> PageLoadStrategy.EAGER;
            case "none" -> PageLoadStrategy.NONE;
            default -> PageLoadStrategy.NORMAL;
        };
//        return Optional.ofNullable(pageLoadStrategy())
//                .map(pageLoadStrategy -> switch ((pageLoadStrategy)) {
//                    case "eager" -> PageLoadStrategy.EAGER;
//                    case "none" -> PageLoadStrategy.NONE;
//                    default -> PageLoadStrategy.NORMAL;
//                })
//                .orElse(PageLoadStrategy.NORMAL);
    }

}
