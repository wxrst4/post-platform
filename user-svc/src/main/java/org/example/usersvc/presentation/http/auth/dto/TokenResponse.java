package org.example.usersvc.presentation.http.auth.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
