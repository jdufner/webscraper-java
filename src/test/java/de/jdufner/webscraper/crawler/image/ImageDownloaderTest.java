package de.jdufner.webscraper.crawler.image;

import de.jdufner.webscraper.crawler.config.SiteConfigurationProperties;
import de.jdufner.webscraper.crawler.data.AnalyzedImage;
import de.jdufner.webscraper.crawler.data.CrawlerRepository;
import de.jdufner.webscraper.crawler.data.DownloadedImage;
import de.jdufner.webscraper.crawler.data.Image;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageDownloaderTest {

    @Mock
    private ImageDownloaderConfigurationProperties imageDownloaderConfigurationProperties;

    @Mock
    private SiteConfigurationProperties siteConfigurationProperties;

    @Mock
    private CrawlerRepository crawlerRepository;

    @Mock
    private ImageGetterAhc imageGetter;

    @Mock
    private ImageAnalyzer imageAnalyzer;

    @InjectMocks
    private ImageDownloader imageDownloader;

    @Test
    void given_configuration_when_download_expect_file() {
        // arrange
        when(imageDownloaderConfigurationProperties.fileExtensions()).thenReturn(new String[]{"jpeg", "jpg", "png", "webp"});
        URI uri = URI.create("https://localhost/image.jpg");
        Image image = new Image(1, uri);
        when(imageAnalyzer.analyze(any())).thenReturn(new AnalyzedImage(0L, null, null, null));

        // act
        DownloadedImage downloadedImage = imageDownloader.download(image);

        // assert
        assertThat(downloadedImage.fileName()).isEqualTo(".\\image.jpg");
    }

    @Test
    void given_configuration_when_download_all_expect_url_multiple_times() {
        // arrange
        when(imageDownloaderConfigurationProperties.numberImages()).thenReturn(2);
        when(imageDownloaderConfigurationProperties.fileExtensions()).thenReturn(new String[]{"jpeg", "jpg", "png", "webp"});
        URI uri = URI.create("https://localhost/image.jpg");
        Image image = new Image(1, uri);
        when(crawlerRepository.getNextImageIfAvailable()).thenReturn(Optional.of(image));
        when(siteConfigurationProperties.isNotBlocked(any())).thenReturn(true);
        when(imageAnalyzer.analyze(any())).thenReturn(new AnalyzedImage(0L, null, null, null));

        // act
        imageDownloader.download();
        imageDownloader.download();
        imageDownloader.download();

        // assert
        verify(imageGetter, times(2)).download(uri, new File("./image.jpg"));
    }

    @Test
    void given_string_when_contains_legal_chars_only_expect_same_string() {
        // arrange
        String potentiallyFraudFilename = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_/";

        // act
        String result = ImageDownloader.removeIllegalCharacters(potentiallyFraudFilename);

        // assert
        assertThat(result).isEqualTo(potentiallyFraudFilename);
    }

    @Test
    void given_string_when_contains_illegal_chars_only_expect_cleaned_string() {
        // arrange
        String potentiallyFraudFilename = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_/?*äöüÄÖÜ";

        // act
        String result = ImageDownloader.removeIllegalCharacters(potentiallyFraudFilename);

        // assert
        assertThat(result).isEqualTo("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_/________");
    }

    @Test
    void given_string_when_contains_no_dir_up_expect_same_string() {
        // arrange
        String potentiallyFraudFilename = "abc";

        // act
        String result = ImageDownloader.removeDirectoryUpRepeatedly(potentiallyFraudFilename);

        // assert
        assertThat(result).isEqualTo(potentiallyFraudFilename);
    }

    @Test
    void given_string_when_contains_dir_up_expect_cleaned_string() {
        // arrange
        String potentiallyFraudFilename = "../abc";

        // act
        String result = ImageDownloader.removeDirectoryUpRepeatedly(potentiallyFraudFilename);

        // assert
        assertThat(result).isEqualTo("abc");
    }

    @Test
    void given_string_when_contains_nested_dir_up_expect_cleaned_string() {
        // arrange
        String potentiallyFraudFilename = "....//abc";

        // act
        String result = ImageDownloader.removeDirectoryUpRepeatedly(potentiallyFraudFilename);

        // assert
        assertThat(result).isEqualTo("abc");
    }

    @Test
    void given_string_when_contains_multiple_dir_up_expect_cleaned_string() {
        // arrange
        String potentiallyFraudFilename = "../../abc";

        // act
        String result = ImageDownloader.removeDirectoryUpRepeatedly(potentiallyFraudFilename);

        // assert
        assertThat(result).isEqualTo("abc");
    }

    @Test
    void given_string_when_contains_no_multiple_directory_separators_in_a_row_expect_same_string() {
        // arrange
        String potentiallyFraudFilename = "./abc/def/filename.ext";

        // act
        String result = ImageDownloader.removeMultipleDirectorySeparators(potentiallyFraudFilename);

        // assert
        assertThat(result).isEqualTo("./abc/def/filename.ext");
    }

    @Test
    void given_string_when_contains_multiple_directory_separators_in_a_row_expect_single_directory_separator() {
        // arrange
        String potentiallyFraudFilename = "./abc///def//filename.ext";

        // act
        String result = ImageDownloader.removeMultipleDirectorySeparators(potentiallyFraudFilename);

        // assert
        assertThat(result).isEqualTo("./abc/def/filename.ext");
    }

    @Test
    void given_string_when_contains_no_multiple_dots_in_a_row_expect_single_dot() {
        // arrange
        String potentiallyFraudFilename = "./abc/def/filename.ext";

        // act
        String result = ImageDownloader.removeMultipleDots(potentiallyFraudFilename);

        // assert
        assertThat(result).isEqualTo("./abc/def/filename.ext");
    }

    @Test
    void given_string_when_contains_multiple_dots_in_a_row_expect_single_dot() {
        // arrange
        String potentiallyFraudFilename = "./abc/def/filename..ext";

        // act
        String result = ImageDownloader.removeMultipleDots(potentiallyFraudFilename);

        // assert
        assertThat(result).isEqualTo("./abc/def/filename.ext");
    }

    @Test
    void given_string_starts_with_top_level_directory_expect_relative_directory() {
        // arrange
        String potentiallyFraudFilename = "/abc/def/filename.ext";

        // act
        String result = ImageDownloader.fixTopLevelDirectory(potentiallyFraudFilename);

        // assert
        assertThat(result).isEqualTo("./abc/def/filename.ext");
    }

    @Test
    void given_string_starts_with_no_directory_expect_relative_directory() {
        // arrange
        String potentiallyFraudFilename = "abc/def/filename.ext";

        // act
        String result = ImageDownloader.fixTopLevelDirectory(potentiallyFraudFilename);

        // assert
        assertThat(result).isEqualTo("./abc/def/filename.ext");
    }

    @Test
    void given_filename_when_filename_not_exists_expect_filename_valid() {
        // arrange
        File file = new File("./path/basename.ext");

        // act
        File validatedFilename = ImageDownloader.validateFilename(file);

        // assert
        assertThat(validatedFilename).isEqualTo(file);
    }

    @Test
    void given_filename_when_filename_exists_expect_increased_filename_valid() throws Exception {
        // arrange
        File file = new File("./basename.ext");
        file.createNewFile();

        try {
            // act
            File validatedFilename = ImageDownloader.validateFilename(file);

            // assert
            assertThat(validatedFilename).isEqualTo(new File("./basename_1.ext"));
        } finally {
            file.delete();
        }
    }

    @Test
    void given_file_extensions_when_has_jpg_extension_expect_true() {
        // arrange
        when(imageDownloaderConfigurationProperties.fileExtensions()).thenReturn(new String[]{"jpeg", "jpg", "png", "webp"});
        String filename = "./image.jpg";

        // act
        boolean hasFileExtension = imageDownloader.hasAcceptedFileExtension(filename);

        // assert
        assertThat(hasFileExtension).isTrue();
    }

    @Test
    void given_file_extensions_when_has_no_extension_expect_true() {
        // arrange
        when(imageDownloaderConfigurationProperties.fileExtensions()).thenReturn(new String[]{"jpeg", "jpg", "png", "webp"});
        String filename = "./image";

        // act
        boolean hasFileExtension = imageDownloader.hasAcceptedFileExtension(filename);

        // assert
        assertThat(hasFileExtension).isFalse();
    }

    @Test
    void given_file_extensions_when_has_other_extension_expect_true() {
        // arrange
        when(imageDownloaderConfigurationProperties.fileExtensions()).thenReturn(new String[]{"jpeg", "jpg", "png", "webp"});
        String filename = "./image.svg";

        // act
        boolean hasFileExtension = imageDownloader.hasAcceptedFileExtension(filename);

        // assert
        assertThat(hasFileExtension).isFalse();
    }

}
