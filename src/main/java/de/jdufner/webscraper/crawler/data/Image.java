package de.jdufner.webscraper.crawler.data;

import org.jspecify.annotations.NonNull;

import java.net.URI;

public record Image(int id, @NonNull URI uri, @NonNull ImageState state) {

    public Image skip() {
        if (state != ImageState.INITIALIZED) {
            throw new IllegalStateException();
        }
        return new Image(id, uri, ImageState.SKIPPED);
    }

    public Image download() {
        if (state != ImageState.INITIALIZED) {
            throw new IllegalStateException();
        }
        return new Image(id, uri, ImageState.DOWNLOADED);
    }

    public Image error() {
        return new Image(id, uri, ImageState.ERROR);
    }

}
