package org.example.notificationsvc.application.notification;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationsvc.application.exceptions.NotificationNotFoundException;
import org.example.notificationsvc.application.notification.events.NotificationRequestedEvent;
import org.example.notificationsvc.domain.entity.Notification;
import org.example.notificationsvc.domain.entity.Operation;
import org.example.notificationsvc.infrastructure.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<Notification> getAll(UUID recipientId, boolean unreadOnly) {
        if (unreadOnly) {
            return notificationRepository.findAllByRecipientIdAndIsRead(recipientId, false);
        }

        return notificationRepository.findAllByRecipientId(recipientId);
    }

    public Notification get(UUID id) {
        return notificationRepository
                .findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
    }

    public long getUnreadCount(UUID recipientId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(recipientId);
    }

    @Transactional
    public Notification markAsRead(UUID id) {
        Notification notification = get(id);
        notification.setIsRead(true);
        return notification;
    }

    public void createFromNotificationRequestedEvent(NotificationRequestedEvent event) {
        Operation operation = switch (event.type()) {
            case "NEW_POST" -> Operation.POST_PUBLISHED;
            case "LIKE" -> Operation.POST_LIKED;
            case "SUBSCRIBE" -> Operation.CHANNEL_SUBSCRIBED;
            default -> throw new IllegalArgumentException("Unknown notification type: " + event.type());
        };

        Notification notification = Notification.builder()
                .operation(operation)
                .postId(event.postId())
                .postTitle(event.postTitle())
                .recipientId(event.recipientId())
                .title(event.title())
                .createdAt(event.createdAt().toInstant(java.time.ZoneOffset.UTC))
                .build();

        notificationRepository.save(notification);

        log.info(
                "Notification created: eventId={}, operation={}, recipientId={}",
                event.eventId(),
                operation,
                event.recipientId()
        );
    }
}
