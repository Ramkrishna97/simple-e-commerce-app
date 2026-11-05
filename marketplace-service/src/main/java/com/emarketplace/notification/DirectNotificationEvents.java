package com.emarketplace.notification;

import org.springframework.stereotype.Service;

/** Free-tier delivery stores events directly instead of running an embedded Kafka broker. */
@Service
public class DirectNotificationEvents {
    private final NotificationRepository notifications;

    public DirectNotificationEvents(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    public void publish(NotificationEvent event) {
        notifications.save(new NotificationMessage(event.senderUsername(), event.recipientUsername(),
            event.recipientRole(), event.message(), event.createdAt()));
    }
}
