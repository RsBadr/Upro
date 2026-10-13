package io.upro.practice.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewCardTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 1, 10);

    @Test
    void correctAnswerMovesCardUpOneBoxAndSpacesTheReview() {
        ReviewCard card = ReviewCard.fresh("q1", TODAY).answer(true, TODAY);

        assertThat(card.box()).isEqualTo(2);
        assertThat(card.nextReviewOn()).isEqualTo(TODAY.plusDays(1));
        assertThat(card.isDue(TODAY)).isFalse();
        assertThat(card.successes()).isEqualTo(1);
    }

    @Test
    void wrongAnswerSendsCardBackToFirstBox() {
        ReviewCard card = new ReviewCard("q1", 4, TODAY, 5, 4).answer(false, TODAY);

        assertThat(card.box()).isEqualTo(LeitnerSchedule.FIRST_BOX);
        assertThat(card.isDue(TODAY)).isTrue();
        assertThat(card.attempts()).isEqualTo(6);
        assertThat(card.successes()).isEqualTo(4);
    }

    @Test
    void boxIsCappedAtLastBox() {
        ReviewCard card = new ReviewCard("q1", LeitnerSchedule.lastBox(), TODAY, 10, 10).answer(true, TODAY);

        assertThat(card.box()).isEqualTo(LeitnerSchedule.lastBox());
        assertThat(card.isMastered()).isTrue();
    }
}
