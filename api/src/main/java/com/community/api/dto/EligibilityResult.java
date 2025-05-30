package com.community.api.dto;

import com.community.api.services.CartService;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public  class EligibilityResult {
    private CartService.EligibilityStatus status;
    private List<String> reasons;
    private List<String> warnings;

    public EligibilityResult() {
        this.reasons = new ArrayList<>();
        this.warnings = new ArrayList<>();
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