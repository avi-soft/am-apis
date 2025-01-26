package com.community.api.dto;

import com.community.api.entity.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PostProjectionDTO {

    private Long postId;
    private String postName;
    private Long postTotalVacancies;
    private String postCode;
    private List<VacancyDistributionType> vacancyDistributionTypeIds;
    private List<StateDistribution> stateDistributions;
    private List<ZoneDistribution> zoneDistributions;
    private List<OtherDistribution> otherDistributions;
    private GenderWiseDistribution genderWiseDistribution;
    @JsonProperty("reserve_category_age")
    List<ReserveCategoryAge>reserveCategoryAge;
    private QualificationEligibility qualificationEligibility;
   private List<CustomProductGenderPhysicalRequirementRef> physicalRequirements ;
}
