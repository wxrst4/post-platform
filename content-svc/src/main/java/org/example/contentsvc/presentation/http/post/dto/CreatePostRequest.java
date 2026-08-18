package org.example.contentsvc.presentation.http.post.dto;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record CreatePostRequest(
        @NotNull(message = "channelId is required")
        UUID channelId,
        @NotBlank(message = "title is required")
        String title,
        @NotBlank(message = "content is required")
        String content
) {
}
