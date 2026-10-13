package io.upro.practice.application;

import io.upro.catalog.application.CatalogService;
import io.upro.catalog.application.port.CatalogRepository;
import io.upro.catalog.domain.LearningModule;
import io.upro.catalog.domain.Level;
import io.upro.catalog.domain.Question;
import io.upro.practice.infrastructure.memory.InMemoryReviewCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PracticeServiceTest {

    private static final Question Q1 = question("q1");
    private static final Question Q2 = question("q2");

    private PracticeService practice;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-10T08:00:00Z"), ZoneOffset.UTC);
        CatalogService catalog = new CatalogService(new StubCatalog());
        practice = new PracticeService(catalog, new InMemoryReviewCardRepository(), clock);
    }

    @Test
    void newQuestionsAreDueImmediately() {
        assertThat(practice.nextQuestions("java", 10)).containsExactly(Q1, Q2);
    }

    @Test
    void correctlyAnsweredQuestionIsNoLongerDueToday() {
        AnswerResult result = practice.answer("q1", 0);

        assertThat(result.correct()).isTrue();
        assertThat(practice.nextQuestions("java", 10)).containsExactly(Q2);
    }

    @Test
    void wrongAnswerKeepsQuestionDueAndReturnsExplanation() {
        AnswerResult result = practice.answer("q1", 1);

        assertThat(result.correct()).isFalse();
        assertThat(result.correctChoice()).isZero();
        assertThat(result.explanation()).isEqualTo("because");
        assertThat(practice.nextQuestions("java", 10)).contains(Q1);
    }

    @Test
    void progressCountsSeenAndDueQuestions() {
        practice.answer("q1", 0);

        assertThat(practice.progress())
                .containsExactly(new ModuleProgress("java", 2, 1, 0, 1));
    }

    @Test
    void rejectsOutOfRangeChoice() {
        assertThatThrownBy(() -> practice.answer("q1", 5)).isInstanceOf(IllegalArgumentException.class);
    }

    private static Question question(String id) {
        return new Question(id, "java", "topic", Level.SENIOR, "prompt?", List.of("good", "bad"), 0, "because");
    }

    private static class StubCatalog implements CatalogRepository {

        private final LearningModule java = new LearningModule("java", "Java", "");

        @Override
        public List<LearningModule> findAllModules() {
            return List.of(java);
        }

        @Override
        public Optional<LearningModule> findModule(String moduleId) {
            return java.id().equals(moduleId) ? Optional.of(java) : Optional.empty();
        }

        @Override
        public List<Question> findQuestionsByModule(String moduleId) {
            return List.of(Q1, Q2);
        }

        @Override
        public Optional<Question> findQuestion(String questionId) {
            return findQuestionsByModule("java").stream().filter(q -> q.id().equals(questionId)).findFirst();
        }
    }
}
