package de.jdufner.webscraper.picturechoice;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface PictureRepository {

    int save(@NonNull Picture picture);

}
