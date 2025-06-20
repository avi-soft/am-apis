package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SectorDTO {
    @JsonProperty("sector_id")
    private Long sectorId;
    @JsonProperty("sector_name")
    private String sectorName;
    @JsonProperty("sector_description")
    private String sectorDescription;
    List<CompressedProductWrapper>products=new ArrayList<>();
}
