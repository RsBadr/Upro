package io.upro.practice.domain;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Etat d'apprentissage d'une question : boite Leitner, prochaine revision, statistiques.
 * Objet immuable : chaque reponse produit une nouvelle carte.
 */
public record ReviewCard(String questionId, int box, LocalDate nextReviewOn, int attempts, int successes) {

    public ReviewCard {
        Objects.requireNonNull(questionId, "questionId is required");
        Objects.requireNonNull(nextReviewOn, "nextReviewOn is required");
    }

    public static ReviewCard fresh(String questionId, LocalDate today) {
        return new ReviewCard(questionId, LeitnerSchedule.FIRST_BOX, today, 0, 0);
    }

    public ReviewCard answer(boolean correct, LocalDate today) {
        int newBox = correct ? Math.min(box + 1, LeitnerSchedule.lastBox()) : LeitnerSchedule.FIRST_BOX;
        return new ReviewCard(questionId, newBox, LeitnerSchedule.nextReview(newBox, today),
                attempts + 1, successes + (correct ? 1 : 0));
    }

    public boolean isDue(LocalDate today) {
        return !nextReviewOn.isAfter(today);
    }

    public boolean isMastered() {
        return box == LeitnerSchedule.lastBox();
    }
}
