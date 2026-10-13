package io.upro.catalog.application;

public class QuestionNotFoundException extends RuntimeException {

    public QuestionNotFoundException(String questionId) {
        super("Unknown question: " + questionId);
    }
}
