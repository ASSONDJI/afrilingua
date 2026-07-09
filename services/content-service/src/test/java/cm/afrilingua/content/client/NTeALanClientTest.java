package cm.afrilingua.content.client;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NTeALanClientTest {

    private String loadFixture(String filename) throws Exception {
        ClassPathResource resource = new ClassPathResource("ntealan/" + filename);
        return Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);
    }

    /**
     * Real Duala API response where translations.equivalent serializes as a
     * JSON array (article has 2 equivalents: "il", "elle"). We expect the
     * FIRST equivalent to be picked.
     */
    @Test
    void parsesEntryWhenEquivalentIsArray() throws Exception {
        String json = loadFixture("duala_array_equivalent.json");
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(json);

        NTeALanClient client = new NTeALanClient(restTemplate);
        List<NTeALanWordEntry> entries = client.fetchArticles("dl_fr_2018", 1, 100);

        assertThat(entries).hasSize(1);
        NTeALanWordEntry entry = entries.get(0);
        assertThat(entry.word()).isEqualTo("a");
        assertThat(entry.translation()).isEqualTo("il");
        assertThat(entry.grammaticalCategory()).isEqualTo("verb");
    }

    /**
     * Real Bassa API response where translations.equivalent serializes as a
     * bare object instead of a one-element array -- the XML-to-JSON
     * conversion artifact this parser is specifically defended against.
     */
    @Test
    void parsesEntryWhenEquivalentIsSingleObject() throws Exception {
        String json = loadFixture("bassa_single_equivalent.json");
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(json);

        NTeALanClient client = new NTeALanClient(restTemplate);
        List<NTeALanWordEntry> entries = client.fetchArticles("bs_fr_2019", 1, 100);

        assertThat(entries).hasSize(1);
        NTeALanWordEntry entry = entries.get(0);
        assertThat(entry.word()).isEqualTo("áá!");
        assertThat(entry.translation()).isEqualTo("ah!");
        assertThat(entry.grammaticalCategory()).isEqualTo("noun");
    }

    @Test
    void returnsEmptyListWhenPageHasNoArticles() throws Exception {
        String json = loadFixture("empty_page.json");
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(json);

        NTeALanClient client = new NTeALanClient(restTemplate);
        List<NTeALanWordEntry> entries = client.fetchArticles("dl_fr_2018", 5, 100);

        assertThat(entries).isEmpty();
    }

    /**
     * An article with no translation at all must be skipped rather than
     * producing a Word with a null/blank translation.
     */
    @Test
    void skipsEntryWithMissingTranslation() throws Exception {
        String json = loadFixture("missing_translation.json");
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(json);

        NTeALanClient client = new NTeALanClient(restTemplate);
        List<NTeALanWordEntry> entries = client.fetchArticles("dl_fr_2018", 1, 100);

        assertThat(entries).isEmpty();
    }

    @Test
    void returnsEmptyListWhenRestTemplateThrows() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("network error"));

        NTeALanClient client = new NTeALanClient(restTemplate);
        List<NTeALanWordEntry> entries = client.fetchArticles("dl_fr_2018", 1, 100);

        assertThat(entries).isEmpty();
    }

    // Static import helper (avoids importing org.mockito.ArgumentMatchers.eq
    // alongside AssertJ's own naming, keeps the matcher call readable above)
    private static <T> T eq(T value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}
