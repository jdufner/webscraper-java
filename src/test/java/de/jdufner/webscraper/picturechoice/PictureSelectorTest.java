package de.jdufner.webscraper.picturechoice;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PictureSelectorTest {

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
        Assertions.assertThat(count[0]).isEqualTo(0);
        Assertions.assertThat(count[1]).isGreaterThanOrEqualTo(1);
        Assertions.assertThat(count[10]).isGreaterThanOrEqualTo(1);
    }

}