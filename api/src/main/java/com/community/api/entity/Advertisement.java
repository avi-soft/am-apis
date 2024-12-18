package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "advertisement")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Advertisement {

    @Column(name="advertisement_id")
    @JsonProperty("advertisement_id")
    private Long advertisementId;

    @Column(name = "number")
    @JsonProperty("number")
    private Long number;

    @Column(name = "title")
    @JsonProperty("title")
    private String title;

    @Column(name = "description")
    @JsonProperty("description")
    private String description;

    @Column(name = "created_date")
    @JsonProperty("created_date")
    private Date createdDate;

    @Column(name = "creator_user_id")
    @JsonProperty("creator_user_id")
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "creator_role_id")
    @JsonProperty("creator_role_id")
    private Role creatorRole;

    @Column(name = "modified_date")
    @JsonProperty("modified_date")
    private Date modifiedDate;

    @Column(name = "modifier_user_id")
    @JsonProperty("modifier_user_id")
    private Long modifierId;

    @ManyToOne
    @JoinColumn(name = "modifier_role_id")
    @JsonProperty("modifier_role_id")
    private Role modifierRole;

    @Column(name = "active_start_date")
    @JsonProperty("active_start_date")
    private Date activeStartDate;

    @Column(name = "active_end_date")
    @JsonProperty("active_end_date")
    private Date activeEndDate;

    @Column(name = "url")
    @JsonProperty("url")
    private String url;
}