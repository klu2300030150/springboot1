package com.example.guardrails.repository;

import com.example.guardrails.domain.Bot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BotRepository extends JpaRepository<Bot, Long> {
}
