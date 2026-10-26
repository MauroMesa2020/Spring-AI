package com.agent.mailservice.service;

import org.springframework.ai.chat.client.ChatClient;

import org.springframework.stereotype.Service;

import com.agent.mailservice.agent.tools.MailTools;
import com.agent.mailservice.dto.ChatResponse;

@Service 
public class ChatService {
    private final MailTools _mailTools;
    private final ChatClient _chatClient;

    public ChatService(MailTools mailTools,  ChatClient.Builder chatClientBuilder){
        _mailTools = mailTools;
        _chatClient = chatClientBuilder.build();
    }

    public ChatResponse analyzeEmails(String message) {

        String result = _chatClient.prompt()
            .system("""
                You are an email assistant.

                You can retrieve, analyze, and send emails using the available tools.

                When the user requests an email to be sent:
                1. Call sendMail once with the provided recipient, subject, and body.
                2. Wait for the tool result.
                3. If successful, tell the user the email was sent.
                4. Do not call sendMail again for the same request.

                Never claim success if the tool fails.
                Use other tools only when needed.
                Keep responses concise.
                """)
            .user(message)
            .tools(_mailTools)
            .call()
            .content();

        return new ChatResponse(
            "GENERAL",
            result,
            null
        );
    }
}
