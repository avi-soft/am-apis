package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
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

    private LocalDate timeStamp;

}
