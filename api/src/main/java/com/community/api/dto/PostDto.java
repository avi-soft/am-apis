package com.community.api.dto;

import com.community.api.entity.AddProductAgeDTO;
import com.community.api.entity.OtherDistribution;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
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
    @JsonProperty("reserve_category_age")
    List<AddProductAgeDTO>reserveCategoryAge;
    private List<QualificationEligibilityDto> qualificationEligibility;
    private List<AddPhysicalRequirementDto> physicalRequirements = new ArrayList<>();
    private List<OtherDistribution> otherDistributions = new ArrayList<>();
    private String postAdditionalComments;
    @JsonProperty("state_distribution_additional_comments")
    private String stateDistributionAdditionalComments;
    @JsonProperty("zone_distribution_additional_comments")
    private String zoneDistributionAdditionalComments;
    @JsonProperty("gender_distribution_additional_comments")
    private String genderDistributionAdditionalComments;
    @JsonProperty("reserve_category_age_additional_comments")
    private String reserveCatAgeAdditionalComments;
    @JsonProperty("qualification_additional_comments")
    private String qualificationAdditionalComments;
    @JsonProperty("physical_additional_comments")
    private String physicalAdditionalComments;
    @JsonProperty("other_distribution_additional_comments")
    private String otherDistributionAdditionalComments;
}
