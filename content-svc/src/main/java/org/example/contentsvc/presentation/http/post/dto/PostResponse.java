package org.example.contentsvc.presentation.http.post.dto;

import org.example.contentsvc.domain.entity.PostStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostResponse(
        UUID id,
        UUID channelId,
        UUID authorId,
        String title,
        String content,
        PostStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime publishedAt
) {
}
