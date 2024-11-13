package de.jdufner.webscraper.crawler.image;

import org.asynchttpclient.AsyncHttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ImageConfigurationTest {

    @InjectMocks
    private ImageConfiguration imageConfiguration;

    @Test
    void when_call_dsl_static_builder_expect_async_http_client() {
        // arrange

        // act
        AsyncHttpClient asc = imageConfiguration.getAsyncHttpClient();

        // assert
        assertThat(asc).isInstanceOf(AsyncHttpClient.class);
    }

}
