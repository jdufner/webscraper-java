package de.jdufner.webscraper.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WebFetcher {

    private static final Logger logger = LoggerFactory.getLogger(WebFetcher.class);
    static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

    private final SeleniumWrapper seleniumWrapper;

    public WebFetcher(@NonNull SeleniumWrapper seleniumWrapper) {
        this.seleniumWrapper = seleniumWrapper;
    }

    public @NonNull HtmlPage get(@NonNull String url) {
        logger.info("get url = {}", url);
        URI uri = URI.create(url);
        String html = seleniumWrapper.getHtml(url);
        Date downloadedAt = new Date();
        Document document = Jsoup.parse(html);
        Optional<String> title = extractTitle(document);
        Optional<Date> createdAt = extractCreatedAt(document);
        List<String> authors = extractAuthors(document);
        List<String> categories = extractCategories(document);
        List<URI> links = extractLinks(url, document);
        List<URI> images = extractImages(url, document);
        logger.info("title = {}, createdAt = {}, authors = {}, categories = {}, links = {}, images = {}",
                title, createdAt, authors, categories, links, images);
        return new HtmlPage(uri, html, downloadedAt, title.orElse(null), createdAt.orElse(null), authors, categories, links, images);
    }

    @NonNull Optional<String> extractTitle(@NonNull Document document) {
        return document.select("head title").stream()
                .map(Element::text)
                .map(title -> StringUtils.trimLeadingCharacter(title, ' '))
                .map(title -> StringUtils.trimTrailingCharacter(title, ' '))
                .map(title -> title.replaceAll("\t", " "))
                .map(title -> title.replaceAll("\r", ""))
                .findFirst();
    }

    @NonNull Optional<Date> extractCreatedAt(@NonNull Document document) {
        return document.select("div.a-publish-info time[datetime]").stream()
                .map(element -> element.attr("datetime"))
                .map(this::parseString)
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }

    private @NonNull Optional<Date> parseString(@NonNull String s) {
        try {
            return Optional.of(DATE_FORMAT.parse(s));
        } catch (ParseException e) {
            return Optional.empty();
        }
    }

    @NonNull List<String> extractAuthors(@NonNull Document document) {
        return document.select("div.creator ul li").stream()
                .map(Element::text)
                .collect(Collectors.toList());
    }

    @NonNull List<String> extractCategories(@NonNull Document document) {
        return document.select("div.content-categories a").stream()
                .map(Element::text)
                .collect(Collectors.toList());
    }

    @NonNull List<URI> extractLinks(@NonNull String baseUrl, @NonNull Document document) {
        return document.select("a[href]").stream()
                .map(element -> element.attr("href"))
                .map(uri -> buildUrl(baseUrl, uri))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(WebFetcher::removeQueryAndFragment)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .distinct()
                .collect(Collectors.toList());
    }

    @NonNull List<URI> extractImages(@NonNull String baseUrl, @NonNull Document document) {
        return document.select("img[src]").stream()
                .map(element -> element.attr("src"))
                .map(uri -> buildUrl(baseUrl, uri))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(WebFetcher::removeQueryAndFragment)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .distinct()
                .collect(Collectors.toList());
    }

    private static @NonNull Optional<URI> removeQueryAndFragment(@NonNull URI uri) {
        try {
            URI newUri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), null, null);
            return Optional.of(newUri);
        } catch (URISyntaxException e) {
            logger.warn("URISyntaxException = {}", e.toString());
            return Optional.empty();
        }
    }

    private static @NonNull Optional<URI> buildUrl(@NonNull String baseUrl, @NonNull String url) {
        URI uri = URI.create(url);
        if (!uri.isAbsolute()) {
            URI baseUri = URI.create(baseUrl);
            uri = baseUri.resolve(uri);
        }
        return Optional.of(uri);
    }

}
