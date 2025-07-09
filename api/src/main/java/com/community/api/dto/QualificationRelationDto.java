
package com.community.api.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class QualificationRelationDto {
    
    private Long qualificationRelationId;
    private Long operatorId;
    private Integer qualificationId;
}