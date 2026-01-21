package com.devops.platform.notification.repository;

import com.devops.platform.notification.model.NotificationChannel;
import com.devops.platform.notification.model.NotificationTemplate;
import com.devops.platform.notification.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for NotificationTemplate entities.
 */
@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {

    Optional<NotificationTemplate> findByName(String name);

    List<NotificationTemplate> findByNotificationTypeAndEnabledTrue(NotificationType type);

    Optional<NotificationTemplate> findByNotificationTypeAndChannelAndIsDefaultTrue(
            NotificationType type, NotificationChannel channel);

    Optional<NotificationTemplate> findByNotificationTypeAndChannelAndEnabledTrue(
            NotificationType type, NotificationChannel channel);

    List<NotificationTemplate> findByEnabledTrue();

    boolean existsByName(String name);
}
