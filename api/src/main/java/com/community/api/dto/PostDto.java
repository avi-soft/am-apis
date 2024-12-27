package com.community.api.dto;

import com.community.api.entity.QualificationEligibility;
import com.community.api.entity.ZoneDistribution;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PostDto {

    private String postName;
    private Long postTotalVacancies;
    private String postCode;
    private List<Integer> vacancyDistributionTypeIds;
    private List<StateDistributionDto> stateDistributions;
    private List<ZoneDistributionDto> zoneDistributions;
    private GenderDistributionDto genderWiseDistribution;
    private QualificationEligibilityDto qualificationEligibilityDto;

}
