package cm.afrilingua.lesson.service;

import cm.afrilingua.lesson.dto.AddWordToLessonRequest;
import cm.afrilingua.lesson.dto.CreateLessonRequest;
import cm.afrilingua.lesson.entity.Lesson;
import cm.afrilingua.lesson.exception.LessonNotFoundException;
import cm.afrilingua.lesson.repository.LessonRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private LessonService lessonService;

    private static final UUID LANGUAGE_ID = UUID.randomUUID();
    private static final String TITLE = "Salutations";

    @Test
    void create_shouldSaveLesson_withEmptyWordIds() {
        CreateLessonRequest request = new CreateLessonRequest()
                .languageId(LANGUAGE_ID).title(TITLE).order(1).level(1);

        cm.afrilingua.lesson.dto.Lesson response = lessonService.create(request);

        assertThat(response.getTitle()).isEqualTo(TITLE);
        assertThat(response.getLanguageId()).isEqualTo(LANGUAGE_ID);
        assertThat(response.getOrder()).isEqualTo(1);
        assertThat(response.getLevel()).isEqualTo(1);
        assertThat(response.getWordIds()).isEmpty();

        verify(lessonRepository).save(any(Lesson.class));
    }

    @Test
    void getById_shouldReturnLesson_whenExists() {
        UUID lessonId = UUID.randomUUID();
        Lesson lesson = Lesson.builder()
                .id(lessonId).languageId(LANGUAGE_ID).title(TITLE).order(1).level(1)
                .build();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        cm.afrilingua.lesson.dto.Lesson response = lessonService.getById(lessonId);

        assertThat(response.getId()).isEqualTo(lessonId);
        assertThat(response.getTitle()).isEqualTo(TITLE);
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        UUID lessonId = UUID.randomUUID();
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.getById(lessonId))
                .isInstanceOf(LessonNotFoundException.class);
    }

    @Test
    void listByLanguage_shouldReturnLessonsOrderedByOrder() {
        Lesson lesson1 = Lesson.builder().id(UUID.randomUUID()).languageId(LANGUAGE_ID).title("A").order(1).level(1).build();
        Lesson lesson2 = Lesson.builder().id(UUID.randomUUID()).languageId(LANGUAGE_ID).title("B").order(2).level(1).build();

        when(lessonRepository.findByLanguageIdOrderByOrderAsc(LANGUAGE_ID)).thenReturn(List.of(lesson1, lesson2));

        List<cm.afrilingua.lesson.dto.Lesson> responses = lessonService.listByLanguage(LANGUAGE_ID);

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(cm.afrilingua.lesson.dto.Lesson::getTitle)
                .containsExactly("A", "B");
    }

    @Test
    void addWord_shouldAddWordIdToLesson_whenLessonExists() {
        UUID lessonId = UUID.randomUUID();
        UUID wordId = UUID.randomUUID();
        Lesson lesson = Lesson.builder()
                .id(lessonId).languageId(LANGUAGE_ID).title(TITLE).order(1).level(1)
                .build();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        AddWordToLessonRequest request = new AddWordToLessonRequest().wordId(wordId);
        cm.afrilingua.lesson.dto.Lesson response = lessonService.addWord(lessonId, request);

        assertThat(response.getWordIds()).containsExactly(wordId);
        verify(lessonRepository).save(lesson);
    }

    @Test
    void addWord_shouldThrow_whenLessonDoesNotExist() {
        UUID lessonId = UUID.randomUUID();
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        AddWordToLessonRequest request = new AddWordToLessonRequest().wordId(UUID.randomUUID());

        assertThatThrownBy(() -> lessonService.addWord(lessonId, request))
                .isInstanceOf(LessonNotFoundException.class);
    }
}
