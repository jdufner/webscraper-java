package de.jdufner.webscraper.crawler;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;

@Service
public class HsqldbRepository implements Repository {

    private final JdbcTemplate jdbcTemplate;

    public HsqldbRepository(@NonNull JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(@NonNull HtmlPage htmlPage) {
        Number documentId = saveDocument(htmlPage);
        saveAuthors(htmlPage, documentId);
        saveCategories(htmlPage, documentId);
        saveLinks(htmlPage, documentId);
        saveImages(htmlPage, documentId);
    }

    @Override
    public URI getNextImageUri() {
        return URI.create("https://localhost/test.jpg");
    }

    private void saveAuthors(@NonNull HtmlPage htmlPage, @NonNull Number documentId) {
        for (String author: htmlPage.authors()) {
            ResultSetExtractor<Number> rse = rs -> {
                if (rs.next()) {
                    return rs.getLong("ID");
                }
                return null;
            };
            Number authorId = jdbcTemplate.query("select ID from AUTHORS where NAME = ?", rse, author);
            if (authorId == null) {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                PreparedStatementCreator psc = con -> {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO AUTHORS (NAME) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS);
                    ps.setString(1, author);
                    return ps;
                };
                jdbcTemplate.update(psc, keyHolder);
                authorId = keyHolder.getKey();
            }
            jdbcTemplate.update("INSERT INTO DOCUMENTS_TO_AUTHORS (DOCUMENT_ID, AUTHOR_ID) values (?, ?)", documentId, authorId);
        }
    }

    private void saveCategories(@NonNull HtmlPage htmlPage, @NonNull Number documentId) {
        for (String category: htmlPage.categories()) {
            ResultSetExtractor<Number> rse = rs -> {
                if (rs.next()) {
                    return rs.getLong("ID");
                }
                return null;
            };
            Number categoryId = jdbcTemplate.query("select ID from CATEGORIES where NAME = ?", rse, category);
            if (categoryId == null) {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                PreparedStatementCreator psc = con -> {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO CATEGORIES (NAME) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS);
                    ps.setString(1, category);
                    return ps;
                };
                jdbcTemplate.update(psc, keyHolder);
                categoryId = keyHolder.getKey();
            }
            jdbcTemplate.update("INSERT INTO DOCUMENTS_TO_CATEGORIES (DOCUMENT_ID, CATEGORY_ID) values (?, ?)", documentId, categoryId);
        }
    }

    private void saveLinks(@NonNull HtmlPage htmlPage, @NonNull Number documentId) {
        for (URI link : htmlPage.links()) {
            ResultSetExtractor<Number> rse = rs -> {
                if (rs.next()) {
                    return rs.getLong("ID");
                }
                return null;
            };
            Number linkId = jdbcTemplate.query("SELECT ID FROM links WHERE URL = ?", rse, link.toString());
            if (linkId == null) {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                PreparedStatementCreator psc = con -> {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO links (URL) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS);
                    ps.setString(1, link.toString());
                    return ps;
                };
                jdbcTemplate.update(psc, keyHolder);
                linkId = keyHolder.getKey();
            }
            jdbcTemplate.update("INSERT INTO DOCUMENTS_TO_LINKS (DOCUMENT_ID, LINK_ID) values (?, ?)", documentId, linkId);
        }
    }

    private void saveImages(@NonNull HtmlPage htmlPage, @NonNull Number documentId) {
        for (URI image : htmlPage.images()) {
            ResultSetExtractor<Number> rse = rs -> {
                if (rs.next()) {
                    return rs.getLong("ID");
                }
                return null;
            };
            Number imageId = jdbcTemplate.query("SELECT ID FROM IMAGES WHERE URL = ?", rse, image.toString());
            if (imageId == null) {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                PreparedStatementCreator psc = con -> {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO IMAGES (URL) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS);
                    ps.setString(1, image.toString());
                    return ps;
                };
                jdbcTemplate.update(psc, keyHolder);
                imageId = keyHolder.getKey();
            }
            jdbcTemplate.update("INSERT INTO DOCUMENTS_TO_IMAGES (DOCUMENT_ID, IMAGE_ID) values (?, ?)", documentId, imageId);
        }
    }

    private Number saveDocument(HtmlPage htmlPage) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator psc = con -> {
            PreparedStatement ps = con.prepareStatement("INSERT INTO documents (URL, CONTENT, DOWNLOADED_AT, CREATED_AT) VALUES (?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, htmlPage.uri().toString());
            ps.setString(2, htmlPage.html());
            ps.setTimestamp(3, convert(htmlPage.downloadedAt()));
            ps.setTimestamp(4, convert(htmlPage.createdAt()));
            return ps;
        };
        jdbcTemplate.update(psc, keyHolder);
        return keyHolder.getKey();
    }

    private static Timestamp convert(@Nullable Date date) {
        if (date == null) {
            return null;
        }
        return new Timestamp(date.getTime());
    }

}