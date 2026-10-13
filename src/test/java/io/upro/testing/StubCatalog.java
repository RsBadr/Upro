package io.upro.testing;

import io.upro.catalog.application.port.CatalogRepository;
import io.upro.catalog.domain.LearningModule;
import io.upro.catalog.domain.Level;
import io.upro.catalog.domain.Question;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Catalogue construit a la main dans les tests. Bonne reponse : toujours l'index 0. */
public final class StubCatalog implements CatalogRepository {

    private final Map<String, LearningModule> modules = new LinkedHashMap<>();
    private final Map<String, List<Question>> questions = new LinkedHashMap<>();

    public StubCatalog question(String moduleId, String questionId, Level level) {
        modules.computeIfAbsent(moduleId, id -> new LearningModule(id, id, ""));
        questions.computeIfAbsent(moduleId, id -> new ArrayList<>())
                .add(new Question(questionId, moduleId, "topic", level, "prompt?", List.of("good", "bad"), 0, "because"));
        return this;
    }

    @Override
    public List<LearningModule> findAllModules() {
        return List.copyOf(modules.values());
    }

    @Override
    public Optional<LearningModule> findModule(String moduleId) {
        return Optional.ofNullable(modules.get(moduleId));
    }

    @Override
    public List<Question> findQuestionsByModule(String moduleId) {
        return questions.getOrDefault(moduleId, List.of());
    }

    @Override
    public Optional<Question> findQuestion(String questionId) {
        return questions.values().stream().flatMap(List::stream).filter(q -> q.id().equals(questionId)).findFirst();
    }
}
