package io.upro.daily.infrastructure.jdbc;

import io.upro.daily.application.port.DailyQuizRepository;
import io.upro.daily.domain.DailyQuiz;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class JdbcDailyQuizRepository implements DailyQuizRepository {

    private final JdbcClient jdbc;

    public JdbcDailyQuizRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<DailyQuiz> find(LocalDate date) {
        List<String> ids = jdbc.sql("select question_id from daily_quiz_item where quiz_date = ? order by position")
                .param(date)
                .query(String.class)
                .list();
        return ids.isEmpty() ? Optional.empty() : Optional.of(new DailyQuiz(date, ids));
    }

    @Override
    public void save(DailyQuiz quiz) {
        jdbc.sql("delete from daily_quiz_item where quiz_date = ?").param(quiz.date()).update();
        for (int i = 0; i < quiz.questionIds().size(); i++) {
            jdbc.sql("insert into daily_quiz_item (quiz_date, position, question_id) values (?, ?, ?)")
                    .params(quiz.date(), i, quiz.questionIds().get(i))
                    .update();
        }
    }
}
