package io.upro.daily.application;

import java.time.LocalDate;
import java.util.List;

public record DailyQuizView(LocalDate date, int total, int answered, int correct, List<Item> items) {

    /** Question du quiz, sans la bonne reponse. {@code correct} est null tant qu'elle n'est pas repondue. */
    public record Item(String id, String moduleId, String topic, String level, String prompt,
                       List<String> choices, boolean answered, Boolean correct) {
    }
}
