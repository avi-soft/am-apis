package com.community.api.dto;

import com.community.api.entity.CustomProduct;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class ProductUpdateRequest {
    JsonObject old;
    JsonObject latest;
}
