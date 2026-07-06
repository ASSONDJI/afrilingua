package cm.afrilingua.content.service;

import cm.afrilingua.content.dto.CreateWordRequest;
import cm.afrilingua.content.entity.Language;
import cm.afrilingua.content.entity.Word;
import cm.afrilingua.content.exception.LanguageNotFoundException;
import cm.afrilingua.content.exception.WordNotFoundException;
import cm.afrilingua.content.repository.WordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WordServiceTest {

    @Mock
    private WordRepository wordRepository;

    @Mock
    private LanguageService languageService;

    @InjectMocks
    private WordService wordService;

    private static final String WORD = "nsem";
    private static final String TRANSLATION = "maison";
    private static final String CATEGORY = "noun";

    @Test
    void create_shouldCreateWord_withDefaultDifficulty_whenNoneProvided() {
        UUID languageId = UUID.randomUUID();
        Language language = Language.builder().id(languageId).name("Yemba").code("yem").region("Ouest").build();
        CreateWordRequest request = new CreateWordRequest().word(WORD).translation(TRANSLATION).grammaticalCategory(CATEGORY);

        when(languageService.getEntityById(languageId)).thenReturn(language);

        cm.afrilingua.content.dto.Word response = wordService.create(languageId, request);

        assertThat(response.getWord()).isEqualTo(WORD);
        assertThat(response.getTranslation()).isEqualTo(TRANSLATION);
        assertThat(response.getLanguageId()).isEqualTo(languageId);
        assertThat(response.getDifficultyLevel())
                .isEqualTo(cm.afrilingua.content.dto.Word.DifficultyLevelEnum.BEGINNER);

        verify(wordRepository).save(any(Word.class));
    }

    @Test
    void create_shouldThrow_whenLanguageDoesNotExist() {
        UUID languageId = UUID.randomUUID();
        CreateWordRequest request = new CreateWordRequest().word(WORD).translation(TRANSLATION).grammaticalCategory(CATEGORY);

        when(languageService.getEntityById(languageId)).thenThrow(new LanguageNotFoundException(languageId));

        assertThatThrownBy(() -> wordService.create(languageId, request))
                .isInstanceOf(LanguageNotFoundException.class);

        verify(wordRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnWord_whenExists() {
        UUID wordId = UUID.randomUUID();
        Language language = Language.builder().id(UUID.randomUUID()).name("Yemba").code("yem").region("Ouest").build();
        Word word = Word.builder()
                .id(wordId)
                .language(language)
                .word(WORD)
                .translation(TRANSLATION)
                .grammaticalCategory(CATEGORY)
                .difficultyLevel(Word.DifficultyLevel.BEGINNER)
                .build();

        when(wordRepository.findById(wordId)).thenReturn(Optional.of(word));

        cm.afrilingua.content.dto.Word response = wordService.getById(wordId);

        assertThat(response.getId()).isEqualTo(wordId);
        assertThat(response.getWord()).isEqualTo(WORD);
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        UUID wordId = UUID.randomUUID();
        when(wordRepository.findById(wordId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wordService.getById(wordId))
                .isInstanceOf(WordNotFoundException.class);
    }

    @Test
    void listByLanguage_shouldReturnWords_whenLanguageExists() {
        UUID languageId = UUID.randomUUID();
        Language language = Language.builder().id(languageId).name("Yemba").code("yem").region("Ouest").build();
        Word word = Word.builder()
                .id(UUID.randomUUID())
                .language(language)
                .word(WORD)
                .translation(TRANSLATION)
                .grammaticalCategory(CATEGORY)
                .difficultyLevel(Word.DifficultyLevel.BEGINNER)
                .build();

        when(languageService.getEntityById(languageId)).thenReturn(language);
        when(wordRepository.findByLanguageId(languageId)).thenReturn(List.of(word));

        List<cm.afrilingua.content.dto.Word> responses = wordService.listByLanguage(languageId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getWord()).isEqualTo(WORD);
    }

    @Test
    void listByLanguage_shouldThrow_whenLanguageDoesNotExist() {
        UUID languageId = UUID.randomUUID();
        when(languageService.getEntityById(languageId)).thenThrow(new LanguageNotFoundException(languageId));

        assertThatThrownBy(() -> wordService.listByLanguage(languageId))
                .isInstanceOf(LanguageNotFoundException.class);
    }
}