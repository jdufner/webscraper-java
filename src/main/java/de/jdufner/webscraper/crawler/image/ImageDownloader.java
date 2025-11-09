package de.jdufner.webscraper.crawler.image;

import de.jdufner.webscraper.crawler.config.SiteConfigurationProperties;
import de.jdufner.webscraper.crawler.data.*;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.Optional;

import static java.util.Arrays.asList;

@Service
public class ImageDownloader {

    enum ImageStatus {
        NOT_FOUND, DOWNLOADED, SKIPPED, ERROR
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageDownloader.class);

    @NonNull
    private final ImageDownloaderConfigurationProperties imageDownloaderConfigurationProperties;
    @NonNull
    private final SiteConfigurationProperties siteConfigurationProperties;
    @NonNull
    private final CrawlerRepository crawlerRepository;
    @NonNull
    private final ImageGetter imageGetter;
    @NonNull
    private final ImageAnalyzer imageAnalyzer;

    private int numberDownloadedImages = 0;

    public ImageDownloader(@NonNull ImageDownloaderConfigurationProperties imageDownloaderConfigurationProperties,
                           @NonNull SiteConfigurationProperties siteConfigurationProperties,
                           @NonNull CrawlerRepository crawlerRepository,
                           @NonNull ImageGetterAhc imageGetter,
                           @NonNull ImageAnalyzer imageAnalyzer) {
        this.imageDownloaderConfigurationProperties = imageDownloaderConfigurationProperties;
        this.siteConfigurationProperties = siteConfigurationProperties;
        this.crawlerRepository = crawlerRepository;
        this.imageGetter = imageGetter;
        this.imageAnalyzer = imageAnalyzer;
    }

    void download() {
        LOGGER.debug("download()");
        if (numberDownloadedImages < imageDownloaderConfigurationProperties.numberImages()) {
            ImageStatus imageStatus = findAndDownloadNextImage();
            if (imageStatus == ImageStatus.DOWNLOADED) {
                numberDownloadedImages++;
            }
        }
    }

    private ImageStatus findAndDownloadNextImage() {
        Optional<Image> optionalImage = crawlerRepository.getNextImageIfAvailable();
        return optionalImage.map(this::handleImage).orElse(ImageStatus.NOT_FOUND);
    }

    private @NonNull ImageStatus handleImage(Image image) {
        try {
            if (siteConfigurationProperties.isNotBlocked(image.uri()) &&
                    hasAcceptedFileExtension(image.uri().toString())) {
                downloadAndSave(image);
                return ImageStatus.DOWNLOADED;
            } else {
                setState(image, ImageState.SKIPPED);
                return ImageStatus.SKIPPED;
            }
        } catch (Exception e) {
            LOGGER.error("Error while downloading image", e);
            setState(image, ImageState.ERROR);
            return ImageStatus.ERROR;
        }
    }

    private void downloadAndSave(Image image) {
        // TODO why to store the file locally instead of using S3 protocol (MinIO)
        DownloadedImage downloadedImage = download(image);
        //crawlerRepository.setImageDownloadedAndFilename(image, file);
        crawlerRepository.saveDownloadedImage(downloadedImage);
    }

    private void setState(@NonNull Image image, @NonNull ImageState state) {
        crawlerRepository.setImageState(image, state);
    }

    @NonNull DownloadedImage download(@NonNull Image image) {
        LOGGER.debug("Downloading {}", image.uri());
        Date downloadStartedAt = new Date();
        File file = buildAndValidateFileName(image.uri());
        imageGetter.download(image.uri(), file);
        AnalyzedImage analyzedImage = imageAnalyzer.analyze(file);
        Date downloadFinishedAt = new Date();
        LOGGER.debug("Downloaded {} of size {} in {} seconds", file.getPath(), file.length(), (downloadFinishedAt.getTime() - downloadStartedAt.getTime())/1000d);
        return new DownloadedImage(image.id(), ImageState.DOWNLOADED, file.getPath(),
                downloadStartedAt, downloadFinishedAt,
                analyzedImage.fileSize(), analyzedImage.dimensionHeight(), analyzedImage.dimensionWidth(),
                analyzedImage.hashValue());
    }

    private @NonNull File buildAndValidateFileName(@NonNull URI uri) {
        File file = getFileName(uri);
        hasAcceptedFileExtension(file.getPath());
        file = validateFilename(file);
        return file;
    }

    static @NonNull File validateFilename(@NonNull File file) {
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

    boolean hasAcceptedFileExtension(@NonNull String fileName) {
        try {
            String extension = fileName.substring(fileName.toLowerCase().lastIndexOf('.') + 1);
            return asList(imageDownloaderConfigurationProperties.fileExtensions()).contains(extension);
        }  catch (Exception e) {
            LOGGER.error("Error while checking file extension", e);
            return false;
        }
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
