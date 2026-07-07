package cm.afrilingua.quiz.exception;

public abstract class QuizServiceException extends RuntimeException {
    public QuizServiceException(String message) {
        super(message);
    }
}
