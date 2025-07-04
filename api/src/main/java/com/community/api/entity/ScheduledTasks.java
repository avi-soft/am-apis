package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ScheduledTasks {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "schedule_id")
    private Long scheduleId;
    @Column(name = "schedule_name")
    private String scheduleName;
    @Column(name = "last_run")
    private Date lastRun;
    @Column(name = "last_run_passed?")
    private Boolean lastRunPass;
    @Column(name = "api_path")
    private String apiPath;
}
