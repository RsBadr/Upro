package io.upro.practice.domain;

import io.upro.catalog.domain.Level;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Trace d'une reponse : alimente les bilans quotidiens et l'historique de niveau.
 */
public record AnswerRecord(
        String questionId,
        String moduleId,
        Level questionLevel,
        boolean correct,
        Instant answeredAt,
        LocalDate answeredOn,
        double ratingBefore,
        double ratingAfter) {
}
