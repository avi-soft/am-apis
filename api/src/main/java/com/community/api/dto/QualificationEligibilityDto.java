package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class QualificationEligibilityDto {
    
    private Long qualificationEligibilityId;

    private List<Integer> qualificationIds;
    private List<Long> customSubjectIds;

    private List<Long> customStreamIds;
    private String qualificationIdRunningField;
    private String subjectIdRunningField;
    private String streamIdRunningField;
    private Long customReserveCategoryId;
    private String reserveCatIdRunningField;
    private Long percentage;

    private Long postId;

    private Double cgpa;

    private Boolean isPercentage;
    @JsonIgnore
    private String additionalComments;
    private Boolean isAppearing;
    private List<String> highestQualificationSubjectNames;

}
