package com.community.api.entity;

import com.community.api.entity.CustomGender;
import com.community.api.utils.CustomDateDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.micrometer.core.lang.Nullable;
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
public class AddProductAgeDTO {
    @JsonProperty("born_before_after")
    Boolean bornBeofreAfter;
    @JsonProperty("reserve_category_id")
    Long reserveCategory;
    @Nullable
    @JsonProperty("born_before")
    Date bornBefore;
    @JsonProperty("category_running_field")
    String categoryRunningField;
    @JsonProperty("gender_running_field")
    String genderRunningField;
    @Nullable
    @JsonProperty("born_after")
    Date bornAfter;
    @Nullable
    @JsonProperty("as_of_date")
    String asOfDate;
    @Nullable
    @JsonProperty("minimum_age")
    Integer minAge;
    @Nullable
    @JsonProperty("maximum_age")
    Integer maxAge;
    @JsonProperty("gender_id")
    Long gender;
    @JsonIgnore
    private String additionalComments;
}