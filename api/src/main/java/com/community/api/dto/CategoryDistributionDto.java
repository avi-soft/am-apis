package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDistributionDto {

    private Long id;
    private Long categoryId;
    private String categoryRunningField;
    @JsonProperty("categoryVacancies")
    private Integer vacancyCount;
    private Boolean isStateLevelCategory;
    private String stateLevelCategory;
    private String additionalComment;
    private Boolean isGenderWise;
    private Integer stateId;
    @JsonProperty("maleVacancy")
    Long maleVacancy;
    @JsonProperty("femaleVacancy")
    Long femaleVacancy;
    @JsonProperty("totalVacancy")
    Long totalVacancy;
}
