package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateTicketDto {

    @NotNull
    @JsonProperty("ticket_state")
    private Long ticketState;

    @JsonProperty("ticket_status")
    private Long ticketStatus;

    @NotNull
    @JsonProperty("ticket_type")
    private Long ticketType;

    @NotNull
    @JsonProperty("assignee")
    private Long assignee;

    @NotNull
    @JsonProperty("assignee_role")
    private Integer assigneeRole;

    @NotNull
    @JsonProperty("target_completion_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH-mm-ss")
    private Date targetCompletionDate;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("title")
    private String title;

    @JsonProperty("task")
    private String task;

    @JsonProperty("is_complete")
    private Boolean isComplete;

    @JsonProperty("work_quality_id")
    private Long workQualityId;

    @JsonProperty("is_review_required")
    private Boolean isReviewRequired;

}
