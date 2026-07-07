package cm.afrilingua.user.integration;

import cm.afrilingua.user.dto.CreateUserProfileRequest;
import cm.afrilingua.user.dto.RegisterDeviceRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class UserIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("afrilingua_user_test")
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

    private UUID createProfile(String displayName) throws Exception {
        UUID accountId = UUID.randomUUID();
        CreateUserProfileRequest request = new CreateUserProfileRequest()
                .accountId(accountId)
                .displayName(displayName);

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        return accountId;
    }

    @Test
    void createProfile_shouldReturn201() throws Exception {
        CreateUserProfileRequest request = new CreateUserProfileRequest()
                .accountId(UUID.randomUUID())
                .displayName("Malaika");

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.displayName").value("Malaika"));
    }

    @Test
    void createProfile_shouldReturn409_whenAccountAlreadyHasProfile() throws Exception {
        UUID accountId = UUID.randomUUID();
        CreateUserProfileRequest request = new CreateUserProfileRequest()
                .accountId(accountId)
                .displayName("Malaika");

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        CreateUserProfileRequest duplicate = new CreateUserProfileRequest()
                .accountId(accountId)
                .displayName("Malaika bis");

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict());
    }

    @Test
    void getProfile_shouldReturn404_whenAccountIdDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/users/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProfile_shouldReturn400_whenIdIsNotAValidUuid() throws Exception {
        mockMvc.perform(get("/api/users/pas-un-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatePreferences_shouldReplaceLearningLanguages_notMerge() throws Exception {
        UUID accountId = createProfile("Malaika");

        mockMvc.perform(put("/api/users/" + accountId + "/preferences")
                        .contentType("application/json")
                        .content("{\"learningLanguages\":[\"yem\"]}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/users/" + accountId + "/preferences")
                        .contentType("application/json")
                        .content("{\"learningLanguages\":[\"dua\",\"bas\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.learningLanguages", org.hamcrest.Matchers.containsInAnyOrder("dua", "bas")));

        mockMvc.perform(get("/api/users/" + accountId + "/preferences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.learningLanguages", org.hamcrest.Matchers.containsInAnyOrder("dua", "bas")));
    }

    @Test
    void registerDevice_shouldUpsert_onSameDeviceId() throws Exception {
        UUID accountId = createProfile("Malaika");

        RegisterDeviceRequest first = new RegisterDeviceRequest("phone-001", cm.afrilingua.user.dto.DeviceType.ANDROID);
        String firstResponse = mockMvc.perform(post("/api/users/" + accountId + "/devices")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String firstId = objectMapper.readTree(firstResponse).get("id").asText();

        RegisterDeviceRequest second = new RegisterDeviceRequest("phone-001", cm.afrilingua.user.dto.DeviceType.IOS);
        mockMvc.perform(post("/api/users/" + accountId + "/devices")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(firstId))
                .andExpect(jsonPath("$.deviceType").value("IOS"));

        mockMvc.perform(get("/api/users/" + accountId + "/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void logActivity_thenListActivities_shouldReturnLoggedEntry() throws Exception {
        UUID accountId = createProfile("Malaika");

        mockMvc.perform(post("/api/users/" + accountId + "/activities")
                        .contentType("application/json")
                        .content("{\"activityType\":\"LESSON_COMPLETED\",\"metadata\":\"lessonId=abc\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users/" + accountId + "/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activityType").value("LESSON_COMPLETED"))
                .andExpect(jsonPath("$[0].metadata").value("lessonId=abc"));
    }
}