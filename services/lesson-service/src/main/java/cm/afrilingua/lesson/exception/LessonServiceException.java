package cm.afrilingua.lesson.exception;

public abstract class LessonServiceException extends RuntimeException {
    public LessonServiceException(String message) {
        super(message);
    }
}
