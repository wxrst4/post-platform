package org.example.contentsvc.presentation.http.post.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdatePostRequest(
        @NotBlank(message = "title is required")
        String title,
        @NotBlank(message = "content is required")
        String content
) {
}
