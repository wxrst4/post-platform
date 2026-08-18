package org.example.contentsvc.presentation.http.media.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PostMediaResponse(
        UUID id,
        UUID postId,
        String originalName,
        String contentType,
        Long sizeBytes,
        String url,
        OffsetDateTime urlExpiresAt
) {
}
