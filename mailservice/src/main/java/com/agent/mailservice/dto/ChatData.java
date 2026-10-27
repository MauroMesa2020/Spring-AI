package com.agent.mailservice.dto;

import java.util.List;

import com.agent.mailservice.agent.types.EmailAnalysisResponse;

public record ChatData(
    SenderResponse sender,
    List<LastMailsResponse> last,
    EmailAnalysisResponse emaiAnalysisResponse
) {}