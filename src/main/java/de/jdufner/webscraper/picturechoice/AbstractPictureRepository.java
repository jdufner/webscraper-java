package de.jdufner.webscraper.picturechoice;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;

public abstract class AbstractPictureRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPictureRepository.class);

    protected JdbcTemplate jdbcTemplate;

    public AbstractPictureRepository(@NonNull JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    protected abstract @NonNull Number getIdFromKeyholder(@NonNull KeyHolder keyHolder);

    public int save(@NonNull Picture picture) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator preparedStatementCreator = con -> {
            PreparedStatement ps = con.prepareStatement("INSERT INTO PICTURES (FILENAME, HTML_FILENAME, STATE) VALUES (?, ?, ?) ", PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, picture.file().toAbsolutePath().toString());
            ps.setString(2, picture.htmlFileName());
            ps.setString(3, picture.state().toString());
            return ps;
        };
        jdbcTemplate.update(preparedStatementCreator, keyHolder);
        return getIdFromKeyholder(keyHolder).intValue();
    }

}
