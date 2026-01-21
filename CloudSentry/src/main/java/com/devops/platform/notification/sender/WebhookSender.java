package com.devops.platform.notification.sender;

import com.devops.platform.notification.model.Notification;
import com.devops.platform.notification.model.NotificationChannel;
import com.devops.platform.notification.model.NotificationConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic webhook notification sender.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookSender implements NotificationSender {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public boolean send(Notification notification, NotificationConfig config) {
        try {
            String webhookUrl = config.getWebhookUrl();
            if (webhookUrl == null || webhookUrl.isEmpty()) {
                log.error("Webhook URL not configured");
                notification.setErrorMessage("Webhook URL not configured");
                return false;
            }

            Map<String, Object> payload = buildWebhookPayload(notification);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Add custom headers from config
            if (config.getHeaders() != null && !config.getHeaders().isEmpty()) {
                try {
                    Map<String, String> customHeaders = objectMapper.readValue(
                            config.getHeaders(),
                            objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
                    customHeaders.forEach(headers::add);
                } catch (Exception e) {
                    log.warn("Failed to parse custom headers: {}", e.getMessage());
                }
            }

            // Add API key if configured
            if (config.getApiKey() != null) {
                headers.add("Authorization", "Bearer " + config.getApiKey());
            }

            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(payload), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    webhookUrl, HttpMethod.POST, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Webhook notification sent successfully to: {}", webhookUrl);
                notification.setExternalId(extractExternalId(response.getBody()));
                return true;
            } else {
                notification.setErrorMessage("Webhook returned: " + response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            log.error("Failed to send webhook notification: {}", e.getMessage());
            notification.setErrorMessage(e.getMessage());
            return false;
        }
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.WEBHOOK;
    }

    private Map<String, Object> buildWebhookPayload(Notification notification) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", notification.getId().toString());
        payload.put("type", notification.getNotificationType().name());
        payload.put("priority", notification.getPriority().name());
        payload.put("subject", notification.getSubject());
        payload.put("content", notification.getContent());
        payload.put("recipient", notification.getRecipient());
        payload.put("timestamp", notification.getCreatedAt().toString());

        if (notification.getReferenceType() != null) {
            payload.put("referenceType", notification.getReferenceType());
        }
        if (notification.getReferenceId() != null) {
            payload.put("referenceId", notification.getReferenceId().toString());
        }

        return payload;
    }

    private String extractExternalId(String responseBody) {
        try {
            if (responseBody != null) {
                Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
                if (response.containsKey("id")) {
                    return response.get("id").toString();
                }
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return null;
    }
}
