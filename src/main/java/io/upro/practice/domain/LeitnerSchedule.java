package io.upro.practice.domain;

import java.time.LocalDate;

/**
 * Repetition espacee facon Leitner : une bonne reponse fait monter la carte
 * d'une boite (revision plus espacee), une erreur la renvoie en boite 1.
 */
public final class LeitnerSchedule {

    public static final int FIRST_BOX = 1;
    /** Delai avant la prochaine revision, indexe par boite (boite 1 = a revoir le jour meme). */
    private static final int[] INTERVAL_DAYS = {0, 1, 3, 7, 15, 30};

    private LeitnerSchedule() {
    }

    public static int lastBox() {
        return INTERVAL_DAYS.length;
    }

    public static LocalDate nextReview(int box, LocalDate today) {
        if (box < FIRST_BOX || box > lastBox()) {
            throw new IllegalArgumentException("Box out of range: " + box);
        }
        return today.plusDays(INTERVAL_DAYS[box - 1]);
    }
}
