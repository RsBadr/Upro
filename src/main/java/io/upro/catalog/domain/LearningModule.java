package io.upro.catalog.domain;

import java.util.Objects;

/**
 * Un module d'apprentissage (Java, Architecture, Anglais, Best practices...).
 */
public record LearningModule(String id, String title, String description) {

    public LearningModule {
        requireText(id, "id");
        requireText(title, "title");
        description = description == null ? "" : description;
    }

    private static void requireText(String value, String field) {
        if (Objects.requireNonNull(value, field + " is required").isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
