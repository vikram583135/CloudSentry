package com.devops.platform.notification.dto;

import com.devops.platform.notification.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for notification response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private UUID id;
    private NotificationType notificationType;
    private NotificationChannel channel;
    private NotificationPriority priority;
    private DeliveryStatus deliveryStatus;
    private String subject;
    private String content;
    private String recipient;
    private String recipientName;
    private String referenceType;
    private UUID referenceId;
    private Integer retryCount;
    private Instant sentAt;
    private Instant deliveredAt;
    private String errorMessage;
    private Instant createdAt;
}
