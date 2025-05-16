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
    private String duration;
    @JsonProperty("reserve_category_age")
    List<ReserveCategoryAgeDto>reserveCategoryAge;
    private List<QualificationEligibility> qualificationEligibility;
   private List<CustomProductGenderPhysicalRequirementRef> physicalRequirements ;
    @JsonProperty("post_additional_comments")
    String additionalComments;
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
    @JsonProperty("total_seats_visible")
    private Boolean totalSeatsVisible;
    @JsonProperty("income_threshold")
    private Double income;
    @JsonProperty("religion_eligibility")
    private List<String>religion;
    @JsonProperty("religion_additional_comments")
    private String ReligionAdditionalComments;
    @JsonProperty("income_additional_comments")
    private String incomeAdditionalComments;
    @JsonProperty("additional_eligibility")
    private String additionalEligibility;
}
