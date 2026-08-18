package org.example.socialsvc.application.subscription.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostPublishedEvent(
        UUID eventId,
        UUID postId,
        UUID channelId,
        UUID authorId,
        String title,
        LocalDateTime publishedAt
) {
}
