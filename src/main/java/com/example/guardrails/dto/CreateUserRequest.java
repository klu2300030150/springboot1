package com.example.guardrails.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank String username,
        boolean premium
) {
}
