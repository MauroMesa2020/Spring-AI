package com.agent.mailservice.agent.tools;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import com.agent.mailservice.dto.LastMailsResponse;
import com.agent.mailservice.dto.MailRequest;
import com.agent.mailservice.dto.MailResponse;
import com.agent.mailservice.service.MailService;

import lombok.extern.slf4j.Slf4j;

@Component 
@Slf4j 
public class MailTools {
    private final MailService _mailService;

    public MailTools(MailService mailService){
        _mailService = mailService;
    }

    @Tool(description = """
        Retrieves the most recent emails from the user's inbox.
        Use this tool when the user asks to retrieve or review recent emails.
    """)
    public List<LastMailsResponse> lastMails(
        @ToolParam (description = "Number of recent emails to retrieve") int number
    ){

        log.info("Start getLastTool");

            if (number < 1 || number > 100) {
                throw new IllegalArgumentException(
                        "The number must be between 1 and 10"
                );
            }

        return _mailService.getLast(number);
    }


    
    @Tool(description = """
    Sends an email to a specified recipient.
    Use this tool when the user explicitly asks to send an email.
    After successful execution, report that the email was sent.
    Do not call this tool again for the same request after it succeeds.
    """)
    public MailResponse sendMail(
            @ToolParam(description = "Recipient email address") String to,
            @ToolParam(description = "Email message content") String body,
            @ToolParam(description = "Email subject line") String subject) {

        log.info("START sendMail tool. Recipient: {}", to);

        MailRequest request = new MailRequest();
        request.setTo(to);
        request.setBody(body);
        request.setSubject(subject);

        MailResponse response = _mailService.sender(request);

        log.info("END sendMail tool. Recipient: {}", to);

        return response;
    }

    @Tool(description = """
    Retrieves the most recent emails from a specific sender.
    Use this when the user asks about emails received from a given address.
    """)
    public List<LastMailsResponse> getLastEmailsFromSender(
        @ToolParam(description = "Sender email address")
        String sender,

        @ToolParam(description = "Maximum number of emails to retrieve")
        int number
    ) {
        if (number < 1 || number > 10) {
            throw new IllegalArgumentException(
                "Number must be between 1 and 10"
            );
        }

        return _mailService.getLastFromSender(sender, number);
    }
}
