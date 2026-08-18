package org.example.socialsvc.presentation.comment.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID userId,
        UUID postId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
