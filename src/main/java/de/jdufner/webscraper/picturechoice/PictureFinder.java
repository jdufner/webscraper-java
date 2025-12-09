package de.jdufner.webscraper.picturechoice;

import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

class PictureFinder {

    static List<String> findPictures(@NonNull Path directory, @NonNull String pattern) throws IOException {
        List<String> list = new ArrayList<>();

        FileVisitor<Path> matcherVisitor = new SimpleFileVisitor<>() {
            @Override
            public @NonNull FileVisitResult visitFile(@NonNull Path file, @NonNull BasicFileAttributes attribs) {
                FileSystem fs = FileSystems.getDefault();
                PathMatcher matcher = fs.getPathMatcher(pattern);
                Path name = file.getFileName();
                if (matcher.matches(name)) {
                    // list.add(name.toString());
                    list.add(file.toAbsolutePath().toString());
                }
                return FileVisitResult.CONTINUE;
            }
        };

        Files.walkFileTree(directory, matcherVisitor);
        return list;
    }

}
