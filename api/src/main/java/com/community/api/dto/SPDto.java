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
public class SPDto {
    @JsonProperty(value = "serivce_provider_id")
    Long spId;
    @JsonProperty(value = "name")
    String name;
}
