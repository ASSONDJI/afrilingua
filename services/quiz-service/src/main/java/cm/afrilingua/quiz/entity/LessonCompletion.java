package cm.afrilingua.quiz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/** One row per (user, lesson) the first time that lesson is completed --
 * guards against re-publishing lesson.completed/quiz.completed every time a
 * question is answered correctly after the lesson was already finished
 * (e.g. re-answering an already-correct question, or answering questions
 * out of order near the completion point). */
@Entity
@Table(name = "lesson_completions", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "lesson_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonCompletion {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "lesson_id", nullable = false)
    private UUID lessonId;

    @Column(name = "completed_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant completedAt = Instant.now();
}
