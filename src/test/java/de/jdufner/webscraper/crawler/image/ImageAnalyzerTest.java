package de.jdufner.webscraper.crawler.image;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.security.MessageDigest;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageAnalyzerTest {

    @Mock
    private ImageConfiguration imageConfiguration;

    @InjectMocks
    private ImageAnalyzer imageAnalyzer;

    @Test
    public void given_jpeg_when_analyze_expect_dimensions() {
        // arrange
        File file = new File("src/test/resources/images/Schmetterling.jpg");

        // act
        ImageAnalyzer.Dimension dimension = ImageAnalyzer.getImageDimensions(file);

        // assert
        assertThat(Objects.requireNonNull(dimension).width()).isEqualTo(1993);
        assertThat(Objects.requireNonNull(dimension).height()).isEqualTo(1329);
    }

    @Test
    public void given_png_when_analyze_expect_dimensions() {
        // arrange
        File file = new File("src/test/resources/images/Schmetterling.png");

        // act
        ImageAnalyzer.Dimension dimension = ImageAnalyzer.getImageDimensions(file);

        // assert
        assertThat(Objects.requireNonNull(dimension).width()).isEqualTo(989);
        assertThat(Objects.requireNonNull(dimension).height()).isEqualTo(767);
    }

    @Test
    public void given_webp_when_analyze_expect_dimensions() {
        // arrange
        File file = new File("src/test/resources/images/Schmetterling.webp");

        // act
        ImageAnalyzer.Dimension dimension = ImageAnalyzer.getImageDimensions(file);

        // assert
        assertThat(Objects.requireNonNull(dimension).width()).isEqualTo(1456);
        assertThat(Objects.requireNonNull(dimension).height()).isEqualTo(819);
    }

    @Test
    public void given_jpeg_when_analyze_expect_size() {
        // arrange
        File file = new File("src/test/resources/images/Schmetterling.jpg");

        // act
        long size = ImageAnalyzer.getFileSize(file);

        // assert
        assertThat(size).isEqualTo(1_479_120);
    }

    @Test
    public void given_png_when_analyze_expect_size() {
        // arrange
        File file = new File("src/test/resources/images/Schmetterling.png");

        // act
        long size = ImageAnalyzer.getFileSize(file);

        // assert
        assertThat(size).isEqualTo(1_454_018);
    }

    @Test
    public void given_webp_when_analyze_expect_size() {
        // arrange
        File file = new File("src/test/resources/images/Schmetterling.webp");

        // act
        long size = ImageAnalyzer.getFileSize(file);

        // assert
        assertThat(size).isEqualTo(275_710);
    }

    @Test
    public void given_jpeg_when_calculate_hash1_expect_value() throws Exception {
        // arrange
        when(imageConfiguration.messageDigest()).thenReturn(MessageDigest.getInstance("MD5"));
        File file = new File("src/test/resources/images/Schmetterling.jpg");

        // act
        String hash = imageAnalyzer.calculateHash(file);

        // assert
        assertThat(hash).isEqualTo("41571a9f3291b985bd88a717df22a421");
    }

}
