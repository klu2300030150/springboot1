package com.example.guardrails.service;

import com.example.guardrails.domain.AuthorType;
import com.example.guardrails.domain.Post;
import com.example.guardrails.dto.CreatePostRequest;
import com.example.guardrails.dto.PostResponse;
import com.example.guardrails.exception.NotFoundException;
import com.example.guardrails.repository.PostRepository;
import com.example.guardrails.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final AuthorValidationService authorValidationService;
    private final RedisGuardrailService redisGuardrailService;

    public PostService(
            PostRepository postRepository,
            UserRepository userRepository,
            AuthorValidationService authorValidationService,
            RedisGuardrailService redisGuardrailService
    ) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.authorValidationService = authorValidationService;
        this.redisGuardrailService = redisGuardrailService;
    }

    @Transactional
    public PostResponse createPost(CreatePostRequest request) {
        authorValidationService.ensureAuthorExists(request.authorType(), request.authorId());

        Post post = new Post();
        post.setAuthorId(request.authorId());
        post.setAuthorType(request.authorType());
        post.setContent(request.content());

        Post saved = postRepository.save(post);
        return PostResponse.from(saved, redisGuardrailService.getViralityScore(saved.getId()));
    }

    public PostResponse likePost(long postId, long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post %d not found".formatted(postId)));
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User %d not found".formatted(userId));
        }

        redisGuardrailService.incrementViralityScore(postId, InteractionType.HUMAN_LIKE);
        return PostResponse.from(post, redisGuardrailService.getViralityScore(postId));
    }
}
