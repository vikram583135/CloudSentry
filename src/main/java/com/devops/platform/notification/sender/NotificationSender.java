package com.devops.platform.notification.sender;

import com.devops.platform.notification.model.Notification;
import com.devops.platform.notification.model.NotificationConfig;

/**
 * Interface for notification channel senders.
 */
public interface NotificationSender {

    /**
     * Sends the notification.
     * 
     * @return true if sent successfully
     */
    boolean send(Notification notification, NotificationConfig config);

    /**
     * Gets the channel this sender supports.
     */
    com.devops.platform.notification.model.NotificationChannel getChannel();
}
