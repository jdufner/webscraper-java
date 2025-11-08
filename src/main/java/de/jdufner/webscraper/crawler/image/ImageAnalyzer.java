package de.jdufner.webscraper.crawler.image;

import io.micrometer.common.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

@Service
public class ImageAnalyzer {

    void analyze(@NonNull File file) {

    }

    static Dimension getImageDimensions(File file) throws IOException {
        Iterator<ImageReader> readers = ImageIO.getImageReaders(file);
        if (readers.hasNext()) {
            ImageReader reader = readers.next();
            try {
                // Nur den Header lesen, um die Größe zu erhalten
                reader.setInput(ImageIO.createImageInputStream(file));
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                return new Dimension(width, height);
            } finally {
                reader.dispose(); // Ressourcen freigeben
            }
        }
        throw new IOException("Kein ImageReader für die Datei " + file.getName() + " gefunden.");
    }

    static class Dimension {
        public final int width;
        public final int height;
        public Dimension(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

}
