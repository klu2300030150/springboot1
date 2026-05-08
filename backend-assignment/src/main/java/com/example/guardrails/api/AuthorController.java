package com.example.guardrails.api;

import com.example.guardrails.domain.Bot;
import com.example.guardrails.domain.User;
import com.example.guardrails.dto.BotResponse;
import com.example.guardrails.dto.CreateBotRequest;
import com.example.guardrails.dto.CreateUserRequest;
import com.example.guardrails.dto.UserResponse;
import com.example.guardrails.repository.BotRepository;
import com.example.guardrails.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthorController {

    private final UserRepository userRepository;
    private final BotRepository botRepository;

    public AuthorController(UserRepository userRepository, BotRepository botRepository) {
        this.userRepository = userRepository;
        this.botRepository = botRepository;
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setPremium(request.premium());
        return UserResponse.from(userRepository.save(user));
    }

    @PostMapping("/bots")
    @ResponseStatus(HttpStatus.CREATED)
    public BotResponse createBot(@Valid @RequestBody CreateBotRequest request) {
        Bot bot = new Bot();
        bot.setName(request.name());
        bot.setPersonaDescription(request.personaDescription());
        return BotResponse.from(botRepository.save(bot));
    }
}
