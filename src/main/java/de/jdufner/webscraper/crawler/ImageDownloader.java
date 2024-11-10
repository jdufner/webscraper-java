package de.jdufner.webscraper.crawler;

import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.Response;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;

@Service
public class ImageDownloader {

    private final Repository repository;
    private final AsyncHttpClient asyncHttpClient;

    public ImageDownloader(@NonNull HsqldbRepository repository, @NonNull AsyncHttpClient asyncHttpClient) {
        this.repository = repository;
        this.asyncHttpClient = asyncHttpClient;
    }

    void downloadAll() {
        URI uri = repository.getNextImageUri();
        download(uri);
    }

    void download(URI uri) {
        try {
            File file = new File("." + uri.getPath());
            file.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(file);
            asyncHttpClient.prepareGet(uri.toString()).execute(
                            new AsyncCompletionHandler<FileOutputStream>() {

                                @Override
                                public State onBodyPartReceived(HttpResponseBodyPart content) throws Exception {
                                    fos.getChannel().write(content.getBodyByteBuffer());
                                    return State.CONTINUE;
                                }

                                @Override
                                public FileOutputStream onCompleted(Response response) throws Exception {
                                    return fos;
                                }
                            }
                    )
                    .get();
            fos.getChannel().close();
            //asyncHttpClient.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
