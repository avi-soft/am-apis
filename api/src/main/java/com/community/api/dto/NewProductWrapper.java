package com.community.api.dto;


import com.community.api.entity.StateCode;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NewProductWrapper  {
        @JsonProperty("product_id")
        private Long id;
        @JsonProperty("meta_title")
        private String metaTitle;
        @JsonProperty("display_template")
        private String displayTemplate;
        @JsonProperty("meta_description")
        private String metaDescription;
        @JsonProperty("category_name")
        private String categoryName;
        @JsonProperty("active_start_date")
        private Date activeStartDate;
        @JsonProperty("active_end_date")
        private Date activeEndDate;
        @JsonProperty("go_live_date")
        private Date activeGoLiveDate;
        @JsonProperty("state")
        private String state;
        @JsonProperty("state_id")
        private Long stateId;
}
