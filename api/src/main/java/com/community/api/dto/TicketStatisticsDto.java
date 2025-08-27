package com.community.api.dto;

import com.community.api.entity.CustomTicketType;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketStatisticsDto {

    @JsonProperty("ticket_type")
    CustomTicketType ticketType;

    @JsonProperty("due_in_three_days")
    Integer dueInThreeDays;

    @JsonProperty("rejected")
    Integer rejected;

    @JsonProperty("total")
    Integer total;

    @JsonProperty("overdue")
    Integer overdue;

}
