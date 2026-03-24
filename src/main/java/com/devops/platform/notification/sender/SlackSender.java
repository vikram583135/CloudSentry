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
import java.util.List;
import java.util.Map;

/**
 * Slack notification sender.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SlackSender implements NotificationSender {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public boolean send(Notification notification, NotificationConfig config) {
        try {
            String webhookUrl = config.getWebhookUrl();
            if (webhookUrl == null || webhookUrl.isEmpty()) {
                log.error("Slack webhook URL not configured");
                notification.setErrorMessage("Slack webhook URL not configured");
                return false;
            }

            Map<String, Object> payload = buildSlackPayload(notification, config);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(payload), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    webhookUrl, HttpMethod.POST, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Slack notification sent successfully");
                return true;
            } else {
                notification.setErrorMessage("Slack API returned: " + response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            log.error("Failed to send Slack notification: {}", e.getMessage());
            notification.setErrorMessage(e.getMessage());
            return false;
        }
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SLACK;
    }

    private Map<String, Object> buildSlackPayload(Notification notification, NotificationConfig config) {
        Map<String, Object> payload = new HashMap<>();

        if (config.getSlackChannel() != null) {
            payload.put("channel", config.getSlackChannel());
        }

        // Build rich message with blocks
        List<Map<String, Object>> blocks = List.of(
                Map.of(
                        "type", "header",
                        "text", Map.of(
                                "type", "plain_text",
                                "text", notification.getSubject(),
                                "emoji", true)),
                Map.of(
                        "type", "section",
                        "text", Map.of(
                                "type", "mrkdwn",
                                "text", notification.getContent())),
                Map.of(
                        "type", "context",
                        "elements", List.of(
                                Map.of(
                                        "type", "mrkdwn",
                                        "text", String.format("*Type:* %s | *Priority:* %s",
                                                notification.getNotificationType(),
                                                notification.getPriority())))));

        payload.put("blocks", blocks);
        payload.put("text", notification.getSubject()); // Fallback text

        return payload;
    }
}
