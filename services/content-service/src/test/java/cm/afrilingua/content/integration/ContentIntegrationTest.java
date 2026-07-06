package cm.afrilingua.content.integration;

import cm.afrilingua.content.dto.CreateLanguageRequest;
import cm.afrilingua.content.dto.CreateWordRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ContentIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("afrilingua_content_test")
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
    void createLanguage_shouldReturn201_withZeroTotalWords() throws Exception {
        CreateLanguageRequest request = new CreateLanguageRequest().name("Yemba").code("yem").region("Ouest");

        mockMvc.perform(post("/api/content/languages")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Yemba"))
                .andExpect(jsonPath("$.code").value("yem"))
                .andExpect(jsonPath("$.totalWords").value(0));
    }

    @Test
    void createLanguage_shouldReturn409_whenCodeAlreadyExists() throws Exception {
        CreateLanguageRequest request = new CreateLanguageRequest().name("Duala").code("dua").region("Littoral");

        mockMvc.perform(post("/api/content/languages")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        CreateLanguageRequest duplicate = new CreateLanguageRequest().name("Duala bis").code("dua").region("Littoral");

        mockMvc.perform(post("/api/content/languages")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A language already exists with code: dua"));
    }

    @Test
    void listLanguages_shouldReturn200_withCreatedLanguage() throws Exception {
        CreateLanguageRequest request = new CreateLanguageRequest().name("Bassa").code("bas").region("Centre");

        mockMvc.perform(post("/api/content/languages")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/content/languages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'bas')]").exists());
    }

    @Test
    void getLanguage_shouldReturn400_whenIdIsNotAValidUuid() throws Exception {
        mockMvc.perform(get("/api/content/languages/pas-un-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getLanguage_shouldReturn404_whenIdDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/content/languages/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createWord_shouldReturn201_withBeginnerDifficultyByDefault() throws Exception {
        CreateLanguageRequest languageRequest = new CreateLanguageRequest().name("Ewondo").code("ewo").region("Centre");

        String languageResponse = mockMvc.perform(post("/api/content/languages")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(languageRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String languageId = objectMapper.readTree(languageResponse).get("id").asText();

        CreateWordRequest wordRequest = new CreateWordRequest().word("nsem").translation("maison").grammaticalCategory("noun");

        mockMvc.perform(post("/api/content/languages/" + languageId + "/words")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(wordRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.word").value("nsem"))
                .andExpect(jsonPath("$.translation").value("maison"))
                .andExpect(jsonPath("$.difficultyLevel").value("BEGINNER"));
    }

    @Test
    void createWord_shouldReturn404_whenLanguageDoesNotExist() throws Exception {
        CreateWordRequest wordRequest = new CreateWordRequest().word("nsem").translation("maison").grammaticalCategory("noun");

        mockMvc.perform(post("/api/content/languages/00000000-0000-0000-0000-000000000000/words")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(wordRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void listWordsByLanguage_shouldReturn200_withCreatedWord() throws Exception {
        CreateLanguageRequest languageRequest = new CreateLanguageRequest().name("Fulfulde").code("ful").region("Nord");

        String languageResponse = mockMvc.perform(post("/api/content/languages")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(languageRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String languageId = objectMapper.readTree(languageResponse).get("id").asText();

        CreateWordRequest wordRequest = new CreateWordRequest().word("suudu").translation("maison").grammaticalCategory("noun");

        mockMvc.perform(post("/api/content/languages/" + languageId + "/words")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(wordRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/content/languages/" + languageId + "/words"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].word").value("suudu"));
    }
}