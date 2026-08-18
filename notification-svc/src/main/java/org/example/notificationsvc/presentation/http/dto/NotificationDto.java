package org.example.notificationsvc.presentation.http.dto;

import org.example.notificationsvc.domain.entity.Operation;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        Operation type,
        UUID postId,
        String postTitle,
        UUID recipientId,
        Instant createdAt,
        Boolean isRead
) {
}
