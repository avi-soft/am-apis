package com.community.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class ZoneDistributionDto
{
    private Integer zoneId;
    private Boolean isDivisionDistribution;
    private Boolean isGenderWise;
    private Integer maleVacancy;
    private Integer femaleVacancy;
    private Integer totalVacanciesInZone;
    private List<CategoryDistributionDto> categoryDistributions = new ArrayList<>();
    private List<DivisionDistributionDto> divisionDistributions = new ArrayList<>();
}
