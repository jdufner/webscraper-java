package de.jdufner.webscraper.picturechoice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PictureSelectorTest {

    @Mock
    private PictureRepository pictureRepository;

    @InjectMocks
    private PictureSelector pictureSelector;

    @Test
    void given_interval_when_call_thousand_times_expect_min_and_max() {
        // arrange
        int min = 1;
        int max = 10;
        int[] count = new int[max + 1];

        // act
        for (int i = 0; i < 1_000; i++) {
            int random = PictureSelector.randInt(min, max);
            count[random]++;
        }

        // assert
        assertThat(count[0]).isEqualTo(0);
        assertThat(count[1]).isGreaterThanOrEqualTo(1);
        assertThat(count[10]).isGreaterThanOrEqualTo(1);
    }

    @Test
    void given_interval_and_exclude_when_call_thousand_times_expect_min_and_max() {
        // arrange
        int min = 1;
        int max = 10;
        int exclude = 3;
        int[] count = new int[max + 1];

        // act
        for (int i = 0; i < 1_000; i++) {
            int random = PictureSelector.randInt(min, max, exclude);
            count[random]++;
        }

        // assert
        assertThat(count[0]).isEqualTo(0);
        assertThat(count[min]).isGreaterThanOrEqualTo(1);
        assertThat(count[exclude]).isEqualTo(0);
        assertThat(count[max]).isGreaterThanOrEqualTo(1);
    }

    @Test
    void given_table_when_select_then_return_random_pictures() {
        // arrange
        when(pictureRepository.totalNumberPictures()).thenReturn(10);
        when(pictureRepository.loadPictureOrNextAfter(anyInt()))
                .thenReturn(new Picture(Path.of("1"), "1", Picture.State.INITIALIZED))
                .thenReturn(new Picture(Path.of("2"), "2", Picture.State.INITIALIZED));

        // act
        Picture[] pictures = pictureSelector.selectTwoRandomPictures();

        // assert
        assertThat(pictures.length).isEqualTo(2);
    }
}