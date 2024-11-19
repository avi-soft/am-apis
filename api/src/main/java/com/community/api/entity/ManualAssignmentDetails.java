package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.Date;
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class ManualAssignmentDetails {
        @JsonProperty("ticket_state")
        public Long ticketState;

        @JsonProperty("ticket_status")
        public Long ticketStatus;

        @JsonProperty("ticket_type")
        public Long ticketType;

        @JsonProperty("assignee")
        public Long assignee;

        @JsonProperty("assignee_role")
        public Integer assigneeRole;

        @JsonProperty("target_completion_time")
        public Date targetCompletionDate;

    }


