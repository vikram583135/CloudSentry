package com.devops.platform.notification.sender;

import com.devops.platform.notification.model.Notification;
import com.devops.platform.notification.model.NotificationChannel;
import com.devops.platform.notification.model.NotificationConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Email notification sender.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailSender implements NotificationSender {

    @Override
    public boolean send(Notification notification, NotificationConfig config) {
        try {
            JavaMailSender mailSender = createMailSender(config);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(notification.getRecipient());
            message.setSubject(notification.getSubject());
            message.setText(notification.getContent());

            if (config.getFromAddress() != null) {
                message.setFrom(config.getFromAddress());
            }

            mailSender.send(message);
            log.info("Email sent successfully to: {}", notification.getRecipient());
            return true;

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", notification.getRecipient(), e.getMessage());
            notification.setErrorMessage(e.getMessage());
            return false;
        }
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    private JavaMailSender createMailSender(NotificationConfig config) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(config.getSmtpHost() != null ? config.getSmtpHost() : "smtp.gmail.com");
        mailSender.setPort(config.getSmtpPort() != null ? config.getSmtpPort() : 587);
        mailSender.setUsername(config.getSmtpUsername());
        mailSender.setPassword(config.getSmtpPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");

        if (Boolean.TRUE.equals(config.getSmtpUseTls())) {
            props.put("mail.smtp.starttls.enable", "true");
        }

        return mailSender;
    }
}
