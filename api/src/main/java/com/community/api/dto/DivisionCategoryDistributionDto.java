package com.community.api.dto;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;

@Getter
@Setter
public class DivisionCategoryDistributionDto {
    private Long id;
    private Long categoryId;
    private Integer vacancyCount;
    private String additionalComment;
    private Long maleVacancy;
    private Long femaleVacancy;
    private Long totalVacancy;

}