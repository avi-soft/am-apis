package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdvertisementCompressedDTO {
    @JsonProperty("advertisement_id")
    Long advertisement_id;
    @JsonProperty("title")
    String advertisementTitle;
    @JsonProperty("description")
    String advertisementDesc;
    @JsonProperty("products")
    List<CompressedProductWrapper> productList=new ArrayList<>();
}
