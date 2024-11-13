package de.jdufner.webscraper.crawler.image;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImageConfiguration {

    @Bean
    public @NonNull AsyncHttpClient getAsyncHttpClient() {
        return Dsl.asyncHttpClient();
    }

}
