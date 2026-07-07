package cm.afrilingua.lesson.repository;

import cm.afrilingua.lesson.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    List<Lesson> findByLanguageIdOrderByOrderAsc(UUID languageId);
}
