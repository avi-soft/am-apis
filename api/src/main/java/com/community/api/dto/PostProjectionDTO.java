package com.community.api.dto;

import com.community.api.entity.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor  // This is crucial for Jackson deserialization
@AllArgsConstructor
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
    @JsonProperty("reserve_category_ages")
    List<ReserveCategoryAge> reserveCategoryAges;
    private QualificationEligibility qualificationEligibility;
   private List<CustomProductGenderPhysicalRequirementRef> physicalRequirements ;
}
