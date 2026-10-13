package io.upro.daily.application;

import java.time.LocalDate;
import java.util.List;

/**
 * Historique de progression : un point par jour, avec le niveau de chaque module.
 */
public record DailyReport(LocalDate date, int answers, int correct, List<ModuleDay> modules) {

    public record ModuleDay(String moduleId, int rating, String level, int answers, int correct) {
    }
}
