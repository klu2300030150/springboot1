package com.example.guardrails.dto;

import com.example.guardrails.domain.AuthorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePostRequest(
        @NotNull Long authorId,
        @NotNull AuthorType authorType,
        @NotBlank String content
) {
}
