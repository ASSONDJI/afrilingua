package cm.afrilingua.quiz.exception;

import java.util.UUID;

public class QuestionNotFoundException extends QuizServiceException {
    public QuestionNotFoundException(UUID questionId) {
        super("Question not found with id: " + questionId);
    }
}
