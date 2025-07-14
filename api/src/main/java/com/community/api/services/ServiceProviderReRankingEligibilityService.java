package com.community.api.services;

import com.community.api.dto.ServiceProviderReRankingEligibilityDto;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.ServiceProviderReRankingEligibility;
import com.community.api.services.exception.ExceptionHandlingService;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@Service
public class ServiceProviderReRankingEligibilityService {

    @Autowired
    EntityManager entityManager;

    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    @Transactional
    public ServiceProviderReRankingEligibility addServiceProviderReRankingEligibility(ServiceProviderEntity serviceProvider) throws Exception {
        try {

            if(serviceProvider == null) {
                throw new IllegalArgumentException("Service Provider Not found.");
            }
            ServiceProviderReRankingEligibility serviceProviderReRankingEligibility = new ServiceProviderReRankingEligibility();
            serviceProviderReRankingEligibility.setServiceProvider(serviceProvider);
            return entityManager.merge(serviceProviderReRankingEligibility);

        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    @Transactional
    public ServiceProviderReRankingEligibility updateServiceProviderReRankingEligibility(ServiceProviderEntity serviceProvider, ServiceProviderReRankingEligibilityDto serviceProviderReRankingEligibilityDto) throws Exception {
        try {

            if(serviceProvider == null) {
                throw new IllegalArgumentException("Service Provider Not found.");
            }

            ServiceProviderReRankingEligibility serviceProviderReRankingEligibility = entityManager.find(ServiceProviderReRankingEligibility.class, serviceProvider.getService_provider_id());
            if(serviceProviderReRankingEligibility == null) {
                throw new IllegalArgumentException("Service Provider Re-Ranking Eligibility not found.");
            }

            if(serviceProviderReRankingEligibilityDto.getEligibleForReRanking() != null) {
                serviceProviderReRankingEligibility.setEligibleForReRanking(serviceProviderReRankingEligibilityDto.getEligibleForReRanking());
            }
            if(serviceProviderReRankingEligibilityDto.getAdminOverridden() != null) {
                serviceProviderReRankingEligibility.setAdminOverridden(serviceProviderReRankingEligibilityDto.getAdminOverridden());
            }
            return entityManager.merge(serviceProviderReRankingEligibility);

        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    public ServiceProviderReRankingEligibility getServiceProvideReRankingEligibilityByServiceProviderId(Long serviceProviderId) throws Exception {
        try {

            if (serviceProviderId <= 0) {
                throw new IllegalArgumentException("Service Provider Id cannot be <= 0.");
            }
            ServiceProviderReRankingEligibility serviceProviderReRankingEligibility = entityManager.find(ServiceProviderReRankingEligibility.class, serviceProviderId);
            if (serviceProviderReRankingEligibility == null) {
                throw new NotFoundException("Service Provider not found with this Id.");
            }
            return serviceProviderReRankingEligibility;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

}
