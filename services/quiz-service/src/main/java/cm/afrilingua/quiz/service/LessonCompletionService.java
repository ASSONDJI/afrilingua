package cm.afrilingua.quiz.service;

import cm.afrilingua.quiz.entity.Question;
import cm.afrilingua.quiz.messaging.LessonCompletionEventPublisher;
import cm.afrilingua.quiz.entity.LessonCompletion;
import cm.afrilingua.quiz.repository.AnswerAttemptRepository;
import cm.afrilingua.quiz.repository.LessonCompletionRepository;
import cm.afrilingua.quiz.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Detects lesson completion after each correct answer and fires
 * quiz.completed accordingly. A lesson is "complete" for a user when every
 * question in it has at least one correct attempt from that user -- this
 * intentionally does not require completing them in a single sitting or
 * session, matching how Duolingo-style apps let learners resume lessons
 * across multiple visits.
 *
 * Questions without a wordId (legacy data, or non-vocabulary question types)
 * are excluded from the emitted correct_word_ids but still count toward
 * "all questions answered" -- a lesson with a mix of word-based and other
 * question types can still complete.
 */
@Service
@RequiredArgsConstructor
public class LessonCompletionService {

    private final QuestionRepository questionRepository;
    private final AnswerAttemptRepository answerAttemptRepository;
    private final LessonCompletionRepository lessonCompletionRepository;
    private final LessonCompletionEventPublisher eventPublisher;

    public void checkAndPublishIfComplete(UUID userId, UUID lessonId) {
        if (userId == null) {
            return; // no trusted identity (e.g. request bypassed the gateway) -- skip silently
        }

        // Idempotency guard: once this (user, lesson) pair is recorded, never
        // re-publish -- without this, every correct answer submitted AFTER
        // the lesson was already completed (e.g. re-answering a question
        // that was already correct) would re-fire both events and
        // re-award XP/badges.
        if (lessonCompletionRepository.existsByUserIdAndLessonId(userId, lessonId)) {
            return;
        }

        List<Question> lessonQuestions = questionRepository.findByLessonId(lessonId);
        if (lessonQuestions.isEmpty()) {
            return;
        }

        List<UUID> questionIds = lessonQuestions.stream().map(Question::getId).toList();

        Set<UUID> correctlyAnsweredQuestionIds = answerAttemptRepository
                .findByUserIdAndQuestionIdInAndIsCorrectTrue(userId, questionIds)
                .stream()
                .map(cm.afrilingua.quiz.entity.AnswerAttempt::getQuestionId)
                .collect(Collectors.toSet());

        boolean allAnswered = questionIds.stream().allMatch(correctlyAnsweredQuestionIds::contains);
        if (!allAnswered) {
            return;
        }

        List<UUID> correctWordIds = lessonQuestions.stream()
                .map(Question::getWordId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        // Accurate perfection check: true only if NO wrong attempt exists for
        // any question in this lesson, not just "eventually got it right".
        boolean isPerfect = !answerAttemptRepository
                .existsByUserIdAndQuestionIdInAndIsCorrectFalse(userId, questionIds);

        lessonCompletionRepository.save(
                LessonCompletion.builder().userId(userId).lessonId(lessonId).build()
        );

        eventPublisher.publishLessonCompleted(userId);
        eventPublisher.publishQuizCompleted(userId, correctWordIds, isPerfect);
    }
}
