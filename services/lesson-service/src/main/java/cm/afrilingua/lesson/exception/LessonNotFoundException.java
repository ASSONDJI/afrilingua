package cm.afrilingua.lesson.exception;

import java.util.UUID;

public class LessonNotFoundException extends LessonServiceException {
    public LessonNotFoundException(UUID lessonId) {
        super("Lesson not found with id: " + lessonId);
    }
}
