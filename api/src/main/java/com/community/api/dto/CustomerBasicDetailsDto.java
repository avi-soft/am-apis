package com.community.api.dto;

import com.community.api.entity.QualificationDetails;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    @JsonProperty("secondary_mobile_number")
    String secondaryMobileNumber;
    @JsonProperty("current_district_id")
    Integer current_district_id;
    @JsonProperty("current_state_id")
    Integer current_state_id;
    @JsonProperty("permanent_district_id")
    Integer permanent_district_id;
    @JsonProperty("permanent_state_id")
    Integer permanent_state_id;
    @JsonProperty("qualification_detail_ids")
    List<Long> qualification_detail_ids= new ArrayList<>();
    @JsonProperty("qualification_ids")
    List<Integer> qualification_ids= new ArrayList<>();
    @JsonProperty("profileComplete")
    Boolean profileComplete;
    @JsonProperty("suspended")
    Boolean suspended;
    @JsonProperty("created_date")
    Date createdDate;
    @JsonProperty("updated_date")
    Date updatedDate;
}
