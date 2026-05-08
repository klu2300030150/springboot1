package com.example.guardrails.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateBotRequest(
        @NotBlank String name,
        @NotBlank String personaDescription
) {
}
