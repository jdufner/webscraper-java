package de.jdufner.webscraper.picturechoice;

import org.jspecify.annotations.NonNull;

public interface PictureRepository {

    int save(@NonNull Picture picture);

}
