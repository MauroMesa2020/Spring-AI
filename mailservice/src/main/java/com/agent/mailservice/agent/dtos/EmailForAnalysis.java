package com.agent.mailservice.agent.dtos;

import java.time.LocalDateTime;

public record EmailForAnalysis(
    String subject,
    LocalDateTime receivedAt,
    String body
) {}
