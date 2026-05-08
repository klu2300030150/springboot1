package com.example.guardrails.dto;

import com.example.guardrails.domain.User;

public record UserResponse(Long id, String username, boolean premium) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.isPremium());
    }
}
