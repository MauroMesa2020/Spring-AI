package com.agent.mailservice.agent.types;

import java.util.List;

public record EmailAnalysisResponse(
    String sender,
    int requestedCount,
    int analyzedCount,
    List<EmailTopic> topics,
    List<EmailSummary> emails
) {}