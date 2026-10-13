package io.upro.daily.domain;

import io.upro.catalog.domain.Level;
import io.upro.catalog.domain.Question;
import io.upro.practice.domain.ReviewCard;
import io.upro.practice.domain.SkillRating;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DailyQuizComposerTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 3, 2);

    private final DailyQuizComposer composer = new DailyQuizComposer();

    @Test
    void picksNewQuestionsCloseToTheEstimatedLevelPlusAStep() {
        ModuleState lead = module("archi", 1400, fresh("archi", Level.values()));

        DailyQuiz quiz = composer.compose(TODAY, 2, List.of(lead));

        // cible = 1400 + 75 : LEAD (1400) puis ARCHITECT (1600)
        assertThat(quiz.questionIds()).containsExactlyInAnyOrder("archi-LEAD-0", "archi-ARCHITECT-0");
    }

    @Test
    void weakerLevelGetsEasierQuestions() {
        ModuleState confirmed = module("java", 1000, fresh("java", Level.values()));

        DailyQuiz quiz = composer.compose(TODAY, 2, List.of(confirmed));

        assertThat(quiz.questionIds()).containsExactlyInAnyOrder("java-CONFIRMED-0", "java-SENIOR-0");
    }

    @Test
    void reservesPartOfTheQuizForDueReviews() {
        List<QuestionState> questions = new ArrayList<>(fresh("java", Level.SENIOR, Level.SENIOR, Level.SENIOR, Level.SENIOR));
        questions.add(seen("java", "due-1", TODAY.minusDays(1)));
        questions.add(seen("java", "due-2", TODAY));
        questions.add(seen("java", "later", TODAY.plusDays(3)));

        DailyQuiz quiz = composer.compose(TODAY, 5, List.of(module("java", 1200, questions)));

        assertThat(quiz.questionIds()).hasSize(5).contains("due-1", "due-2").doesNotContain("later");
    }

    @Test
    void spreadsNewQuestionsAcrossModules() {
        DailyQuiz quiz = composer.compose(TODAY, 4, List.of(
                module("java", 1200, fresh("java", Level.SENIOR, Level.SENIOR, Level.SENIOR)),
                module("english", 1200, fresh("english", Level.SENIOR, Level.SENIOR, Level.SENIOR))));

        assertThat(quiz.questionIds()).filteredOn(id -> id.startsWith("java")).hasSize(2);
        assertThat(quiz.questionIds()).filteredOn(id -> id.startsWith("english")).hasSize(2);
    }

    @Test
    void fillsWithWeakestQuestionsWhenTheBankIsExhausted() {
        List<QuestionState> questions = List.of(
                seen("java", "mastered", TODAY.plusDays(30), 5, 5),
                seen("java", "shaky", TODAY.plusDays(1), 4, 1));

        DailyQuiz quiz = composer.compose(TODAY, 1, List.of(module("java", 1200, questions)));

        assertThat(quiz.questionIds()).containsExactly("shaky");
    }

    @Test
    void sameDayGivesSameQuiz() {
        List<ModuleState> modules = List.of(module("java", 1200, fresh("java", Level.SENIOR, Level.SENIOR, Level.SENIOR,
                Level.SENIOR, Level.SENIOR, Level.SENIOR)));

        assertThat(composer.compose(TODAY, 3, modules)).isEqualTo(composer.compose(TODAY, 3, modules));
    }

    private static ModuleState module(String id, double rating, List<QuestionState> questions) {
        return new ModuleState(id, new SkillRating(id, rating, 20), questions);
    }

    private static List<QuestionState> fresh(String moduleId, Level... levels) {
        List<QuestionState> result = new ArrayList<>();
        for (int i = 0; i < levels.length; i++) {
            long sameLevelBefore = result.stream().filter(q -> q.question().level() == levels[result.size()]).count();
            String id = moduleId + "-" + levels[i].name() + "-" + sameLevelBefore;
            result.add(new QuestionState(question(moduleId, id, levels[i]), ReviewCard.fresh(id, TODAY)));
        }
        return result;
    }

    private static QuestionState seen(String moduleId, String id, LocalDate nextReview) {
        return seen(moduleId, id, nextReview, 1, 0);
    }

    private static QuestionState seen(String moduleId, String id, LocalDate nextReview, int attempts, int successes) {
        return new QuestionState(question(moduleId, id, Level.SENIOR), new ReviewCard(id, 2, nextReview, attempts, successes));
    }

    private static Question question(String moduleId, String id, Level level) {
        return new Question(id, moduleId, "t", level, "p", List.of("a", "b"), 0, "e");
    }
}
