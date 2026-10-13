package io.upro.practice.application.port;

import io.upro.practice.domain.ReviewCard;

import java.util.Optional;

/**
 * Port de sortie : persistance de la progression.
 */
public interface ReviewCardRepository {

    Optional<ReviewCard> find(String questionId);

    void save(ReviewCard card);
}
