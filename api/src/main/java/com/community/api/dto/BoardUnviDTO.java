package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BoardUnviDTO {


    @JsonProperty("board_university_name")
    private String board_university_name;

    @JsonProperty("board_university_code")
    private String board_university_code;

    @JsonProperty("created_date")
    private String created_date;

    @JsonProperty("modified_date")
    private String modified_date;

    @JsonProperty("created_by")
    private String created_by;

    @JsonProperty("modified_by")
    private String modified_by;

    @JsonProperty("sort_order")
    private Long sort_order;

    @JsonProperty("archived")
    private Boolean archived;

    @JsonProperty("board_university_location")
    private String board_university_location;

    @JsonProperty("board_university_type")
    private String board_university_type;

}
