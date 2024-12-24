package com.community.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DivisionCategoryDistributionDto {
    private Long id;
    private Long categoryId;
    private Integer vacancyCount;
}