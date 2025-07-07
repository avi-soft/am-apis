package com.community.api.dto;

import com.community.api.enums.LogicalOperator;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class QualificationEligibilityGroupDto {
    private Long groupId;
    private String groupName;
    private LogicalOperator logicalOperator = LogicalOperator.AND;
    private List<QualificationEligibilityDto> qualificationEligibilities = new ArrayList<>();
}