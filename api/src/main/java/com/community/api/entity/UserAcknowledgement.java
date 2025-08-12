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
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "user_acknowledgement")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserAcknowledgement {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "acknowledgement_id")
    Long acknowledgementId;
    @Column(name = "acknowledged_by")
    Long userId;
    @Column(name = "acknowledged_at")
    Date acknowledgedAt;
    @Column(name = "acknowledgement_version")
    String acknowledgementVersion;
}
