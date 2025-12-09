package de.jdufner.webscraper.picturechoice;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PictureFinderTest {

    @Test
    void given_path_when_find_by_extension_expect_all_pictures() throws Exception{
        // arrange
        String separator = FileSystems.getDefault().getSeparator();
        Path directory = Path.of(System.getProperty("java.io.tmpdir")+ separator + "webscraper-test");
        String pattern = "glob:*.{jpeg,jpg,png,webp}";

        File file1 = new File(directory + separator + "file1.jpg");
        FileUtils.touch(file1);
        File file2 = new File(directory + separator + "file2.jpeg");
        FileUtils.touch(file2);
        File file3 = new File(directory + separator + "file3.png");
        FileUtils.touch(file3);
        File file4 = new File(directory + separator + "file4.jpg");
        FileUtils.touch(file4);

        // act
        List<String> pictures = PictureFinder.findPictures(directory, pattern);

        // assert
        assertThat(pictures).hasSize(4);
        assertThat(file1.delete()).isTrue();
        assertThat(file2.delete()).isTrue();
        assertThat(file3.delete()).isTrue();
        assertThat(file4.delete()).isTrue();
    }

}
