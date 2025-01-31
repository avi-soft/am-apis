package com.community.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerBasicDetailsDto {
    Long customerId;
    String fullName;
    String State;
    String email;
    String phone;
    String gender;
    String username;
    String primaryRef;
    Long primaryRefId;
    String phoneNumber;
}
