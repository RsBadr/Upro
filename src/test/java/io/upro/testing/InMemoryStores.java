package io.upro.testing;

import io.upro.daily.application.port.DailyQuizRepository;
import io.upro.daily.application.port.DailySnapshotRepository;
import io.upro.daily.domain.DailyQuiz;
import io.upro.daily.domain.DailySnapshot;
import io.upro.practice.application.port.AnswerLog;
import io.upro.practice.application.port.ReviewCardRepository;
import io.upro.practice.application.port.SkillRatingRepository;
import io.upro.practice.domain.AnswerRecord;
import io.upro.practice.domain.ReviewCard;
import io.upro.practice.domain.SkillRating;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Implementations en memoire des ports, pour tester les cas d'usage sans base de donnees.
 */
public final class InMemoryStores {

    public final Cards cards = new Cards();
    public final Ratings ratings = new Ratings();
    public final Answers answers = new Answers();
    public final Quizzes quizzes = new Quizzes();
    public final Snapshots snapshots = new Snapshots();

    public static final class Cards implements ReviewCardRepository {
        private final Map<String, ReviewCard> store = new HashMap<>();

        @Override
        public Optional<ReviewCard> find(String questionId) {
            return Optional.ofNullable(store.get(questionId));
        }

        @Override
        public void save(ReviewCard card) {
            store.put(card.questionId(), card);
        }
    }

    public static final class Ratings implements SkillRatingRepository {
        private final Map<String, SkillRating> store = new HashMap<>();

        @Override
        public Optional<SkillRating> find(String moduleId) {
            return Optional.ofNullable(store.get(moduleId));
        }

        @Override
        public void save(SkillRating rating) {
            store.put(rating.moduleId(), rating);
        }
    }

    public static final class Answers implements AnswerLog {
        private final List<AnswerRecord> records = new ArrayList<>();

        @Override
        public void append(AnswerRecord record) {
            records.add(record);
        }

        @Override
        public List<AnswerRecord> answeredOn(LocalDate day) {
            return records.stream().filter(r -> r.answeredOn().equals(day)).toList();
        }

        @Override
        public Optional<Double> ratingAtEndOf(String moduleId, LocalDate day) {
            return records.stream()
                    .filter(r -> r.moduleId().equals(moduleId) && !r.answeredOn().isAfter(day))
                    .reduce((first, second) -> second)
                    .map(AnswerRecord::ratingAfter);
        }

        @Override
        public Optional<LocalDate> firstAnswerDay() {
            return records.stream().map(AnswerRecord::answeredOn).min(Comparator.naturalOrder());
        }
    }

    public static final class Quizzes implements DailyQuizRepository {
        private final Map<LocalDate, DailyQuiz> store = new HashMap<>();

        @Override
        public Optional<DailyQuiz> find(LocalDate date) {
            return Optional.ofNullable(store.get(date));
        }

        @Override
        public void save(DailyQuiz quiz) {
            store.put(quiz.date(), quiz);
        }
    }

    public static final class Snapshots implements DailySnapshotRepository {
        private final Map<String, DailySnapshot> store = new TreeMap<>();

        @Override
        public void saveAll(List<DailySnapshot> snapshots) {
            snapshots.forEach(s -> store.put(s.date() + "/" + s.moduleId(), s));
        }

        @Override
        public Optional<LocalDate> lastSnapshotDate() {
            return store.values().stream().map(DailySnapshot::date).max(Comparator.naturalOrder());
        }

        @Override
        public List<DailySnapshot> between(LocalDate from, LocalDate to) {
            return store.values().stream()
                    .filter(s -> !s.date().isBefore(from) && !s.date().isAfter(to))
                    .toList();
        }

        public List<DailySnapshot> all() {
            return List.copyOf(store.values());
        }
    }
}
