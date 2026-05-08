package com.example.guardrails.service;

import com.example.guardrails.exception.TooManyRequestsException;
import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisGuardrailService {

    public static final int MAX_BOT_REPLIES_PER_POST = 100;
    public static final int MAX_COMMENT_DEPTH = 20;

    private static final Duration BOT_HUMAN_COOLDOWN = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;

    public RedisGuardrailService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void incrementViralityScore(long postId, InteractionType interactionType) {
        redisTemplate.opsForValue().increment(viralityKey(postId), interactionType.viralityPoints());
    }

    public void decrementViralityScore(long postId, InteractionType interactionType) {
        redisTemplate.opsForValue().decrement(viralityKey(postId), interactionType.viralityPoints());
    }

    public long getViralityScore(long postId) {
        String value = redisTemplate.opsForValue().get(viralityKey(postId));
        return value == null ? 0L : Long.parseLong(value);
    }

    public BotReplyReservation reserveBotReply(long postId, long botId, Long targetHumanId) {
        long currentCount = redisTemplate.opsForValue().increment(botCountKey(postId));
        if (currentCount > MAX_BOT_REPLIES_PER_POST) {
            redisTemplate.opsForValue().decrement(botCountKey(postId));
            throw new TooManyRequestsException("Post has reached the 100 bot reply limit");
        }

        String cooldownKey = null;
        if (targetHumanId != null) {
            cooldownKey = cooldownKey(botId, targetHumanId);
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(cooldownKey, "1", BOT_HUMAN_COOLDOWN);
            if (!Boolean.TRUE.equals(acquired)) {
                redisTemplate.opsForValue().decrement(botCountKey(postId));
                throw new TooManyRequestsException("Bot is cooling down for this human");
            }
        }

        return new BotReplyReservation(postId, cooldownKey);
    }

    public void release(BotReplyReservation reservation) {
        if (reservation == null) {
            return;
        }
        redisTemplate.opsForValue().decrement(botCountKey(reservation.postId()));
        if (reservation.cooldownKey() != null) {
            redisTemplate.delete(reservation.cooldownKey());
        }
    }

    private String viralityKey(long postId) {
        return "post:%d:virality_score".formatted(postId);
    }

    private String botCountKey(long postId) {
        return "post:%d:bot_count".formatted(postId);
    }

    private String cooldownKey(long botId, long humanId) {
        return "cooldown:bot_%d:human_%d".formatted(botId, humanId);
    }
}
