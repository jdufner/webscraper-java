package de.jdufner.webscraper.crawler.config;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class SiteConfigurationTest {

    @Test
    void given_list_when_uri_is_in_list_expect_true() {
        // arrange
        String[] whiteList = {"https://www.google.com"};
        String[] blackList = {};
        SiteConfiguration config = new SiteConfiguration(whiteList, blackList);

        URI uri = URI.create("https://www.google.com");

        // act
        boolean inList = config.isInArray(whiteList, uri);

        // assert
        assertThat(inList).isTrue();
    }

    @Test
    void given_list_when_uri_with_following_slash_is_in_list_expect_true() {
        // arrange
        String[] whiteList = {"https://www.google.com"};
        String[] blackList = {};
        SiteConfiguration config = new SiteConfiguration(whiteList, blackList);

        URI uri = URI.create("https://www.google.com/");

        // act
        boolean inList = config.isInArray(whiteList, uri);

        // assert
        assertThat(inList).isTrue();
    }

    @Test
    void given_list_when_uri_with_different_protocols_is_in_list_expect_true() {
        // arrange
        String[] whiteList = {"https://www.google.com"};
        String[] blackList = {};
        SiteConfiguration config = new SiteConfiguration(whiteList, blackList);

        URI uri = URI.create("http://www.google.com/");

        // act
        boolean inList = config.isInArray(whiteList, uri);

        // assert
        assertThat(inList).isTrue();
    }

    @Test
    void given_list_when_uri_is_not_in_list_expect_false() {
        // arrange
        String[] whiteList = {"https://start.duckduckgo.com"};
        String[] blackList = {};
        SiteConfiguration config = new SiteConfiguration(whiteList, blackList);

        URI uri = URI.create("https://www.google.com");

        // act
        boolean inList = config.isInArray(whiteList, uri);

        // assert
        assertThat(inList).isFalse();
    }

}