package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;

@Getter
@Setter
public class CategoryDistributionDto {

    private Long id;
    private Long categoryId;
    private Integer categoryVacancies;
    private String additionalComment;
    @JsonProperty("male_vacancy")
    Long maleVacancy;
    @JsonProperty("female_vacancy")
    Long femaleVacancy;
    @JsonProperty("total_vacancy")
    Long totalVacancy;
}
