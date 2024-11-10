package de.jdufner.webscraper.crawler;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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