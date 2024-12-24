package com.community.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class StateDistributionDto {

        private Integer stateCodeId;
        private Boolean isDistrictDistribution;
        private Boolean isGenderWise;  // Single boolean for state-level
        private Integer maleVacancy;   // Single integer for state-level
        private Integer femaleVacancy; // Single integer for state-level
        private Integer totalVacanciesInState;
        private List<CategoryDistributionDto> categoryDistributions = new ArrayList<>();
        private List<DistrictDistributionDto> districtDistributions = new ArrayList<>();

}
