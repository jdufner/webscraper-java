package de.jdufner.webscraper.crawler;

import org.assertj.core.api.Assertions;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageDownloaderTest {

    @Mock
    private HsqldbRepository repository;

    @Mock
    private AsyncHttpClient asyncHttpClient;

    @InjectMocks
    private ImageDownloader imageDownloader;

    @Test
    void when_download_all_expect_url() {
        // arrange
        String url = "https://test.com/image.jpg";
        URI uri = URI.create(url);
        when(repository.getNextImageUri()).thenReturn(uri);
        when(asyncHttpClient.prepareGet(anyString())).thenReturn(mock(BoundRequestBuilder.class));

        // act
        imageDownloader.downloadAll();

        // assert
        verify(asyncHttpClient).prepareGet(url);
    }

    @Test
    void when_download_expect_output_file_created() {
        // arrange
        String url = "https://test.com/image.jpg";
        URI uri = URI.create(url);
        when(asyncHttpClient.prepareGet(anyString())).thenReturn(mock(BoundRequestBuilder.class));

        // act
        imageDownloader.download(uri);

        // assert
        //when(asyncHttpClient.prepareGet(anyString())).thenReturn(mock(BoundRequestBuilder.class));
    }

}