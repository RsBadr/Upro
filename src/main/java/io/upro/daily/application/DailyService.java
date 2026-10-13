package io.upro.daily.application;

import io.upro.catalog.application.CatalogService;
import io.upro.catalog.domain.LearningModule;
import io.upro.catalog.domain.Level;
import io.upro.daily.application.port.DailyQuizRepository;
import io.upro.daily.application.port.DailySnapshotRepository;
import io.upro.daily.domain.DailyQuiz;
import io.upro.daily.domain.DailyQuizComposer;
import io.upro.daily.domain.DailySnapshot;
import io.upro.daily.domain.ModuleState;
import io.upro.daily.domain.QuestionState;
import io.upro.practice.application.PracticeService;
import io.upro.practice.application.port.AnswerLog;
import io.upro.practice.domain.AnswerRecord;
import io.upro.practice.domain.SkillRating;
import io.upro.shared.application.Transactions;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Quiz du jour et traitement quotidien :
 * cloture des journees passees (bilan + niveau) puis preparation du quiz du jour.
 */
public class DailyService {

    /** On ne rattrape pas plus loin que ca si l'application est restee eteinte. */
    static final int MAX_CATCH_UP_DAYS = 60;

    private final CatalogService catalog;
    private final PracticeService practice;
    private final AnswerLog answerLog;
    private final DailyQuizRepository quizzes;
    private final DailySnapshotRepository snapshots;
    private final DailyQuizComposer composer;
    private final Transactions transactions;
    private final Clock clock;
    private final int quizSize;

    public DailyService(CatalogService catalog, PracticeService practice, AnswerLog answerLog,
                        DailyQuizRepository quizzes, DailySnapshotRepository snapshots,
                        Transactions transactions, Clock clock, int quizSize) {
        this.catalog = catalog;
        this.practice = practice;
        this.answerLog = answerLog;
        this.quizzes = quizzes;
        this.snapshots = snapshots;
        this.composer = new DailyQuizComposer();
        this.transactions = transactions;
        this.clock = clock;
        this.quizSize = quizSize;
    }

    /**
     * Traitement quotidien, idempotent : peut etre relance sans effet de bord.
     */
    public void runDailyRoutine() {
        LocalDate today = today();
        transactions.inTransaction(() -> {
            closePastDays(today);
            quizOf(today);
            return null;
        });
    }

    public DailyQuizView todayQuiz() {
        LocalDate today = today();
        DailyQuiz quiz = transactions.inTransaction(() -> quizOf(today));
        Map<String, Boolean> lastResult = new HashMap<>();
        for (AnswerRecord record : answerLog.answeredOn(today)) {
            lastResult.put(record.questionId(), record.correct());
        }
        List<DailyQuizView.Item> items = quiz.questionIds().stream()
                .map(catalog::findQuestion)
                .flatMap(java.util.Optional::stream)
                .map(q -> new DailyQuizView.Item(q.id(), q.moduleId(), q.topic(), q.level().name(), q.prompt(),
                        q.choices(), lastResult.containsKey(q.id()), lastResult.get(q.id())))
                .toList();
        int answered = (int) items.stream().filter(DailyQuizView.Item::answered).count();
        int correct = (int) items.stream().filter(i -> Boolean.TRUE.equals(i.correct())).count();
        return new DailyQuizView(today, items.size(), answered, correct, items);
    }

    public List<DailyReport> history(int days) {
        LocalDate today = today();
        List<DailySnapshot> rows = snapshots.between(today.minusDays(days), today.minusDays(1));
        Map<LocalDate, List<DailySnapshot>> byDate = rows.stream()
                .collect(Collectors.groupingBy(DailySnapshot::date, LinkedHashMap::new, Collectors.toList()));
        return byDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new DailyReport(e.getKey(),
                        e.getValue().stream().mapToInt(DailySnapshot::answers).sum(),
                        e.getValue().stream().mapToInt(DailySnapshot::correct).sum(),
                        e.getValue().stream()
                                .map(s -> new DailyReport.ModuleDay(s.moduleId(), (int) Math.round(s.rating()),
                                        s.level().name(), s.answers(), s.correct()))
                                .toList()))
                .toList();
    }

    private DailyQuiz quizOf(LocalDate date) {
        return quizzes.find(date).orElseGet(() -> {
            DailyQuiz quiz = composer.compose(date, quizSize, moduleStates(date));
            quizzes.save(quiz);
            return quiz;
        });
    }

    private List<ModuleState> moduleStates(LocalDate date) {
        List<ModuleState> states = new ArrayList<>();
        for (LearningModule module : catalog.listModules()) {
            List<QuestionState> questions = catalog.questionsOf(module.id()).stream()
                    .map(q -> new QuestionState(q, practice.cardOf(q.id(), date)))
                    .toList();
            states.add(new ModuleState(module.id(), practice.ratingOf(module.id()), questions));
        }
        return states;
    }

    private void closePastDays(LocalDate today) {
        LocalDate start = snapshots.lastSnapshotDate()
                .map(d -> d.plusDays(1))
                .or(answerLog::firstAnswerDay)
                .orElse(today);
        LocalDate earliest = today.minusDays(MAX_CATCH_UP_DAYS);
        if (start.isBefore(earliest)) {
            start = earliest;
        }
        for (LocalDate day = start; day.isBefore(today); day = day.plusDays(1)) {
            snapshots.saveAll(closeDay(day));
        }
    }

    private List<DailySnapshot> closeDay(LocalDate day) {
        Map<String, List<AnswerRecord>> answersByModule = answerLog.answeredOn(day).stream()
                .collect(Collectors.groupingBy(AnswerRecord::moduleId));
        return catalog.listModules().stream()
                .map(LearningModule::id)
                .map(moduleId -> {
                    List<AnswerRecord> answers = answersByModule.getOrDefault(moduleId, List.of());
                    double rating = answerLog.ratingAtEndOf(moduleId, day).orElse(SkillRating.INITIAL_RATING);
                    int correct = (int) answers.stream().filter(AnswerRecord::correct).count();
                    return new DailySnapshot(day, moduleId, rating, Level.fromRating(rating), answers.size(), correct);
                })
                .toList();
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
