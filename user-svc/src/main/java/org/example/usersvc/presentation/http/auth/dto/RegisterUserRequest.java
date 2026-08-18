package org.example.usersvc.presentation.http.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterUserRequest(
        @NotBlank
        String email,
        @NotBlank
        String username,
        @NotBlank
        String password
) {
}
