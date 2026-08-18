package org.example.notificationsvc.application.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.notificationsvc.application.notification.events.NotificationRequestedEvent;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostNotificationHandler {

    private final NotificationService notificationService;

    public void handleNotificationRequested(
            ConsumerRecord<String, NotificationRequestedEvent> record
    ) {

        var event = record.value();

        if (event == null) {
            return;
        }

        log.info(
                "Received NOTIFICATION_REQUESTED_EVENT: " +
                        "eventId={}, type={}, recipientId={}, " +
                        "postId={}, channelId={}, postTitle={}, " +
                        "title={}, createdAt={}",
                event.eventId(),
                event.type(),
                event.recipientId(),
                event.postId(),
                event.channelId(),
                event.postTitle(),
                event.title(),
                event.createdAt()
        );

        notificationService.createFromNotificationRequestedEvent(event);
    }
}
