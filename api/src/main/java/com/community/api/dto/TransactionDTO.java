package com.community.api.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TransactionDTO {
    private Long userId;
    private Double amountToSettle;
    private List<Long> txnIds;
    private String settlementRemarks;
}