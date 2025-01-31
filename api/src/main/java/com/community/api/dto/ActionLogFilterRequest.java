package com.community.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActionLogFilterRequest {
    private Long serviceProviderId;
    private Long customerId;
    private Integer modeId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String deliveryStatus;
}