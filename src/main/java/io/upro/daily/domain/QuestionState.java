package io.upro.daily.domain;

import io.upro.catalog.domain.Question;
import io.upro.practice.domain.ReviewCard;

/**
 * Une question et son etat d'apprentissage, vus par le compositeur de quiz.
 */
public record QuestionState(Question question, ReviewCard card) {

    boolean isNew() {
        return card.attempts() == 0;
    }

    double successRatio() {
        return card.attempts() == 0 ? 0 : (double) card.successes() / card.attempts();
    }
}
