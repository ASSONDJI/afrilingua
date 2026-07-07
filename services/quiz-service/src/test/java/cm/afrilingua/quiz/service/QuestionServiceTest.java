package cm.afrilingua.quiz.service;

import cm.afrilingua.quiz.dto.AnswerResult;
import cm.afrilingua.quiz.dto.CreateQuestionRequest;
import cm.afrilingua.quiz.dto.Question;
import cm.afrilingua.quiz.dto.QuestionWithAnswer;
import cm.afrilingua.quiz.dto.SubmitAnswerRequest;
import cm.afrilingua.quiz.exception.QuestionNotFoundException;
import cm.afrilingua.quiz.repository.QuestionRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private QuestionService questionService;

    private static final UUID LESSON_ID = UUID.randomUUID();
    private static final String QUESTION_TEXT = "Comment dit-on maison en yemba ?";
    private static final List<String> OPTIONS = List.of("nsem", "tsa", "ndap");
    private static final String CORRECT_ANSWER = "nsem";

    @Test
    void create_shouldSaveQuestion_andReturnDtoWithAnswer() {
        CreateQuestionRequest request = new CreateQuestionRequest()
                .lessonId(LESSON_ID)
                .type(CreateQuestionRequest.TypeEnum.MULTIPLE_CHOICE)
                .questionText(QUESTION_TEXT)
                .options(OPTIONS)
                .correctAnswer(CORRECT_ANSWER);

        QuestionWithAnswer response = questionService.create(request);

        assertThat(response.getLessonId()).isEqualTo(LESSON_ID);
        assertThat(response.getQuestionText()).isEqualTo(QUESTION_TEXT);
        assertThat(response.getOptions()).containsExactlyElementsOf(OPTIONS);
        assertThat(response.getCorrectAnswer()).isEqualTo(CORRECT_ANSWER);

        org.mockito.Mockito.verify(questionRepository).save(any(cm.afrilingua.quiz.entity.Question.class));
    }

    @Test
    void getById_shouldReturnQuestion_withoutCorrectAnswer() {
        UUID questionId = UUID.randomUUID();
        cm.afrilingua.quiz.entity.Question question = cm.afrilingua.quiz.entity.Question.builder()
                .id(questionId)
                .lessonId(LESSON_ID)
                .type(cm.afrilingua.quiz.entity.Question.QuestionType.MULTIPLE_CHOICE)
                .questionText(QUESTION_TEXT)
                .options(OPTIONS)
                .correctAnswer(CORRECT_ANSWER)
                .build();

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));

        Question response = questionService.getById(questionId);

        assertThat(response.getId()).isEqualTo(questionId);
        assertThat(response.getOptions()).containsExactlyElementsOf(OPTIONS);
        // Question (public DTO) has no correctAnswer field at all -- enforced at compile time,
        // not just by omission, since the DTO class itself doesn't expose that getter.
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        UUID questionId = UUID.randomUUID();
        when(questionRepository.findById(questionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionService.getById(questionId))
                .isInstanceOf(QuestionNotFoundException.class);
    }

    @Test
    void listByLesson_shouldReturnQuestionsForThatLesson() {
        cm.afrilingua.quiz.entity.Question question = cm.afrilingua.quiz.entity.Question.builder()
                .id(UUID.randomUUID())
                .lessonId(LESSON_ID)
                .type(cm.afrilingua.quiz.entity.Question.QuestionType.MULTIPLE_CHOICE)
                .questionText(QUESTION_TEXT)
                .options(OPTIONS)
                .correctAnswer(CORRECT_ANSWER)
                .build();

        when(questionRepository.findByLessonId(LESSON_ID)).thenReturn(List.of(question));

        List<Question> responses = questionService.listByLesson(LESSON_ID);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getQuestionText()).isEqualTo(QUESTION_TEXT);
    }

    @Test
    void submitAnswer_shouldReturnCorrectTrue_whenAnswerMatchesExactly() {
        UUID questionId = UUID.randomUUID();
        cm.afrilingua.quiz.entity.Question question = cm.afrilingua.quiz.entity.Question.builder()
                .id(questionId)
                .lessonId(LESSON_ID)
                .type(cm.afrilingua.quiz.entity.Question.QuestionType.MULTIPLE_CHOICE)
                .questionText(QUESTION_TEXT)
                .options(OPTIONS)
                .correctAnswer(CORRECT_ANSWER)
                .build();

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));

        SubmitAnswerRequest request = new SubmitAnswerRequest().submittedAnswer("nsem");
        AnswerResult result = questionService.submitAnswer(questionId, request);

        assertThat(result.getCorrect()).isTrue();
        assertThat(result.getCorrectAnswer()).isEqualTo(CORRECT_ANSWER);
    }

    @Test
    void submitAnswer_shouldReturnCorrectTrue_whenAnswerDiffersOnlyByCaseAndWhitespace() {
        UUID questionId = UUID.randomUUID();
        cm.afrilingua.quiz.entity.Question question = cm.afrilingua.quiz.entity.Question.builder()
                .id(questionId)
                .lessonId(LESSON_ID)
                .type(cm.afrilingua.quiz.entity.Question.QuestionType.MULTIPLE_CHOICE)
                .questionText(QUESTION_TEXT)
                .options(OPTIONS)
                .correctAnswer(CORRECT_ANSWER)
                .build();

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));

        SubmitAnswerRequest request = new SubmitAnswerRequest().submittedAnswer("  NSEM  ");
        AnswerResult result = questionService.submitAnswer(questionId, request);

        assertThat(result.getCorrect()).isTrue();
    }

    @Test
    void submitAnswer_shouldReturnCorrectFalse_whenAnswerIsWrong() {
        UUID questionId = UUID.randomUUID();
        cm.afrilingua.quiz.entity.Question question = cm.afrilingua.quiz.entity.Question.builder()
                .id(questionId)
                .lessonId(LESSON_ID)
                .type(cm.afrilingua.quiz.entity.Question.QuestionType.MULTIPLE_CHOICE)
                .questionText(QUESTION_TEXT)
                .options(OPTIONS)
                .correctAnswer(CORRECT_ANSWER)
                .build();

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));

        SubmitAnswerRequest request = new SubmitAnswerRequest().submittedAnswer("tsa");
        AnswerResult result = questionService.submitAnswer(questionId, request);

        assertThat(result.getCorrect()).isFalse();
    }

    @Test
    void submitAnswer_shouldThrow_whenQuestionNotFound() {
        UUID questionId = UUID.randomUUID();
        when(questionRepository.findById(questionId)).thenReturn(Optional.empty());

        SubmitAnswerRequest request = new SubmitAnswerRequest().submittedAnswer("nsem");

        assertThatThrownBy(() -> questionService.submitAnswer(questionId, request))
                .isInstanceOf(QuestionNotFoundException.class);
    }
}