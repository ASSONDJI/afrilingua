package cm.afrilingua.content.service;

import cm.afrilingua.content.client.NTeALanClient;
import cm.afrilingua.content.client.NTeALanWordEntry;
import cm.afrilingua.content.dto.CreateWordRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NTeALanImportService {

    private static final int PAGE_SIZE = 100;
    // Safety net: stops the loop even if the API misbehaves and never
    // returns an empty page (e.g. due to an unexpected upstream bug).
    private static final int MAX_PAGES = 50;

    private final NTeALanClient nteALanClient;
    private final WordService wordService;
    private final LanguageService languageService;

    /**
     * Imports every article from a NTeALan dictionary into the given
     * language, paginating through the full dictionary. Idempotent: words
     * already present for this language (case-insensitive) are skipped
     * rather than duplicated, so this method is safe to re-run.
     */
    public ImportResult importDictionary(UUID languageId, String dictionaryId) {
        // Fail fast with a 404 before starting the batch, rather than
        // discovering a bad languageId only after the first word attempt.
        languageService.getEntityById(languageId);

        int imported = 0;
        int skipped = 0;
        int failed = 0;

        for (int page = 1; page <= MAX_PAGES; page++) {
            List<NTeALanWordEntry> entries = nteALanClient.fetchArticles(dictionaryId, page, PAGE_SIZE);
            if (entries.isEmpty()) {
                break;
            }

            for (NTeALanWordEntry entry : entries) {
                try {
                    if (wordService.wordExists(languageId, entry.word())) {
                        skipped++;
                        continue;
                    }

                    CreateWordRequest request = new CreateWordRequest()
                            .word(entry.word())
                            .translation(entry.translation())
                            .grammaticalCategory(entry.grammaticalCategory());

                    wordService.create(languageId, request);
                    imported++;
                } catch (Exception ex) {
                    log.warn("Failed to import NTeALan word '{}' for dictionary {}", entry.word(), dictionaryId, ex);
                    failed++;
                }
            }
        }

        log.info("NTeALan import complete for dictionary {}: {} imported, {} skipped, {} failed",
                dictionaryId, imported, skipped, failed);

        return new ImportResult(imported, skipped, failed);
    }

    public record ImportResult(int imported, int skipped, int failed) {
    }
}
