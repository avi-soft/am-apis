package com.community.api.services;

import com.community.api.entity.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

import static com.community.api.endpoint.avisoft.controller.Admin.SchedulerController.*;

@Service
public class BackupService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    @Qualifier("blMailSender")
    private JavaMailSender mailSender;

    @Value("${email.from}")
    private String fromEmail;

    @Autowired
    private RestTemplate restTemplate;
    private static final Long SCHEDULE_ID = 4L;
    private static final String TARGET_IP = "192.168.20.106";
    private static final String BACKUP_ENDPOINT = "http://" + TARGET_IP + ":8081/run-backup";
    @Async
    @Transactional
    public CompletableFuture<Void> triggerBackupAsync() {

        Schedule schedule = entityManager.find(Schedule.class, 4L);

        if (schedule == null) {
            System.err.println("Schedule not found for ID " + 4L);
            return CompletableFuture.completedFuture(null);
        }

        try {
            String response = restTemplate.getForObject(BACKUP_ENDPOINT, String.class);
            System.out.println("Response from " + TARGET_IP + ": " + response);

            boolean success = response != null && response.contains("exit code: 0");

            schedule.setLastRun(LocalDateTime.now());
            schedule.setLastRunPass(success);
            entityManager.merge(schedule);

        } catch (Exception e) {
            System.err.println("Backup trigger failed: " + e.getMessage());

            schedule.setLastRun(LocalDateTime.now());
            schedule.setLastRunPass(false);
            entityManager.merge(schedule);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Async
    public void sendFailureMail(String scheduleName,String desc, LocalDateTime lastRun, LocalDateTime nextRun) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            String formattedLastRun = lastRun != null ? lastRun.format(formatter) : "N/A";
            String formattedNextRun = nextRun != null ? nextRun.format(formatter) : "N/A";

            String subject = "Schedule Failure Alert: " + scheduleName;

            String htmlContent = String.format(
                    """
                    <html>
                        <body style="font-family: 'Segoe UI', Arial, sans-serif; background-color: #f8f9fa; padding: 20px; color: #333; line-height: 1.6;">
                            <div style="max-width: 700px; margin: auto; background-color: #ffffff; border-radius: 8px; padding: 30px; box-shadow: 0 4px 12px rgba(0,0,0,0.05);">
                                <div style="text-align: center; margin-bottom: 25px;">
                                    <img src="https://static.vecteezy.com/system/resources/previews/007/048/183/non_2x/fail-red-grunge-stamp-fail-stamp-or-label-illustration-vector.jpg" alt="Alert Icon" width="100" style="margin-bottom: 20px;" />
                                    <h2 style="color: #e74c3c; margin: 10px 0; font-size: 24px;">Schedule Execution Issue</h2>
                                    <p style="color: #7f8c8d; margin: 0;">Action Required: Schedule Failed to Complete</p>
                                </div>
                                
                                <div style="background-color: #f9f9f9; border-left: 4px solid #e74c3c; padding: 15px; margin: 20px 0;">
                                    <p style="margin: 0; font-weight: 500;">The system encountered an issue while executing this scheduled task. Please review the details below.</p>
                                </div>
                                
                                <table style="width: 100%%; border-collapse: collapse; margin: 25px 0;">
                                    <tr>
                                        <td style="padding: 12px 0; border-bottom: 1px solid #eee; font-weight: 600; width: 40%%;">Schedule Name</td>
                                        <td style="padding: 12px 0; border-bottom: 1px solid #eee;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 12px 0; border-bottom: 1px solid #eee; font-weight: 600;">Description</td>
                                        <td style="padding: 12px 0; border-bottom: 1px solid #eee;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 12px 0; border-bottom: 1px solid #eee; font-weight: 600;">Last Run Time</td>
                                        <td style="padding: 12px 0; border-bottom: 1px solid #eee;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 12px 0; font-weight: 600;">Next Scheduled Run</td>
                                        <td style="padding: 12px 0;">%s</td>
                                    </tr>
                                </table>
                                
                                <div style="margin: 30px 0 20px;">
                                    <p style="margin: 0 0 15px;">Recommended actions:</p>
                                    <ul style="margin: 0; padding-left: 20px;">
                                        <li style="margin-bottom: 8px;">Review the logs.</li>
                                        <li style="margin-bottom: 8px;">Verify System up-time.</li>
                                        <li style="margin-bottom: 8px;">Check for internet issues if any.</li>
                                    </ul>
                                </div>
                                
                                <div style="text-align: center; margin-top: 30px; color: #95a5a6; font-size: 14px; border-top: 1px solid #eee; padding-top: 20px;">
                                    <p style="margin: 5px 0;">Need assistance? Contact Development Team</p>
                                    <p style="margin: 5px 0;">Monitoring System • %tF</p>
                                </div>
                            </div>
                        </body>
                    </html>
                    """,
                    scheduleName, desc, formattedLastRun, formattedNextRun, LocalDate.now()
            );
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo("kshtjvats@gmail.com");
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = isHtml

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}