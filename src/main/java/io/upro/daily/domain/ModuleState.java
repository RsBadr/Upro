package io.upro.daily.domain;

import io.upro.practice.domain.SkillRating;

import java.util.List;

public record ModuleState(String moduleId, SkillRating rating, List<QuestionState> questions) {

    public ModuleState {
        questions = List.copyOf(questions);
    }
}
