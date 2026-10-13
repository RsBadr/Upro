package io.upro.practice.application.port;

import io.upro.practice.domain.AnswerRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Journal des reponses (append-only).
 */
public interface AnswerLog {

    void append(AnswerRecord record);

    List<AnswerRecord> answeredOn(LocalDate day);

    /** Rating du module a la fin de la journee donnee, si au moins une reponse existe jusque-la. */
    Optional<Double> ratingAtEndOf(String moduleId, LocalDate day);

    Optional<LocalDate> firstAnswerDay();
}
