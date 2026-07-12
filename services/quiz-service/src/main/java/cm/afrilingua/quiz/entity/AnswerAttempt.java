package cm.afrilingua.quiz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "answer_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerAttempt {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    /** Extracted from the X-User-Id header injected by api-gateway's
     * JwtAuthenticationFilter -- nullable for legacy rows only, always
     * populated for attempts submitted through the gateway. */
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "submitted_answer", nullable = false)
    private String submittedAnswer;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant submittedAt = Instant.now();
}
