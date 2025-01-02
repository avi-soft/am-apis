package com.community.api.entity;

import com.community.api.entity.CustomGender;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("reserve_category_id")
    Long reserveCategory;
    @Nullable
    @JsonProperty("born_before")
    Date bornBefore;
    @Nullable
    @JsonProperty("born_after")
    Date bornAfter;
    @Nullable
    @JsonProperty("as_of_date")
    Date asOfDate;
    @Nullable
    @JsonProperty("minimum_age")
    Integer minAge;
    @Nullable
    @JsonProperty("maximum_age")
    Integer maxAge;
    @JsonProperty("gender_id")
    Long gender;
}