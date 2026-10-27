package com.agent.mailservice.agent.tools;

import java.time.LocalDate;
import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import com.agent.mailservice.dto.LastMailsResponse;
import com.agent.mailservice.dto.MailRequest;
import com.agent.mailservice.dto.SenderResponse;
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
    public SenderResponse sendMail(
            @ToolParam(description = "Recipient email address") String to,
            @ToolParam(description = "Email message content") String body,
            @ToolParam(description = "Email subject line") String subject) {

        log.info("START sendMail tool. Recipient: {}", to);

        MailRequest request = new MailRequest();
        request.setTo(to);
        request.setBody(body);
        request.setSubject(subject);

        SenderResponse response = _mailService.sender(request);

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

    @Tool(
    name = "searchEmails",
    description = """
        Search emails in the INBOX using optional criteria:
        subject, body keywords, sender email address, and received date range.
        Multiple criteria are combined: emails must match all specified criteria.
        At least one criterion must be provided.
        Results are ordered from newest to oldest.
        Use this tool for requests such as finding emails about a job interview,
        messages mentioning Docker, or emails received during a specific period.
        """
    )
    public List<LastMailsResponse> searchEmails(
        @ToolParam(
            description = "Optional subject text to search for. Use null if not specified."
        )
        String subject,

        @ToolParam(
            description = "Optional word or phrase to search for in the email body. Use null if not specified."
        )
        String body,

        @ToolParam(
            description = "Optional sender email address or text to match against the sender. Use null if not specified."
        )
        String sender,

        @ToolParam(
            description = "Optional start date for received emails, inclusive, in ISO format YYYY-MM-DD. Use null if not specified."
        )
        LocalDate fromDate,

        @ToolParam(
            description = "Optional end date for received emails, inclusive, in ISO format YYYY-MM-DD. Use null if not specified."
        )
        LocalDate toDate,

        @ToolParam(
            description = "Maximum number of emails to return, between 1 and 100. Use 5 by default."
        )
        int limit
    ) {
        return _mailService.searchEmails(
            subject,
            body,
            sender,
            fromDate,
            toDate,
            limit
        );
    }
}
