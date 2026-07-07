package cm.afrilingua.quiz.integration;

import cm.afrilingua.quiz.dto.CreateQuestionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class QuizIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("afrilingua_quiz_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("eureka.client.enabled", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String createQuestion(UUID lessonId, String text, List<String> options, String correctAnswer) throws Exception {
        CreateQuestionRequest request = new CreateQuestionRequest()
                .lessonId(lessonId)
                .type(CreateQuestionRequest.TypeEnum.MULTIPLE_CHOICE)
                .questionText(text)
                .options(options)
                .correctAnswer(correctAnswer);

        return mockMvc.perform(post("/api/quizzes")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void createQuestion_shouldReturn201_withCorrectAnswerIncluded() throws Exception {
        UUID lessonId = UUID.randomUUID();

        String response = createQuestion(lessonId, "Comment dit-on maison en yemba ?",
                List.of("nsem", "tsa", "ndap"), "nsem");

        org.assertj.core.api.Assertions.assertThat(
                objectMapper.readTree(response).get("correctAnswer").asText()
        ).isEqualTo("nsem");
    }

    @Test
    void getQuestion_shouldReturn200_withoutCorrectAnswerField() throws Exception {
        UUID lessonId = UUID.randomUUID();
        String createResponse = createQuestion(lessonId, "Comment dit-on maison en yemba ?",
                List.of("nsem", "tsa", "ndap"), "nsem");
        String questionId = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(get("/api/quizzes/" + questionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.options[0]").value("nsem"))
                .andExpect(jsonPath("$.correctAnswer").doesNotExist());
    }

    @Test
    void getQuestion_shouldReturn400_whenIdIsNotAValidUuid() throws Exception {
        mockMvc.perform(get("/api/quizzes/pas-un-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getQuestion_shouldReturn404_whenIdDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/quizzes/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listQuizzesByLesson_shouldReturn200_withoutCorrectAnswerField() throws Exception {
        UUID lessonId = UUID.randomUUID();
        createQuestion(lessonId, "Comment dit-on maison en yemba ?",
                List.of("nsem", "tsa", "ndap"), "nsem");

        mockMvc.perform(get("/api/quizzes").param("lessonId", lessonId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].questionText").value("Comment dit-on maison en yemba ?"))
                .andExpect(jsonPath("$[0].correctAnswer").doesNotExist());
    }

    @Test
    void submitAnswer_shouldReturnCorrectTrue_whenAnswerMatches() throws Exception {
        UUID lessonId = UUID.randomUUID();
        String createResponse = createQuestion(lessonId, "Comment dit-on maison en yemba ?",
                List.of("nsem", "tsa", "ndap"), "nsem");
        String questionId = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(post("/api/quizzes/" + questionId + "/submit")
                        .contentType("application/json")
                        .content("{\"submittedAnswer\":\"  NSEM  \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.correctAnswer").value("nsem"));
    }

    @Test
    void submitAnswer_shouldReturnCorrectFalse_whenAnswerIsWrong() throws Exception {
        UUID lessonId = UUID.randomUUID();
        String createResponse = createQuestion(lessonId, "Comment dit-on maison en yemba ?",
                List.of("nsem", "tsa", "ndap"), "nsem");
        String questionId = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(post("/api/quizzes/" + questionId + "/submit")
                        .contentType("application/json")
                        .content("{\"submittedAnswer\":\"tsa\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(false));
    }

    @Test
    void submitAnswer_shouldReturn404_whenQuestionDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/quizzes/00000000-0000-0000-0000-000000000000/submit")
                        .contentType("application/json")
                        .content("{\"submittedAnswer\":\"nsem\"}"))
                .andExpect(status().isNotFound());
    }
}