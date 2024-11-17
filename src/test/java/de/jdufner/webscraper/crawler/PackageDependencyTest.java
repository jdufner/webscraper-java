package de.jdufner.webscraper.crawler;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class PackageDependencyTest {

    @Test
    public void test_web_must_not_depend_on_image() {
        // arrange
        JavaClasses importedClasses = new ClassFileImporter().importPackages("de.jdufner.webscraper");

        // act
        ArchRule rule = noClasses().that().resideInAPackage("..web..")
                .should().dependOnClassesThat().resideInAPackage("..image..");

        // assert
        rule.check(importedClasses);
    }

    @Test
    public void test_image_must_not_depend_on_web() {
        // arrange
        JavaClasses importedClasses = new ClassFileImporter().importPackages("de.jdufner.webscraper");

        // act
        ArchRule rule = noClasses().that().resideInAPackage("..image..")
                .should().dependOnClassesThat().resideInAPackage("..web..");

        // assert
        rule.check(importedClasses);
    }

    @Test
    public void test_config_only_have_dependencies_from_image_and_web() {
        // arrange
        JavaClasses importedClasses = new ClassFileImporter().importPackages("de.jdufner.webscraper");

        // act
        ArchRule rule = classes().that().resideInAPackage("..config..")
                .should().onlyHaveDependentClassesThat().resideInAnyPackage("..config..", "..image..", "..web..");

        // assert
        rule.check(importedClasses);
    }

    @Test
    public void test_data_only_have_dependencies_from_image_and_web() {
        // arrange
        JavaClasses importedClasses = new ClassFileImporter().importPackages("de.jdufner.webscraper");

        // act
        ArchRule rule = classes().that().resideInAPackage("..data..")
                .should().onlyHaveDependentClassesThat().resideInAnyPackage("..data..", "..image..", "..web..");

        // assert
        rule.check(importedClasses);
    }

}
