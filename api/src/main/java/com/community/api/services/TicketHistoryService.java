package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.CustomTicketHistory;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

@Service
public class TicketHistoryService {

    @Autowired
    EntityManager entityManager;

    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    public List<CustomTicketHistory> fetchTicketHistoryByTicketId(Long ticketId) throws Exception {
        try {
            if (ticketId == null || ticketId <= 0) {
                throw new IllegalArgumentException("TicketId cannot be <=0 or null");
            }

            Query query = entityManager.createNativeQuery(Constant.GET_TICKET_HISTORY_BY_TICKET_ID, CustomTicketHistory.class);
            query.setParameter("ticketId", ticketId);
            List<CustomTicketHistory> ticketHistory = query.getResultList();

            return ticketHistory;

        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException("Illegal Argument Exception Caught: " + illegalArgumentException.getMessage());
        }
        catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught: " + exception.getMessage());
        }
    }
}
