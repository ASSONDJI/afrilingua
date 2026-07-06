package cm.afrilingua.content.service;

import cm.afrilingua.content.dto.CreateLanguageRequest;
import cm.afrilingua.content.entity.Language;
import cm.afrilingua.content.exception.LanguageCodeAlreadyExistsException;
import cm.afrilingua.content.exception.LanguageNotFoundException;
import cm.afrilingua.content.repository.LanguageRepository;
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
class LanguageServiceTest {

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private WordRepository wordRepository;

    @InjectMocks
    private LanguageService languageService;

    private static final String NAME = "Yemba";
    private static final String CODE = "yem";
    private static final String REGION = "Ouest";

    @Test
    void create_shouldSaveLanguage_whenCodeIsNotTaken() {
        CreateLanguageRequest request = new CreateLanguageRequest().name(NAME).code(CODE).region(REGION);

        when(languageRepository.existsByCode(CODE)).thenReturn(false);
        when(wordRepository.findByLanguageId(any())).thenReturn(List.of());

        cm.afrilingua.content.dto.Language response = languageService.create(request);

        assertThat(response.getName()).isEqualTo(NAME);
        assertThat(response.getCode()).isEqualTo(CODE);
        assertThat(response.getRegion()).isEqualTo(REGION);
        assertThat(response.getTotalWords()).isEqualTo(0);

        verify(languageRepository).save(org.mockito.ArgumentMatchers.argThat(language ->
                language.getName().equals(NAME) &&
                        language.getCode().equals(CODE) &&
                        language.getRegion().equals(REGION)
        ));
    }

    @Test
    void create_shouldThrow_whenCodeAlreadyExists() {
        CreateLanguageRequest request = new CreateLanguageRequest().name(NAME).code(CODE).region(REGION);
        when(languageRepository.existsByCode(CODE)).thenReturn(true);

        assertThatThrownBy(() -> languageService.create(request))
                .isInstanceOf(LanguageCodeAlreadyExistsException.class);

        verify(languageRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnLanguage_whenExists() {
        UUID languageId = UUID.randomUUID();
        Language language = Language.builder()
                .id(languageId)
                .name(NAME)
                .code(CODE)
                .region(REGION)
                .build();

        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));
        when(wordRepository.findByLanguageId(languageId)).thenReturn(List.of());

        cm.afrilingua.content.dto.Language response = languageService.getById(languageId);

        assertThat(response.getId()).isEqualTo(languageId);
        assertThat(response.getName()).isEqualTo(NAME);
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        UUID languageId = UUID.randomUUID();
        when(languageRepository.findById(languageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> languageService.getById(languageId))
                .isInstanceOf(LanguageNotFoundException.class);
    }

    @Test
    void listAll_shouldReturnAllLanguages() {
        Language language1 = Language.builder().id(UUID.randomUUID()).name("Yemba").code("yem").region("Ouest").build();
        Language language2 = Language.builder().id(UUID.randomUUID()).name("Duala").code("dua").region("Littoral").build();

        when(languageRepository.findAll()).thenReturn(List.of(language1, language2));
        when(wordRepository.findByLanguageId(any())).thenReturn(List.of());

        List<cm.afrilingua.content.dto.Language> responses = languageService.listAll();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(cm.afrilingua.content.dto.Language::getCode)
                .containsExactlyInAnyOrder("yem", "dua");
    }

    @Test
    void getEntityById_shouldReturnEntity_whenExists() {
        UUID languageId = UUID.randomUUID();
        Language language = Language.builder().id(languageId).name(NAME).code(CODE).region(REGION).build();
        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));

        Language result = languageService.getEntityById(languageId);

        assertThat(result).isEqualTo(language);
    }

    @Test
    void getEntityById_shouldThrow_whenNotFound() {
        UUID languageId = UUID.randomUUID();
        when(languageRepository.findById(languageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> languageService.getEntityById(languageId))
                .isInstanceOf(LanguageNotFoundException.class);
    }
}