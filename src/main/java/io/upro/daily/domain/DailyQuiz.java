package io.upro.daily.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public record DailyQuiz(LocalDate date, List<String> questionIds) {

    public DailyQuiz {
        Objects.requireNonNull(date, "date is required");
        questionIds = List.copyOf(questionIds);
    }
}
