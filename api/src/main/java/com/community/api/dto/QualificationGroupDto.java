package com.community.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class QualificationGroupDto {
    
    private Long qualificationGroupId;
    private Integer groupOrder;
    private String groupName;
    private List<QualificationEligibilityDto> qualificationEligibilityInGroup = new ArrayList<>();
    private String additionalComments;
}