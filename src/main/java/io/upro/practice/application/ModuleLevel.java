package io.upro.practice.application;

/**
 * Jugement de niveau sur un module.
 *
 * @param calibrating vrai tant qu'il y a trop peu de reponses pour que l'estimation soit fiable
 */
public record ModuleLevel(
        String moduleId,
        String level,
        String nextLevel,
        int rating,
        int answers,
        boolean calibrating,
        int progressToNextLevel) {
}
