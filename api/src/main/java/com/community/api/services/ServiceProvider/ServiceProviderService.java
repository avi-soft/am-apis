package com.community.api.services.ServiceProvider;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

public interface ServiceProviderService {
    ServiceProviderEntity saveServiceProvider(ServiceProviderEntity serviceProviderEntity);
    ResponseEntity<?> updateServiceProvider(Long userId, @RequestBody Map<String, Object> updates, @RequestHeader(value = "Authorization") String authHeader) throws Exception;
    ServiceProviderEntity getServiceProviderById(Long userId);
}