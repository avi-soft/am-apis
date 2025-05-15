package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReserveCategoryDto {
    @JsonProperty("product_id")
    Long productId;
    @JsonProperty("reserve_category_id")
    Long reserveCategoryId;
    @JsonProperty("running_field")
    String runningField;
    @JsonProperty("fee")
    Double fee;
    @JsonProperty("post")
    Integer post;
    @JsonProperty("reserve_category")
    String reserveCategory;
    @JsonProperty("gender_id")
    Long genderId;
    @JsonProperty("gender_running_field")
    String genderRunningField;
    @JsonProperty("gender_name")
    String genderName;
    @JsonProperty("fee_additional_comments")
    String additionalComments;
    @JsonProperty("is_other_or_state_category")
    Boolean isOtherOrStateCategory;
    @JsonProperty("other_or_state_category")
    String otherOrStateCategory;
}
