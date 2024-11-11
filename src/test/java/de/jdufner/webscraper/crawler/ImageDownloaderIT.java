package de.jdufner.webscraper.crawler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;

@SpringBootTest
class ImageDownloaderIT {

    @Autowired
    private ImageDownloader imageDownloader;

    @Test
    void when_download_expect_output_file_created() {
        // arrange
        URI uri = URI.create("https://apod.nasa.gov/apod/image/2409/iss071e564695_4096.jpg");

        // act
        imageDownloader.download(uri);

        // assert
        //when(asyncHttpClient.prepareGet(anyString())).thenReturn(mock(BoundRequestBuilder.class));
    }

}