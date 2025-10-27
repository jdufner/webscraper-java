package de.jdufner.webscraper.crawler.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebCrawlerTriggerTest {

    @Mock
    private WebCrawlerConfigurationProperties webCrawlerConfigurationProperties;

    @Mock
    private WebCrawler webCrawler;

    @InjectMocks
    private WebCrawlerTrigger webCrawlerTrigger;

    @Test
    void given_start_automatically_is_true_when_do_start_download_expect_download() {
        // arrange
        when(webCrawlerConfigurationProperties.startAutomatically()).thenReturn(true);

        // act
        webCrawlerTrigger.doStartDownload();

        // assert
        verify(webCrawler, Mockito.times(1)).download();
    }

    @Test
    void given_start_automatically_is_false_when_do_start_download_expect_download() {
        // arrange
        when(webCrawlerConfigurationProperties.startAutomatically()).thenReturn(false);

        // act
        webCrawlerTrigger.doStartDownload();

        // assert
        verify(webCrawler, Mockito.times(0)).download();
    }

    @Test
    void given_start_automatically_is_true_when_do_start_analysis_expect_analyze() {
        // arrange
        when(webCrawlerConfigurationProperties.startAutomatically()).thenReturn(true);

        // act
        webCrawlerTrigger.doStartAnalysis();

        // assert
        verify(webCrawler, Mockito.times(1)).analyze();
    }

    @Test
    void given_start_automatically_is_false_when_do_start_analysis_expect_analyze() {
        // arrange
        when(webCrawlerConfigurationProperties.startAutomatically()).thenReturn(false);

        // act
        webCrawlerTrigger.doStartAnalysis();

        // assert
        verify(webCrawler, Mockito.times(0)).analyze();
    }

}