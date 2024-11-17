package de.jdufner.webscraper.crawler;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class PackageDependencyTest {

    @Test
    public void test_web_must_depend_on_image() {
        // arrange
        JavaClasses importedClasses = new ClassFileImporter().importPackages("de.jdufner.webscraper");

        // act
        ArchRule rule = noClasses().that().resideInAPackage("..web..")
                .should().dependOnClassesThat().resideInAPackage("..image..");

        // assert
        rule.check(importedClasses);
    }
}
