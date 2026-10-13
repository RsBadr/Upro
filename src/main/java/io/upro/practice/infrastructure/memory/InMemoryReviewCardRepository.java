package io.upro.practice.infrastructure.memory;

import io.upro.practice.application.port.ReviewCardRepository;
import io.upro.practice.domain.ReviewCard;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter provisoire : la progression est perdue au redemarrage.
 * A remplacer par un adapter JPA/PostgreSQL (voir docs/ROADMAP.md).
 */
public class InMemoryReviewCardRepository implements ReviewCardRepository {

    private final Map<String, ReviewCard> store = new ConcurrentHashMap<>();

    @Override
    public Optional<ReviewCard> find(String questionId) {
        return Optional.ofNullable(store.get(questionId));
    }

    @Override
    public void save(ReviewCard card) {
        store.put(card.questionId(), card);
    }
}
