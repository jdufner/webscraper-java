package de.jdufner.webscraper.crawler.image;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ImageDownloaderIT {

    @Autowired
    private ImageDownloader imageDownloader;

    @Test
    void given_configuration_when_download_expect_output_file_created() {
        // arrange
        URI uri = URI.create("https://apod.nasa.gov/apod/image/2409/iss071e564695_4096.jpg");

        // act
        File file = imageDownloader.download(uri);

        // assert
        assertThat(file).exists();
    }

}