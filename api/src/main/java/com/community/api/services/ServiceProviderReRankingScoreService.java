package com.community.api.services;

import com.community.api.dto.ServiceProviderReRankingScoreDto;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.ServiceProviderReRankingScore;
import com.community.api.services.exception.ExceptionHandlingService;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@Service
public class ServiceProviderReRankingScoreService {

    @Autowired
    EntityManager entityManager;

    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    @Transactional
    public ServiceProviderReRankingScore addServiceProviderReRankingScore(ServiceProviderEntity serviceProvider) throws Exception {
        try {

            if(serviceProvider == null) {
                throw new IllegalArgumentException("Service Provider Not found.");
            }
            ServiceProviderReRankingScore serviceProviderReRankingScore = new ServiceProviderReRankingScore();
            serviceProviderReRankingScore.setServiceProvider(serviceProvider);
            return entityManager.merge(serviceProviderReRankingScore);

        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    @Transactional
    public ServiceProviderReRankingScore updateServiceProviderReRankingScore(ServiceProviderEntity serviceProvider, ServiceProviderReRankingScoreDto serviceProviderReRankingScoreDto) throws Exception {
        try {

            if(serviceProvider == null) {
                throw new IllegalArgumentException("Service Provider Not found.");
            }

            ServiceProviderReRankingScore serviceProviderReRankingScore = entityManager.find(ServiceProviderReRankingScore.class, serviceProvider.getService_provider_id());
            if(serviceProviderReRankingScore == null) {
                throw new IllegalArgumentException("Service Provider Re-Ranking Score not found.");
            }

            if(serviceProviderReRankingScoreDto.getTimeCompletionScore() != null) {
                serviceProviderReRankingScore.setTimeCompletionScore(serviceProviderReRankingScore.getTimeCompletionScore() + serviceProviderReRankingScoreDto.getTimeCompletionScore());
            }
            if(serviceProviderReRankingScoreDto.getReviewTicketStatusScore() != null) {
                serviceProviderReRankingScore.setReviewTicketStatusScore(serviceProviderReRankingScore.getReviewTicketStatusScore() + serviceProviderReRankingScoreDto.getReviewTicketStatusScore());
            }
            if(serviceProviderReRankingScoreDto.getReviewTicketFeedbackScore() != null) {
                serviceProviderReRankingScore.setReviewTicketFeedbackScore(serviceProviderReRankingScore.getReviewTicketFeedbackScore() + serviceProviderReRankingScoreDto.getReviewTicketFeedbackScore());
            }
            return entityManager.merge(serviceProviderReRankingScore);

        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    public ServiceProviderReRankingScore getServiceProvideReRankingScoreByServiceProviderId(Long serviceProviderId) throws Exception {
        try {

            if (serviceProviderId <= 0) {
                throw new IllegalArgumentException("Service Provider Id cannot be <= 0.");
            }
            ServiceProviderReRankingScore serviceProviderReRankingScore = entityManager.find(ServiceProviderReRankingScore.class, serviceProviderId);
            if (serviceProviderReRankingScore == null) {
                throw new NotFoundException("Service Provider not found with this Id.");
            }
            return serviceProviderReRankingScore;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }
}
