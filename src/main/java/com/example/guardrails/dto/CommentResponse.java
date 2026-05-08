package com.example.guardrails.dto;

import com.example.guardrails.domain.AuthorType;
import com.example.guardrails.domain.Comment;
import java.time.Instant;

public record CommentResponse(
        Long id,
        Long postId,
        Long parentCommentId,
        Long authorId,
        AuthorType authorType,
        String content,
        int depthLevel,
        Instant createdAt
) {
    public static CommentResponse from(Comment comment) {
        Long parentId = comment.getParentComment() == null ? null : comment.getParentComment().getId();
        return new CommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                parentId,
                comment.getAuthorId(),
                comment.getAuthorType(),
                comment.getContent(),
                comment.getDepthLevel(),
                comment.getCreatedAt()
        );
    }
}
