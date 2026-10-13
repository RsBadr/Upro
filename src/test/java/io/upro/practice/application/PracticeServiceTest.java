package io.upro.practice.application;

import io.upro.catalog.application.CatalogService;
import io.upro.catalog.domain.Level;
import io.upro.practice.domain.SkillRating;
import io.upro.shared.application.Transactions;
import io.upro.testing.InMemoryStores;
import io.upro.testing.MutableClock;
import io.upro.testing.StubCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PracticeServiceTest {

    private final InMemoryStores stores = new InMemoryStores();
    private PracticeService practice;

    @BeforeEach
    void setUp() {
        StubCatalog catalog = new StubCatalog()
                .question("java", "q1", Level.SENIOR)
                .question("java", "q2", Level.LEAD);
        practice = new PracticeService(new CatalogService(catalog), stores.cards, stores.ratings, stores.answers,
                Transactions.none(), new MutableClock("2026-01-10T08:00:00Z"));
    }

    @Test
    void newQuestionsAreDueImmediately() {
        assertThat(practice.nextQuestions("java", 10)).extracting("id").containsExactly("q1", "q2");
    }

    @Test
    void correctlyAnsweredQuestionIsNoLongerDueToday() {
        AnswerResult result = practice.answer("q1", 0);

        assertThat(result.correct()).isTrue();
        assertThat(practice.nextQuestions("java", 10)).extracting("id").containsExactly("q2");
    }

    @Test
    void wrongAnswerKeepsQuestionDueAndReturnsExplanation() {
        AnswerResult result = practice.answer("q1", 1);

        assertThat(result.correct()).isFalse();
        assertThat(result.correctChoice()).isZero();
        assertThat(result.explanation()).isEqualTo("because");
        assertThat(practice.nextQuestions("java", 10)).extracting("id").contains("q1");
    }

    @Test
    void answeringUpdatesTheLevelAndLogsTheAnswer() {
        AnswerResult result = practice.answer("q2", 0);

        assertThat(result.ratingDelta()).isPositive();
        assertThat(practice.ratingOf("java").rating()).isGreaterThan(SkillRating.INITIAL_RATING);
        assertThat(practice.levels()).singleElement().satisfies(level -> {
            assertThat(level.answers()).isEqualTo(1);
            assertThat(level.calibrating()).isTrue();
        });
        assertThat(stores.answers.firstAnswerDay()).isPresent();
    }

    @Test
    void progressCountsSeenAndDueQuestions() {
        practice.answer("q1", 0);

        assertThat(practice.progress()).containsExactly(new ModuleProgress("java", 2, 1, 0, 1));
    }

    @Test
    void rejectsOutOfRangeChoice() {
        assertThatThrownBy(() -> practice.answer("q1", 5)).isInstanceOf(IllegalArgumentException.class);
    }
}
