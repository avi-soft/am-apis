package com.community.api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import com.community.api.entity.CommunicationContent;
import com.community.api.entity.ContentFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.StringUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;

@Service
public class EmailService {

    @Autowired
    @Qualifier("blMailSender")
    private JavaMailSender mailSender;

    @Value("${email.from}")
    private String fromEmail;

    public void sendExpirationEmail(String to, String customerFirstName, String customerLastName) throws IOException {
        String template = loadTemplate("email-templates/expiration-email.txt");
        String messageBody = template
                .replace("{firstName}", customerFirstName)
                .replace("{lastName}", customerLastName);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        // @Todo :- need to set subject dynamically
        message.setSubject("Your Application Form is About to Expire");
        message.setText(messageBody);
        mailSender.send(message);
    }

    private String loadTemplate(String templateName) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(templateName)) {
            if (inputStream == null) {
                throw new IOException("Template file not found: " + templateName);
            }
            Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name());
            return scanner.useDelimiter("\\A").next();
        }
    }
    public void sendEmailWithAttachments(List<String> recipients, String subject, String content, List<File> attachments) throws MessagingException {
        if (recipients == null || recipients.isEmpty()) {
            throw new MessagingException("Recipients list cannot be empty");
        }

        String[] recipientArray = recipients.toArray(new String[0]);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setBcc(recipientArray);

            // Set empty string if subject is null
            helper.setSubject(subject == null ? "" : subject);

            // Set empty string if content is null
            helper.setText(content == null ? "" : content);

            // Add attachments
            if (attachments != null) {
                for (File file : attachments) {
                    if (file != null && file.exists()) {
                        helper.addAttachment(file.getName(), file);
                    }
                }
            }

            mailSender.send(message);
            System.out.println("Email Semt");
        } catch (MessagingException e) {
            throw new MessagingException("Failed to send email: " + e.getMessage(), e);
        }
    }

}
