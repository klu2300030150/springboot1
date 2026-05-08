package com.example.guardrails.service;

public enum InteractionType {
    BOT_REPLY(1),
    HUMAN_LIKE(20),
    HUMAN_COMMENT(50);

    private final long viralityPoints;

    InteractionType(long viralityPoints) {
        this.viralityPoints = viralityPoints;
    }

    public long viralityPoints() {
        return viralityPoints;
    }
}
