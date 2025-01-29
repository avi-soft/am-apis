package com.community.api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

    public void sendEmail(String to, CommunicationContent content) throws MessagingException {
        // Validate required fields
        validateEmailContent(content);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(content.getSubject());

        // Set content text if available
        if (!StringUtils.isEmpty(content.getContentText())) {
            helper.setText(content.getContentText(), true); // true enables HTML content
        } else {
            helper.setText(" "); // Empty space as we have attachments
        }

        // Add attachments if available
        if (content.getContentFiles() != null && !content.getContentFiles().isEmpty()) {
            for (ContentFile contentFile : content.getContentFiles()) {
                FileSystemResource file = new FileSystemResource(new File(contentFile.getFilePath()));
                helper.addAttachment(contentFile.getFileName(), file);
            }
        }

        mailSender.send(message);
    }

    private void validateEmailContent(CommunicationContent content) {
        if (content == null) {
            throw new IllegalArgumentException("Communication content cannot be null");
        }

        // Check if subject is present (mandatory)
        if (StringUtils.isEmpty(content.getSubject())) {
            throw new IllegalArgumentException("Email subject is mandatory");
        }

        // Check if either content text or files are present
        boolean hasContentText = !StringUtils.isEmpty(content.getContentText());
        boolean hasFiles = content.getContentFiles() != null && !content.getContentFiles().isEmpty();

        if (!hasContentText && !hasFiles) {
            throw new IllegalArgumentException("Either content text or file attachments must be provided");
        }
    }

    private void sendSimpleEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

}
