package io.upro.catalog.domain;

import java.util.List;
import java.util.Objects;

/**
 * Question a choix multiples. Invariants verifies a la construction :
 * au moins deux choix et un index de bonne reponse valide.
 */
public record Question(
        String id,
        String moduleId,
        String topic,
        Level level,
        String prompt,
        List<String> choices,
        int answerIndex,
        String explanation) {

    public Question {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(moduleId, "moduleId is required");
        Objects.requireNonNull(level, "level is required");
        Objects.requireNonNull(prompt, "prompt is required");
        choices = List.copyOf(Objects.requireNonNull(choices, "choices are required"));
        if (choices.size() < 2) {
            throw new IllegalArgumentException("Question " + id + " needs at least two choices");
        }
        if (answerIndex < 0 || answerIndex >= choices.size()) {
            throw new IllegalArgumentException("Question " + id + " has an invalid answerIndex: " + answerIndex);
        }
        topic = topic == null ? "" : topic;
        explanation = explanation == null ? "" : explanation;
    }

    public boolean isCorrect(int choiceIndex) {
        return choiceIndex == answerIndex;
    }
}
