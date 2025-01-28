package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReserveCategoryAgeDto {
    @JsonProperty("reserve_category_id")
    Long reserveCategoryId;
    @JsonProperty("post")
    Integer post;
    @JsonProperty("reserve_category")
    String reserveCategory;
    @JsonProperty("born_before_after")
    Boolean bornBeforeAfter;
    @JsonProperty("born_before")
     Date bornBefore;
     @JsonProperty("born_after")
     Date bornAfter;
    @JsonProperty("gender_id")
    Long genderId;
    @JsonProperty("gender_name")
    String genderName;
    @JsonProperty("min_age")
    Integer minAge;
    @JsonProperty("max_age")
    Integer maxAge;
    @JsonProperty("as_of_date")
    Date asOfDate;
}
