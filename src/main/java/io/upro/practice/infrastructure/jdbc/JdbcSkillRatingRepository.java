package io.upro.practice.infrastructure.jdbc;

import io.upro.practice.application.port.SkillRatingRepository;
import io.upro.practice.domain.SkillRating;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.Optional;

public class JdbcSkillRatingRepository implements SkillRatingRepository {

    private final JdbcClient jdbc;

    public JdbcSkillRatingRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<SkillRating> find(String moduleId) {
        return jdbc.sql("select module_id, rating, answers from skill_rating where module_id = ?")
                .param(moduleId)
                .query((rs, n) -> new SkillRating(rs.getString("module_id"), rs.getDouble("rating"), rs.getInt("answers")))
                .optional();
    }

    @Override
    public void save(SkillRating rating) {
        int updated = jdbc.sql("update skill_rating set rating = ?, answers = ? where module_id = ?")
                .params(rating.rating(), rating.answers(), rating.moduleId())
                .update();
        if (updated == 0) {
            jdbc.sql("insert into skill_rating (module_id, rating, answers) values (?, ?, ?)")
                    .params(rating.moduleId(), rating.rating(), rating.answers())
                    .update();
        }
    }
}
