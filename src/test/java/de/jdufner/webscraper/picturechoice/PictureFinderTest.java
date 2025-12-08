package de.jdufner.webscraper.picturechoice;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class PictureFinderTest {

    @Test
    void given_path_when_find_by_extension_expect_all_pictures() throws Exception{
        // arrange
        String separator = FileSystems.getDefault().getSeparator();
        Path directory = Path.of(System.getProperty("java.io.tmpdir")+ separator + "webscraper");
        String pattern = "glob:*.{jpeg,jpg,png,webp}";

        File file = new File(directory + separator + "file1.jpg");
        FileUtils.touch(file);

        // act
        PictureFinder pictureFinder = new PictureFinder(directory, pattern);
        List<String> pictures = pictureFinder.findPictures();

        // assert
        boolean delete = file.delete();
    }

}
