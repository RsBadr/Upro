package io.upro.practice.infrastructure.web;

import io.upro.catalog.domain.Question;
import io.upro.practice.application.AnswerResult;
import io.upro.practice.application.ModuleProgress;
import io.upro.practice.application.PracticeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/practice")
@Validated
class PracticeController {

    private final PracticeService practice;

    PracticeController(PracticeService practice) {
        this.practice = practice;
    }

    @GetMapping("/{moduleId}/next")
    List<QuestionView> next(@PathVariable String moduleId,
                            @RequestParam(defaultValue = "5") @Min(1) @Max(50) int count) {
        return practice.nextQuestions(moduleId, count).stream().map(QuestionView::of).toList();
    }

    @PostMapping("/answers")
    AnswerResult answer(@Valid @RequestBody AnswerRequest request) {
        return practice.answer(request.questionId(), request.choice());
    }

    @GetMapping("/progress")
    List<ModuleProgress> progress() {
        return practice.progress();
    }

    /** Vue exposee au client : surtout pas la bonne reponse. */
    record QuestionView(String id, String topic, String level, String prompt, List<String> choices) {
        static QuestionView of(Question q) {
            return new QuestionView(q.id(), q.topic(), q.level().name(), q.prompt(), q.choices());
        }
    }

    record AnswerRequest(@NotBlank String questionId, @NotNull @Min(0) Integer choice) {
    }
}
