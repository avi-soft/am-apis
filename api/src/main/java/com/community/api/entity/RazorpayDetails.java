package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "razorpay_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RazorpayDetails{
    @Id
    private Long orderId;

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private String status;

    private LocalDateTime timeStamp;

    @Column(name = "verified",columnDefinition = "BOOLEAN DEFAULT FALSE")
    public Boolean verified=false;

}
