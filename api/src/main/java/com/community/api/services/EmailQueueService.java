package com.community.api.services;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.EmailQueue;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmailQueueService {

    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    public List<EmailQueue> getAllEmailQueue () throws Exception {
        try {
            return new ArrayList<>();
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    public Boolean sendTicketAllocationMail(Customer customer, CustomServiceProviderTicket ticket) throws Exception {
        try {

            return true;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    public Boolean sendTicketAllocationMail(ServiceProviderEntity serviceProvider, CustomServiceProviderTicket ticket) throws Exception {
        try {

            return true;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

}
