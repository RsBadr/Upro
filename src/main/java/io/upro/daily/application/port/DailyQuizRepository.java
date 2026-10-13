package io.upro.daily.application.port;

import io.upro.daily.domain.DailyQuiz;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyQuizRepository {

    Optional<DailyQuiz> find(LocalDate date);

    void save(DailyQuiz quiz);
}
