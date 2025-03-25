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
@Table(name = "earnings")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Earnings{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column(name = "order_id")
    Long orderId;
    @Column(name = "provider_id")
    Long providerId;
    @Column(name = "ticket_id")
    Long ticketId;

    @Column(name = "platform_fee")
    Double platformFee;

    @Column(name = "commission")
    Double commission;

    @Column(name = "paid")
    Double paid;

    @Column(name = "pending")
    Double pending;

    @Column(name = "payment_done")
    Boolean paymentDone;

    @Column(name = "date")
    Date date;
    @Column(name = "settled",columnDefinition = "BOOLEAN DEFAULT FALSE")
    boolean settled;


}
