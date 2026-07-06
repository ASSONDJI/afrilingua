package cm.afrilingua.content.service;

import cm.afrilingua.content.entity.Language;
import cm.afrilingua.content.exception.LanguageCodeAlreadyExistsException;
import cm.afrilingua.content.exception.LanguageNotFoundException;
import cm.afrilingua.content.repository.LanguageRepository;
import cm.afrilingua.content.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LanguageService {

    private final LanguageRepository languageRepository;
    private final WordRepository wordRepository;

    @Transactional
    public cm.afrilingua.content.dto.Language create(cm.afrilingua.content.dto.CreateLanguageRequest request) {
        if (languageRepository.existsByCode(request.getCode())) {
            throw new LanguageCodeAlreadyExistsException(request.getCode());
        }

        Language language = Language.builder()
                .name(request.getName())
                .code(request.getCode())
                .region(request.getRegion())
                .build();

        languageRepository.save(language);
        return toDto(language);
    }

    public cm.afrilingua.content.dto.Language getById(UUID languageId) {
        Language language = languageRepository.findById(languageId)
                .orElseThrow(() -> new LanguageNotFoundException(languageId));
        return toDto(language);
    }

    public List<cm.afrilingua.content.dto.Language> listAll() {
        return languageRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /** Also used by WordService to validate a languageId exists before attaching a word to it. */
    Language getEntityById(UUID languageId) {
        return languageRepository.findById(languageId)
                .orElseThrow(() -> new LanguageNotFoundException(languageId));
    }

    private cm.afrilingua.content.dto.Language toDto(Language language) {
        long wordCount = wordRepository.findByLanguageId(language.getId()).size();
        return new cm.afrilingua.content.dto.Language()
                .id(language.getId())
                .name(language.getName())
                .code(language.getCode())
                .region(language.getRegion())
                .totalWords((int) wordCount);
    }
}
