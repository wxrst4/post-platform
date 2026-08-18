package org.example.usersvc.presentation.http.role.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateRoleRequest(
        @NotBlank(message = "name is required")
        String name
) {
}
