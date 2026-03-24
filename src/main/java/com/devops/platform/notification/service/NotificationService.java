package com.devops.platform.notification.service;

import com.devops.platform.common.exception.BadRequestException;
import com.devops.platform.common.exception.ResourceNotFoundException;
import com.devops.platform.notification.dto.*;
import com.devops.platform.notification.model.*;
import com.devops.platform.notification.repository.*;
import com.devops.platform.notification.sender.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing and sending notifications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationConfigRepository configRepository;
    private final NotificationTemplateRepository templateRepository;
    private final EmailSender emailSender;
    private final SlackSender slackSender;
    private final WebhookSender webhookSender;

    /**
     * Sends a notification.
     */
    @Transactional
    public NotificationResponse send(SendNotificationRequest request) {
        // Check for deduplication
        if (request.getDedupKey() != null && notificationRepository.existsByDedupKey(request.getDedupKey())) {
            log.info("Duplicate notification detected, skipping: {}", request.getDedupKey());
            return null;
        }

        // Determine channel
        NotificationChannel channel = request.getChannel();
        if (channel == null) {
            channel = determineDefaultChannel(request.getNotificationType());
        }

        // Get template if content not provided
        String subject = request.getSubject();
        String content = request.getContent();
        if (subject == null || content == null) {
            NotificationTemplate template = getTemplate(request.getNotificationType(), channel);
            if (template != null) {
                subject = processTemplate(template.getSubjectTemplate(), request.getTemplateVariables());
                content = processTemplate(template.getBodyTemplate(), request.getTemplateVariables());
            }
        }

        if (subject == null)
            subject = request.getNotificationType().name();
        if (content == null)
            content = "Notification: " + request.getNotificationType().name();

        // Create notification
        Notification notification = Notification.builder()
                .notificationType(request.getNotificationType())
                .channel(channel)
                .priority(request.getPriority())
                .deliveryStatus(DeliveryStatus.PENDING)
                .subject(subject)
                .content(content)
                .recipient(request.getRecipient())
                .recipientName(request.getRecipientName())
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .applicationId(request.getApplicationId())
                .dedupKey(request.getDedupKey())
                .build();

        notification = notificationRepository.save(notification);

        // Send asynchronously
        sendAsync(notification);

        return toResponse(notification);
    }

    /**
     * Sends notification asynchronously.
     */
    @Async
    public void sendAsync(Notification notification) {
        try {
            NotificationConfig config = getConfig(notification.getChannel(), notification.getApplicationId());
            if (config == null) {
                notification.setDeliveryStatus(DeliveryStatus.FAILED);
                notification.setErrorMessage("No configuration found for channel: " + notification.getChannel());
                notification.setFailedAt(Instant.now());
                notificationRepository.save(notification);
                return;
            }

            boolean success = sendToChannel(notification, config);

            if (success) {
                notification.setDeliveryStatus(DeliveryStatus.SENT);
                notification.setSentAt(Instant.now());
                log.info("Notification sent: id={}, channel={}", notification.getId(), notification.getChannel());
            } else {
                handleSendFailure(notification);
            }

            notificationRepository.save(notification);

        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage(), e);
            notification.setDeliveryStatus(DeliveryStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            notification.setFailedAt(Instant.now());
            notificationRepository.save(notification);
        }
    }

    /**
     * Retries failed notifications.
     */
    @Transactional
    public int retryFailedNotifications() {
        List<Notification> pendingRetries = notificationRepository.findPendingRetries(Instant.now());
        int retried = 0;

        for (Notification notification : pendingRetries) {
            if (notification.canRetry()) {
                notification.incrementRetry();
                notificationRepository.save(notification);
                sendAsync(notification);
                retried++;
            }
        }

        log.info("Retried {} failed notifications", retried);
        return retried;
    }

    /**
     * Gets a notification by ID.
     */
    public NotificationResponse getNotification(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        return toResponse(notification);
    }

    /**
     * Gets notifications by status.
     */
    public List<NotificationResponse> getByStatus(DeliveryStatus status, int limit) {
        return notificationRepository.findByDeliveryStatusOrderByCreatedAtDesc(status, PageRequest.of(0, limit))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets notifications by recipient.
     */
    public List<NotificationResponse> getByRecipient(String recipient, int limit) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient, PageRequest.of(0, limit))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets notification statistics.
     */
    public Map<String, Object> getStats() {
        Instant last24h = Instant.now().minus(24, ChronoUnit.HOURS);

        Map<String, Long> byChannel = new HashMap<>();
        for (Object[] row : notificationRepository.countByChannelSince(last24h)) {
            byChannel.put(((NotificationChannel) row[0]).name(), (Long) row[1]);
        }

        Map<String, Long> byStatus = new HashMap<>();
        for (Object[] row : notificationRepository.countByStatusSince(last24h)) {
            byStatus.put(((DeliveryStatus) row[0]).name(), (Long) row[1]);
        }

        return Map.of(
                "pending", notificationRepository.countByDeliveryStatus(DeliveryStatus.PENDING),
                "sent", notificationRepository.countByDeliveryStatus(DeliveryStatus.SENT),
                "failed", notificationRepository.countByDeliveryStatus(DeliveryStatus.FAILED),
                "last24hByChannel", byChannel,
                "last24hByStatus", byStatus);
    }

    // Template management

    public List<NotificationTemplate> getAllTemplates() {
        return templateRepository.findByEnabledTrue();
    }

    public NotificationTemplate getTemplate(UUID id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationTemplate", "id", id));
    }

    // Config management

    public List<NotificationConfig> getAllConfigs() {
        return configRepository.findByEnabledTrue();
    }

    public NotificationConfig getConfig(UUID id) {
        return configRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationConfig", "id", id));
    }

    // Private helper methods

    private boolean sendToChannel(Notification notification, NotificationConfig config) {
        return switch (notification.getChannel()) {
            case EMAIL -> emailSender.send(notification, config);
            case SLACK -> slackSender.send(notification, config);
            case WEBHOOK -> webhookSender.send(notification, config);
            default -> {
                log.warn("Unsupported channel: {}", notification.getChannel());
                notification.setErrorMessage("Unsupported channel: " + notification.getChannel());
                yield false;
            }
        };
    }

    private void handleSendFailure(Notification notification) {
        if (notification.canRetry()) {
            notification.incrementRetry();
            notification.setNextRetryAt(Instant.now().plus(5 * notification.getRetryCount(), ChronoUnit.MINUTES));
            notification.setDeliveryStatus(DeliveryStatus.RETRYING);
        } else {
            notification.setDeliveryStatus(DeliveryStatus.FAILED);
            notification.setFailedAt(Instant.now());
        }
    }

    private NotificationChannel determineDefaultChannel(NotificationType type) {
        return switch (type) {
            case INCIDENT_CREATED, ANOMALY_DETECTED, THRESHOLD_BREACH -> NotificationChannel.SLACK;
            case SCHEDULED_REPORT, COST_RECOMMENDATION -> NotificationChannel.EMAIL;
            default -> NotificationChannel.WEBHOOK;
        };
    }

    private NotificationConfig getConfig(NotificationChannel channel, UUID applicationId) {
        if (applicationId != null) {
            Optional<NotificationConfig> appConfig = configRepository.findByApplicationIdAndChannelAndEnabledTrue(
                    applicationId, channel);
            if (appConfig.isPresent())
                return appConfig.get();
        }
        return configRepository.findByChannelAndIsDefaultTrue(channel).orElse(null);
    }

    private NotificationTemplate getTemplate(NotificationType type, NotificationChannel channel) {
        return templateRepository.findByNotificationTypeAndChannelAndEnabledTrue(type, channel)
                .orElseGet(() -> templateRepository.findByNotificationTypeAndChannelAndIsDefaultTrue(type, channel)
                        .orElse(null));
    }

    private String processTemplate(String template, Map<String, Object> variables) {
        if (template == null || variables == null)
            return template;

        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}",
                    entry.getValue() != null ? entry.getValue().toString() : "");
        }
        return result;
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .notificationType(notification.getNotificationType())
                .channel(notification.getChannel())
                .priority(notification.getPriority())
                .deliveryStatus(notification.getDeliveryStatus())
                .subject(notification.getSubject())
                .content(notification.getContent())
                .recipient(notification.getRecipient())
                .recipientName(notification.getRecipientName())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .retryCount(notification.getRetryCount())
                .sentAt(notification.getSentAt())
                .deliveredAt(notification.getDeliveredAt())
                .errorMessage(notification.getErrorMessage())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
