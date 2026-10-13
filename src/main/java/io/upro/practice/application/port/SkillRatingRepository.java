package io.upro.practice.application.port;

import io.upro.practice.domain.SkillRating;

import java.util.Optional;

public interface SkillRatingRepository {

    Optional<SkillRating> find(String moduleId);

    void save(SkillRating rating);
}
