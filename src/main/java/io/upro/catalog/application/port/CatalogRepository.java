package io.upro.catalog.application.port;

import io.upro.catalog.domain.LearningModule;
import io.upro.catalog.domain.Question;

import java.util.List;
import java.util.Optional;

/**
 * Port de sortie : source du contenu pedagogique (fichiers YAML, base, API...).
 */
public interface CatalogRepository {

    List<LearningModule> findAllModules();

    Optional<LearningModule> findModule(String moduleId);

    List<Question> findQuestionsByModule(String moduleId);

    Optional<Question> findQuestion(String questionId);
}
