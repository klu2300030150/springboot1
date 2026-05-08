package com.example.guardrails.service;

import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final Duration USER_NOTIFICATION_COOLDOWN = Duration.ofMinutes(15);
    private static final String PENDING_USERS_KEY = "users:pending_notifs";

    private final StringRedisTemplate redisTemplate;

    public NotificationService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void notifyBotInteraction(long userId, String botName) {
        String cooldownKey = cooldownKey(userId);
        Boolean sentImmediately = redisTemplate.opsForValue()
                .setIfAbsent(cooldownKey, "1", USER_NOTIFICATION_COOLDOWN);

        if (Boolean.TRUE.equals(sentImmediately)) {
            log.info("Push Notification Sent to User {}: {} replied to your post", userId, botName);
            return;
        }

        String notification = "%s replied to your post".formatted(botName);
        redisTemplate.opsForList().rightPush(pendingListKey(userId), notification);
        redisTemplate.opsForSet().add(PENDING_USERS_KEY, String.valueOf(userId));
    }

    @Scheduled(fixedRate = 5 * 60 * 1000L)
    public void sweepPendingNotifications() {
        var userIds = redisTemplate.opsForSet().members(PENDING_USERS_KEY);
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        for (String rawUserId : userIds) {
            String listKey = pendingListKey(rawUserId);
            List<String> messages = redisTemplate.opsForList().range(listKey, 0, -1);
            redisTemplate.delete(listKey);
            redisTemplate.opsForSet().remove(PENDING_USERS_KEY, rawUserId);

            if (messages == null || messages.isEmpty()) {
                continue;
            }

            String firstBot = extractBotName(messages.get(0));
            int others = Math.max(0, messages.size() - 1);
            log.info("Summarized Push Notification: {} and {} others interacted with your posts.",
                    firstBot, others);
        }
    }

    private String extractBotName(String notification) {
        int repliedIndex = notification.indexOf(" replied");
        return repliedIndex > 0 ? notification.substring(0, repliedIndex) : notification;
    }

    private String cooldownKey(long userId) {
        return "user:%d:notif_cooldown".formatted(userId);
    }

    private String pendingListKey(long userId) {
        return "user:%d:pending_notifs".formatted(userId);
    }

    private String pendingListKey(String userId) {
        return "user:%s:pending_notifs".formatted(userId);
    }
}
