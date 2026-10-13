package io.upro.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Regles d'architecture executables : si le code les viole, le build casse.
 * Voir docs/adr/0001-architecture-hexagonale.md.
 */
@AnalyzeClasses(packages = "io.upro", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainIsFrameworkFree = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta..", "tools.jackson..", "..application..", "..infrastructure..");

    @ArchTest
    static final ArchRule applicationDoesNotDependOnFrameworkOrAdapters = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta..", "tools.jackson..", "..infrastructure..");

    @ArchTest
    static final ArchRule catalogDoesNotDependOnPractice = noClasses()
            .that().resideInAPackage("io.upro.catalog..")
            .should().dependOnClassesThat().resideInAPackage("io.upro.practice..");

    @ArchTest
    static final ArchRule practiceDoesNotDependOnDaily = noClasses()
            .that().resideInAnyPackage("io.upro.catalog..", "io.upro.practice..")
            .should().dependOnClassesThat().resideInAPackage("io.upro.daily..");

    @ArchTest
    static final ArchRule adaptersDoNotTalkToEachOther = noClasses()
            .that().resideInAPackage("..infrastructure.web..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..infrastructure.jdbc..", "..infrastructure.yaml..", "..infrastructure.scheduling..");
}
