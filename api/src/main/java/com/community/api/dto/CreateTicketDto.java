package com.community.api.dto;

import com.community.api.entity.CustomTicketState;
import com.community.api.entity.CustomTicketStatus;
import com.community.api.entity.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTicketDto {

    @NotNull
    @JsonProperty("ticket_state")
    private Long ticketState;

    @JsonProperty("ticket_status")
    private Long ticketStatus;

    @JsonProperty("ticket_type")
    private Long ticketType;

    @JsonProperty("assignee")
    private Long assignee;

    @JsonProperty("assignee_role")
    private Role assigneeRole;

    @JsonProperty("target_completion_time")
    private Date targetCompletionDate;

}
