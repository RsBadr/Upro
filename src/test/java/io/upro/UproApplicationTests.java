package io.upro;

import io.upro.catalog.application.CatalogService;
import io.upro.daily.application.DailyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class UproApplicationTests {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    DailyService daily;

    @Autowired
    CatalogService catalog;

    @Test
    void listsModules() {
        assertThat(mvc.get().uri("/api/modules"))
                .hasStatusOk()
                .bodyJson().extractingPath("$[0].id").isEqualTo("java");
    }

    @Test
    void nextQuestionsDoNotLeakTheAnswer() {
        assertThat(mvc.get().uri("/api/practice/java/next?count=1"))
                .hasStatusOk()
                .bodyJson().doesNotHavePath("$[0].answerIndex");
    }

    @Test
    void answersAQuestion() {
        assertThat(mvc.post().uri("/api/practice/answers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"questionId\":\"java-records-01\",\"choice\":" + correctChoiceOf("java-records-01") + "}"))
                .hasStatusOk()
                .bodyJson().extractingPath("$.correct").isEqualTo(true);
    }

    @Test
    void unknownModuleIsA404ProblemDetail() {
        assertThat(mvc.get().uri("/api/modules/cobol"))
                .hasStatus(404)
                .bodyJson().extractingPath("$.detail").isEqualTo("Unknown module: cobol");
    }

    @Test
    void invalidAnswerIsA400() {
        assertThat(mvc.post().uri("/api/practice/answers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"questionId\":\"java-records-01\",\"choice\":9}"))
                .hasStatus(400);
    }

    @Test
    void exposesHealthEndpointForRailway() {
        assertThat(mvc.get().uri("/actuator/health"))
                .hasStatusOk()
                .bodyJson().extractingPath("$.status").isEqualTo("UP");
    }

    @Test
    void servesTheDailyQuizWithoutAnswers() {
        assertThat(mvc.get().uri("/api/daily"))
                .hasStatusOk()
                .bodyJson()
                .satisfies(json -> {
                    json.assertThat().extractingPath("$.total").isEqualTo(10);
                    json.assertThat().doesNotHavePath("$.items[0].answerIndex");
                });
    }

    @Test
    void exposesLevelsPerModule() {
        assertThat(mvc.get().uri("/api/practice/levels"))
                .hasStatusOk()
                .bodyJson().extractingPath("$[0].level").isNotNull();
    }

    @Test
    void dailyRoutineRunsAgainstTheRealDatabase() {
        daily.runDailyRoutine();
        daily.runDailyRoutine();

        assertThat(daily.todayQuiz().total()).isEqualTo(10);
        assertThat(daily.history(30)).isEmpty();
    }

    private int correctChoiceOf(String questionId) {
        return catalog.getQuestion(questionId).answerIndex();
    }
}
