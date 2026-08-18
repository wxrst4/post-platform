package org.example.socialsvc.presentation.subscription.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SubscriptionResponse(
        UUID id,
        UUID userId,
        UUID channelId,
        LocalDateTime createdAt
) {
}
