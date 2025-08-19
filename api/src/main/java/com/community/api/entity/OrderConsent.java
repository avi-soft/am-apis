package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name ="order_consent")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderConsent {
    @Column(name = "consent_id")
    String ackId;
    @Column(name = "order_id")
    Long orderId;
    @Column(name = "user_id")
    Long userId;
    @Column(name = "timestamp")
    Date timeStamp;
}
