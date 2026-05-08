package com.example.guardrails.api;

import com.example.guardrails.dto.CommentResponse;
import com.example.guardrails.dto.CreateCommentRequest;
import com.example.guardrails.dto.CreatePostRequest;
import com.example.guardrails.dto.LikePostRequest;
import com.example.guardrails.dto.PostResponse;
import com.example.guardrails.service.CommentService;
import com.example.guardrails.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    public PostController(PostService postService, CommentService commentService) {
        this.postService = postService;
        this.commentService = commentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse createPost(@Valid @RequestBody CreatePostRequest request) {
        return postService.createPost(request);
    }

    @PostMapping("/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse addComment(
            @PathVariable long postId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        return commentService.addComment(postId, request);
    }

    @PostMapping("/{postId}/like")
    public PostResponse likePost(
            @PathVariable long postId,
            @Valid @RequestBody LikePostRequest request
    ) {
        return postService.likePost(postId, request.userId());
    }
}
