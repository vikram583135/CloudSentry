package com.devops.platform.notification.dto;

import com.devops.platform.notification.model.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * DTO for sending a notification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {

    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;

    private NotificationChannel channel;

    @Builder.Default
    private NotificationPriority priority = NotificationPriority.MEDIUM;

    @NotBlank(message = "Recipient is required")
    private String recipient;

    private String recipientName;

    private String subject;

    private String content;

    private String referenceType;

    private UUID referenceId;

    private UUID applicationId;

    private Map<String, Object> templateVariables;

    private String dedupKey;
}
