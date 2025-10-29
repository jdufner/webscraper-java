package de.jdufner.webscraper.crawler.image;

import de.jdufner.webscraper.crawler.config.SiteConfigurationProperties;
import de.jdufner.webscraper.crawler.data.CrawlerRepository;
import de.jdufner.webscraper.crawler.data.Image;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URI;
import java.util.Optional;

@Service
public class ImageDownloader {

    private static final Logger logger = LoggerFactory.getLogger(ImageDownloader.class);

    @NonNull
    private final ImageDownloaderConfigurationProperties imageDownloaderConfigurationProperties;
    @NonNull
    private final SiteConfigurationProperties siteConfigurationProperties;
    @NonNull
    private final CrawlerRepository crawlerRepository;
    @NonNull
    private final ImageGetter imageGetter;

    public ImageDownloader(@NonNull ImageDownloaderConfigurationProperties imageDownloaderConfigurationProperties,
                           @NonNull SiteConfigurationProperties siteConfigurationProperties,
                           @NonNull CrawlerRepository crawlerRepository,
                           @NonNull ImageGetterAhc imageGetter) {
        this.imageDownloaderConfigurationProperties = imageDownloaderConfigurationProperties;
        this.siteConfigurationProperties = siteConfigurationProperties;
        this.crawlerRepository = crawlerRepository;
        this.imageGetter = imageGetter;
    }

    void downloadAll() {
        for(int i = 0; i < imageDownloaderConfigurationProperties.numberImages(); i++) {
            downloadNextImage();
        }
    }

    private void downloadNextImage() {
        Optional<Image> optionalImage = crawlerRepository.getNextImageIfAvailable();
        if (optionalImage.isPresent()) {
            Image image = optionalImage.get();
            if (siteConfigurationProperties.isNotBlocked(optionalImage.get().uri())) {
                // TODO why to store the file locally instead of using S3 protocol (MinIO)
                File file = download(image.uri());
                crawlerRepository.setImageDownloadedAndFilename(image, file);
            } else {
                crawlerRepository.setImageSkip(image);
            }
        }
    }

    @NonNull File download(@NonNull URI uri) {
        logger.debug("Downloading {}", uri);
        File file = buildAndValidateFilename(uri);
        imageGetter.download(uri, file);
        logger.debug("Downloaded {}", file.getPath());
        return file;
    }

    private static @NonNull File buildAndValidateFilename(URI uri) {
        File file = getFileName(uri);
        hasImageExtension(file);
        file = validateFilename(file);
        return file;
    }

    static @NonNull File validateFilename(File file) {
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
