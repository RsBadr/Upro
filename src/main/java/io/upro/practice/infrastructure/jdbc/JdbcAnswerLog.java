package io.upro.practice.infrastructure.jdbc;

import io.upro.catalog.domain.Level;
import io.upro.practice.application.port.AnswerLog;
import io.upro.practice.domain.AnswerRecord;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class JdbcAnswerLog implements AnswerLog {

    private final JdbcClient jdbc;

    public JdbcAnswerLog(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void append(AnswerRecord r) {
        jdbc.sql("""
                        insert into answer_log (question_id, module_id, question_level, correct, answered_at,
                                                answered_on, rating_before, rating_after)
                        values (?, ?, ?, ?, ?, ?, ?, ?)""")
                .params(r.questionId(), r.moduleId(), r.questionLevel().name(), r.correct(),
                        Timestamp.from(r.answeredAt()), r.answeredOn(), r.ratingBefore(), r.ratingAfter())
                .update();
    }

    @Override
    public List<AnswerRecord> answeredOn(LocalDate day) {
        return jdbc.sql("""
                        select question_id, module_id, question_level, correct, answered_at, answered_on,
                               rating_before, rating_after
                        from answer_log where answered_on = ? order by id""")
                .param(day)
                .query((rs, n) -> new AnswerRecord(rs.getString("question_id"), rs.getString("module_id"),
                        Level.valueOf(rs.getString("question_level")), rs.getBoolean("correct"),
                        rs.getTimestamp("answered_at").toInstant(), rs.getObject("answered_on", LocalDate.class),
                        rs.getDouble("rating_before"), rs.getDouble("rating_after")))
                .list();
    }

    @Override
    public Optional<Double> ratingAtEndOf(String moduleId, LocalDate day) {
        return jdbc.sql("""
                        select rating_after from answer_log
                        where module_id = ? and answered_on <= ?
                        order by id desc limit 1""")
                .params(moduleId, day)
                .query(Double.class)
                .optional();
    }

    @Override
    public Optional<LocalDate> firstAnswerDay() {
        // max()/min() renvoient une ligne avec NULL quand la table est vide
        return jdbc.sql("select min(answered_on) from answer_log")
                .query((rs, n) -> Optional.ofNullable(rs.getObject(1, LocalDate.class)))
                .single();
    }
}
