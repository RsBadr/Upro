package io.upro.daily.infrastructure.web;

import io.upro.daily.application.DailyQuizView;
import io.upro.daily.application.DailyReport;
import io.upro.daily.application.DailyService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/daily")
@Validated
class DailyController {

    private final DailyService daily;

    DailyController(DailyService daily) {
        this.daily = daily;
    }

    @GetMapping
    DailyQuizView today() {
        return daily.todayQuiz();
    }

    @GetMapping("/history")
    List<DailyReport> history(@RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {
        return daily.history(days);
    }
}
