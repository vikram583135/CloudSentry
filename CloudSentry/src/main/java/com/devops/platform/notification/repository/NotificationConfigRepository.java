package com.devops.platform.notification.repository;

import com.devops.platform.notification.model.NotificationChannel;
import com.devops.platform.notification.model.NotificationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for NotificationConfig entities.
 */
@Repository
public interface NotificationConfigRepository extends JpaRepository<NotificationConfig, UUID> {

    List<NotificationConfig> findByChannelAndEnabledTrue(NotificationChannel channel);

    List<NotificationConfig> findByApplicationIdAndEnabledTrue(UUID applicationId);

    Optional<NotificationConfig> findByChannelAndIsDefaultTrue(NotificationChannel channel);

    Optional<NotificationConfig> findByApplicationIdAndChannelAndEnabledTrue(UUID applicationId,
            NotificationChannel channel);

    List<NotificationConfig> findByEnabledTrue();

    boolean existsByName(String name);
}
