package de.jdufner.webscraper.crawler.data;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
@Profile("hsqldb")
public class HsqldbCrawlerRepository extends AbstractCrawlerRepository implements CrawlerRepository {

    public HsqldbCrawlerRepository(@NonNull JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    protected @NonNull Number getIdFromKeyholder(@NonNull KeyHolder keyHolder) {
        return Objects.requireNonNull(keyHolder.getKey());
    }

}
