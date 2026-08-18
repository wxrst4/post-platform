package org.example.socialsvc.application.subscription.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationRequestedEvent(
        UUID eventId,
        String type,
        UUID recipientId,
        UUID postId,
        UUID channelId,
        String postTitle,
        String title,
        LocalDateTime createdAt
) {
}
