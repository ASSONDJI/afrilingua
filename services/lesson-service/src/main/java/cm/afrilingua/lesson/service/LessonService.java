package cm.afrilingua.lesson.service;

import cm.afrilingua.lesson.dto.AddWordToLessonRequest;
import cm.afrilingua.lesson.dto.CreateLessonRequest;
import cm.afrilingua.lesson.entity.Lesson;
import cm.afrilingua.lesson.exception.LessonNotFoundException;
import cm.afrilingua.lesson.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;

    @Transactional
    public cm.afrilingua.lesson.dto.Lesson create(CreateLessonRequest request) {
        Lesson lesson = Lesson.builder()
                .languageId(request.getLanguageId())
                .title(request.getTitle())
                .order(request.getOrder())
                .level(request.getLevel())
                .build();

        lessonRepository.save(lesson);
        return toDto(lesson);
    }
    @Transactional(readOnly = true)
    public cm.afrilingua.lesson.dto.Lesson getById(UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException(lessonId));
        return toDto(lesson);
    }
    @Transactional(readOnly = true)
    public List<cm.afrilingua.lesson.dto.Lesson> listByLanguage(UUID languageId) {
        return lessonRepository.findByLanguageIdOrderByOrderAsc(languageId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public cm.afrilingua.lesson.dto.Lesson addWord(UUID lessonId, AddWordToLessonRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException(lessonId));

        lesson.getWordIds().add(request.getWordId());
        lessonRepository.save(lesson);
        return toDto(lesson);
    }

    private cm.afrilingua.lesson.dto.Lesson toDto(Lesson lesson) {
        return new cm.afrilingua.lesson.dto.Lesson()
                .id(lesson.getId())
                .languageId(lesson.getLanguageId())
                .title(lesson.getTitle())
                .order(lesson.getOrder())
                .level(lesson.getLevel())
                .wordIds(lesson.getWordIds().stream().toList());
    }
}
