package de.jdufner.webscraper.picturechoice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PictureSelectorIT {

    @Autowired
    private PictureRepository pictureRepository;

    @Autowired
    private PictureSelector pictureSelector;

    @Test
    void given_table_when_then() {
        // arrange
        pictureRepository.save(new Picture(Path.of("/tmp/webscraper/image1.jpg"), "/webscraper/image1.jpg", Picture.State.INITIALIZED));
        pictureRepository.save(new Picture(Path.of("/tmp/webscraper/image2.jpg"), "/webscraper/image2.jpg", Picture.State.INITIALIZED));
        pictureRepository.save(new Picture(Path.of("/tmp/webscraper/image3.jpg"), "/webscraper/image3.jpg", Picture.State.INITIALIZED));
        pictureRepository.save(new Picture(Path.of("/tmp/webscraper/image4.jpg"), "/webscraper/image4.jpg", Picture.State.INITIALIZED));
        pictureRepository.save(new Picture(Path.of("/tmp/webscraper/image5.jpg"), "/webscraper/image5.jpg", Picture.State.INITIALIZED));

        // act
        Picture[] pictures = pictureSelector.selectTwoRandomPictures();

        // assert
        assertThat(pictures.length).isEqualTo(2);
    }

}
