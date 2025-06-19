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
public class CategoryProductDTO {
    @JsonProperty("category_id")
    Long categoryId;
    @JsonProperty("category_name")
    String categoryName;
    @JsonProperty("products")
    List<ProductCompressedDTO>products=new ArrayList<>();

}
