package com.community.api.entity;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Transaction {
    @Id
    @GeneratedValue
    @Column(name = "transaction_id")
    Long txnId;

    @Column(name = "current_month_payable")
    Double currentMonthPayable;

    @Column(name = "last_month_payable")
    Double lastMonthPayable;

    @Column(name = "settled_amount")
    Double settledAmount;

    @Column(name = "balance")
    Double balance;

    @Column(name = "settlement_remarks")
    String settlementRemarks;

    @Column(name = "user_id")
    Long userId;

    @Column(name = "role")
    Integer role;

    @Column(name = "date")
    Date date;
}