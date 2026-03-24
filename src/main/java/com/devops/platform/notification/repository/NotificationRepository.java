package com.devops.platform.notification.repository;

import com.devops.platform.notification.model.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Notification entities.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByDeliveryStatusOrderByCreatedAtDesc(DeliveryStatus status, Pageable pageable);

    List<Notification> findByChannelAndDeliveryStatusOrderByCreatedAtDesc(
            NotificationChannel channel, DeliveryStatus status, Pageable pageable);

    List<Notification> findByRecipientOrderByCreatedAtDesc(String recipient, Pageable pageable);

    List<Notification> findByReferenceIdOrderByCreatedAtDesc(UUID referenceId);

    Optional<Notification> findByDedupKey(String dedupKey);

    @Query("SELECT n FROM Notification n WHERE n.deliveryStatus = 'RETRYING' AND n.nextRetryAt <= :now AND n.retryCount < n.maxRetries")
    List<Notification> findPendingRetries(@Param("now") Instant now);

    @Query("SELECT n FROM Notification n WHERE n.deliveryStatus = 'PENDING' ORDER BY n.priority DESC, n.createdAt ASC")
    List<Notification> findPendingNotifications(Pageable pageable);

    @Query("SELECT n.channel, COUNT(n) FROM Notification n WHERE n.createdAt > :since GROUP BY n.channel")
    List<Object[]> countByChannelSince(@Param("since") Instant since);

    @Query("SELECT n.deliveryStatus, COUNT(n) FROM Notification n WHERE n.createdAt > :since GROUP BY n.deliveryStatus")
    List<Object[]> countByStatusSince(@Param("since") Instant since);

    long countByDeliveryStatus(DeliveryStatus status);

    boolean existsByDedupKey(String dedupKey);
}
