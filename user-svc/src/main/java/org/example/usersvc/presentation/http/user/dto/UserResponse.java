package org.example.usersvc.presentation.http.user.dto;

import java.time.LocalDateTime;

public record UserResponse(
        String email,
        String username,
        String bio,
        LocalDateTime createdAt
) {
}
