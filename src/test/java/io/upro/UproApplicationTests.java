package io.upro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class UproApplicationTests {

    @Autowired
    MockMvcTester mvc;

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
                .content("{\"questionId\":\"java-records-01\",\"choice\":1}"))
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
}
