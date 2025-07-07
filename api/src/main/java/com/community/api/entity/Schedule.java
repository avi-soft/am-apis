package com.community.api.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "schedule_name", length = 255)
    private String scheduleName;

    @Column(name = "description")
    private String scheduleDesc;

    @Column(name = "last_run")
    private LocalDateTime lastRun;

    @Column(name = "next_scheduled_run")
    private LocalDateTime nextScheduledRun;

    @Column(name = "last_run_pass")
    private Boolean lastRunPass;

    @Column(name = "api_path", length = 255)
    private String apiPath;

    @Column(name = "frequency", length = 255)
    private String frequency;

    @Column(name = "call_manual_email_trigger", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean emailTrigger;

}
