package io.upro.catalog.infrastructure.yaml;

import io.upro.catalog.application.port.CatalogRepository;
import io.upro.catalog.domain.LearningModule;
import io.upro.catalog.domain.Level;
import io.upro.catalog.domain.Question;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Adapter qui charge le contenu depuis {@code classpath:content/*.yml}.
 * Un fichier = un module. Ajouter du contenu ne demande aucune ligne de Java.
 */
public class YamlCatalogRepository implements CatalogRepository {

    private final Map<String, LearningModule> modules = new LinkedHashMap<>();
    private final Map<String, List<Question>> questionsByModule = new LinkedHashMap<>();
    private final Map<String, Question> questionsById = new LinkedHashMap<>();

    public YamlCatalogRepository(String locationPattern) {
        YAMLMapper mapper = new YAMLMapper();
        for (Resource resource : resolve(locationPattern)) {
            load(mapper, resource);
        }
    }

    private static List<Resource> resolve(String locationPattern) {
        try {
            List<Resource> resources = new ArrayList<>(List.of(
                    new PathMatchingResourcePatternResolver().getResources(locationPattern)));
            resources.sort(Comparator.comparing(r -> String.valueOf(r.getFilename())));
            return resources;
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot list content files: " + locationPattern, e);
        }
    }

    private void load(YAMLMapper mapper, Resource resource) {
        ModuleFile file;
        try (InputStream in = resource.getInputStream()) {
            file = mapper.readValue(in, ModuleFile.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + resource.getFilename(), e);
        }
        LearningModule module = new LearningModule(file.id(), file.title(), file.description());
        if (modules.putIfAbsent(module.id(), module) != null) {
            throw new IllegalStateException("Duplicate module id: " + module.id());
        }
        List<Question> questions = new ArrayList<>();
        for (QuestionEntry entry : file.questions() == null ? List.<QuestionEntry>of() : file.questions()) {
            Question question = entry.toDomain(module.id());
            if (questionsById.putIfAbsent(question.id(), question) != null) {
                throw new IllegalStateException("Duplicate question id: " + question.id());
            }
            questions.add(question);
        }
        questionsByModule.put(module.id(), List.copyOf(questions));
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
        return questionsByModule.getOrDefault(moduleId, List.of());
    }

    @Override
    public Optional<Question> findQuestion(String questionId) {
        return Optional.ofNullable(questionsById.get(questionId));
    }

    record ModuleFile(String id, String title, String description, List<QuestionEntry> questions) {
    }

    record QuestionEntry(String id, String topic, Level level, String prompt,
                         List<String> choices, int answer, String explanation) {

        /**
         * Les auteurs ecrivent souvent la bonne reponse au meme endroit : on melange les choix
         * de facon deterministe (meme ordre a chaque demarrage) pour ne pas creer de biais de position.
         */
        Question toDomain(String moduleId) {
            if (choices == null || answer < 0 || answer >= choices.size()) {
                return new Question(id, moduleId, topic, level, prompt, choices, answer, explanation);
            }
            List<String> shuffled = new ArrayList<>(choices);
            Collections.shuffle(shuffled, new Random(id.hashCode()));
            return new Question(id, moduleId, topic, level, prompt, shuffled,
                    shuffled.indexOf(choices.get(answer)), explanation);
        }
    }
}
