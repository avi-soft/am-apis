package com.community.api.dto;

import com.community.api.entity.CustomProductGenderPhysicalRequirementRef;
import com.community.api.entity.GenderWiseDistribution;
import com.community.api.entity.OtherDistribution;
import com.community.api.entity.QualificationEligibility;
import com.community.api.entity.StateDistribution;
import com.community.api.entity.VacancyDistributionType;
import com.community.api.entity.ZoneDistribution;
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
    List<ReserveCategoryAgeDto>reserveCategoryAge;
    private List<QualificationEligibility> qualificationEligibility;
   private List<CustomProductGenderPhysicalRequirementRef> physicalRequirements ;
}
