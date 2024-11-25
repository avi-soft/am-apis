package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomJobGroup;
import com.community.api.entity.CustomOrderState;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.CustomTicketState;
import com.community.api.entity.CustomTicketStatus;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketStateService {

    @PersistenceContext
    protected EntityManager entityManager;
    @Autowired
    protected TicketStatusService ticketStatusService;
    @Autowired
    protected OrderService orderService;
    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    public List<CustomTicketState> getAllTicketState() throws Exception {
        try {
            List<CustomTicketState> ticketStateList = entityManager.createQuery(Constant.GET_ALL_TICKET_STATE, CustomTicketState.class).getResultList();

            if(!ticketStateList.isEmpty()) {
                return ticketStateList;
            } else {
                throw new IllegalArgumentException("No ticket state found");
            }

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some Exception Caught: " + exception.getMessage());
        }
    }

    public CustomTicketState getTicketStateByTicketId(Long ticketStateId) throws Exception {
        try {

            Query query = entityManager.createQuery(Constant.GET_TICKET_STATE_BY_TICKET_STATE_ID, CustomTicketState.class);
            query.setParameter("ticketStateId", ticketStateId);
            List<CustomTicketState> ticketState = query.getResultList();

            if (!ticketState.isEmpty()) {
                return ticketState.get(0);
            } else {
                throw new IllegalArgumentException("No ticket state found with this ticket state id");
            }

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some Exception caught: " + exception.getMessage());
        }
    }
    @Transactional
    public ResponseEntity<?> updateStateAndStatus(Long ticketId,Long ticketStateId,Long ticketStatusId)
    {
        try {
            if (ticketId == null)
                return ResponseService.generateErrorResponse("Ticket Id not provided", HttpStatus.BAD_REQUEST);
            CustomServiceProviderTicket ticket = entityManager.find(CustomServiceProviderTicket.class, ticketId);
            if (ticket == null)
                return ResponseService.generateErrorResponse("Ticket not found", HttpStatus.NOT_FOUND);
            if (ticketStateId == null)
                return ResponseService.generateErrorResponse("Ticket State not specified", HttpStatus.BAD_REQUEST);
            CustomTicketState ticketState = getTicketStateByTicketId(ticketStateId);
            ticket.setTicketState(ticketState);
            Query query=null;
            if(ticketStatusId!=null) {
                CustomTicketStatus ticketStatus = ticketStatusService.getTicketStatusByTicketStatusId(ticketStatusId);
                if (ticketStatus == null)
                    return ResponseService.generateErrorResponse("Ticket Status not found", HttpStatus.NOT_FOUND);
                query = entityManager.createNativeQuery(Constant.GET_TICKET_STATUS_LINKED_WITH_TICKET_STATE);
                query.setParameter("ticketStateId", ticketStateId);
                List<BigInteger> resultList = query.getResultList();
                // Convert BigInteger list to Long list
                List<Long> resultListLong = resultList.stream()
                        .map(BigInteger::longValue)  // Convert BigInteger to long
                        .collect(Collectors.toList());
                /*System.out.println(resultList.get(0)+","+resultList.get(1));
                System.out.println(resultList.get(0).getClass().getName());*/
                if (!resultListLong.contains(ticketStatusId))
                    return ResponseService.generateErrorResponse("Invalid Status selected for ticket State :" + ticketState.getTicketState(), HttpStatus.BAD_REQUEST);
                ticket.setTicketStatus(ticketStatus);
            }
            Order order=orderService.findOrderById(ticket.getOrder().getId());
            CustomOrderState orderState=entityManager.find(CustomOrderState.class,order.getId());
            query=entityManager.createNativeQuery(Constant.GET_ORDER_STATE_LINKED_WITH_TICKET);
            query.setParameter("ticketStateId",ticketStateId);
            Integer orderStateId=(Integer)query.getSingleResult();
            orderState.setOrderStateId(orderStateId);
            entityManager.merge(ticket);
            entityManager.merge(orderState);
            return ResponseService.generateSuccessResponse("Ticket State Updated",ticket,HttpStatus.OK);
        }
        catch (PersistenceException e)
        {
            return ResponseService.generateErrorResponse("Cannot find valid result : "+e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(Exception e)
         {
             return ResponseService.generateErrorResponse("Cannot fetch ticket state :"+e.getMessage(),HttpStatus.NOT_FOUND);
         }
    }
}
