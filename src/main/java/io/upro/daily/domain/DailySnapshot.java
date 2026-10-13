package io.upro.daily.domain;

import io.upro.catalog.domain.Level;

import java.time.LocalDate;

/**
 * Bilan d'une journee sur un module : niveau en fin de journee et activite.
 */
public record DailySnapshot(LocalDate date, String moduleId, double rating, Level level, int answers, int correct) {
}
