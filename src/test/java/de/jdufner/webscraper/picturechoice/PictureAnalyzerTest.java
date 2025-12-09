package de.jdufner.webscraper.picturechoice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PictureAnalyzerTest {

    @Mock
    private PictureChoiceConfigurationProperties pictureChoiceConfigurationProperties;

    @InjectMocks
    private PictureAnalyzer pictureAnalyzer;

    @Test
    void given_picture_directory_when_analyzing_expect_picture_metadata_stored_in_database() {
        // arrange
        when(pictureChoiceConfigurationProperties.directory()).thenReturn("C:\\Users\\jurge\\AppData\\Local\\Temp\\webscraper");
        when(pictureChoiceConfigurationProperties.filenamePattern()).thenReturn("glob:*.{jpeg,jpg,png,webp}");

        // act
        pictureAnalyzer.analyze();

        // assert
    }

    @Test
    void given_static_location_when_static_location_prefix_expect_sub_path() {
        // arrange
        Path staticLocationDirectory = Path.of("C:\\Users\\jurge\\AppData\\Local\\Temp\\webscraper");
        Path file = Path.of("C:\\Users\\jurge\\AppData\\Local\\Temp\\webscraper\\nature\\dd3f29vge00g1.jpeg");

        // act
        String subPath = PictureAnalyzer.determinePath(staticLocationDirectory, file);

        // assert
        assertThat(subPath).isEqualTo("/nature/dd3f29vge00g1.jpeg");
    }

    @Test
    void given_static_location_when_static_location_does_not_prefix_expect_null() {
        // arrange
        Path staticLocationDirectory = Path.of("C:\\Users\\jurge\\AppData\\Local\\Temp\\webscraper-test");
        Path file = Path.of("C:\\Users\\jurge\\AppData\\Local\\Temp\\webscraper\\nature\\dd3f29vge00g1.jpeg");

        // act
        String subPath = PictureAnalyzer.determinePath(staticLocationDirectory, file);

        // assert
        assertThat(subPath).isNull();
    }

}
