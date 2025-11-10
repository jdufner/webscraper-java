package de.jdufner.webscraper.crawler.image;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Configuration
public class ImageConfiguration {

    @Bean
    public @NonNull AsyncHttpClient asyncHttpClient() {
        return Dsl.asyncHttpClient();
    }

    @Bean
    public @NonNull HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

    @Bean
    public @NonNull MessageDigest messageDigest() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
