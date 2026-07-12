package cm.afrilingua.quiz.service;

import cm.afrilingua.quiz.dto.AnswerResult;
import cm.afrilingua.quiz.dto.CreateQuestionRequest;
import cm.afrilingua.quiz.dto.Question;
import cm.afrilingua.quiz.dto.QuestionWithAnswer;
import cm.afrilingua.quiz.dto.SubmitAnswerRequest;
import cm.afrilingua.quiz.entity.AnswerAttempt;
import cm.afrilingua.quiz.exception.QuestionNotFoundException;
import cm.afrilingua.quiz.repository.AnswerAttemptRepository;
import cm.afrilingua.quiz.repository.QuestionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final AnswerAttemptRepository answerAttemptRepository;
    private final HttpServletRequest httpServletRequest;
    private final LessonCompletionService lessonCompletionService;

    @Transactional
    public QuestionWithAnswer create(CreateQuestionRequest request) {
        cm.afrilingua.quiz.entity.Question question = cm.afrilingua.quiz.entity.Question.builder()
                .lessonId(request.getLessonId())
                .wordId(request.getWordId())
                .type(cm.afrilingua.quiz.entity.Question.QuestionType.valueOf(request.getType().getValue()))
                .questionText(request.getQuestionText())
                .options(request.getOptions() != null ? request.getOptions() : List.of())
                .correctAnswer(request.getCorrectAnswer())
                .build();

        questionRepository.save(question);
        return toDtoWithAnswer(question);
    }

    // Learner-facing: correctAnswer is intentionally never included here.
    // readOnly = true keeps the Hibernate session open long enough for
    // toPublicDto() to read the lazily-loaded "options" collection.
    @Transactional(readOnly = true)
    public Question getById(UUID questionId) {
        cm.afrilingua.quiz.entity.Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException(questionId));
        return toPublicDto(question);
    }

    // Same reasoning as getById: readOnly = true is required here too.
    @Transactional(readOnly = true)
    public List<Question> listByLesson(UUID lessonId) {
        return questionRepository.findByLessonId(lessonId).stream()
                .map(this::toPublicDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AnswerResult submitAnswer(UUID questionId, SubmitAnswerRequest request) {
        cm.afrilingua.quiz.entity.Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException(questionId));

        boolean isCorrect = normalize(question.getCorrectAnswer())
                .equalsIgnoreCase(normalize(request.getSubmittedAnswer()));

        UUID userId = extractUserId();

        AnswerAttempt attempt = AnswerAttempt.builder()
                .questionId(question.getId())
                .userId(userId)
                .submittedAnswer(request.getSubmittedAnswer())
                .isCorrect(isCorrect)
                .build();
        answerAttemptRepository.save(attempt);

        if (isCorrect) {
            lessonCompletionService.checkAndPublishIfComplete(userId, question.getLessonId());
        }

        return new AnswerResult()
                .questionId(question.getId())
                .correct(isCorrect)
                .correctAnswer(question.getCorrectAnswer());
    }

    /** Normalizes to NFC before comparison: the words dataset (Yemba tones in
     * particular) can be stored with combining diacritics (NFD) while user
     * input -- or even the reference data itself -- may use precomposed
     * characters (NFC). Without this, visually identical strings compare
     * as different, silently marking correct answers as wrong. */
    private String normalize(String value) {
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFC);
    }

    /** Reads the trusted X-User-Id header injected by api-gateway's
     * JwtAuthenticationFilter. Never trust a client-supplied value here --
     * the gateway strips any client-provided X-User-Id before setting its
     * own, so if this header is present, it came from a validated JWT. */
    private UUID extractUserId() {
        String userId = httpServletRequest.getHeader("X-User-Id");
        return userId != null ? UUID.fromString(userId) : null;
    }

    private Question toPublicDto(cm.afrilingua.quiz.entity.Question question) {
        return new Question()
                .id(question.getId())
                .lessonId(question.getLessonId())
                .wordId(question.getWordId())
                .type(Question.TypeEnum.valueOf(question.getType().name()))
                .questionText(question.getQuestionText())
                .options(new java.util.ArrayList<>(question.getOptions()));
    }

    private QuestionWithAnswer toDtoWithAnswer(cm.afrilingua.quiz.entity.Question question) {
        return new QuestionWithAnswer()
                .id(question.getId())
                .lessonId(question.getLessonId())
                .type(QuestionWithAnswer.TypeEnum.valueOf(question.getType().name()))
                .questionText(question.getQuestionText())
                .options(new java.util.ArrayList<>(question.getOptions()))
                .correctAnswer(question.getCorrectAnswer());
    }
}
