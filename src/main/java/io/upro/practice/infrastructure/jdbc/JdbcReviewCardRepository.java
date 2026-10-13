package io.upro.practice.infrastructure.jdbc;

import io.upro.practice.application.port.ReviewCardRepository;
import io.upro.practice.domain.ReviewCard;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.time.LocalDate;
import java.util.Optional;

public class JdbcReviewCardRepository implements ReviewCardRepository {

    private final JdbcClient jdbc;

    public JdbcReviewCardRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<ReviewCard> find(String questionId) {
        return jdbc.sql("select question_id, box, next_review_on, attempts, successes from review_card where question_id = ?")
                .param(questionId)
                .query((rs, n) -> new ReviewCard(rs.getString("question_id"), rs.getInt("box"),
                        rs.getObject("next_review_on", LocalDate.class), rs.getInt("attempts"), rs.getInt("successes")))
                .optional();
    }

    @Override
    public void save(ReviewCard card) {
        int updated = jdbc.sql("update review_card set box = ?, next_review_on = ?, attempts = ?, successes = ? where question_id = ?")
                .params(card.box(), card.nextReviewOn(), card.attempts(), card.successes(), card.questionId())
                .update();
        if (updated == 0) {
            jdbc.sql("insert into review_card (question_id, box, next_review_on, attempts, successes) values (?, ?, ?, ?, ?)")
                    .params(card.questionId(), card.box(), card.nextReviewOn(), card.attempts(), card.successes())
                    .update();
        }
    }
}
