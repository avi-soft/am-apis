package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DivisionResponse {
    @JsonProperty("division_code")
    private String divisionCode;

    @JsonProperty("division_name")
    private String divisionName;

    @JsonProperty("division_id")
    private Integer divisionId;

    @JsonProperty("zone_id")
    private Integer zoneId;

    @JsonProperty("zoneName")
    private String zoneName;
}