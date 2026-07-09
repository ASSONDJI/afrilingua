package cm.afrilingua.content.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Client for the NTeALan collaborative dictionary API
 * (https://apis.ntealan.net), used to import Duala and Bassa vocabulary
 * per cahier des charges §5.1.
 *
 * Uses manual JsonNode traversal rather than strict POJO/Jackson binding
 * because the API's repeatable fields (translations.equivalent,
 * examples.example) inconsistently serialize as either a JSON array or a
 * bare object depending on cardinality -- an XML-to-JSON conversion
 * artifact confirmed by inspecting real API responses. A strict
 * List<Equivalent> binding would throw a JSON mapping exception on every
 * entry that happens to have exactly one translation.
 */
@Slf4j
@Component
public class NTeALanClient {

    private static final String BASE_URL = "https://apis.ntealan.net/ntealan/dictionaries/articles";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NTeALanClient(@Qualifier("externalRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<NTeALanWordEntry> fetchArticles(String dictionaryId, int page, int limit) {
        String url = String.format("%s/%s?limit=%d&page=%d&sort=ASC", BASE_URL, dictionaryId, limit, page);

        String rawResponse;
        try {
            rawResponse = restTemplate.getForObject(url, String.class);
        } catch (Exception ex) {
            log.error("Failed to fetch NTeALan dictionary {} page {}", dictionaryId, page, ex);
            return List.of();
        }

        if (rawResponse == null || rawResponse.isBlank()) {
            return List.of();
        }

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode articles = root.path("articles");

            List<NTeALanWordEntry> entries = new ArrayList<>();
            for (JsonNode articleWrapper : articles) {
                NTeALanWordEntry entry = parseEntry(articleWrapper);
                if (entry != null) {
                    entries.add(entry);
                }
            }
            return entries;
        } catch (Exception ex) {
            log.error("Failed to parse NTeALan response for dictionary {} page {}", dictionaryId, page, ex);
            return List.of();
        }
    }

    private NTeALanWordEntry parseEntry(JsonNode articleWrapper) {
        String radical = articleWrapper.path("radical").asText(null);
        if (radical == null || radical.isBlank()) {
            return null;
        }

        JsonNode articleBody = articleWrapper.path("article").path("article");
        String grammaticalCategory = articleBody.path("type").asText(null);

        String translation = extractFirstEquivalent(articleBody.path("translations").path("equivalent"));
        if (translation == null || translation.isBlank()) {
            return null;
        }

        return new NTeALanWordEntry(radical.trim(), translation.trim(), grammaticalCategory);
    }

    /**
     * Handles the array-or-bare-object inconsistency: a single translation
     * serializes as a bare object instead of a one-element array.
     */
    private String extractFirstEquivalent(JsonNode equivalentNode) {
        if (equivalentNode.isArray() && !equivalentNode.isEmpty()) {
            return equivalentNode.get(0).path("content").asText(null);
        }
        if (equivalentNode.isObject()) {
            return equivalentNode.path("content").asText(null);
        }
        return null;
    }
}
