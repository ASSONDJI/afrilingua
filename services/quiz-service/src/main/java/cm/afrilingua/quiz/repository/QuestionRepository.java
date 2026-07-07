package cm.afrilingua.quiz.repository;

import cm.afrilingua.quiz.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
    List<Question> findByLessonId(UUID lessonId);
}
