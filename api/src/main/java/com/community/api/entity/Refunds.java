package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Refunds {
    @Id
    @Column(name = "order_id")
    Long orderId;
    @Column(name = "payment_id")
    String paymentId;
    @Column(name = "refund_amount")
    Double refundAmount;
    @Column(name = "success",columnDefinition = "BOOLEAN DEFAULT FALSE")
    Boolean refundSuccess;
    @Column(name = "refund_id")
    String refundId;
    @Column(name = "generated_at")
    Date generatedAt;
    @Column(name = "modified_at")
    Date modifiedAt;
    @Column(name = "refund_state")
    String refundState;
}
