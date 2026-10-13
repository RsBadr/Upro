package io.upro.catalog.domain;

/**
 * Niveau de seniorite vise par une question. Sert a mesurer la progression
 * vers les postes Lead / Staff / Architecte.
 *
 * <p>Chaque niveau a une difficulte exprimee sur une echelle Elo : c'est ce qui permet
 * de comparer le niveau estime du developpeur a celui d'une question.
 */
public enum Level {
    CONFIRMED(1000),
    SENIOR(1200),
    LEAD(1400),
    ARCHITECT(1600);

    private final int difficulty;

    Level(int difficulty) {
        this.difficulty = difficulty;
    }

    public int difficulty() {
        return difficulty;
    }

    /** Niveau dont la difficulte est la plus proche de ce rating. */
    public static Level fromRating(double rating) {
        Level closest = CONFIRMED;
        for (Level level : values()) {
            if (Math.abs(level.difficulty - rating) < Math.abs(closest.difficulty - rating)) {
                closest = level;
            }
        }
        return closest;
    }

    /** Niveau suivant, ou {@code this} si c'est deja le plus haut. */
    public Level next() {
        return this == ARCHITECT ? this : values()[ordinal() + 1];
    }
}
