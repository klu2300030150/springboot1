package com.example.guardrails.dto;

import com.example.guardrails.domain.AuthorType;
import com.example.guardrails.domain.Post;
import java.time.Instant;

public record PostResponse(
        Long id,
        Long authorId,
        AuthorType authorType,
        String content,
        Instant createdAt,
        long viralityScore
) {
    public static PostResponse from(Post post, long viralityScore) {
        return new PostResponse(
                post.getId(),
                post.getAuthorId(),
                post.getAuthorType(),
                post.getContent(),
                post.getCreatedAt(),
                viralityScore
        );
    }
}
