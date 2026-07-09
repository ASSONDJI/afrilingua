package cm.afrilingua.content.service;

import cm.afrilingua.content.client.NTeALanClient;
import cm.afrilingua.content.client.NTeALanWordEntry;
import cm.afrilingua.content.dto.CreateWordRequest;
import cm.afrilingua.content.dto.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NTeALanImportServiceTest {

    private NTeALanClient nteALanClient;
    private WordService wordService;
    private LanguageService languageService;
    private NTeALanImportService importService;
    private UUID languageId;

    @BeforeEach
    void setUp() {
        nteALanClient = mock(NTeALanClient.class);
        wordService = mock(WordService.class);
        languageService = mock(LanguageService.class);
        importService = new NTeALanImportService(nteALanClient, wordService, languageService);
        languageId = UUID.randomUUID();

        // getEntityById is only used here as a fail-fast existence check;
        // a non-null return is enough for the import to proceed.
        when(languageService.getEntityById(languageId)).thenReturn(mock(cm.afrilingua.content.entity.Language.class));
    }

    @Test
    void importsAllNewWordsOnFirstPage() {
        NTeALanWordEntry entry1 = new NTeALanWordEntry("aba", "partager", "verb");
        NTeALanWordEntry entry2 = new NTeALanWordEntry("a", "il", "pron");

        when(nteALanClient.fetchArticles(eq("dl_fr_2018"), eq(1), anyInt()))
                .thenReturn(List.of(entry1, entry2));
        when(nteALanClient.fetchArticles(eq("dl_fr_2018"), eq(2), anyInt()))
                .thenReturn(List.of());

        when(wordService.wordExists(eq(languageId), anyString())).thenReturn(false);

        NTeALanImportService.ImportResult result = importService.importDictionary(languageId, "dl_fr_2018");

        assertThat(result.imported()).isEqualTo(2);
        assertThat(result.skipped()).isEqualTo(0);
        assertThat(result.failed()).isEqualTo(0);
        verify(wordService, times(2)).create(eq(languageId), any(CreateWordRequest.class));
    }

    /**
     * Re-running the import on a dictionary already imported must skip every
     * word already present rather than duplicating it -- this is the
     * idempotence guarantee the service is designed around.
     */
    @Test
    void skipsWordsThatAlreadyExist() {
        NTeALanWordEntry entry = new NTeALanWordEntry("aba", "partager", "verb");

        when(nteALanClient.fetchArticles(eq("dl_fr_2018"), eq(1), anyInt()))
                .thenReturn(List.of(entry));
        when(nteALanClient.fetchArticles(eq("dl_fr_2018"), eq(2), anyInt()))
                .thenReturn(List.of());

        when(wordService.wordExists(languageId, "aba")).thenReturn(true);

        NTeALanImportService.ImportResult result = importService.importDictionary(languageId, "dl_fr_2018");

        assertThat(result.imported()).isEqualTo(0);
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.failed()).isEqualTo(0);
        verify(wordService, never()).create(any(), any());
    }

    /**
     * A single word failing to persist (e.g. a transient DB error) must not
     * abort the whole batch -- the loop continues and the failure is only
     * reflected in the counter.
     */
    @Test
    void continuesImportWhenOneWordFailsToSave() {
        NTeALanWordEntry entry1 = new NTeALanWordEntry("aba", "partager", "verb");
        NTeALanWordEntry entry2 = new NTeALanWordEntry("a", "il", "pron");

        when(nteALanClient.fetchArticles(eq("dl_fr_2018"), eq(1), anyInt()))
                .thenReturn(List.of(entry1, entry2));
        when(nteALanClient.fetchArticles(eq("dl_fr_2018"), eq(2), anyInt()))
                .thenReturn(List.of());

        when(wordService.wordExists(eq(languageId), anyString())).thenReturn(false);
        when(wordService.create(eq(languageId), any(CreateWordRequest.class)))
                .thenThrow(new RuntimeException("db error"))
                .thenReturn(mock(cm.afrilingua.content.dto.Word.class));

        NTeALanImportService.ImportResult result = importService.importDictionary(languageId, "dl_fr_2018");

        assertThat(result.imported()).isEqualTo(1);
        assertThat(result.skipped()).isEqualTo(0);
        assertThat(result.failed()).isEqualTo(1);
    }

    @Test
    void stopsPaginationOnFirstEmptyPage() {
        when(nteALanClient.fetchArticles(eq("dl_fr_2018"), eq(1), anyInt()))
                .thenReturn(List.of());

        NTeALanImportService.ImportResult result = importService.importDictionary(languageId, "dl_fr_2018");

        assertThat(result.imported()).isEqualTo(0);
        verify(nteALanClient, times(1)).fetchArticles(eq("dl_fr_2018"), anyInt(), anyInt());
    }

    @Test
    void throwsWhenLanguageDoesNotExist() {
        UUID unknownId = UUID.randomUUID();
        when(languageService.getEntityById(unknownId))
                .thenThrow(new RuntimeException("language not found"));

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> importService.importDictionary(unknownId, "dl_fr_2018"));

        verify(nteALanClient, never()).fetchArticles(anyString(), anyInt(), anyInt());
    }
}
