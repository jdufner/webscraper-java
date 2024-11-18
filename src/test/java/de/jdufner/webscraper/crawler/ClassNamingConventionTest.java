package de.jdufner.webscraper.crawler;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public class ClassNamingConventionTest {

    @Test
    public void testConfigurationClassNames() {
        // arrange
        JavaClasses importedClasses = new ClassFileImporter().importPackages("de.jdufner.webscraper");

        // act
        ArchRule rule = classes().that().haveSimpleNameEndingWith("Configuration")
                .should().beAnnotatedWith(Configuration.class);

        // assert
        rule.check(importedClasses);
    }

    @Test
    public void testConfigurationPropertiesClassNames() {
        // arrange
        JavaClasses importedClasses = new ClassFileImporter().importPackages("de.jdufner.webscraper");

        // act
        ArchRule rule = classes().that().haveSimpleNameEndingWith("ConfigurationProperties")
                .should().beAnnotatedWith(ConfigurationProperties.class)
                .andShould().beRecords();

        // assert
        rule.check(importedClasses);
    }

}
