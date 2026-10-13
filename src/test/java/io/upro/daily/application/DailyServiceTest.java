package io.upro.daily.application;

import io.upro.catalog.application.CatalogService;
import io.upro.catalog.domain.Level;
import io.upro.daily.domain.DailySnapshot;
import io.upro.practice.application.PracticeService;
import io.upro.shared.application.Transactions;
import io.upro.testing.InMemoryStores;
import io.upro.testing.MutableClock;
import io.upro.testing.StubCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DailyServiceTest {

    private final InMemoryStores stores = new InMemoryStores();
    private final MutableClock clock = new MutableClock("2026-03-02T09:00:00Z");
    private PracticeService practice;
    private DailyService daily;

    @BeforeEach
    void setUp() {
        StubCatalog stub = new StubCatalog();
        for (int i = 0; i < 6; i++) {
            stub.question("java", "java-" + i, Level.values()[i % 4]);
            stub.question("english", "en-" + i, Level.values()[i % 4]);
        }
        CatalogService catalog = new CatalogService(stub);
        practice = new PracticeService(catalog, stores.cards, stores.ratings, stores.answers, Transactions.none(), clock);
        daily = new DailyService(catalog, practice, stores.answers, stores.quizzes, stores.snapshots,
                Transactions.none(), clock, 4);
    }

    @Test
    void todayQuizIsStableDuringTheDayAndTracksAnswers() {
        DailyQuizView first = daily.todayQuiz();
        String questionId = first.items().getFirst().id();

        practice.answer(questionId, 0);
        DailyQuizView second = daily.todayQuiz();

        assertThat(second.items()).extracting(DailyQuizView.Item::id)
                .containsExactlyElementsOf(first.items().stream().map(DailyQuizView.Item::id).toList());
        assertThat(second.total()).isEqualTo(4);
        assertThat(second.answered()).isEqualTo(1);
        assertThat(second.correct()).isEqualTo(1);
    }

    @Test
    void dailyRoutineClosesPreviousDaysAndIsIdempotent() {
        practice.answer("java-1", 0);
        practice.answer("java-2", 1);

        clock.advance(Duration.ofDays(2));
        daily.runDailyRoutine();
        daily.runDailyRoutine();

        LocalDate day1 = LocalDate.of(2026, 3, 2);
        assertThat(stores.snapshots.all()).hasSize(4);
        DailySnapshot javaDay1 = stores.snapshots.all().stream()
                .filter(s -> s.date().equals(day1) && s.moduleId().equals("java"))
                .findFirst().orElseThrow();
        assertThat(javaDay1.answers()).isEqualTo(2);
        assertThat(javaDay1.correct()).isEqualTo(1);
        assertThat(javaDay1.rating()).isEqualTo(practice.ratingOf("java").rating());

        assertThat(daily.history(30)).extracting(DailyReport::date)
                .containsExactly(day1, day1.plusDays(1));
        assertThat(stores.quizzes.find(day1.plusDays(2))).isPresent();
    }
}
