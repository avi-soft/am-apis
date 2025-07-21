package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class QualificationEligibilityDto {

    private List<Integer> qualificationIds;
    private List<Long> customSubjectIds;

    private List<Long> customStreamIds;
    private String qualificationIdRunningField;
    private String subjectIdRunningField;
    private String streamIdRunningField;
    private Long customReserveCategoryId;
    private String reserveCatIdRunningField;
    private Long percentage;
    private Double cgpa;

    private Boolean isPercentage;
    @JsonIgnore
    private String additionalComments;
    private Boolean isAppearing;
    private List<String> highestQualificationSubjectNames;
    private Long streamsRelationId=1L;
    private Long subjectsRelationId=1L;
    private Boolean streamsMandatory = true;
    private Boolean subjectsMandatory = true;
    private Boolean reserveCategoryMandatory = true;
    private Long qualificationOperatorId=1L;
    private Boolean isCertificationRequired=false;
}
