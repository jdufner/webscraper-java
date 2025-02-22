package de.jdufner.webscraper.crawler.image;

import org.jspecify.annotations.NonNull;

import java.io.File;
import java.net.URI;

public interface ImageGetter {

    void download(@NonNull URI uri, @NonNull File file);

}
