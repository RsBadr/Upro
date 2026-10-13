package io.upro.practice.application;

public record ModuleProgress(String moduleId, int totalQuestions, int seen, int mastered, int dueToday) {
}
