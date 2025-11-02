package de.jdufner.webscraper.crawler.web;

import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
public class WebCrawlerTriggerIT {

    @MockitoSpyBean
    WebCrawlerTrigger webCrawlerTrigger;

    @Test
    @Disabled("Since this test takes 20 seconds, it is disabled for now.")
    public void given_scheduler_when_running_expect_called_periodically() {
        Awaitility.await().atMost(Durations.ONE_MINUTE).untilAsserted(() ->
                Mockito.verify(webCrawlerTrigger, Mockito.atLeast(2)).doStartAnalysis());
    }

}
