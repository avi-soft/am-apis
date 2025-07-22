package com.community.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QualificationInputDto {
    
    private List<QualificationEligibilityDto> qualificationEligibilities;
    private List<String> operators;
    
    // Helper method to determine if we need to create a new group
    public boolean shouldCreateNewGroup(int index) {
        return index > 0 && operators != null && 
               index - 1 < operators.size() && 
               "AND".equalsIgnoreCase(operators.get(index - 1));
    }
}