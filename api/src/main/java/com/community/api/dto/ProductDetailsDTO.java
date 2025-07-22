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
    @JsonProperty("meta_title")
    String metaTitle;
    @JsonProperty("product_id")
    Long id;
    @JsonProperty("display_template")
    String displayTemplate;
    Double fee;
    @JsonProperty("age_limit")
    String ageLimit;
    @JsonProperty("active_end_date")
    Date activeEndDate;
    @JsonProperty("active_start_date")
    Date activeStartDate;
    @JsonProperty("total_vacancies_in_product")
    Long totalVacanicies;
}
