package com.community.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class QualificationGroupDto {
    private Integer groupOrder;
    private String groupName;
    private List<QualificationEligibilityDto> qualificationGroups = new ArrayList<>();
    private String additionalComments;
}