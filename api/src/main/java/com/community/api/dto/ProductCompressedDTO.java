package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductCompressedDTO {
    @JsonProperty("product_id")
    Long productId;
    @JsonProperty("meta_title")
    String metaTitle;
}
