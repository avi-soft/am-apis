package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "acknowledgement_reference")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AckRef {
    @Id
    @Column(name = "ack_ref")
    String ackRef;
    @Column(name = "roleId")
    Integer roleId;
    @Column(name = "user_id")
    Long userId;
}
