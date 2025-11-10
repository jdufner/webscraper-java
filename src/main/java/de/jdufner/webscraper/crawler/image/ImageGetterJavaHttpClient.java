package de.jdufner.webscraper.crawler.image;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

@Service
public class ImageGetterJavaHttpClient implements ImageGetter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageGetterJavaHttpClient.class);

    private final HttpClient httpClient;

    public ImageGetterJavaHttpClient(@NonNull HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void download(@NonNull URI uri, @NonNull File file) {
        HttpRequest request = HttpRequest.newBuilder()
                //.version(HttpClient.Version.HTTP_2)
                .uri(uri)
                .GET()
                .build();
        try {
            HttpResponse.BodyHandler<Path> pathBodyHandler = HttpResponse.BodyHandlers.ofFile(file.toPath());
            HttpResponse<Path> httpResponse = httpClient.send(request, pathBodyHandler);
            if (httpResponse.statusCode() == 200) {
                Path body = httpResponse.body();
            } else {

            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
