package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DivisionProjectionDTO {
    @JsonProperty("division_id")
    private Integer divisionId;
    @JsonProperty("division_name")
    private String divisionName;
    @JsonProperty("division_code")
    private String divisionCode;
}