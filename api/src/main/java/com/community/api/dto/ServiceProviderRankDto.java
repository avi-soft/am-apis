package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ServiceProviderRankDto {

    @JsonProperty("rank_description")
    private String rankDescription;

    @JsonProperty("maximum_ticket_size")
    private Integer maximumTicketSize;

    @JsonProperty("maximum_binding_size")
    private Integer maximumBindingSize;

}
