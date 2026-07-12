package cm.afrilingua.quiz.repository;

import cm.afrilingua.quiz.entity.AnswerAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnswerAttemptRepository extends JpaRepository<AnswerAttempt, UUID> {

    List<AnswerAttempt> findByUserIdAndQuestionIdInAndIsCorrectTrue(UUID userId, List<UUID> questionIds);

    boolean existsByUserIdAndQuestionIdInAndIsCorrectFalse(UUID userId, List<UUID> questionIds);
}
