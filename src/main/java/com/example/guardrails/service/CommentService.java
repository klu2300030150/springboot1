package com.example.guardrails.service;

import com.example.guardrails.domain.AuthorType;
import com.example.guardrails.domain.Bot;
import com.example.guardrails.domain.Comment;
import com.example.guardrails.domain.Post;
import com.example.guardrails.dto.CommentResponse;
import com.example.guardrails.dto.CreateCommentRequest;
import com.example.guardrails.exception.BadRequestException;
import com.example.guardrails.exception.NotFoundException;
import com.example.guardrails.repository.BotRepository;
import com.example.guardrails.repository.CommentRepository;
import com.example.guardrails.repository.PostRepository;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class CommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final BotRepository botRepository;
    private final AuthorValidationService authorValidationService;
    private final RedisGuardrailService redisGuardrailService;
    private final NotificationService notificationService;

    public CommentService(
            PostRepository postRepository,
            CommentRepository commentRepository,
            BotRepository botRepository,
            AuthorValidationService authorValidationService,
            RedisGuardrailService redisGuardrailService,
            NotificationService notificationService
    ) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.botRepository = botRepository;
        this.authorValidationService = authorValidationService;
        this.redisGuardrailService = redisGuardrailService;
        this.notificationService = notificationService;
    }

    @Transactional
    public CommentResponse addComment(long postId, CreateCommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post %d not found".formatted(postId)));
        authorValidationService.ensureAuthorExists(request.authorType(), request.authorId());

        Comment parent = resolveParentComment(postId, request.parentCommentId());
        int depthLevel = parent == null ? 1 : parent.getDepthLevel() + 1;
        if (depthLevel > RedisGuardrailService.MAX_COMMENT_DEPTH) {
            throw new BadRequestException("Comment thread depth cannot exceed 20");
        }

        BotReplyReservation reservation = null;
        InteractionType interactionType = request.authorType() == AuthorType.BOT
                ? InteractionType.BOT_REPLY
                : InteractionType.HUMAN_COMMENT;
        AtomicBoolean viralityIncremented = new AtomicBoolean(false);

        try {
            if (request.authorType() == AuthorType.BOT) {
                Long targetHumanId = resolveTargetHumanId(post, parent);
                reservation = redisGuardrailService.reserveBotReply(postId, request.authorId(), targetHumanId);
                registerRedisRollback(reservation, postId, interactionType, viralityIncremented);
            }

            Comment comment = new Comment();
            comment.setPost(post);
            comment.setParentComment(parent);
            comment.setAuthorId(request.authorId());
            comment.setAuthorType(request.authorType());
            comment.setContent(request.content());
            comment.setDepthLevel(depthLevel);

            Comment saved = commentRepository.save(comment);
            redisGuardrailService.incrementViralityScore(postId, interactionType);
            viralityIncremented.set(true);

            if (request.authorType() == AuthorType.BOT) {
                notifyTargetHuman(post, parent, request.authorId());
            }

            return CommentResponse.from(saved);
        } catch (RuntimeException ex) {
            if (reservation != null && !TransactionSynchronizationManager.isSynchronizationActive()) {
                redisGuardrailService.release(reservation);
            }
            throw ex;
        }
    }

    private Comment resolveParentComment(long postId, Long parentCommentId) {
        if (parentCommentId == null) {
            return null;
        }

        Comment parent = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new NotFoundException("Parent comment %d not found".formatted(parentCommentId)));
        if (!parent.getPost().getId().equals(postId)) {
            throw new BadRequestException("Parent comment does not belong to post %d".formatted(postId));
        }
        return parent;
    }

    private Long resolveTargetHumanId(Post post, Comment parent) {
        if (parent != null && parent.getAuthorType() == AuthorType.USER) {
            return parent.getAuthorId();
        }
        if (post.getAuthorType() == AuthorType.USER) {
            return post.getAuthorId();
        }
        return null;
    }

    private void notifyTargetHuman(Post post, Comment parent, long botId) {
        Long targetHumanId = resolveTargetHumanId(post, parent);
        if (targetHumanId == null) {
            return;
        }
        String botName = botRepository.findById(botId).map(Bot::getName).orElse("Bot " + botId);
        notificationService.notifyBotInteraction(targetHumanId, botName);
    }

    private void registerRedisRollback(
            BotReplyReservation reservation,
            long postId,
            InteractionType interactionType,
            AtomicBoolean viralityIncremented
    ) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    redisGuardrailService.release(reservation);
                    if (viralityIncremented.get()) {
                        redisGuardrailService.decrementViralityScore(postId, interactionType);
                    }
                }
            }
        });
    }
}
