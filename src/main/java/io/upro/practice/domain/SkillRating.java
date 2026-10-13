package io.upro.practice.domain;

import io.upro.catalog.domain.Level;

import java.util.Objects;

/**
 * Niveau estime sur un module, calcule facon Elo : chaque reponse est un "match"
 * contre une question dont la difficulte depend de son niveau. Reussir une question
 * difficile fait beaucoup monter le rating ; rater une question facile le fait baisser.
 */
public record SkillRating(String moduleId, double rating, int answers) {

    /** Hypothese de depart : niveau Senior, affinee par les reponses. */
    public static final double INITIAL_RATING = Level.SENIOR.difficulty();
    /** Nombre de reponses en dessous duquel l'estimation est consideree provisoire. */
    public static final int CALIBRATION_ANSWERS = 15;

    private static final double K_CALIBRATION = 40;
    private static final double K_STABLE = 20;

    public SkillRating {
        Objects.requireNonNull(moduleId, "moduleId is required");
        if (answers < 0) {
            throw new IllegalArgumentException("answers must be >= 0");
        }
    }

    public static SkillRating initial(String moduleId) {
        return new SkillRating(moduleId, INITIAL_RATING, 0);
    }

    /** Probabilite attendue de reussir une question de ce niveau. */
    public double expectedSuccess(Level questionLevel) {
        return 1.0 / (1.0 + Math.pow(10, (questionLevel.difficulty() - rating) / 400.0));
    }

    public SkillRating record(Level questionLevel, boolean correct) {
        double k = isCalibrating() ? K_CALIBRATION : K_STABLE;
        double delta = k * ((correct ? 1.0 : 0.0) - expectedSuccess(questionLevel));
        return new SkillRating(moduleId, rating + delta, answers + 1);
    }

    public Level level() {
        return Level.fromRating(rating);
    }

    public boolean isCalibrating() {
        return answers < CALIBRATION_ANSWERS;
    }

    /**
     * Avancement (0-100) entre le niveau courant et le suivant.
     */
    public int progressToNextLevel() {
        Level current = level();
        if (current == Level.ARCHITECT) {
            return 100;
        }
        double from = current.difficulty() - 100.0;
        double to = current.next().difficulty() - 100.0;
        double pct = 100.0 * (rating - from) / (to - from);
        return (int) Math.max(0, Math.min(100, Math.round(pct)));
    }
}
