package com.example.guardrails.dto;

import com.example.guardrails.domain.AuthorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCommentRequest(
        @NotNull Long authorId,
        @NotNull AuthorType authorType,
        Long parentCommentId,
        @NotBlank String content
) {
}
