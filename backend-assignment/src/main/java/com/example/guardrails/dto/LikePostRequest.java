package com.example.guardrails.dto;

import jakarta.validation.constraints.NotNull;

public record LikePostRequest(@NotNull Long userId) {
}
