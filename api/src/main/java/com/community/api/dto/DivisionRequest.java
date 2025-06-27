package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DivisionRequest {
    @NotBlank
    @JsonProperty("division_name")
    private String divisionName;

    @NotBlank
    @JsonProperty("division_code")
    @Pattern(regexp = "^[a-zA-Z]+$")
    private String divisionCode;

    @NotNull
    @JsonProperty("zone_id")
    private Integer zoneId;

    @NotNull
    @JsonProperty("zone_name")
    private String zoneName;
}