package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomTicketState;
import com.community.api.entity.CustomTicketStatus;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collections;
import java.util.List;

@Service
public class TicketStatusService {
    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    public List<CustomTicketStatus> getAllTicketStatus() throws Exception {
        try {
            List<CustomTicketStatus> ticketStatusList = entityManager.createQuery(Constant.GET_ALL_TICKET_STATUS, CustomTicketStatus.class).getResultList();

            if (!ticketStatusList.isEmpty()) {
                return ticketStatusList;
            } else {
                throw new IllegalArgumentException("No ticket status found with this ticket status id");
            }

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some Exception Caught: " + exception.getMessage());
        }
    }

    public CustomTicketStatus getTicketStatusByTicketStatusId(Long ticketStatusId) throws Exception {
        try {

            Query query = entityManager.createQuery(Constant.GET_TICKET_STATE_BY_TICKET_STATUS_ID, CustomTicketStatus.class);
            query.setParameter("ticketStatusId", ticketStatusId);
            List<CustomTicketStatus> ticketState = query.getResultList();

            if (!ticketState.isEmpty()) {
                return ticketState.get(0);
            } else {
                throw new IllegalArgumentException("No ticket status found with this ticket status id");
            }

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }
}
