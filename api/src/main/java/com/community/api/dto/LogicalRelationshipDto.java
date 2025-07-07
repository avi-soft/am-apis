package com.community.api.dto;

import com.community.api.enums.LogicalOperator;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogicalRelationshipDto {
    private LogicalOperator streamOperator = LogicalOperator.AND;
    private Boolean streamsAreMandatory = true;
    
    private LogicalOperator subjectOperator = LogicalOperator.AND;
    private Boolean subjectsAreMandatory = true;
    
    private Boolean reserveCategoryIsMandatory = false;
}