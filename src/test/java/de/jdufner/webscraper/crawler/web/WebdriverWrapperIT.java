package de.jdufner.webscraper.crawler.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ConfigurationPropertiesScan
@DirtiesContext
class WebdriverWrapperIT {

    @Autowired
    private WebdriverWrapper wrapper;

    @Test
    public void given_integrated_wrapper_when_get_Html_page_expect_cookie_consented_and_page_loaded() {
        // arrange

        // act
        String html = wrapper.getHtml("https://www.heise.de");

        // assert
        assertThat(html).isNotEmpty();
        assertThat(html).contains("<title>heise online");
    }

}
