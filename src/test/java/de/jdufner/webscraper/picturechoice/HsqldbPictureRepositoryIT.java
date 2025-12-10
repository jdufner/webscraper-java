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

}
