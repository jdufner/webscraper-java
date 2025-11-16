package de.jdufner.webscraper.crawler.data;

import org.jspecify.annotations.NonNull;

import java.net.URI;

public record Link(int id, @NonNull URI uri, @NonNull LinkState state) {

    public Link skip() {
        if (state != LinkState.INITIALIZED) {
            throw new IllegalStateException();
        }
        return new Link(id, uri, LinkState.SKIPPED);
    }

    public Link download() {
        if (state != LinkState.INITIALIZED) {
            throw new IllegalStateException();
        }
        return new Link(id, uri, LinkState.DOWNLOADED);
    }

}
