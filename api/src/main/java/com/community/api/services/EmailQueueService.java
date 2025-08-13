package com.community.api.services;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.EmailQueue;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class EmailQueueService {

    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    @Autowired
    EntityManager entityManager;

    @Autowired
    RoleService roleService;

    public void addEmailQueue(ServiceProviderEntity serviceProvider, CustomServiceProviderTicket ticket) throws Exception {
        try {

            EmailQueue emailQueue = new EmailQueue();
            emailQueue.setRole(roleService.getRoleByRoleId(serviceProvider.getRole()));
            emailQueue.setUserId(serviceProvider.getService_provider_id());
            emailQueue.setArchived(false);
            emailQueue.setCreatedDate(new Date());
            emailQueue.setTicket(ticket);
            entityManager.merge(emailQueue);

        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    public List<EmailQueue> getAllEmailQueue () throws Exception {
        try {

            String jpql = "SELECT e FROM EmailQueue e WHERE e.archived = false";
            return entityManager.createQuery(jpql, EmailQueue.class).getResultList();

        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

}
