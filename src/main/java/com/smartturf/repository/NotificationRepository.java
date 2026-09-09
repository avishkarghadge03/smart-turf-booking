package com.smartturf.repository;

import com.smartturf.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Spring Data JPA Repository for In-App Customer Notifications.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Find all notifications for a user ordered by newest
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Find unread notifications for a user
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    // Count unread notifications for a user (for badge counter)
    long countByUserIdAndIsReadFalse(Long userId);
}
