package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Priority;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@JsonPropertyOrder({"txnId"})
@Entity
@Table(name = "earnings")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Earnings{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
            @JsonProperty("txnId")
    Long id;

    @Column(name = "order_id")
    Long orderId;
    @Column(name = "carry_over", columnDefinition = "DOUBLE PRECISION DEFAULT 0")
    private Double carryOver;
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

    @Column(name = "order_amount")
    Long orderAmount;

    @Column(name = "date")
    Date date;
    @Column(name = "settled",columnDefinition = "BOOLEAN DEFAULT FALSE")
    boolean settled;


}
