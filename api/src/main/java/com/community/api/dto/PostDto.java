package com.community.api.dto;

import com.community.api.entity.OtherDistribution;
import com.community.api.entity.Post;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@NoArgsConstructor  // This is crucial for Jackson deserialization
@AllArgsConstructor
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
//    @JsonProperty("reserve_category_age")
//    List<AddProductAgeDTO>reserveCategoryAge;
    private QualificationEligibilityDto qualificationEligibility;
    private List<AddPhysicalRequirementDto> physicalRequirements = new ArrayList<>();
    @JsonProperty("reserve_category_ages")
    private List<ReserveCategoryAgeDto> reserveCategoryAge = new ArrayList<>();
    private List<OtherDistribution> otherDistributions = new ArrayList<>();
    public PostDto(Post post) {
        this.postName = post.getPostName();
        this.postTotalVacancies = post.getPostTotalVacancies();
        this.postCode = post.getPostCode();
        this.vacancyDistributionTypeIds = post.getVacancyDistributionTypes()
                .stream()
                .map(vacancyDistributionType -> vacancyDistributionType.getVacancyDistributionTypeId()) // Use the correct getter method
                .collect(Collectors.toList());
        // Additional mappings for other fields as needed
    }
}
