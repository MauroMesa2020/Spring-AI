package com.agent.mailservice.agent.types;

import java.time.LocalDateTime;

public record EmailSummary(
    String subject,
    LocalDateTime receivedAt,
    String summary
) {}