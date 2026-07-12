package cm.afrilingua.quiz.repository;

import cm.afrilingua.quiz.entity.LessonCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LessonCompletionRepository extends JpaRepository<LessonCompletion, UUID> {

    boolean existsByUserIdAndLessonId(UUID userId, UUID lessonId);
}
