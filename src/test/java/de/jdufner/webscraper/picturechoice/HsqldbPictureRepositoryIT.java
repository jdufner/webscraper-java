package de.jdufner.webscraper.picturechoice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Profile("hsqldb")
class HsqldbPictureRepositoryIT {

    @Autowired
    private HsqldbPictureRepository hsqldbPictureRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("delete from PICTURES");
    }

    @Test
    void given_when_save_picture_expect_id() {
        // arrange
        Picture picture1 = new Picture(Path.of("/tmp/webscraper/image1.jpg"), "/webscraper/image1.jpg", Picture.State.INITIALIZED);

        // act
        int id = hsqldbPictureRepository.save(picture1);

        // assert
        assertThat(id).isGreaterThanOrEqualTo(0);
    }

    @Test
    void given_zero_picture_when_total_number_pictures_expect_zero() {
        // arrange
        // intentionally insert no data

        // act
        int totalNumberPictures = hsqldbPictureRepository.totalNumberPictures();

        // assert
        assertThat(totalNumberPictures).isEqualTo(0);
    }

    @Test
    void given_one_picture_when_total_number_pictures_expect_one() {
        // arrange
        jdbcTemplate.update("insert into PICTURES (FILENAME, HTML_FILENAME, STATE) values (?, ?, ?)", ps -> {
            ps.setString(1, "/tmp/webscraper/image1.jpg");
            ps.setString(2, "/webscraper/image1.jpg");
            ps.setString(3, Picture.State.INITIALIZED.toString());
        });

        // act
        int totalNumberPictures = hsqldbPictureRepository.totalNumberPictures();

        // assert
        assertThat(totalNumberPictures).isEqualTo(1);
    }

    @Test
    void given_one_picture_when_load_picture_expect_one() {
        // arrange
        jdbcTemplate.update("insert into PICTURES (FILENAME, HTML_FILENAME, STATE) values (?, ?, ?)", ps -> {
            ps.setString(1, "/tmp/webscraper/image1.jpg");
            ps.setString(2, "/webscraper/image1.jpg");
            ps.setString(3, Picture.State.INITIALIZED.toString());
        });

        // act
        Picture picture = hsqldbPictureRepository.loadPictureOrNextAfter(0);

        // assert
        assertThat(picture).isNotNull();
    }

}
