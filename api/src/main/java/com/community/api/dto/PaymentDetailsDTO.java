package com.community.api.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PaymentDetailsDTO {
    Long userId;
    String name;
    String address;
    double thisMonthPayable;
    double lastMonthPayable;
    double totalBalance;
}