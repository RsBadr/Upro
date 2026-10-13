package io.upro.practice.application;

import io.upro.catalog.application.CatalogService;
import io.upro.catalog.domain.Question;
import io.upro.practice.application.port.AnswerLog;
import io.upro.practice.application.port.ReviewCardRepository;
import io.upro.practice.application.port.SkillRatingRepository;
import io.upro.practice.domain.AnswerRecord;
import io.upro.practice.domain.ReviewCard;
import io.upro.practice.domain.SkillRating;
import io.upro.shared.application.Transactions;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Cas d'usage d'entrainement : tirer les questions a reviser, corriger une reponse
 * (repetition espacee + niveau Elo + journal), calculer la progression.
 */
public class PracticeService {

    private final CatalogService catalog;
    private final ReviewCardRepository cards;
    private final SkillRatingRepository ratings;
    private final AnswerLog answerLog;
    private final Transactions transactions;
    private final Clock clock;

    public PracticeService(CatalogService catalog, ReviewCardRepository cards, SkillRatingRepository ratings,
                           AnswerLog answerLog, Transactions transactions, Clock clock) {
        this.catalog = catalog;
        this.cards = cards;
        this.ratings = ratings;
        this.answerLog = answerLog;
        this.transactions = transactions;
        this.clock = clock;
    }

    /**
     * Questions dues aujourd'hui, les plus fragiles (petite boite) en premier.
     */
    public List<Question> nextQuestions(String moduleId, int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be positive");
        }
        LocalDate today = today();
        return catalog.questionsOf(moduleId).stream()
                .map(q -> new Candidate(q, cardOf(q.id(), today)))
                .filter(c -> c.card().isDue(today))
                .sorted(Comparator.comparingInt((Candidate c) -> c.card().box())
                        .thenComparing(c -> c.card().nextReviewOn()))
                .limit(limit)
                .map(Candidate::question)
                .toList();
    }

    public AnswerResult answer(String questionId, int choiceIndex) {
        Question question = catalog.getQuestion(questionId);
        if (choiceIndex < 0 || choiceIndex >= question.choices().size()) {
            throw new IllegalArgumentException("Invalid choice " + choiceIndex + " for question " + questionId);
        }
        return transactions.inTransaction(() -> {
            LocalDate today = today();
            boolean correct = question.isCorrect(choiceIndex);

            ReviewCard card = cardOf(questionId, today).answer(correct, today);
            cards.save(card);

            SkillRating before = ratingOf(question.moduleId());
            SkillRating after = before.record(question.level(), correct);
            ratings.save(after);

            answerLog.append(new AnswerRecord(questionId, question.moduleId(), question.level(), correct,
                    clock.instant(), today, before.rating(), after.rating()));

            return new AnswerResult(correct, question.answerIndex(), question.explanation(),
                    card.box(), card.nextReviewOn(), after.level().name(), round(after.rating()),
                    round(after.rating() - before.rating()));
        });
    }

    public List<ModuleProgress> progress() {
        LocalDate today = today();
        return catalog.listModules().stream().map(module -> {
            List<Question> questions = catalog.questionsOf(module.id());
            int seen = 0;
            int mastered = 0;
            int due = 0;
            for (Question q : questions) {
                ReviewCard card = cardOf(q.id(), today);
                if (card.attempts() > 0) {
                    seen++;
                }
                if (card.isMastered()) {
                    mastered++;
                }
                if (card.isDue(today)) {
                    due++;
                }
            }
            return new ModuleProgress(module.id(), questions.size(), seen, mastered, due);
        }).toList();
    }

    public List<ModuleLevel> levels() {
        return catalog.listModules().stream()
                .map(module -> {
                    SkillRating r = ratingOf(module.id());
                    return new ModuleLevel(module.id(), r.level().name(), r.level().next().name(),
                            (int) Math.round(r.rating()), r.answers(), r.isCalibrating(), r.progressToNextLevel());
                })
                .toList();
    }

    public SkillRating ratingOf(String moduleId) {
        return ratings.find(moduleId).orElseGet(() -> SkillRating.initial(moduleId));
    }

    public ReviewCard cardOf(String questionId, LocalDate today) {
        return cards.find(questionId).orElseGet(() -> ReviewCard.fresh(questionId, today));
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }

    private static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private record Candidate(Question question, ReviewCard card) {
    }
}
