package com.community.api.dto;

import com.community.api.entity.AddProductAgeDTO;
import com.community.api.entity.CustomProductGenderPhysicalRequirementRef;
import com.community.api.entity.ZoneDistribution;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.micrometer.core.lang.Nullable;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class PostDto {

    private String postName;
    private Long postTotalVacancies;
    private String postCode;
    @Nullable
    @JsonProperty("born_before_after")
    Boolean bornBeforeAfter;
    private List<Integer> vacancyDistributionTypeIds;
    private List<StateDistributionDto> stateDistributions;
    private List<ZoneDistributionDto> zoneDistributions;
    private GenderDistributionDto genderWiseDistribution;
    @JsonProperty("reserve_category_age")
    List<AddProductAgeDTO>reserveCategoryAge;
    private QualificationEligibilityDto qualificationEligibilityDto;
    private List<AddPhysicalRequirementDto> physicalRequirements = new ArrayList<>();
}
