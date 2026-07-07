package cm.afrilingua.user.repository;

import cm.afrilingua.user.entity.ActivityLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
    List<ActivityLog> findByProfileIdOrderByOccurredAtDesc(UUID profileId, Pageable pageable);
}