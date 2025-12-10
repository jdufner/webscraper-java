package de.jdufner.webscraper.picturechoice;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

public record Picture(@NonNull Path file, @Nullable String htmlFileName, @NonNull State state) {

    public enum State {
        INITIALIZED,
    }

}
