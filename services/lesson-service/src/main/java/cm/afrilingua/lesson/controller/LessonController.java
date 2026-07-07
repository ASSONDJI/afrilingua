package cm.afrilingua.lesson.controller;

import cm.afrilingua.lesson.api.LessonsApi;
import cm.afrilingua.lesson.dto.AddWordToLessonRequest;
import cm.afrilingua.lesson.dto.CreateLessonRequest;
import cm.afrilingua.lesson.dto.Lesson;
import cm.afrilingua.lesson.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LessonController implements LessonsApi {

    private final LessonService lessonService;

    @Override
    public ResponseEntity<Lesson> createLesson(CreateLessonRequest createLessonRequest) {
        Lesson response = lessonService.create(createLessonRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<Lesson> getLesson(UUID lessonId) {
        return ResponseEntity.ok(lessonService.getById(lessonId));
    }

    @Override
    public ResponseEntity<List<Lesson>> listLessonsByLanguage(UUID languageId) {
        return ResponseEntity.ok(lessonService.listByLanguage(languageId));
    }

    @Override
    public ResponseEntity<Lesson> addWordToLesson(UUID lessonId, AddWordToLessonRequest addWordToLessonRequest) {
        return ResponseEntity.ok(lessonService.addWord(lessonId, addWordToLessonRequest));
    }
}
