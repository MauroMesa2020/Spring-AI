package com.agent.mailservice.service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.agent.mailservice.dto.LastMailsResponse;
import com.agent.mailservice.dto.MailRequest;
import com.agent.mailservice.dto.MailResponse;

import jakarta.mail.Address;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.search.FromTerm;
import jakarta.mail.search.SearchTerm;
import lombok.extern.slf4j.Slf4j;

// para extraer contenidos

import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import org.jsoup.Jsoup;

@Service 
@Slf4j 
public class MailService {

    @Autowired 
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String from;

    @Value("${mail.imap.host}")
    private String imapHost;

    @Value("${mail.imap.port}")
    private int imapPort;

    @Value("${mail.imap.username}")
    private String imapUsername;

    @Value("${mail.imap.password}")
    private String imapPassword;


    public MailResponse sender(MailRequest mailRequest) {

        log.info("Starting email send process. Recipient: {}", mailRequest.getTo());

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(mailRequest.getTo());
            message.setSubject(mailRequest.getSubject());
            message.setText(mailRequest.getBody());

            mailSender.send(message);

            log.info("Email sent successfully to {}", mailRequest.getTo());

            MailResponse response = new MailResponse();
            response.setTo(mailRequest.getTo());
            response.setSubject(mailRequest.getSubject());
            response.setBody(mailRequest.getBody());
            response.setSuccess(true);

            return response;

        } catch (Exception error) {
            log.error("Failed to send email to {}", mailRequest.getTo(), error);
            throw new RuntimeException("Error sending email", error);
        }
    }

    private String extractBody(Part message) throws Exception {

        StringBuilder plainText = new StringBuilder();
        StringBuilder htmlText = new StringBuilder();

        collectText(message, plainText, htmlText);

        // Preferir el texto plano cuando esté disponible.
        if (!plainText.isEmpty()) {
            return plainText.toString().trim();
        }

        // Si no existe texto plano, convertir el HTML a texto.
        if (!htmlText.isEmpty()) {
            return Jsoup.parse(htmlText.toString()).text();
        }

        return "";
    }

    private void collectText(
            Part part,
            StringBuilder plainText,
            StringBuilder htmlText
    ) throws Exception {

        // No procesar archivos adjuntos.
        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())
                || part.getFileName() != null) {
            return;
        }

        if (part.isMimeType("text/plain")) {

            Object content = part.getContent();

            if (content instanceof String text) {
                plainText.append(text).append("\n");
            }

        } else if (part.isMimeType("text/html")) {

            Object content = part.getContent();

            if (content instanceof String html) {
                htmlText.append(html).append("\n");
            }

        } else if (part.isMimeType("multipart/*")) {

            Object content = part.getContent();

            if (content instanceof Multipart multipart) {

                for (int i = 0; i < multipart.getCount(); i++) {

                    BodyPart bodyPart = multipart.getBodyPart(i);

                    collectText(bodyPart, plainText, htmlText);
                }
            }
        }
    }

    public List<LastMailsResponse> getLast(int number){
        

        if (number < 1 || number > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Number must be between 1 and 100"
            );
        }

        Store store = null;
        Folder inbox = null;

        try{

            Properties properties = new Properties();
            properties.put("mail.store.protocol", "imaps");
            properties.put("mail.imaps.host", imapHost);
            properties.put("mail.imaps.port", imapPort);
            properties.put("mail.imaps.ssl.enable", "true");

            Session session = Session.getInstance(properties);

            store = session.getStore("imaps");
            store.connect(imapHost, imapUsername, imapPassword);

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            int totalMessages = inbox.getMessageCount();

            if (totalMessages == 0) {
                return new ArrayList<>();
            }

            // Determinar el primer mensaje que necesitamos recuperar.
            int start = Math.max(1, totalMessages - number + 1);

            Message[] messages = inbox.getMessages(
                    start,
                    totalMessages
            );

            List<LastMailsResponse> result = new ArrayList<>();

            // Recorrer desde el mensaje más reciente.
            for (int i = messages.length - 1; i >= 0; i--){
                
                Message message = messages[i];

                LastMailsResponse response = new LastMailsResponse();

                Address[] senders = message.getFrom();

                if (senders != null && senders.length > 0) {
                    response.setFrom(
                            senders[0].toString()
                    );
                }

                response.setSubject(message.getSubject());

                if (message.getReceivedDate() != null) {
                    response.setReceivedAt(
                            message.getReceivedDate()
                                    .toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDateTime()
                    );
                }

                response.setBody(truncateBody(extractBody(message), 3000));

                result.add(response);
            }

        
            log.info("Retrieved {} emails", result.size());

            return result;


        }catch(Exception error){
            log.error("Error retrieving emails", error);

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error retrieving emails"
            );
        }finally{

            // Para close conexion

            try {
                if (inbox != null && inbox.isOpen()) {
                    inbox.close(false);
                }
            } catch (MessagingException error) {
                log.warn("Error closing inbox", error);
            }

            try {
                if (store != null && store.isConnected()) {
                    store.close();
                }
            } catch (MessagingException error) {
                log.warn("Error closing mail connection", error);
            }
        }

    }

    public List<LastMailsResponse> getLastFromSender(String sender, int number) 
    {

        if (sender == null || sender.isBlank() || !sender.contains("@")) {
            throw new IllegalArgumentException("Invalid sender email");
        }

        if (number < 1 || number > 100) {
            throw new IllegalArgumentException(
                "Number must be between 1 and 10"
            );
        }

        Properties properties = new Properties();
        properties.put("mail.imaps.host", imapHost);
        properties.put("mail.imaps.port", imapPort);
        properties.put("mail.imaps.ssl.enable", "true");

        Store store = null;
        Folder inbox = null;

        try {
            Session session = Session.getInstance(properties);

            store = session.getStore("imaps");
            store.connect(imapHost, imapUsername, imapPassword);

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            SearchTerm searchTerm =
                new FromTerm(new InternetAddress(sender));

            Message[] messages = inbox.search(searchTerm);

            // Ordenar del más reciente al más antiguo.
            Arrays.sort(messages, (a, b) -> {
                try {
                    Date dateA = a.getReceivedDate();
                    Date dateB = b.getReceivedDate();

                    if (dateA == null) {
                        dateA = a.getSentDate();
                    }
                    if (dateB == null) {
                        dateB = b.getSentDate();
                    }

                    if (dateA == null) return dateB == null ? 0 : 1;
                    if (dateB == null) return -1;

                    return dateB.compareTo(dateA);
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            });

            List<LastMailsResponse> result = new ArrayList<>();

            int limit = Math.min(number, messages.length);

            for (int i = 0; i < limit; i++) {
                Message message = messages[i];

                LastMailsResponse response = new LastMailsResponse();

                Address[] fromAddresses = message.getFrom();

                response.setFrom(
                    fromAddresses != null && fromAddresses.length > 0
                        ? fromAddresses[0].toString()
                        : sender
                );

                response.setSubject(message.getSubject());

                try{
                    response.setBody(truncateBody(extractBody(message), 3000));
                }catch(Exception error){
                    throw new RuntimeException(
                        "Error retrieving emails from " + sender, error
                    );
                }

                Date receivedDate = message.getReceivedDate();

                if (receivedDate == null) {
                    receivedDate = message.getSentDate();
                }

                if (receivedDate != null) {
                    response.setReceivedAt(
                        receivedDate.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
                    );
                }

                result.add(response);
            }

            return result;

        } catch (MessagingException e) {
            throw new RuntimeException(
                "Error retrieving emails from " + sender, e
            );
        } finally {
            try {
                if (inbox != null && inbox.isOpen()) {
                    inbox.close(false);
                }
            } catch (MessagingException ignored) {
            }

            try {
                if (store != null && store.isConnected()) {
                    store.close();
                }
            } catch (MessagingException ignored) {
            }
        }
    }

    private String truncateBody(String body, int maxChars) {
        if (body == null) {
            return "";
        }

        if (body.length() <= maxChars) {
            return body;
        }

        return body.substring(0, maxChars) + "...";
    }
}
