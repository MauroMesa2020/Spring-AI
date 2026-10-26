package com.agent.mailservice.service;

import org.springframework.ai.chat.client.ChatClient;

import org.springframework.stereotype.Service;

import com.agent.mailservice.agent.tools.MailTools;
import com.agent.mailservice.agent.types.EmailAnalysisResponse;
import com.agent.mailservice.dto.ChatData;
import com.agent.mailservice.dto.LastMailsResponse;
import com.agent.mailservice.dto.SenderResponse;

import lombok.extern.slf4j.Slf4j;

@Service 
@Slf4j 
public class ChatService {
    private final MailTools _mailTools;
    private final ChatClient _chatClient;

    public ChatService(MailTools mailTools,  ChatClient.Builder chatClientBuilder){
        _mailTools = mailTools;
        _chatClient = chatClientBuilder.build();
    }

    public ChatData analyzeEmails(String message) {

    String result = _chatClient.prompt()
        .system("""
            You are an email assistant.
            Use the available tools to retrieve, analyze, or send emails.

            Return the result in this exact format:
            TYPE: <type>
            RESULT: <complete result>

            TYPE must be exactly one of:
            SENDER, LAST, LAST_BY_USER, EMAIL_ANALYSIS.

            Include all relevant data returned by the tools.
            Never invent information.
            """)
        .user(message)
        .tools(_mailTools)
        .call()
        .content();

    log.info("Agent result: {}", result);

    String type = result.lines()
        .filter(line -> line.startsWith("TYPE:"))
        .map(line -> line.substring("TYPE:".length()).trim())
        .findFirst()
        .orElse("UNKNOWN");

    String response = result.substring(
        result.indexOf("RESULT:") + "RESULT:".length()
    ).trim();

    switch (type) {
        case "SENDER" -> {
            SenderResponse sender = _chatClient.prompt()
                .system("Convert the supplied result into a SenderResponse JSON object.")
                .user(response)
                .call()
                .entity(SenderResponse.class);

            return new ChatData(sender, null, null, null);
        }

        case "LAST" -> {
            LastMailsResponse last = _chatClient.prompt()
                .system("Convert the supplied result into a LastMailsResponse JSON object. Preserve all email data.")
                .user(response)
                .call()
                .entity(LastMailsResponse.class);

            return new ChatData(null, last, null, null);
        }

        case "LAST_BY_USER" -> {
            LastMailsResponse lastByUser = _chatClient.prompt()
                .system("Convert the supplied result into a LastMailsResponse JSON object. Preserve all email data.")
                .user(response)
                .call()
                .entity(LastMailsResponse.class);

            return new ChatData(null, null, lastByUser, null);
        }

        case "EMAIL_ANALYSIS" -> {
            EmailAnalysisResponse analysis = _chatClient.prompt()
                .system("Convert the supplied result into an EmailAnalysisResponse JSON object.")
                .user(response)
                .call()
                .entity(EmailAnalysisResponse.class);

            return new ChatData(null, null, null, analysis);
        }

        default -> throw new IllegalArgumentException(
            "Unknown result type: " + type
        );
    }
}
}
