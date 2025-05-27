package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductDetailsDTO {
    String metaTitle;
    Long id;
    String displayTemplate;
    Double fee;
    String ageLimit;
    @JsonProperty("active_end_date")
    Date activeEndDate;
    @JsonProperty("active_start_date")
    Date activeStartDate;
}
