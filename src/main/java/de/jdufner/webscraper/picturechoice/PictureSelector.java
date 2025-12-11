package de.jdufner.webscraper.picturechoice;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
class PictureSelector {

    @NonNull
    private final PictureRepository pictureRepository;

    PictureSelector(@NonNull PictureRepository pictureRepository) {
        this.pictureRepository = pictureRepository;
    }

    Picture[] selectTwoRandomPictures() {
        int totalNumberPictures = pictureRepository.totalNumberPictures();
        int picture1Index = randInt(0,  totalNumberPictures - 1);
        Picture picture1 = pictureRepository.loadPictureOrNextAfter(picture1Index);
        int picture2Index = randInt(0,  totalNumberPictures - 1, picture1Index);
        Picture picture2 = pictureRepository.loadPictureOrNextAfter(picture2Index);
        return new Picture[]{picture1, picture2};
    }

    static int randInt(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }

    static int randInt(int min, int max, int exclude) {
        int candidate = randInt(min, max);
        while (candidate == exclude) {
            candidate = randInt(min, max);
        }
        return candidate;
    }

}
