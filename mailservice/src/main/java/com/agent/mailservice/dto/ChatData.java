package com.agent.mailservice.dto;

import com.agent.mailservice.agent.types.EmailAnalysisResponse;

public record ChatData(
    SenderResponse sender,
    LastMailsResponse last,
    LastMailsResponse lastByUser,// Sacar despues, seria el last, pero para un remitente dado.
    EmailAnalysisResponse emaiAnalysisResponse
) {}