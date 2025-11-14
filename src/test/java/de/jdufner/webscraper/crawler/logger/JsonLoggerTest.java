package de.jdufner.webscraper.crawler.logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JsonLoggerTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private JsonLogger jsonLogger;

    @Test
    void given_json_logger_when_log_expect_json_generated_and_logged() throws Exception{
        // arrange
        record TestRecord(@NonNull String name) { }
        TestRecord testObject = new TestRecord("test");
        when(objectMapper.writeValueAsString(testObject)).thenReturn("{name=\"test\"}");

        // act
        jsonLogger.failsafeInfo(testObject);

        // assert
        verify(objectMapper).writeValueAsString(testObject);
    }

}
