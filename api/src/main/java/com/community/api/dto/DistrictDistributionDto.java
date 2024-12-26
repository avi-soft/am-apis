package com.community.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DistrictDistributionDto {
    private Integer districtId;
    private Boolean isGenderWise;
    private Integer maleVacancy;
    private Integer femaleVacancy;
    private Integer totalVacancy;
    private List<DistrictCategoryDistributionDto> categoryDistributions = new ArrayList<>();
}