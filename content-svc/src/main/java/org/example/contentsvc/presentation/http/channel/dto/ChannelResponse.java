package org.example.contentsvc.presentation.http.channel.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChannelResponse(
        UUID id,
        UUID ownerId,
        String name,
        String description,
        LocalDateTime createdAt
) {
}
