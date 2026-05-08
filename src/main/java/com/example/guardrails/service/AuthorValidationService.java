package com.example.guardrails.service;

import com.example.guardrails.domain.AuthorType;
import com.example.guardrails.exception.NotFoundException;
import com.example.guardrails.repository.BotRepository;
import com.example.guardrails.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthorValidationService {

    private final UserRepository userRepository;
    private final BotRepository botRepository;

    public AuthorValidationService(UserRepository userRepository, BotRepository botRepository) {
        this.userRepository = userRepository;
        this.botRepository = botRepository;
    }

    public void ensureAuthorExists(AuthorType authorType, long authorId) {
        if (authorType == AuthorType.USER && !userRepository.existsById(authorId)) {
            throw new NotFoundException("User %d not found".formatted(authorId));
        }
        if (authorType == AuthorType.BOT && !botRepository.existsById(authorId)) {
            throw new NotFoundException("Bot %d not found".formatted(authorId));
        }
    }
}
