package de.jdufner.webscraper.crawler;

import java.net.URI;

public record HtmlPage(URI uri, String html, String title, String createdAt, String creator, String categories,
                       java.util.List<URI> links, java.util.List<URI> images) {
}
