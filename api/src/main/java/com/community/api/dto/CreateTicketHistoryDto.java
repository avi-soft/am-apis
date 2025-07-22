package com.community.api.dto;

import com.community.api.entity.Role;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.Date;

public class CreateTicketHistoryDto {

    @NotNull
    @JsonProperty("ticket_state")
    private Long ticketState;

    @JsonProperty("ticket_status")
    private Long ticketStatus;

    @NotNull
    @JsonProperty("ticket_type")
    private Long ticketType;

    @JsonProperty("assignee")
    private Long assignee;

    @JsonProperty("assignee_role")
    private Role assigneeRole;

    @JsonProperty("target_completion_time")
    private Date targetCompletionDate;

    @JsonProperty("comment")
    private String comment;
}
