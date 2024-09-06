package de.jdufner.webscraper.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WebCrawler {

    private static final Logger logger = LoggerFactory.getLogger(WebCrawler.class);

    private final SeleniumWrapper seleniumWrapper;
    private final WebCrawlerConfiguration webCrawlerConfiguration;

    public WebCrawler(SeleniumWrapper seleniumWrapper, WebCrawlerConfiguration webCrawlerConfiguration) {
        this.seleniumWrapper = seleniumWrapper;
        this.webCrawlerConfiguration = webCrawlerConfiguration;
    }

    public void startCrawling() {
        logger.info("startUrl = {}, numberPages = {}, numberImages = {}", webCrawlerConfiguration.startUrl(),
                webCrawlerConfiguration.numberPages(), webCrawlerConfiguration.numberImages());
        String html = seleniumWrapper.getHtml(webCrawlerConfiguration.startUrl());
        Document document = Jsoup.parse(html);
        String title = extractTitle(document);
        String createdAt = extractCreatedAt(document);
        String creator = extractCreator(document);
        String categories = extractCategories(document);
        List<URI> links = extractLinks(webCrawlerConfiguration.startUrl(), document);
        List<URI> images = extractImages(webCrawlerConfiguration.startUrl(), document);
        logger.info("title = {}, createdAt = {}, creator = {}, categories = {}, links = {}, images = {}", title, createdAt, creator, categories, links, images);
    }

    String extractTitle(Document document) {
        return document.select("head title").stream()
                .map(Element::text)
                .map(title -> StringUtils.trimLeadingCharacter(title, ' '))
                .map(title -> StringUtils.trimTrailingCharacter(title, ' '))
                .map(title -> title.replaceAll("\t", " "))
                .map(title -> title.replaceAll("\r", ""))
                .collect(Collectors.joining(" "));
    }

    String extractCreatedAt(Document document) {
        return document.select("div.a-publish-info time[datetime]").stream()
                .map(element -> element.attr("datetime"))
                .collect(Collectors.joining(" "));
    }

    String extractCreator(Document document) {
        return document.select("div.creator ul li").stream()
                .map(Element::text)
                .collect(Collectors.joining(","));
    }

    String extractCategories(Document document) {
        return document.select("div.content-categories a").stream()
                .map(Element::text)
                .collect(Collectors.joining(","));
    }

    List<URI> extractLinks(String baseUrl, Document document) {
        return document.select("a[href]").stream()
                .map(element -> element.attr("href"))
                .map(uri -> WebCrawler.buildUrl(baseUrl, uri))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(WebCrawler::removeQueryAndFragment)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    List<URI> extractImages(String baseUrl, Document document) {
        return document.select("img[src]").stream()
                .map(element -> element.attr("src"))
                .map(uri -> WebCrawler.buildUrl(baseUrl, uri))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(WebCrawler::removeQueryAndFragment)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static Optional<URI> removeQueryAndFragment(URI uri) {
        try {
            URI newUri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), null, null);
            return Optional.of(newUri);
        } catch (URISyntaxException e) {
            logger.warn("URISyntaxException = {}", e.toString());
            return Optional.empty();
        }
    }

    private static Optional<URI> buildUrl(String baseUrl, String url) {
        try {
            URI uri = new URI(url);
            if (uri.isAbsolute()) {
                return Optional.of(uri);
            } else {
                URI baseUri = new URI(baseUrl);
                uri = baseUri.resolve(uri);
                return Optional.of(uri);
            }
        } catch (URISyntaxException e) {
            logger.warn("URISyntaxException = {}", e.toString());
            return Optional.empty();
        }
    }

}
