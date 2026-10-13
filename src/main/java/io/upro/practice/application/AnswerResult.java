package io.upro.practice.application;

import java.time.LocalDate;

public record AnswerResult(boolean correct, int correctChoice, String explanation, int box, LocalDate nextReviewOn) {
}
