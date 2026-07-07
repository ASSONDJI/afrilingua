package cm.afrilingua.quiz.controller;

import cm.afrilingua.quiz.api.QuizzesApi;
import cm.afrilingua.quiz.dto.*;
import cm.afrilingua.quiz.service.QuestionService;
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
public class QuestionController implements QuizzesApi {

    private final QuestionService questionService;

    @Override
    public ResponseEntity<QuestionWithAnswer> createQuestion(CreateQuestionRequest createQuestionRequest) {
        QuestionWithAnswer response = questionService.create(createQuestionRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<Question> getQuestion(UUID questionId) {
        return ResponseEntity.ok(questionService.getById(questionId));
    }

    @Override
    public ResponseEntity<List<Question>> listQuizzesByLesson(UUID lessonId) {
        return ResponseEntity.ok(questionService.listByLesson(lessonId));
    }

    @Override
    public ResponseEntity<AnswerResult> submitAnswer(UUID questionId, SubmitAnswerRequest submitAnswerRequest) {
        return ResponseEntity.ok(questionService.submitAnswer(questionId, submitAnswerRequest));
    }
}
