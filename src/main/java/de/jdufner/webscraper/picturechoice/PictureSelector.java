package de.jdufner.webscraper.picturechoice;

import ch.qos.logback.core.joran.sanity.Pair;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
class PictureSelector {

    @NonNull
    private final PictureRepository pictureRepository;

    PictureSelector(@NonNull PictureRepository pictureRepository) {
        this.pictureRepository = pictureRepository;
    }

    Pair<Picture, Picture> selectTwoRandomPictures() {
        int totalNumberPictures = pictureRepository.totalNumberPictures();
        int picture1Index = randInt(1,  totalNumberPictures);
        int picture2Index = randIntExclude(1,  totalNumberPictures, picture1Index);
        return null;
    }

    private static int randInt(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }

    private static int randIntExclude(int min, int max, int exclude) {
        int candidate = randInt(min, max);
        while (candidate == exclude) {
            candidate = randInt(min, max);
        }
        return candidate;
    }

}
