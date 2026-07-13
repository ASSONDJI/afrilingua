package cm.afrilingua.content.service;

import cm.afrilingua.content.client.RecommendationClient;
import cm.afrilingua.content.dto.CreateWordRequest;
import cm.afrilingua.content.entity.Language;
import cm.afrilingua.content.entity.Word;
import cm.afrilingua.content.exception.WordNotFoundException;
import org.springframework.web.multipart.MultipartFile;
import cm.afrilingua.content.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WordService {

    private final WordRepository wordRepository;
    private final LanguageService languageService;
    private final RecommendationClient recommendationClient;
    private final MediaStorageService mediaStorageService;

    @Transactional
    public cm.afrilingua.content.dto.Word create(UUID languageId, CreateWordRequest request) {
        Language language = languageService.getEntityById(languageId);

        Word.DifficultyLevel difficultyLevel = resolveDifficultyLevel(request);

        Word word = Word.builder()
                .language(language)
                .word(request.getWord())
                .translation(request.getTranslation())
                .grammaticalCategory(request.getGrammaticalCategory())
                .phoneticIpa(request.getPhoneticIpa())
                .audioUrl(request.getAudioUrl())
                .difficultyLevel(difficultyLevel)
                .nbSyllabes(request.getNbSyllabes())
                .tone1(request.getTone1() != null ? request.getTone1().getValue() : null)
                .tone2(request.getTone2() != null ? request.getTone2().getValue() : null)
                .build();

        wordRepository.save(word);
        return toDto(word);
    }

    /**
     * Classification priority:
     * 1. An explicit difficultyLevel in the request always wins (manual override).
     * 2. If tone1/tone2 are both provided, ask recommendation-service.
     * 3. Otherwise, default to BEGINNER — unchanged from the original behavior,
     *    for languages/words without tonal annotation (e.g. Duala, Bassa).
     */
    private Word.DifficultyLevel resolveDifficultyLevel(CreateWordRequest request) {
        if (request.getDifficultyLevel() != null) {
            return Word.DifficultyLevel.valueOf(request.getDifficultyLevel().getValue());
        }

        boolean hasToneAnnotation = request.getTone1() != null && request.getTone2() != null;

        if (hasToneAnnotation) {
            return recommendationClient.classify(
                    request.getTone1().getValue(),
                    request.getTone2().getValue()
            );
        }

        return Word.DifficultyLevel.BEGINNER;
    }

    public cm.afrilingua.content.dto.Word getById(UUID wordId) {
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new WordNotFoundException(wordId));
        return toDto(word);
    }

    @Transactional
    public cm.afrilingua.content.dto.Word uploadAudio(UUID wordId, MultipartFile file) {
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new WordNotFoundException(wordId));
        word.setAudioUrl(mediaStorageService.uploadAudio(file, wordId.toString()));
        wordRepository.save(word);
        return toDto(word);
    }

    @Transactional
    public cm.afrilingua.content.dto.Word uploadImage(UUID wordId, MultipartFile file) {
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new WordNotFoundException(wordId));
        word.setImageUrl(mediaStorageService.uploadImage(file, wordId.toString()));
        wordRepository.save(word);
        return toDto(word);
    }

    public List<cm.afrilingua.content.dto.Word> listByLanguage(UUID languageId) {
        languageService.getEntityById(languageId);

        return wordRepository.findByLanguageId(languageId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /** Used by NTeALanImportService to skip words already imported, keeping re-runs idempotent. */
    public boolean wordExists(UUID languageId, String word) {
        return wordRepository.existsByLanguageIdAndWordIgnoreCase(languageId, word);
    }

    private cm.afrilingua.content.dto.Word toDto(Word word) {
        return new cm.afrilingua.content.dto.Word()
                .id(word.getId())
                .languageId(word.getLanguage().getId())
                .word(word.getWord())
                .translation(word.getTranslation())
                .grammaticalCategory(word.getGrammaticalCategory())
                .phoneticIpa(word.getPhoneticIpa())
                .audioUrl(word.getAudioUrl())
                .difficultyLevel(cm.afrilingua.content.dto.Word.DifficultyLevelEnum.valueOf(word.getDifficultyLevel().name()))
                .nbSyllabes(word.getNbSyllabes())
                .tone1(word.getTone1() != null
                        ? cm.afrilingua.content.dto.Word.Tone1Enum.fromValue(word.getTone1())
                        : null)
                .tone2(word.getTone2() != null
                        ? cm.afrilingua.content.dto.Word.Tone2Enum.fromValue(word.getTone2())
                        : null)
                .imageUrl(word.getImageUrl());
    }
}
