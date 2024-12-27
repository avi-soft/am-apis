package com.community.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QualificationEligibilityDto {
    
    private Long qualificationEligibilityId;

    private List<Integer> qualificationIds;

    private List<Long> customSubjectIds;

    private List<Long> customStreamIds;

    private Long customReserveCategoryId;

    private Long percentage;

    private Long postId;
}
