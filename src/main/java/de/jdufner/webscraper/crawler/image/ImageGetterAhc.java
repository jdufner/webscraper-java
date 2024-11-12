package de.jdufner.webscraper.crawler.image;

import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.Response;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;

@Service
public class ImageGetterAhc implements ImageGetter {

    private static final Logger logger = LoggerFactory.getLogger(ImageGetterAhc.class);

    private final AsyncHttpClient asyncHttpClient;

    public ImageGetterAhc(@NonNull AsyncHttpClient asyncHttpClient) {
        this.asyncHttpClient = asyncHttpClient;
    }

    @Override
    public void download(@NonNull URI uri, @NonNull File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            asyncHttpClient.prepareGet(uri.toString()).execute(
                            new AsyncCompletionHandler<FileOutputStream>() {

                                @Override
                                public State onBodyPartReceived(HttpResponseBodyPart content) throws Exception {
                                    logger.debug("Downloaded {} bytes of HTTP response", content.length());
                                    fos.getChannel().write(content.getBodyByteBuffer());
                                    return State.CONTINUE;
                                }

                                @Override
                                public FileOutputStream onCompleted(Response response) {
                                    logger.debug("Downloaded complete HTTP response");
                                    return fos;
                                }
                            }
                    )
                    .get();
            fos.getChannel().close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
