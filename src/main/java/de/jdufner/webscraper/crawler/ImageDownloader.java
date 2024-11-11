package de.jdufner.webscraper.crawler;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URI;

@Service
public class ImageDownloader {

    private static final Logger logger = LoggerFactory.getLogger(ImageDownloader.class);

    private final Repository repository;
    private final ImageGetter imageGetter;

    public ImageDownloader(@NonNull HsqldbRepository repository, @NonNull ImageGetterAhc imageGetter) {
        this.repository = repository;
        this.imageGetter = imageGetter;
    }

    void downloadAll() {
        URI uri = repository.getNextImageUri();
        download(uri);
    }

    void download(@NonNull URI uri) {
        File file = getFileName(uri);
        logger.debug("Downloading {}", file.getAbsolutePath());
        // TODO: skip other than jpeg and png files
        // TODO: check if file already exists and append _1, _2, _3 .. until a valid filename found
        imageGetter.download(uri, file);
        logger.debug("Downloaded {}", file.getAbsolutePath());
    }

    private static File getFileName(@NonNull URI uri) {
        String filename = uri.getPath();
        filename = checkSecurity(filename);
        File file = new File(filename);
        file.getParentFile().mkdirs();
        return file;
    }

    private static String checkSecurity(@NonNull String filename) {
        String temp = filename;
        temp = removeIllegalCharacters(temp);
        temp = removeDirectoryUpRepeatedly(temp);
        temp = removeMultipleDirectorySeparators(temp);
        temp = removeMultipleDots(temp);
        temp = fixTopLevelDirectory(temp);
        return temp;
    }

    static @NonNull String removeIllegalCharacters(@NonNull String filename) {
        String temp = filename;
        temp = temp.replaceAll("[^a-zA-Z0-9\\-_./]", "_");
        return temp;
    }

    static @NonNull String removeDirectoryUpRepeatedly(@NonNull String filename) {
        String temp = filename;
        while (temp.contains("../")) {
            temp = temp.replace("../", "");
        }
        return temp;
    }

    static @NonNull String removeMultipleDirectorySeparators(@NonNull String filename) {
        String temp = filename;
        while (temp.contains("//")) {
            temp = temp.replace("//", "/");
        }
        return temp;
    }

    static @NonNull String removeMultipleDots(@NonNull String filename) {
        String temp = filename;
        while (temp.contains("..")) {
            temp = temp.replace("..", ".");
        }
        return temp;
    }

    static @NonNull String fixTopLevelDirectory(@NonNull String temp) {
        if (temp.startsWith("/")) {
            temp = temp.substring(1);
        }
        temp = "./" + temp;
        return temp;
    }

}
