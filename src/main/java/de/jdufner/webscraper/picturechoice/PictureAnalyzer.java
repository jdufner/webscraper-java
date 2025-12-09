package de.jdufner.webscraper.picturechoice;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

@Service
class PictureAnalyzer {

    @Value("spring.web.resources.static-locations")
    private String staticLocations;

    @NonNull
    private final PictureChoiceConfigurationProperties pictureChoiceConfigurationProperties;

    PictureAnalyzer(@NonNull PictureChoiceConfigurationProperties pictureChoiceConfigurationProperties) {
        this.pictureChoiceConfigurationProperties = pictureChoiceConfigurationProperties;
    }

    void analyze() {
        List<Path> files = PathFinder.find(pictureDirectory(), fileNamePattern());
        files.forEach(file -> {

        });
    }

    static @Nullable String determinePath(@NonNull Path staticLocationDirectory, @NonNull Path file) {
        String staticlocationDirectory = staticLocationDirectory.toAbsolutePath().toString();
        String fileName = file.toAbsolutePath().toString();
        if (fileName.startsWith(staticlocationDirectory)) {
            fileName = fileName.substring(staticlocationDirectory.length());
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
