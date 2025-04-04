package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "bank_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BankDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

/*    @Column(name = "name", nullable = false)
    private String name;*/

    @Column(name = "role", nullable = false)
    private Integer role;

    @Column(name = "account_number", nullable = false, unique = false)
    private String accountNumber;

    @Column(name = "account_holder", nullable = false)
    private String accountHolder;


    @Column(name = "ifsc_code", nullable = false)
    private String ifscCode;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "branch_name", nullable = false)
    private String branchName;

    @Column(name = "account_type", nullable = false)
    private String accountType;

    @Column(name = "upi_id", nullable = false,unique =false)
    private String upiId;

}

