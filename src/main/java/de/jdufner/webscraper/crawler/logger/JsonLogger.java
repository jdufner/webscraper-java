package de.jdufner.webscraper.crawler.logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JsonLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonLogger.class);

    private final ObjectMapper objectMapper;

    public JsonLogger(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void failsafeInfo(Object object) {
        try {
            String valueAsString = objectMapper.writeValueAsString(object);
            LOGGER.info(valueAsString);
        } catch (
                JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
