package de.jdufner.webscraper.crawler.image;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ImageGetterAsyncHttpClientIT {

    @Autowired
    private ImageGetterAsyncHttpClient imageGetterAsyncHttpClient;

    @Test
    void given_uri_when_download_expect_output_file_created() throws Exception {
        // arrange
        URI uri = URI.create("https://apod.nasa.gov/apod/image/2506/RosettaDeepRed_Mendez_3294.jpg");
        File file = new File("./apod.nasa.gov/apod/image/2506/RosettaDeepRed_Mendez_3294.jpg");
        file.delete();
        try {
            file.getParentFile().mkdirs();

            // act
            imageGetterAsyncHttpClient.download(uri, file);

            // assert
            assertThat(file.length()).isGreaterThan(0);
        } finally {
            file.delete();
        }
    }

    @Test
    void given_mulitple_uris_when_download_expect_output_files_created() throws Exception {
        // arrange
        String[] urls = new String[] {
                "https://apod.nasa.gov/apod/image/2506/TSE2023-Comp48-2a.jpg",
                "https://apod.nasa.gov/apod/image/2506/farside_lro1600.jpg",
                "https://apod.nasa.gov/apod/image/2507/CatsPaw_Webb_1822.jpg",
                "https://apod.nasa.gov/apod/image/2507/Helix_GC_2332.jpg",
                "https://apod.nasa.gov/apod/image/2507/LUA_JULHO_25_2048.jpg",
        };
        for (String url : urls) {
            URI uri = URI.create(url);
            File file = new File(url.replace("https:/", "."));
            file.delete();
            try {
                file.getParentFile().mkdirs();

                // act
                imageGetterAsyncHttpClient.download(uri, file);

                // assert
                assertThat(file.length()).isGreaterThan(0);
            } finally {
                file.delete();
            }
        }
    }

}
