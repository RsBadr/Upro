package io.upro.catalog.infrastructure.yaml;

import io.upro.catalog.domain.LearningModule;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Garde-fou sur le contenu reel : chaque fichier YAML ajoute doit rester valide
 * (ids uniques, index de reponse correct, au moins une question par module).
 */
class YamlCatalogRepositoryTest {

    private final YamlCatalogRepository repository = new YamlCatalogRepository("classpath:content/*.yml");

    @Test
    void loadsAllShippedModules() {
        assertThat(repository.findAllModules())
                .extracting(LearningModule::id)
                .containsExactly("java", "architecture", "best-practices", "english", "leadership");
    }

    @Test
    void everyModuleHasQuestions() {
        repository.findAllModules().forEach(module ->
                assertThat(repository.findQuestionsByModule(module.id()))
                        .as("questions of %s", module.id())
                        .isNotEmpty());
    }

    @Test
    void everyQuestionHasAnExplanation() {
        repository.findAllModules().stream()
                .flatMap(m -> repository.findQuestionsByModule(m.id()).stream())
                .forEach(q -> assertThat(q.explanation()).as("explanation of %s", q.id()).isNotBlank());
    }
}
