package de.jdufner.webscraper.crawler.image;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ImageGetterJavaHttpClientIT {

    @Autowired
    private ImageGetterJavaHttpClient imageGetterJavaHttpClient;

    @Test
    void given_configuration_when_download_expect_output_file_created() throws Exception {
        // arrange
        URI uri = URI.create("https://apod.nasa.gov/apod/image/2506/IssMoon_Holland_1063.jpg");
        File file = new File("./apod.nasa.gov/apod/image/2506/IssMoon_Holland_1063.jpg");
        file.delete();
        try {
            file.getParentFile().mkdirs();

            // act
            imageGetterJavaHttpClient.download(uri, file);

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
                imageGetterJavaHttpClient.download(uri, file);

                // assert
                assertThat(file.length()).isGreaterThan(0);
            } finally {
                file.delete();
            }
        }
    }

}