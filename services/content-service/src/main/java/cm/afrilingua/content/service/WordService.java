package cm.afrilingua.content.service;

import cm.afrilingua.content.dto.CreateWordRequest;
import cm.afrilingua.content.entity.Language;
import cm.afrilingua.content.entity.Word;
import cm.afrilingua.content.exception.WordNotFoundException;
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

    @Transactional
    public cm.afrilingua.content.dto.Word create(UUID languageId, CreateWordRequest request) {
        Language language = languageService.getEntityById(languageId);

        Word word = Word.builder()
                .language(language)
                .word(request.getWord())
                .translation(request.getTranslation())
                .grammaticalCategory(request.getGrammaticalCategory())
                .phoneticIpa(request.getPhoneticIpa())
                .audioUrl(request.getAudioUrl())
                .difficultyLevel(request.getDifficultyLevel() != null
                        ? Word.DifficultyLevel.valueOf(request.getDifficultyLevel().getValue())
                        : Word.DifficultyLevel.BEGINNER)
                .build();

        wordRepository.save(word);
        return toDto(word);
    }

    public cm.afrilingua.content.dto.Word getById(UUID wordId) {
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new WordNotFoundException(wordId));
        return toDto(word);
    }

    public List<cm.afrilingua.content.dto.Word> listByLanguage(UUID languageId) {
        // Ensures a 404 is raised for an unknown languageId rather than silently returning an empty list
        languageService.getEntityById(languageId);

        return wordRepository.findByLanguageId(languageId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
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
                .difficultyLevel(cm.afrilingua.content.dto.Word.DifficultyLevelEnum.valueOf(word.getDifficultyLevel().name()));
    }
}
