package io.upro.catalog.infrastructure.yaml;

import io.upro.catalog.domain.LearningModule;
import io.upro.catalog.domain.Level;
import io.upro.catalog.domain.Question;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Test
    void everyModuleCoversEveryLevel() {
        repository.findAllModules().forEach(module ->
                assertThat(repository.findQuestionsByModule(module.id()))
                        .as("levels of %s", module.id())
                        .extracting(Question::level)
                        .contains(Level.values()));
    }

    @Test
    void correctAnswerPositionIsNotPredictable() {
        List<Question> all = repository.findAllModules().stream()
                .flatMap(m -> repository.findQuestionsByModule(m.id()).stream())
                .toList();
        Map<Integer, Long> byPosition = all.stream()
                .collect(Collectors.groupingBy(Question::answerIndex, Collectors.counting()));

        // aucune position ne concentre plus de la moitie des bonnes reponses
        assertThat(byPosition.values()).allSatisfy(count -> assertThat(count).isLessThan(all.size() / 2));
    }
}
