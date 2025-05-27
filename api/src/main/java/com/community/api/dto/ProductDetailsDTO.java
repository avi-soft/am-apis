package com.community.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductDetailsDTO {
    String metaTitle;
    Long id;
    String displayTemplate;
    Double fee;
    String ageLimit;
}
