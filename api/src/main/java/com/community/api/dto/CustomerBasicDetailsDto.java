package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerBasicDetailsDto {
    @JsonProperty("customer_id")
    Long customerId;
    @JsonProperty("full_name")
    String fullName;
    @JsonProperty("state_name")
    String State;
    @JsonProperty("email")
    String email;
    @JsonProperty("phone_number")
    String phone;
    @JsonProperty("gender")
    String gender;
    @JsonProperty("highest_qualification")
    String highestQualification;
    @JsonProperty("username")
    String username;
    @JsonProperty("primary_referrer_name")
    String primaryRef;
    @JsonProperty("primary_referrer_id")
    Long primaryRefId;
    @JsonProperty("age")
    Integer age;
}
