package com.community.api.endpoint.avisoft.controller.Admin;
import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.entity.Schedule;
import com.community.api.services.BackupService;
import com.community.api.services.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@RestController
@RequestMapping("/schedule")
public class SchedulerController {

    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    BackupService backupService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${db.backup.path}")
    private String backupBasePath;

    @Value("${db.backup.username}")
    private String username;

    @Value("${db.backup.password}")
    private String password;

    @Value("${db.backup.db-name}")
    private String dbName;

    @Authorize(value = {Constant.roleSuperAdmin,Constant.roleAdmin})
    @Transactional
    @PutMapping("/backup-db")
    public ResponseEntity<?> callDBBackup() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dMMM");
        String formattedDate = LocalDate.now().format(formatter);

        String backupPath = backupBasePath+ formattedDate + ".dump";
 /*       String dbName = "postgres";
        String username = "postgres";
        String password = "1234";*/

        ProcessBuilder processBuilder = new ProcessBuilder(
                "pg_dump",
                "-U", username,
                "-F", "c",
                "-f", backupPath,
                dbName
        );

        processBuilder.environment().put("PGPASSWORD", password);
        processBuilder.directory(new File("C:/Program Files/PostgreSQL/16/bin"));

        Schedule schedule = entityManager.find(Schedule.class, 3L);
        if (schedule == null) {
            return ResponseService.generateErrorResponse("Schedule not found", HttpStatus.NOT_FOUND);
        }

        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            LocalDateTime now = LocalDateTime.now();
            if (exitCode == 0) {
                schedule.setLastRun(now);
                schedule.setLastRunPass(true);
                LocalDateTime nextMidnight = now.plusDays(1).toLocalDate().atStartOfDay();
                schedule.setNextScheduledRun(nextMidnight);
                entityManager.merge(schedule);
                // ✅ Trigger Email Alert
                return ResponseService.generateErrorResponse("DB backed up successfully", HttpStatus.OK);
            } else {
                schedule.setLastRun(now);
                schedule.setLastRunPass(false);
                schedule.setNextScheduledRun(now.plusHours(1));
                entityManager.merge(schedule);

                // ✅ Trigger Email Alert
                backupService.sendFailureMail(schedule.getScheduleName(),schedule.getScheduleDesc(), now, now.plusDays(1).toLocalDate().atStartOfDay());

                return ResponseService.generateErrorResponse("Backup failed with exit code: " + exitCode, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (IOException | InterruptedException e) {
            LocalDateTime now = LocalDateTime.now();
            schedule.setLastRun(now);
            schedule.setLastRunPass(false);
            entityManager.merge(schedule);

            // ✅ Trigger Email Alert
            backupService.sendFailureMail(schedule.getScheduleName(),schedule.getScheduleDesc(), now, now.plusDays(1).toLocalDate().atStartOfDay());

            return ResponseService.generateErrorResponse("Backup process failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public ResponseEntity<?> callDBBackupCron() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dMMM");
        String formattedDate = LocalDate.now().format(formatter);

        String backupPath = backupBasePath+ formattedDate + ".dump";
 /*       String dbName = "postgres";
        String username = "postgres";
        String password = "1234";*/

        ProcessBuilder processBuilder = new ProcessBuilder(
                "pg_dump",
                "-U", username,
                "-F", "c",
                "-f", backupPath,
                dbName
        );

        processBuilder.environment().put("PGPASSWORD", password);
        processBuilder.directory(new File("C:/Program Files/PostgreSQL/13/bin"));

        Schedule schedule = entityManager.find(Schedule.class, 3L);
        if (schedule == null) {
            return ResponseService.generateErrorResponse("Schedule not found", HttpStatus.NOT_FOUND);
        }

        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            LocalDateTime now = LocalDateTime.now();
            if (exitCode == 0) {
                schedule.setLastRun(now);
                schedule.setLastRunPass(true);
                LocalDateTime nextMidnight = now.plusDays(1).toLocalDate().atStartOfDay();
                schedule.setNextScheduledRun(nextMidnight);
                entityManager.merge(schedule);
                // ✅ Trigger Email Alert
                return ResponseService.generateErrorResponse("DB backed up successfully", HttpStatus.OK);
            } else {
                schedule.setLastRun(now);
                schedule.setLastRunPass(false);
                schedule.setNextScheduledRun(now.plusHours(1));
                entityManager.merge(schedule);

                // ✅ Trigger Email Alert
                backupService.sendFailureMail(schedule.getScheduleName(),schedule.getScheduleDesc(), now, now.plusDays(1).toLocalDate().atStartOfDay());

                return ResponseService.generateErrorResponse("Backup failed with exit code: " + exitCode, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (IOException | InterruptedException e) {
            LocalDateTime now = LocalDateTime.now();
            schedule.setLastRun(now);
            schedule.setLastRunPass(false);
            entityManager.merge(schedule);

            // ✅ Trigger Email Alert
            backupService.sendFailureMail(schedule.getScheduleName(),schedule.getScheduleDesc(), now, now.plusDays(1).toLocalDate().atStartOfDay());

            return ResponseService.generateErrorResponse("Backup process failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Authorize(value = {Constant.roleSuperAdmin,Constant.roleAdmin})
    @Transactional
    @Scheduled(cron = "0 */5 * * * *")
    @PutMapping("/update-product-states")
    public ResponseEntity<?> updateProductStates() {
        Schedule schedule=entityManager.find(Schedule.class,1L);
        LocalDateTime now = LocalDateTime.now();
        if(schedule==null)
        {
            System.out.println("Schedule not found");
            return ResponseService.generateErrorResponse("Schedule not found",HttpStatus.NOT_FOUND);
        }
        try {
            jdbcTemplate.execute("CALL update_all_product_states();");
            schedule.setLastRun(LocalDateTime.now());
            schedule.setLastRunPass(true);
            schedule.setNextScheduledRun(now.plusMinutes(5));
            entityManager.merge(schedule);
            System.out.println("Product states updated successfully.");
            return ResponseService.generateErrorResponse("Product states updated successfully.", HttpStatus.OK);
        } catch (Exception e) {
            schedule.setLastRun(LocalDateTime.now());
            schedule.setLastRunPass(false);
            schedule.setNextScheduledRun(now.plusMinutes(5));
            entityManager.merge(schedule);
            System.err.println("Error updating product states: " + e.getMessage());
            backupService.sendFailureMail(schedule.getScheduleName(),schedule.getScheduleDesc(), now, now.plusMinutes(5));
            return ResponseService.generateErrorResponse("Product update failed",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Transactional
    @Scheduled(cron = "0 */5 * * * *")
    public ResponseEntity<?> updateProductStatesCron() {
        Schedule schedule=entityManager.find(Schedule.class,1L);
        LocalDateTime now = LocalDateTime.now();
        if(schedule==null)
        {
            System.out.println("Schedule not found");
            return ResponseService.generateErrorResponse("Schedule not found",HttpStatus.NOT_FOUND);
        }
        try {
            jdbcTemplate.execute("CALL update_all_product_states();");
            schedule.setLastRun(LocalDateTime.now());
            schedule.setLastRunPass(true);
            schedule.setNextScheduledRun(now.plusMinutes(5));
            entityManager.merge(schedule);
            System.out.println("Product states updated successfully.");
            return ResponseService.generateErrorResponse("Product states updated successfully.", HttpStatus.OK);
        } catch (Exception e) {
            schedule.setLastRun(LocalDateTime.now());
            schedule.setLastRunPass(false);
            schedule.setNextScheduledRun(now.plusMinutes(5));
            entityManager.merge(schedule);
            System.err.println("Error updating product states: " + e.getMessage());
            backupService.sendFailureMail(schedule.getScheduleName(),schedule.getScheduleDesc(), now, now.plusMinutes(5));
            return ResponseService.generateErrorResponse("Product update failed",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Authorize(value = {Constant.roleSuperAdmin,Constant.roleAdmin})
    @Transactional
    @PutMapping("/run-auto-assigner")
    public ResponseEntity<?> runAutoAssigner() {
        LocalDateTime now = LocalDateTime.now();
        Schedule schedule = entityManager.find(Schedule.class, 2L); // Same schedule ID for both

        if (schedule == null) {
            return ResponseService.generateErrorResponse("Schedule not found", HttpStatus.NOT_FOUND);
        }

        boolean success = true;
        StringBuilder messageBuilder = new StringBuilder();

        try {
            // Run auto assigner
            entityManager.createNativeQuery("CALL run_auto_assigner_with_retries()").executeUpdate();
            messageBuilder.append("Auto Assigner executed successfully. ");
        } catch (Exception e) {
            backupService.sendFailureMail(schedule.getScheduleName(), schedule.getScheduleDesc(), now, calculateNextRun(now));
            success = false;
            messageBuilder.append("Auto Assigner failed: ").append(e.getMessage()).append(". ");
        }

        // Update schedule status
        schedule.setLastRun(now);
        schedule.setLastRunPass(success);
        schedule.setNextScheduledRun(calculateNextRun(now));
        entityManager.merge(schedule);
        if (success) {
            return ResponseService.generateErrorResponse(messageBuilder.toString(), HttpStatus.OK);
        } else {
            schedule.setNextScheduledRun(calculateNextRun(now));
            entityManager.merge(schedule);
            backupService.sendFailureMail(schedule.getScheduleName(),schedule.getScheduleDesc(), now, calculateNextRun(now));
            return ResponseService.generateErrorResponse(messageBuilder.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    private LocalDateTime calculateNextRun(LocalDateTime now) {
        LocalTime sevenThirtyAM = LocalTime.of(7, 30);
        LocalTime threeThirtyPM = LocalTime.of(15, 30);

        if (now.toLocalTime().isBefore(sevenThirtyAM)) {
            return now.toLocalDate().atTime(sevenThirtyAM);
        } else if (now.toLocalTime().isBefore(threeThirtyPM)) {
            return now.toLocalDate().atTime(threeThirtyPM);
        } else {
            // Schedule for next day's 7:30 AM
            return now.plusDays(1).toLocalDate().atTime(sevenThirtyAM);
        }
    }
    @Authorize(value = {Constant.roleSuperAdmin,Constant.roleAdmin})
    @Transactional
    @PutMapping("/run-re-ranking")
    public ResponseEntity<?> runReranking() {
        LocalDateTime now = LocalDateTime.now();
        Schedule schedule = entityManager.find(Schedule.class, 5L); // Same schedule ID for both

        if (schedule == null) {
            return ResponseService.generateErrorResponse("Schedule (id=5) not found", HttpStatus.NOT_FOUND);
        }

        boolean success = true;
        StringBuilder messageBuilder = new StringBuilder();

        try {
            // Run auto assigner
            entityManager.createNativeQuery("CALL run_re_ranking_with_retries()").executeUpdate();
            messageBuilder.append("Re-Ranking executed successfully. ");
        } catch (Exception e) {
            backupService.sendFailureMail(schedule.getScheduleName(),schedule.getScheduleDesc(), now, now.plusDays(1));
            success = false;
            messageBuilder.append("Re-Ranking failed: ").append(e.getMessage()).append(". ");
        }
        // Update schedule status
        schedule.setLastRun(LocalDateTime.now());
        schedule.setLastRunPass(success);
        LocalDate nextMonday = now.toLocalDate().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        schedule.setNextScheduledRun(nextMonday.atStartOfDay());
        entityManager.merge(schedule);

        if (success) {
            return ResponseService.generateErrorResponse(messageBuilder.toString(), HttpStatus.OK);
        } else {
            return ResponseService.generateErrorResponse(messageBuilder.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    private final RestTemplate restTemplate = new RestTemplate();



    private static final Long SCHEDULE_ID = 4L;
    private static final String TARGET_IP = "192.168.0.107";
    private static final String BACKUP_ENDPOINT = "http://" + TARGET_IP + ":8081/run-backup";



    @Authorize(value = {Constant.roleSuperAdmin,Constant.roleAdmin})
    @Scheduled(cron = "0 0 0 * * MON")
    @Transactional
    @PutMapping("file-server-backup")
    public ResponseEntity<?> triggerRemoteBackup() {
        Schedule schedule = entityManager.find(Schedule.class, 4L);
        LocalDateTime now = LocalDateTime.now();
        try {
        backupService.triggerBackupAsync();
        schedule.setLastRun(LocalDateTime.now());
        schedule.setLastRunPass(true);
        LocalDate nextMonday = now.toLocalDate().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        schedule.setNextScheduledRun(nextMonday.atStartOfDay()); // sets time to 00:00
        entityManager.merge(schedule);// this will run in background
        return ResponseService.generateErrorResponse(
                "File Server Backup has been triggered. Running in background.",
                HttpStatus.OK
        );
        }catch (Exception e)
        {
            backupService.sendFailureMail(schedule.getScheduleName(),schedule.getScheduleDesc(), now, now.toLocalDate().with(TemporalAdjusters.next(DayOfWeek.MONDAY)).atStartOfDay());
            return ResponseService.generateErrorResponse("Failed to backup File server",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Scheduled(cron = "0 0 0 * * MON")
    @Transactional
    public ResponseEntity<?> triggerRemoteBackupCron() {
        Schedule schedule = entityManager.find(Schedule.class, 4L);
        LocalDateTime now = LocalDateTime.now();
        try {
            backupService.triggerBackupAsync();
            schedule.setLastRun(LocalDateTime.now());
            schedule.setLastRunPass(true);
            LocalDate nextMonday = now.toLocalDate().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            schedule.setNextScheduledRun(nextMonday.atStartOfDay()); // sets time to 00:00
            entityManager.merge(schedule);// this will run in background
            return ResponseService.generateErrorResponse(
                    "File Server Backup has been triggered. Running in background.",
                    HttpStatus.OK
            );
        }catch (Exception e)
        {
            backupService.sendFailureMail(schedule.getScheduleName(),schedule.getScheduleDesc(), now, now.toLocalDate().with(TemporalAdjusters.next(DayOfWeek.MONDAY)).atStartOfDay());
            return ResponseService.generateErrorResponse("Failed to backup File server",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Authorize(value = {Constant.roleSuperAdmin,Constant.roleAdmin})
    @GetMapping("/all-schedules")
    public ResponseEntity<?> getAll() {
        try {
            List<Schedule> allProcesses = entityManager
                    .createQuery("SELECT s FROM Schedule s", Schedule.class)
                    .getResultList();

            return ResponseService.generateSuccessResponse("Schedules", allProcesses, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Error fetching schedules: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Authorize(value = {Constant.roleSuperAdmin,Constant.roleAdmin})
    @GetMapping("/{scheduleId}")
    public ResponseEntity<?> getProcessById(@PathVariable Long scheduleId) {
        try {
            if(scheduleId==null)
                return ResponseService.generateErrorResponse("Schedule id is required",HttpStatus.BAD_REQUEST);
            Schedule schedule=entityManager.find(Schedule.class,scheduleId);
            if(schedule==null)
                return ResponseService.generateErrorResponse("Schedule not found",HttpStatus.OK);
            return ResponseService.generateSuccessResponse("Schedule found", schedule, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Error fetching schedule: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Transactional
    @Scheduled(cron = "0 * * * * *") // every 1 min
    public void sendFailureEmail() {
        System.out.println("Triggering email for failed schedules");
        List<Schedule> allProcesses = entityManager
                .createQuery("SELECT s FROM Schedule s WHERE emailTrigger = true", Schedule.class)
                .getResultList();
        System.out.println("Number of failed schedules : "+allProcesses.size());
        for (Schedule schedule : allProcesses) {
            backupService.sendFailureMail(
                    schedule.getScheduleName(),
                    schedule.getScheduleDesc(),
                    schedule.getLastRun(),
                    schedule.getNextScheduledRun()
            );
            schedule.setEmailTrigger(false);
            entityManager.merge(schedule);
        }
    }
}
