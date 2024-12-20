package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddAdvertisementDto {

    @JsonProperty("number")
    private String number;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("active_start_date")
    private Date activeStartDate;

    @JsonProperty("active_end_date")
    private Date activeEndDate;

    @JsonProperty("url")
    private String url;

    @JsonProperty("notifying_authority")
    private String notifyingAuthority;

}
