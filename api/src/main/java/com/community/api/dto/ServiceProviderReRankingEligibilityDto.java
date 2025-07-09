package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceProviderReRankingEligibilityDto {

    @JsonProperty("is_admin_overridden")
    private Boolean adminOverridden = false;

    @JsonProperty("is_eligible_for_re_ranking")
    private Boolean eligibleForReRanking;

}
