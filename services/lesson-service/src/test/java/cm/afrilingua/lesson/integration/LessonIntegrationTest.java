package cm.afrilingua.lesson.integration;

import cm.afrilingua.lesson.dto.AddWordToLessonRequest;
import cm.afrilingua.lesson.dto.CreateLessonRequest;
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

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class LessonIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("afrilingua_lesson_test")
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

    @Test
    void createLesson_shouldReturn201_withEmptyWordIds() throws Exception {
        CreateLessonRequest request = new CreateLessonRequest()
                .languageId(UUID.randomUUID()).title("Salutations").order(1).level(1);

        mockMvc.perform(post("/api/lessons")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Salutations"))
                .andExpect(jsonPath("$.wordIds").isEmpty());
    }

    @Test
    void getLesson_shouldReturn400_whenIdIsNotAValidUuid() throws Exception {
        mockMvc.perform(get("/api/lessons/pas-un-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getLesson_shouldReturn404_whenIdDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/lessons/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    // This is the exact scenario that surfaced the LazyInitializationException during manual testing:
    // create a lesson, attach a word, then fetch it back via GET (outside the write transaction).
    @Test
    void addWordThenGetLesson_shouldReturn200_withWordIdPresent() throws Exception {
        CreateLessonRequest lessonRequest = new CreateLessonRequest()
                .languageId(UUID.randomUUID()).title("Nombres").order(1).level(1);

        String lessonResponse = mockMvc.perform(post("/api/lessons")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(lessonRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String lessonId = objectMapper.readTree(lessonResponse).get("id").asText();
        UUID wordId = UUID.randomUUID();

        AddWordToLessonRequest addWordRequest = new AddWordToLessonRequest().wordId(wordId);

        mockMvc.perform(post("/api/lessons/" + lessonId + "/words")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(addWordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wordIds[0]").value(wordId.toString()));

        // The GET below is the call that would fail with LazyInitializationException
        // if @Transactional(readOnly = true) were missing from LessonService.getById.
        mockMvc.perform(get("/api/lessons/" + lessonId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wordIds[0]").value(wordId.toString()));
    }

    @Test
    void listLessonsByLanguage_shouldReturnLessons_forThatLanguageOnly() throws Exception {
        UUID languageId = UUID.randomUUID();
        CreateLessonRequest request = new CreateLessonRequest()
                .languageId(languageId).title("Couleurs").order(1).level(1);

        mockMvc.perform(post("/api/lessons")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // This is also the call that would fail with LazyInitializationException
        // if @Transactional(readOnly = true) were missing from LessonService.listByLanguage.
        mockMvc.perform(get("/api/lessons").param("languageId", languageId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Couleurs"));
    }

    @Test
    void addWordToLesson_shouldReturn404_whenLessonDoesNotExist() throws Exception {
        AddWordToLessonRequest request = new AddWordToLessonRequest().wordId(UUID.randomUUID());

        mockMvc.perform(post("/api/lessons/00000000-0000-0000-0000-000000000000/words")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
