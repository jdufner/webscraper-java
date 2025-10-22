package de.jdufner.webscraper.crawler.data;

import org.jspecify.annotations.NonNull;

public record DocumentOutbox(int documentId, @NonNull DocumentProcessState processState) {
}
