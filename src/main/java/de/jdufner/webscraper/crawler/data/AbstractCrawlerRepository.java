package de.jdufner.webscraper.crawler.data;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCrawlerRepository.class);

    protected final JdbcTemplate jdbcTemplate;

    protected AbstractCrawlerRepository(@NonNull JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    protected abstract @NonNull Number getIdFromKeyholder(@NonNull KeyHolder keyHolder);

    public int saveDownloadedDocument(@NonNull DownloadedDocument downloadedDocument) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator psc = con -> {
            PreparedStatement ps = con.prepareStatement("INSERT INTO documents (url, content, download_started_at, download_stopped_at, STATE) VALUES (?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, downloadedDocument.uri().toString());
            ps.setString(2, downloadedDocument.content());
            ps.setTimestamp(3, convert(downloadedDocument.downloadStartedAt()));
            ps.setTimestamp(4, convert(downloadedDocument.downloadStoppedAt()));
            ps.setString(5, DocumentState.DOWNLOADED.toString());
            return ps;
        };
        jdbcTemplate.update(psc, keyHolder);
        return getIdFromKeyholder(keyHolder).intValue();
    }

    public void saveAnalyzedDocument(@NonNull AnalyzedDocument analyzedDocument) {
        updateAnalyzedDocument(analyzedDocument);
        saveAuthors(analyzedDocument, analyzedDocument.documentId());
        saveCategories(analyzedDocument, analyzedDocument.documentId());
        saveLinks(analyzedDocument, analyzedDocument.documentId());
        saveImages(analyzedDocument, analyzedDocument.documentId());
    }

    protected void updateAnalyzedDocument(@NonNull AnalyzedDocument analyzedDocument) {
        PreparedStatementCreator psc = con -> {
            PreparedStatement ps = con.prepareStatement("update DOCUMENTS set STATE = ?, TITLE = ?, CREATED_AT = ?, ANALYSIS_STARTED_AT = ?, ANALYSIS_STOPPED_AT = ? where ID = ?");
            ps.setString(1, DocumentState.ANALYZED.toString());
            ps.setString(2, analyzedDocument.title());
            ps.setTimestamp(3, convert(analyzedDocument.createdAt()));
            ps.setTimestamp(4, convert(analyzedDocument.analysisStartedAt()));
            ps.setTimestamp(5, convert(analyzedDocument.analysisStoppedAt()));
            ps.setInt(6, analyzedDocument.documentId());
            return ps;
        };
        jdbcTemplate.update(psc);
    }

    protected void saveAuthors(@NonNull AnalyzedDocument analyzedDocument, @NonNull Number documentId) {
        ResultSetExtractor<Optional<Number>> rse = rs -> {
            if (rs.next()) {
                return Optional.of(rs.getLong("ID"));
            }
            return Optional.empty();
        };
        analyzedDocument.authors().forEach(
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

    protected void saveCategories(@NonNull AnalyzedDocument analyzedDocument, @NonNull Number documentId) {
        ResultSetExtractor<Optional<Number>> rse = rs -> {
            if (rs.next()) {
                return Optional.of(rs.getLong("ID"));
            }
            return Optional.empty();
        };
        analyzedDocument.categories().forEach(category -> {
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

    protected void saveLinks(@NonNull AnalyzedDocument analyzedDocument, @NonNull Number documentId) {
        analyzedDocument.links().forEach(uri -> {
            Optional<Number> linkId = saveUriAsLink(uri);
            linkId.ifPresent(number -> jdbcTemplate.update("INSERT INTO DOCUMENTS_TO_LINKS (DOCUMENT_ID, LINK_ID) values (?, ?)", documentId, number));
        });
    }

    protected void saveImages(@NonNull AnalyzedDocument analyzedDocument, @NonNull Number documentId) {
        ResultSetExtractor<Optional<Number>> rse = rs -> {
            if (rs.next()) {
                return Optional.of(rs.getLong("ID"));
            }
            return Optional.empty();
        };
        analyzedDocument.images().forEach(image -> {
            Optional<Number> imageId = Objects.requireNonNull(jdbcTemplate.query("SELECT ID FROM IMAGES WHERE URL = ?", rse, image.toString()));
            if (imageId.isEmpty()) {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                PreparedStatementCreator psc = con -> {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO IMAGES (URL, STATE) VALUES (?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
                    ps.setString(1, image.toString());
                    ps.setString(2, ImageState.INITIALIZED.toString());
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
                        "select ID, URL from IMAGES where STATE = ? order by ID limit 1",
                        rs -> {
                            if (rs.next()) {
                                return Optional.of(new Image(rs.getInt("ID"), URI.create(rs.getString("URL"))));
                            }
                            return Optional.empty();
                        }, ImageState.INITIALIZED.toString())
        );
    }

    public void setImageDownloadedAndFilename(@NonNull Image image, @NonNull File file) {
        jdbcTemplate.update("update IMAGES set STATE = ?, FILENAME = ? where ID = ?", ImageState.DOWNLOADED.toString(), file.getPath(), image.id());
    }

    public @NonNull Optional<Link> getNextLinkIfAvailable() {
        return Objects.requireNonNull(
                jdbcTemplate.query(
                        "select ID, URL from LINKS where STATE = ? order by ID limit 1",
                        rs -> {
                            if (rs.next()) {
                                return Optional.of(new Link(rs.getInt("ID"), URI.create(rs.getString("URL"))));
                            }
                            return Optional.empty();
                        },
                        ImageState.INITIALIZED.toString())
        );
    }

    public void setLinkDownloaded(@NonNull Link link) {
        jdbcTemplate.update("update LINKS set STATE = ?, DOWNLOADED_AT = ? where ID = ?", LinkState.DOWNLOADED.toString(), convert(new Date()), link.id());
    }

    public void setImageSkip(@NonNull Image image) {
        jdbcTemplate.update("update IMAGES set STATE = ? where ID = ?", ImageState.SKIPPED.toString(), image.id());
    }

    public void setLinkSkip(@NonNull Link link) {
        jdbcTemplate.update("update LINKS set STATE = ?, SKIPPED_AT = ? where ID = ?", LinkState.SKIPPED.toString(), convert(new Date()), link.id());
    }

    protected static @Nullable Timestamp convert(@Nullable Date date) {
        if (date == null) {
            return null;
        }
        return new Timestamp(date.getTime());
    }

    public @NonNull Optional<DownloadedDocument> findNextDownloadedDocument() {
        return Objects.requireNonNull(
                jdbcTemplate.query(
                        "select ID, URL, CONTENT, DOWNLOAD_STARTED_AT, DOWNLOAD_STOPPED_AT, STATE from DOCUMENTS where STATE = 'DOWNLOADED' and ID = (select min(ID) from DOCUMENTS where STATE = 'DOWNLOADED') limit 1",
                        rs -> {
                            if (rs.next()) {
                                return Optional.of(new DownloadedDocument(rs.getInt("ID"), URI.create(rs.getString("URL")), rs.getString("CONTENT"), rs.getTimestamp("DOWNLOAD_STARTED_AT"), rs.getTimestamp("DOWNLOAD_STOPPED_AT")));
                            }
                            return Optional.empty();
                        })
        );
    }

    public @NonNull Optional<Number> saveUriAsLink(@NonNull URI uri) {
        ResultSetExtractor<Optional<Number>> rse = rs -> {
            if (rs.next()) {
                return Optional.of(rs.getLong("ID"));
            }
            return Optional.empty();
        };
        Optional<Number> linkId = Objects.requireNonNull(jdbcTemplate.query("SELECT ID FROM LINKS WHERE URL = ?", rse, uri.toString()));
        if (linkId.isEmpty()) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            PreparedStatementCreator psc = con -> {
                PreparedStatement ps = con.prepareStatement("INSERT INTO LINKS (URL, STATE) VALUES (?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setString(1, uri.toString());
                ps.setString(2, LinkState.INITIALIZED.toString());
                return ps;
            };
            jdbcTemplate.update(psc, keyHolder);
            linkId = Optional.of(getIdFromKeyholder(keyHolder));
        }
        LOGGER.debug("LINKS (ID = {}, URI = {})", linkId.get(), uri);
        return linkId;
    }

}
