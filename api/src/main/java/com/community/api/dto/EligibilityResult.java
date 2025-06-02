package com.community.api.dto;

import com.community.api.services.CartService;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public  class EligibilityResult {
    private CartService.EligibilityStatus status;
    private Set<String> reasons;
    private Set<String> warnings;

    public EligibilityResult() {
        this.reasons = new HashSet<>();
        this.warnings = new HashSet<>();
    }

    public void addReason(String reason) {
        reason = reason.trim().toLowerCase(); // normalize
        if (!this.reasons.contains(reason)) {
            this.reasons.add(reason);
        }
    }

    public void addWarning(String warning) {
        warning= warning.trim().toLowerCase();
       if(!this.warnings.contains(warning))
        {
            this.warnings.add(warning);
        }
    }
}