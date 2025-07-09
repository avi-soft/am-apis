package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceProviderReRankingScoreDto {

    @JsonProperty("review_ticket_status_score")
    private Long reviewTicketStatusScore = 0L;

    @JsonProperty("review_ticket_feedback_score")
    private Long reviewTicketFeedbackScore = 0L;

    @JsonProperty("time_completion_score")
    private Long timeCompletionScore;

}
