package com.community.api.dto;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomCustomer;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReferrerDTO {

        private Map<String,Object> serviceProvider;

        private LocalDateTime createdAt;
}
