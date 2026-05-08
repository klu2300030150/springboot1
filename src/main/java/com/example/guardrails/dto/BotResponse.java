package com.example.guardrails.dto;

import com.example.guardrails.domain.Bot;

public record BotResponse(Long id, String name, String personaDescription) {
    public static BotResponse from(Bot bot) {
        return new BotResponse(bot.getId(), bot.getName(), bot.getPersonaDescription());
    }
}
