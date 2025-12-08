package de.jdufner.webscraper.picturechoice;

import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class PictureFinder {

    private final Path directory;
    private final String pattern;

    public PictureFinder(Path directory, String pattern) {
        this.directory = directory;
        this.pattern = pattern;
    }

    List<String> findPictures() throws IOException {
        List<String> list = new ArrayList<String>();

        FileVisitor<Path> matcherVisitor = new SimpleFileVisitor<Path>() {
            @Override
            public @NonNull FileVisitResult visitFile(@NonNull Path file, @NonNull BasicFileAttributes attribs) throws IOException {
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
