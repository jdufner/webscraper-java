package de.jdufner.webscraper.crawler.image;

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
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Iterator;

@Service
public class ImageAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageAnalyzer.class);

    @NonNull
    private final ImageConfiguration imageConfiguration;

    record Dimension(int width, int height) {
    }

    public ImageAnalyzer(@NonNull ImageConfiguration imageConfiguration) {
        this.imageConfiguration = imageConfiguration;
    }

    @Nullable Dimension analyze(@NonNull File file) {
        return getImageDimensions(file);
    }

    static @Nullable Dimension getImageDimensions(File file) {
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
            return null;
        }  catch (IOException e) {
            LOGGER.error("Error while reading image file {}", file.getAbsolutePath(), e);
            return null;
        }
    }

    static long getFileSize(@NonNull File file) {
        return file.length();
    }

    @NonNull String calculateHash1(@NonNull File file) {
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
            throw new RuntimeException(e);
        }
    }

    @NonNull String calculateHash2(@NonNull File file) {
        MessageDigest messageDigest = imageConfiguration.messageDigest();
        try (FileInputStream fis = new FileInputStream(file);
             DigestInputStream dis = new DigestInputStream(fis, messageDigest)) {
            byte[] buffer = new byte[8192];
            while (dis.read(buffer) != -1) {
            }
            dis.close();
            fis.close();
            byte[] digest = messageDigest.digest();
            return bytesToHex(digest);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
