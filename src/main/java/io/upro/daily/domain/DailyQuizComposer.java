package io.upro.daily.domain;

import io.upro.catalog.domain.Level;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Compose le quiz du jour, adapte au niveau estime sur chaque module :
 * <ol>
 *   <li>une part de revisions (questions deja vues et dues, les plus fragiles d'abord) ;</li>
 *   <li>des questions nouvelles, choisies au plus pres du niveau estime + une marche
 *       ({@link #STRETCH}), reparties entre les modules ;</li>
 *   <li>si la banque est epuisee, complement avec les questions les moins bien reussies.</li>
 * </ol>
 * Deterministe pour une date donnee : le meme jour produit toujours le meme quiz.
 */
public final class DailyQuizComposer {

    /** On vise un peu au-dessus du niveau estime pour faire progresser. */
    static final double STRETCH = 75;
    static final double REVIEW_SHARE = 0.4;

    public DailyQuiz compose(LocalDate date, int size, List<ModuleState> modules) {
        if (size < 1) {
            throw new IllegalArgumentException("size must be positive");
        }
        Random random = new Random(date.toEpochDay());
        Map<String, Double> tieBreak = new HashMap<>();
        modules.forEach(m -> m.questions().forEach(q -> tieBreak.put(q.question().id(), random.nextDouble())));

        Set<String> picked = new LinkedHashSet<>();

        List<QuestionState> due = modules.stream()
                .flatMap(m -> m.questions().stream())
                .filter(q -> !q.isNew() && q.card().isDue(date))
                .sorted(Comparator.comparingInt((QuestionState q) -> q.card().box())
                        .thenComparing(q -> tieBreak.get(q.question().id())))
                .toList();
        int reviewQuota = (int) Math.round(size * REVIEW_SHARE);
        due.stream().limit(reviewQuota).forEach(q -> picked.add(q.question().id()));

        pickNewQuestions(modules, size, picked, random, tieBreak);

        due.forEach(q -> addIfRoom(picked, q, size));
        modules.stream()
                .flatMap(m -> m.questions().stream())
                .filter(q -> !q.isNew())
                .sorted(Comparator.comparingDouble(QuestionState::successRatio)
                        .thenComparing(q -> tieBreak.get(q.question().id())))
                .forEach(q -> addIfRoom(picked, q, size));

        List<String> ordered = new ArrayList<>(picked);
        Collections.shuffle(ordered, random);
        return new DailyQuiz(date, ordered);
    }

    private void pickNewQuestions(List<ModuleState> modules, int size, Set<String> picked,
                                  Random random, Map<String, Double> tieBreak) {
        List<Deque<QuestionState>> queues = new ArrayList<>();
        for (ModuleState module : modules) {
            double target = module.rating().rating() + STRETCH;
            List<QuestionState> candidates = module.questions().stream()
                    .filter(QuestionState::isNew)
                    .sorted(Comparator.comparingDouble((QuestionState q) -> distance(q.question().level(), target))
                            .thenComparing(q -> tieBreak.get(q.question().id())))
                    .toList();
            if (!candidates.isEmpty()) {
                queues.add(new ArrayDeque<>(candidates));
            }
        }
        Collections.shuffle(queues, random);
        while (picked.size() < size && !queues.isEmpty()) {
            for (var it = queues.iterator(); it.hasNext() && picked.size() < size; ) {
                Deque<QuestionState> queue = it.next();
                picked.add(queue.removeFirst().question().id());
                if (queue.isEmpty()) {
                    it.remove();
                }
            }
        }
    }

    private static double distance(Level level, double target) {
        return Math.abs(level.difficulty() - target);
    }

    private static void addIfRoom(Set<String> picked, QuestionState q, int size) {
        if (picked.size() < size) {
            picked.add(q.question().id());
        }
    }
}
