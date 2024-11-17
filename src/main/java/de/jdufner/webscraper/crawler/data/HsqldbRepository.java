package de.jdufner.webscraper.crawler.data;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Service
public class HsqldbRepository implements Repository {

    private final JdbcTemplate jdbcTemplate;

    public HsqldbRepository(@NonNull JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int save(@NonNull HtmlPage htmlPage) {
        Number documentId = saveDocument(htmlPage);
        saveAuthors(htmlPage, documentId);
        saveCategories(htmlPage, documentId);
        saveLinks(htmlPage, documentId);
        saveImages(htmlPage, documentId);
        return documentId.intValue();
    }

    private void saveAuthors(@NonNull HtmlPage htmlPage, @NonNull Number documentId) {
        for (String author : htmlPage.authors()) {
            ResultSetExtractor<Optional<Number>> rse = rs -> {
                if (rs.next()) {
                    return Optional.of(rs.getLong("ID"));
                }
                return Optional.empty();
            };
            Optional<Number> authorId = (Objects.requireNonNull(jdbcTemplate.query("select ID from AUTHORS where NAME = ?", rse, author)));
            if (authorId.isEmpty()) {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                PreparedStatementCreator psc = con -> {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO AUTHORS (NAME) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS);
                    ps.setString(1, author);
                    return ps;
                };
                jdbcTemplate.update(psc, keyHolder);
                authorId = Optional.of(Objects.requireNonNull(keyHolder.getKey()));
            }
            jdbcTemplate.update("INSERT INTO DOCUMENTS_TO_AUTHORS (DOCUMENT_ID, AUTHOR_ID) values (?, ?)", documentId, authorId.get());
        }
    }

    private void saveCategories(@NonNull HtmlPage htmlPage, @NonNull Number documentId) {
        for (String category : htmlPage.categories()) {
            ResultSetExtractor<Optional<Number>> rse = rs -> {
                if (rs.next()) {
                    return Optional.of(rs.getLong("ID"));
                }
                return Optional.empty();
            };
            Optional<Number> categoryId = Objects.requireNonNull(jdbcTemplate.query("select ID from CATEGORIES where NAME = ?", rse, category));
            if (categoryId.isEmpty()) {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                PreparedStatementCreator psc = con -> {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO CATEGORIES (NAME) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS);
                    ps.setString(1, category);
                    return ps;
                };
                jdbcTemplate.update(psc, keyHolder);
                categoryId = Optional.of(Objects.requireNonNull(keyHolder.getKey()));
            }
            jdbcTemplate.update("INSERT INTO DOCUMENTS_TO_CATEGORIES (DOCUMENT_ID, CATEGORY_ID) values (?, ?)", documentId, categoryId.get());
        }
    }

    private void saveLinks(@NonNull HtmlPage htmlPage, @NonNull Number documentId) {
        for (URI link : htmlPage.links()) {
            ResultSetExtractor<Optional<Number>> rse = rs -> {
                if (rs.next()) {
                    return Optional.of(rs.getLong("ID"));
                }
                return Optional.empty();
            };
            Optional<Number> linkId = Objects.requireNonNull(jdbcTemplate.query("SELECT ID FROM links WHERE URL = ?", rse, link.toString()));
            if (linkId.isEmpty()) {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                PreparedStatementCreator psc = con -> {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO links (URL) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS);
                    ps.setString(1, link.toString());
                    return ps;
                };
                jdbcTemplate.update(psc, keyHolder);
                linkId = Optional.of(Objects.requireNonNull(keyHolder.getKey()));
            }
            jdbcTemplate.update("INSERT INTO DOCUMENTS_TO_LINKS (DOCUMENT_ID, LINK_ID) values (?, ?)", documentId, linkId.get());
        }
    }

    private void saveImages(@NonNull HtmlPage htmlPage, @NonNull Number documentId) {
        for (URI image : htmlPage.images()) {
            ResultSetExtractor<Optional<Number>> rse = rs -> {
                if (rs.next()) {
                    return Optional.of(rs.getLong("ID"));
                }
                return Optional.empty();
            };
            Optional<Number> imageId = Objects.requireNonNull(jdbcTemplate.query("SELECT ID FROM IMAGES WHERE URL = ?", rse, image.toString()));
            if (imageId.isEmpty()) {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                PreparedStatementCreator psc = con -> {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO IMAGES (URL) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS);
                    ps.setString(1, image.toString());
                    return ps;
                };
                jdbcTemplate.update(psc, keyHolder);
                imageId = Optional.of(Objects.requireNonNull(keyHolder.getKey()));
            }
            jdbcTemplate.update("INSERT INTO DOCUMENTS_TO_IMAGES (DOCUMENT_ID, IMAGE_ID) values (?, ?)", documentId, imageId.get());
        }
    }

    private @NonNull Number saveDocument(@NonNull HtmlPage htmlPage) {
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
        return Objects.requireNonNull(keyHolder.getKey());
    }

    private static @Nullable Timestamp convert(@Nullable Date date) {
        if (date == null) {
            return null;
        }
        return new Timestamp(date.getTime());
    }

    @Override
    public @NonNull Optional<Image> getNextImageIfAvailable() {
        return Objects.requireNonNull(
                jdbcTemplate.query(
                        "select ID, URL from IMAGES where SKIP = false and DOWNLOADED = false order by ID limit 1",
                        rs -> {
                            if (rs.next()) {
                                return Optional.of(new Image(rs.getInt("ID"), URI.create(rs.getString("URL"))));
                            }
                            return Optional.empty();
                        })
        );
    }

    @Override
    public void setImageDownloadedAndFilename(@NonNull Image image, @NonNull File file) {
        jdbcTemplate.update("update IMAGES set DOWNLOADED = true, FILENAME = ? where ID = ?", file.getPath(), image.id());
    }

    @Override
    public @NonNull Optional<Link> getNextLinkIfAvailable() {
        return Objects.requireNonNull(
                jdbcTemplate.query(
                        "select ID, URL from LINKS where SKIP = false and DOWNLOADED = false order by ID limit 1",
                        rs -> {
                            if (rs.next()) {
                                return Optional.of(new Link(rs.getInt("id"), URI.create(rs.getString("url"))));
                            }
                            return Optional.empty();
                        })
        );
    }

    @Override
    public void setLinkDownloaded(@NonNull Link link) {
        jdbcTemplate.update("update LINKS set DOWNLOADED = true where ID = ?", link.id());
    }

    @Override
    public void setImageSkip(@NonNull Image image) {
        jdbcTemplate.update("update IMAGES set SKIP = true where ID = ?", image.id());
    }

    @Override
    public void setLinkSkip(@NonNull Link link) {
        jdbcTemplate.update("update LINKS set SKIP = true where ID = ?", link.id());
    }

}