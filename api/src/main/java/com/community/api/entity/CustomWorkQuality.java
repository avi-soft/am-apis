package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "custom_work_quality")
public class CustomWorkQuality {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "work_quality_id")
    @JsonProperty("work_quality_id")
    protected Long workQualityId;

    @Column(name = "work_quality")
    @JsonProperty("work_quality")
    protected String workQuality;

    @Column(name = "work_quality_description")
    @JsonProperty("work_quality_description")
    protected String workQualityDescription;

}