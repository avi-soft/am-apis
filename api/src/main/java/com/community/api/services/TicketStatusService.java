package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomTicketState;
import com.community.api.entity.CustomTicketStatus;
import com.community.api.entity.CustomTicketType;
import com.community.api.entity.OrderTicketLinkage;
import com.community.api.services.exception.ExceptionHandlingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;

@Slf4j
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

    public OrderTicketLinkage verifyStatus(CustomTicketState ticketState, CustomTicketStatus ticketStatus, CustomTicketType ticketType) throws Exception {
        try {
            if(ticketState == null || ticketStatus == null || ticketType == null) {
                throw new IllegalArgumentException("Ticket Type, Ticket State and Ticket Status cannot be NULL");
            }

            Long ticketTypeId = ticketType.getTicketTypeId();
            Long ticketStateId = ticketState.getTicketStateId();
            Long ticketStatusId = ticketStatus.getTicketStatusId();

            Query query = entityManager.createQuery(Constant.GET_ORDER_TICKET_LINKAGE_BY_TICKET_STATE_AND_TICKET_STATUS, OrderTicketLinkage.class);
            query.setParameter("ticketTypeId", ticketTypeId);
            query.setParameter("ticketStateId", ticketStateId);
            query.setParameter("ticketStatusId", ticketStatusId);

            List<OrderTicketLinkage> orderTicketLinkageList = query.getResultList();
            if(orderTicketLinkageList == null || orderTicketLinkageList.isEmpty()) {
                throw new IllegalArgumentException("Linkage not Found between state and status");
            }

            return orderTicketLinkageList.get(0);

        } catch(IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }
}
