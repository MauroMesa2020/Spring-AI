package com.agent.mailservice.agent.dtos;

import java.time.LocalDateTime;

public record EmailSummary(
    String subject,
    LocalDateTime receivedAt,
    String summary
) {}