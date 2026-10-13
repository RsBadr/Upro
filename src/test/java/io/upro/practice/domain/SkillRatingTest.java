package io.upro.practice.domain;

import io.upro.catalog.domain.Level;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SkillRatingTest {

    private final SkillRating senior = SkillRating.initial("java");

    @Test
    void startsAsSeniorAndCalibrating() {
        assertThat(senior.level()).isEqualTo(Level.SENIOR);
        assertThat(senior.isCalibrating()).isTrue();
    }

    @Test
    void succeedingAHardQuestionGainsMoreThanAnEasyOne() {
        double hard = senior.record(Level.ARCHITECT, true).rating() - senior.rating();
        double easy = senior.record(Level.CONFIRMED, true).rating() - senior.rating();

        assertThat(hard).isGreaterThan(easy).isPositive();
        assertThat(easy).isPositive();
    }

    @Test
    void failingAnEasyQuestionCostsMoreThanAHardOne() {
        double easy = senior.rating() - senior.record(Level.CONFIRMED, false).rating();
        double hard = senior.rating() - senior.record(Level.ARCHITECT, false).rating();

        assertThat(easy).isGreaterThan(hard).isPositive();
    }

    @Test
    void consistentSuccessOnArchitectQuestionsReachesArchitectLevel() {
        SkillRating rating = senior;
        for (int i = 0; i < 60; i++) {
            rating = rating.record(Level.ARCHITECT, true);
        }
        assertThat(rating.level()).isEqualTo(Level.ARCHITECT);
        assertThat(rating.isCalibrating()).isFalse();
    }

    @Test
    void consistentFailureDropsToConfirmed() {
        SkillRating rating = senior;
        for (int i = 0; i < 30; i++) {
            rating = rating.record(Level.CONFIRMED, false);
        }
        assertThat(rating.level()).isEqualTo(Level.CONFIRMED);
    }

    @Test
    void progressToNextLevelIsBetweenZeroAndHundred() {
        assertThat(senior.progressToNextLevel()).isEqualTo(50);
        assertThat(new SkillRating("java", 1290, 20).progressToNextLevel()).isEqualTo(95);
    }
}
