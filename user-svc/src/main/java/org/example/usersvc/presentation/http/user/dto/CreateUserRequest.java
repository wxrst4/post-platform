package org.example.usersvc.presentation.http.user.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank(message = "email is required")
        String email,

        @NotBlank(message = "password is required")
        String password,

        @NotBlank(message = "username is required")
        String username,

        @NotBlank(message = "bio is required")
        String bio
) {
}
