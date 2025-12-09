package de.jdufner.webscraper.picturechoice;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
@Profile("hsqldb")
public class HsqldbPictureRepository extends AbstractPictureRepository implements PictureRepository {

    public HsqldbPictureRepository(@NonNull JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    protected @NonNull Number getIdFromKeyholder(@NonNull KeyHolder keyHolder) {
        return Objects.requireNonNull(keyHolder.getKey());
    }

}
