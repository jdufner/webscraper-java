package de.jdufner.webscraper.crawler.data;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.io.File;
import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractCrawlerRepository {

    protected final JdbcTemplate jdbcTemplate;

    protected AbstractCrawlerRepository(@NonNull JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    protected abstract @NonNull Number getIdFromKeyholder(@NonNull KeyHolder keyHolder);

    public int saveDownloadedDocument(@NonNull DownloadedDocument downloadedDocument) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator psc = con -> {
            PreparedStatement ps = con.prepareStatement("INSERT INTO documents (url, content, downloaded_at, process_state) VALUES (?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, downloadedDocument.uri().toString());
            ps.setString(2, downloadedDocument.content());
            ps.setTimestamp(3, convert(downloadedDocument.downloadedAt()));
            ps.setString(4, DocumentProcessState.DOWNLOADED.toString());
            return ps;
        };
        jdbcTemplate.update(psc, keyHolder);
        return getIdFromKeyholder(keyHolder).intValue();
    }

    public int saveDocument(@NonNull HtmlPage htmlPage) {
        Number documentId = saveDocumentInternal(htmlPage);
        saveAuthors(htmlPage, documentId);
        saveCategories(htmlPage, documentId);
        saveLinks(htmlPage, documentId);
        saveImages(htmlPage, documentId);
        return documentId.intValue();
    }

    protected @NonNull Number saveDocumentInternal(@NonNull HtmlPage htmlPage) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator psc = con -> {
            PreparedStatement ps = con.prepareStatement("INSERT INTO DOCUMENTS (URL, CONTENT, DOWNLOADED_AT, PROCESS_STATE, CREATED_AT) VALUES (?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, htmlPage.uri().toString());
            ps.setString(2, htmlPage.html());
            ps.setTimestamp(3, convert(htmlPage.downloadedAt()));
            ps.setString(4, DocumentProcessState.DOWNLOADED.toString());
            ps.setTimestamp(5, convert(htmlPage.createdAt()));
            return ps;
        };
        jdbcTemplate.update(psc, keyHolder);
        return getIdFromKeyholder(keyHolder);
    }

    protected void saveAuthors(@NonNull HtmlPage htmlPage, @NonNull Number documentId) {
        ResultSetExtractor<Optional<Number>> rse = rs -> {
            if (rs.next()) {
                return Optional.of(rs.getLong("ID"));
            }
            return Optional.empty();
        };
        htmlPage.authors().forEach(
                author -> {
                    Optional<Number> authorId = (Objects.requireNonNull(jdbcTemplate.query("select ID from AUTHORS where NAME = ?", rse, author)));
                    if (authorId.isEmpty()) {
                        KeyHolder keyHolder = new GeneratedKeyHolder();
                        PreparedStatementCreator psc = con -> {
                            PreparedStatement ps = con.prepareStatement("INSERT INTO AUTHORS (NAME) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS);
                            ps.setString(1, author);
                            return ps;
                        };
                        jdbcTemplate.update(psc, keyHolder);
                        authorId = Optional.of(getIdFromKeyholder(keyHolder));
                    }
                    jdbcTemplate.update("INSERT INTO DOCUMENTS_TO_AUTHORS (DOCUMENT_ID, AUTHOR_ID) values (?, ?)", documentId, authorId.get());
                }
        );
    }

    protected void saveCategories(@NonNull HtmlPage htmlPage, @NonNull Number documentId) {
        ResultSetExtractor<Optional<Number>> rse = rs -> {
            if (rs.next()) {
                return Optional.of(rs.getLong("ID"));
            }
            return Optional.empty();
        };
        htmlPage.categories().forEach(category -> {
            Optional<Number> categoryId = Objects.requireNonNull(jdbcTemplate.query("select ID from CATEGORIES where NAME = ?", rse, category));
            if (categoryId.isEmpty()) {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                PreparedStatementCreator psc = con -> {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO CATEGORIES (NAME) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS);
                    ps.setString(1, category);
                    return ps;
                };
                jdbcTemplate.update(psc, keyHolder);
                categoryId = Optional.of(getIdFromKeyholder(keyHolder));
            }
            jdbcTemplate.update("INSERT INTO DOCUMENTS_TO_CATEGORIES (DOCUMENT_ID, CATEGORY_ID) values (?, ?)", documentId, categoryId.get());
        });
    }

    protected void saveLinks(@NonNull HtmlPage htmlPage, @NonNull Number documentId) {
        ResultSetExtractor<Optional<Number>> rse = rs -> {
            if (rs.next()) {
                return Optional.of(rs.getLong("ID"));
            }
            return Optional.empty();
        };
        htmlPage.links().forEach(link -> {
            Optional<Number> linkId = Objects.requireNonNull(jdbcTemplate.query("SELECT ID FROM links WHERE URL = ?", rse, link.toString()));
            if (linkId.isEmpty()) {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                PreparedStatementCreator psc = con -> {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO links (URL) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS);
                    ps.setString(1, link.toString());
                    return ps;
                };
                jdbcTemplate.update(psc, keyHolder);
                linkId = Optional.of(getIdFromKeyholder(keyHolder));
            }
            jdbcTemplate.update("INSERT INTO DOCUMENTS_TO_LINKS (DOCUMENT_ID, LINK_ID) values (?, ?)", documentId, linkId.get());
        });
    }

    protected void saveImages(@NonNull HtmlPage htmlPage, @NonNull Number documentId) {
        ResultSetExtractor<Optional<Number>> rse = rs -> {
            if (rs.next()) {
                return Optional.of(rs.getLong("ID"));
            }
            return Optional.empty();
        };
        htmlPage.images().forEach(image -> {
            Optional<Number> imageId = Objects.requireNonNull(jdbcTemplate.query("SELECT ID FROM IMAGES WHERE URL = ?", rse, image.toString()));
            if (imageId.isEmpty()) {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                PreparedStatementCreator psc = con -> {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO IMAGES (URL) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS);
                    ps.setString(1, image.toString());
                    return ps;
                };
                jdbcTemplate.update(psc, keyHolder);
                imageId = Optional.of(getIdFromKeyholder(keyHolder));
            }
            jdbcTemplate.update("INSERT INTO DOCUMENTS_TO_IMAGES (DOCUMENT_ID, IMAGE_ID) values (?, ?)", documentId, imageId.get());
        });
    }

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

    public void setImageDownloadedAndFilename(@NonNull Image image, @NonNull File file) {
        jdbcTemplate.update("update IMAGES set DOWNLOADED = true, FILENAME = ? where ID = ?", file.getPath(), image.id());
    }

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

    public void setLinkDownloaded(@NonNull Link link) {
        jdbcTemplate.update("update LINKS set DOWNLOADED = true where ID = ?", link.id());
    }

    public void setImageSkip(@NonNull Image image) {
        jdbcTemplate.update("update IMAGES set SKIP = true where ID = ?", image.id());
    }

    public void setLinkSkip(@NonNull Link link) {
        jdbcTemplate.update("update LINKS set SKIP = true where ID = ?", link.id());
    }

    protected static @Nullable Timestamp convert(@Nullable Date date) {
        if (date == null) {
            return null;
        }
        return new Timestamp(date.getTime());
    }

    public @NonNull Optional<DownloadedDocument> getDownloadedDocument() {
        return Objects.requireNonNull(
                jdbcTemplate.query(
                        "select ID, URL, CONTENT, DOWNLOADED_AT, PROCESS_STATE from DOCUMENTS where PROCESS_STATE = 'DOWNLOADED' and ID = (select min(ID) from DOCUMENTS where PROCESS_STATE = 'DOWNLOADED') limit 1",
                        rs -> {
                            if (rs.next()) {
                                return Optional.of(new DownloadedDocument(rs.getInt("ID"), URI.create(rs.getString("URL")), rs.getString("CONTENT"),  rs.getTimestamp("DOWNLOADED_AT")));
                            }
                            return Optional.empty();
                        })
        );
    }

}
