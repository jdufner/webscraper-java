package de.jdufner.webscraper.picturechoice;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

@Service
public class PictureAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PictureAnalyzer.class);

    @Value("${spring.web.resources.static-locations}")
    private String staticLocations;

    @NonNull
    private final PictureChoiceConfigurationProperties pictureChoiceConfigurationProperties;
    @NonNull
    private final PictureRepository pictureRepository;

    public PictureAnalyzer(@NonNull PictureChoiceConfigurationProperties pictureChoiceConfigurationProperties,
                    @NonNull PictureRepository pictureRepository) {
        this.pictureChoiceConfigurationProperties = pictureChoiceConfigurationProperties;
        this.pictureRepository = pictureRepository;
    }

    void analyze() {
        LOGGER.debug("staticLocationDirectory: {}", staticLocations);
        if (staticLocations.startsWith("file:")) {
            staticLocations = staticLocations.substring("file:".length());
        }
        List<Path> files = PathFinder.find(pictureDirectory(), fileNamePattern());
        files.forEach(file -> {
            LOGGER.info("{} found", file);
            String htmlFileName = determineHtmlFileName(Path.of(staticLocations), file);
            Picture picture = new Picture(file, htmlFileName, Picture.State.INITIALIZED);
            int id = pictureRepository.save(picture);
        });
    }

    static @Nullable String determineHtmlFileName(@NonNull Path staticLocationDirectory, @NonNull Path file) {
        String directoryName = staticLocationDirectory.toAbsolutePath().toString();
        String fileName = file.toAbsolutePath().toString();
        if (fileName.startsWith(directoryName)) {
            fileName = fileName.substring(directoryName.length());
            fileName = fileName.replace('\\', '/');
            if (!fileName.startsWith("/")) {
                fileName = "/" + fileName;
            }
            return fileName;
        } else {
            return null;
        }
    }

    private @NonNull Path pictureDirectory() {
        return Path.of(pictureChoiceConfigurationProperties.directory());
    }

    private @NonNull String fileNamePattern() {
        return pictureChoiceConfigurationProperties.filenamePattern();
    }

}
