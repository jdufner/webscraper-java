package de.jdufner.webscraper.crawler.image;

import de.jdufner.webscraper.crawler.dao.HsqldbRepository;
import de.jdufner.webscraper.crawler.dao.Repository;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URI;

@Service
public class ImageDownloader {

    private static final Logger logger = LoggerFactory.getLogger(ImageDownloader.class);

    private final ImageDownloaderConfiguration imageDownloaderConfiguration;
    private final Repository repository;
    private final ImageGetter imageGetter;

    public ImageDownloader(@NonNull ImageDownloaderConfiguration imageDownloaderConfiguration, @NonNull HsqldbRepository repository, @NonNull ImageGetterAhc imageGetter) {
        this.imageDownloaderConfiguration = imageDownloaderConfiguration;
        this.repository = repository;
        this.imageGetter = imageGetter;
    }

    void downloadAll() {
        for(int i = 0; i < imageDownloaderConfiguration.numberPages(); i++) {
            URI uri = repository.getNextImageUri();
            download(uri);
        }
    }

    void download(@NonNull URI uri) {
        File file = getFileName(uri);
        logger.debug("Downloading {}", file.getAbsolutePath());
        hasImageExtension(file);
        file = validateFilename(file);
        imageGetter.download(uri, file);
        logger.debug("Downloaded {}", file.getAbsolutePath());
    }

    static File validateFilename(File file) {
        String fileName = file.getName();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        int index = 1;
        while (file.exists()) {
            file = new File(file.getParentFile(), baseName + "_" + index + "." + extension);
            index++;
        }
        return file;
    }

    static boolean hasImageExtension(File file) {
        String extension = file.getName().substring(file.getName().lastIndexOf('.') + 1).toLowerCase();
        return extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg");
    }

    private static @NonNull File getFileName(@NonNull URI uri) {
        String filename = uri.getPath();
        filename = checkSecurity(filename);
        File file = new File(filename);
        file.getParentFile().mkdirs();
        return file;
    }

    private static @NonNull String checkSecurity(@NonNull String filename) {
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
