package com.community.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class FilteredServiceProviderDTO {
        private Long service_provider_id;
        private String type;
        private String user_name;
        private String first_name;
        private String last_name;
        private String full_name;
        private String country_code;
        private String mobileNumber;
        private String primary_email;
        private Integer role;
        private Boolean approved;
        private Boolean completed;
        private Boolean is_active;
        private Boolean suspended;
        private Boolean is_running_business_unit;
        private String business_name;
        private Map<String, Object> filteredAddress;
        private List<Map<String, Object>> skills;
        private Map<String, Object> rank;
        private Map<String, Object> service_provider_status;
        private Integer total_score;
        private String part_time_or_full_time;
    }