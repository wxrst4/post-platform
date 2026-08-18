package org.example.usersvc.presentation.http.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserBioRequests(
        @NotBlank(message = "bio is required")
        String bio
) {
}
