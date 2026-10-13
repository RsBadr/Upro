package io.upro.catalog.application;

import io.upro.catalog.application.port.CatalogRepository;
import io.upro.catalog.domain.LearningModule;
import io.upro.catalog.domain.Question;

import java.util.List;
import java.util.Optional;

/**
 * Cas d'usage de consultation du catalogue. Volontairement sans dependance
 * a Spring : le cablage se fait dans {@code io.upro.config}.
 */
public class CatalogService {

    private final CatalogRepository repository;

    public CatalogService(CatalogRepository repository) {
        this.repository = repository;
    }

    public List<LearningModule> listModules() {
        return repository.findAllModules();
    }

    public LearningModule getModule(String moduleId) {
        return repository.findModule(moduleId).orElseThrow(() -> new ModuleNotFoundException(moduleId));
    }

    public List<Question> questionsOf(String moduleId) {
        getModule(moduleId);
        return repository.findQuestionsByModule(moduleId);
    }

    public Optional<Question> findQuestion(String questionId) {
        return repository.findQuestion(questionId);
    }

    public Question getQuestion(String questionId) {
        return repository.findQuestion(questionId).orElseThrow(() -> new QuestionNotFoundException(questionId));
    }
}
