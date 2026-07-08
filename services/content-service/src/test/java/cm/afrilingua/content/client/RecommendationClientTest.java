package cm.afrilingua.content.client;

import cm.afrilingua.content.entity.Word;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RecommendationClient recommendationClient;

    @Test
    void classify_shouldFallBackToBeginner_whenRestTemplateThrows() {
        when(restTemplate.postForObject(any(String.class), any(), any()))
                .thenThrow(new RestClientException("recommendation-service unreachable"));

        Word.DifficultyLevel result = recommendationClient.classify("haut", "haut");

        assertThat(result).isEqualTo(Word.DifficultyLevel.BEGINNER);
    }

    @Test
    void classify_shouldFallBackToBeginner_whenResponseIsNull() {
        when(restTemplate.postForObject(any(String.class), any(), any())).thenReturn(null);

        Word.DifficultyLevel result = recommendationClient.classify("bas", "bas");

        assertThat(result).isEqualTo(Word.DifficultyLevel.BEGINNER);
    }
}
