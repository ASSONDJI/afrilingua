package cm.afrilingua.content.client;

import cm.afrilingua.content.entity.Word;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class RecommendationClient {

    private static final String APPLY_RULE_URL =
            "http://RECOMMENDATION-SERVICE/api/recommendations/difficulty/apply-rule";

    private final RestTemplate restTemplate;

    public RecommendationClient(@Qualifier("loadBalancedRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Calls recommendation-service to apply the deterministic tone-based
     * difficulty rule. Best-effort: any failure (service down, timeout,
     * unexpected response) falls back to BEGINNER rather than blocking word
     * creation — the same functional-fallback principle used throughout this
     * project (see cahier des charges §9.2) for non-critical inter-service calls.
     */
    public Word.DifficultyLevel classify(String tone1, String tone2) {
        try {
            ApplyRuleRequest request = new ApplyRuleRequest(tone1, tone2);
            ApplyRuleResponse response = restTemplate.postForObject(APPLY_RULE_URL, request, ApplyRuleResponse.class);

            if (response == null || response.niveau() == null) {
                log.warn("recommendation-service returned an empty response, falling back to BEGINNER");
                return Word.DifficultyLevel.BEGINNER;
            }

            return mapToDifficultyLevel(response.niveau());
        } catch (RestClientException ex) {
            log.warn("recommendation-service classification failed, falling back to BEGINNER", ex);
            return Word.DifficultyLevel.BEGINNER;
        }
    }

    private Word.DifficultyLevel mapToDifficultyLevel(String niveau) {
        return switch (niveau) {
            case "DEBUTANT" -> Word.DifficultyLevel.BEGINNER;
            case "MOYEN" -> Word.DifficultyLevel.INTERMEDIATE;
            case "AVANCE" -> Word.DifficultyLevel.ADVANCED;
            default -> Word.DifficultyLevel.BEGINNER;
        };
    }

    private record ApplyRuleRequest(String ton1, String ton2) {
    }

    private record ApplyRuleResponse(String niveau) {
    }
}
