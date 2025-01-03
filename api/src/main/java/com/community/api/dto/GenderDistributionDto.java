package com.community.api.dto;

import com.community.api.entity.CategoryDistribution;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Getter
@Setter
public class GenderDistributionDto
{
    Boolean isGenderWise;
    Long maleVacancy;
    Long femaleVacancy;
    Long totalVacancy;
    private List<CategoryDistributionDto> categoryDistributionDtos = new ArrayList<>();
}
