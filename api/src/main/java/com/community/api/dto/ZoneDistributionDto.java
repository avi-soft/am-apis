package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
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
    private String additionalComments;
}
