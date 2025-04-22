package com.community.api.dto;

import com.community.api.entity.CustomGender;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddReserveCategoryDto {
    @JsonProperty("fee")
    Double fee;
    @JsonProperty("post")
    Integer post;
    @JsonProperty("reserve_category_id")
    Long reserveCategory;
  /*  @JsonProperty("born_before")
    Date bornBefore;
    @JsonProperty("born_after")
    Date bornAfter;*/
    @JsonProperty("gender_id")
    Long gender;
    @JsonProperty("fee_additional_comments")
    String additionalComment;
    @Column(name = "is_other_or_state_category")
    private Boolean isOtherOrStateCategory;

    @Column(name = "other_or_state_category", columnDefinition = "text")
    private String otherOrStateCategory;
}
