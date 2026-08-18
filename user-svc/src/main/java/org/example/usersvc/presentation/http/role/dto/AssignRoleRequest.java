package org.example.usersvc.presentation.http.role.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignRoleRequest(
        @NotNull(message = "userId is required")
        UUID userId,
        @NotNull(message = "roleId is required")
        Long roleId
) {
}
