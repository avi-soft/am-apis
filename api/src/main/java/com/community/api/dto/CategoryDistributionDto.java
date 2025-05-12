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
    @JsonProperty("maleVacancy")
    Long maleVacancy;
    @JsonProperty("femaleVacancy")
    Long femaleVacancy;
    @JsonProperty("totalVacancy")
    Long totalVacancy;
}
