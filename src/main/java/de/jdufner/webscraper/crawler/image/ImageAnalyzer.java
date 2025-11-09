package de.jdufner.webscraper.crawler.image;

import de.jdufner.webscraper.crawler.data.AnalyzedImage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Iterator;

@Service
public class ImageAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageAnalyzer.class);

    @NonNull
    private final ImageConfiguration imageConfiguration;

    record Dimension(@Nullable Integer width, @Nullable Integer height) {
    }

    public ImageAnalyzer(@NonNull ImageConfiguration imageConfiguration) {
        this.imageConfiguration = imageConfiguration;
    }

    @NonNull AnalyzedImage analyze(@NonNull File file) {
        long fileSize = getFileSize(file);
        Dimension dimension = getImageDimensions(file);
        String hashValue = calculateHash(file);
        return new AnalyzedImage(fileSize, dimension.width, dimension.height, hashValue);
    }

    static @NonNull Dimension getImageDimensions(File file) {
        try {
            String mimeType = Files.probeContentType(file.toPath());
            Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType(mimeType);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(ImageIO.createImageInputStream(file));
                    int width = reader.getWidth(0);
                    int height = reader.getHeight(0);
                    return new Dimension(width, height);
                } finally {
                    reader.dispose();
                }
            }
            LOGGER.error("Image reader for mime type {} not found", mimeType);
            return new Dimension(null, null);
        }  catch (IOException e) {
            LOGGER.error("Error while reading image file {}", file.getAbsolutePath(), e);
            return new Dimension(null, null);
        }
    }

    static long getFileSize(@NonNull File file) {
        return file.length();
    }

    @Nullable String calculateHash(@NonNull File file) {
        MessageDigest messageDigest = imageConfiguration.messageDigest();
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                messageDigest.update(buffer, 0, bytesRead);
            }
            fis.close();
            byte[] digest = messageDigest.digest();
            return bytesToHex(digest);
        } catch (IOException e) {
            LOGGER.error("Error while reading image file {}", file.getAbsolutePath(), e);
            return null;
        }
    }

    private static @NonNull String bytesToHex(byte @NonNull [] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
