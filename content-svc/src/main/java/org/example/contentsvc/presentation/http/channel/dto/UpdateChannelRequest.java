package org.example.contentsvc.presentation.http.channel.dto;

import jakarta.validation.constraints.*;

public record UpdateChannelRequest(
        @NotBlank(message = "name is required")
        @Size(max = 100)
        String name,
        @Size(max = 1000)
        String description
) {
}
