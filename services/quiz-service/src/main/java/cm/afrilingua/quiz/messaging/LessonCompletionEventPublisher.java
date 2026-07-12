package cm.afrilingua.quiz.messaging;

import cm.afrilingua.quiz.config.EventProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Publishes quiz.completed to the afrilingua.events topic exchange, consumed
 * by recommendation-service's gamification pipeline (XP, streak, badges,
 * unique learned words). The payload shape must match what
 * gamification_service.handle_quiz_completed expects: user_id and
 * correct_word_ids.
 *
 * A lesson only "completes" (see LessonCompletionService) once every
 * question has at least one correct attempt -- mistakes must be corrected
 * via retry before completion, matching Duolingo's model. is_perfect
 * separately tracks whether that happened with zero wrong attempts at all.
 */
@Component
@RequiredArgsConstructor
public class LessonCompletionEventPublisher {

    private static final String QUIZ_ROUTING_KEY = "quiz.completed";
    private static final String LESSON_ROUTING_KEY = "lesson.completed";

    private final RabbitTemplate rabbitTemplate;
    private final EventProperties eventProperties;

    /** is_perfect must be accurately computed by the caller (true only if
     * zero incorrect attempts were recorded for any question in this
     * completion pass) -- never hardcode this to true, or the
     * "Perfectionniste" badge and its XP bonus become meaningless. */
    public void publishQuizCompleted(UUID userId, List<UUID> correctWordIds, boolean isPerfect) {
        Map<String, Object> payload = Map.of(
                "user_id", userId.toString(),
                "correct_word_ids", correctWordIds.stream().map(UUID::toString).toList(),
                "is_perfect", isPerfect
        );

        rabbitTemplate.convertAndSend(eventProperties.exchange(), QUIZ_ROUTING_KEY, payload);
    }

    /** Fired whenever a lesson is finished (every question answered
     * correctly at least once), regardless of how many wrong attempts
     * preceded it -- completing a lesson with mistakes still counts as
     * completing it, matching how Duolingo-style apps work. */
    public void publishLessonCompleted(UUID userId) {
        Map<String, Object> payload = Map.of("user_id", userId.toString());
        rabbitTemplate.convertAndSend(eventProperties.exchange(), LESSON_ROUTING_KEY, payload);
    }
}
