package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.CreateTicketDto;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomAdmin;
import com.community.api.entity.CustomJobGroup;
import com.community.api.entity.CustomOrderState;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.CustomTicketState;
import com.community.api.entity.CustomTicketStatus;
import com.community.api.entity.ManualAssignmentDetails;
import com.community.api.entity.Role;
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
    protected JwtUtil jwtTokenUtil;
    @Autowired
    protected RoleService roleService;
    @Autowired
    protected SharedUtilityService sharedUtilityService;
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
    public ResponseEntity<?> updateTicket(CreateTicketDto createTicketDTO, Long ticketId,String authHeader)
    {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            String roleName = roleService.getRoleByRoleId(roleId).getRole_name();
            CustomTicketState ticketState=null;
            if (ticketId == null)
                return ResponseService.generateErrorResponse("Ticket Id not provided", HttpStatus.BAD_REQUEST);
            CustomServiceProviderTicket ticket = entityManager.find(CustomServiceProviderTicket.class, ticketId);
            if (ticket == null)
                return ResponseService.generateErrorResponse("Ticket not found", HttpStatus.NOT_FOUND);
            if(roleName.equals(Constant.roleServiceProvider)&&!ticket.getAssignee().equals(tokenUserId))
                return ResponseService.generateErrorResponse("Not authorized to perform action on this ticket",HttpStatus.UNAUTHORIZED);
            if (createTicketDTO.getTicketState() != null) {
                ticketState = getTicketStateByTicketId(createTicketDTO.getTicketState());
                if(ticketState==null)
                    return ResponseService.generateErrorResponse("Ticket state not found",HttpStatus.NOT_FOUND);
                ticket.setTicketState(ticketState);
            }
            if(createTicketDTO.getTicketStatus()!=null&&createTicketDTO.getTicketState()==null)
                return ResponseService.generateErrorResponse("Ticket State Required",HttpStatus.BAD_REQUEST);
            Query query=null;
            if(createTicketDTO.getTicketStatus()!=null) {
                CustomTicketStatus ticketStatus = ticketStatusService.getTicketStatusByTicketStatusId(createTicketDTO.getTicketStatus());
                if (ticketStatus == null)
                    return ResponseService.generateErrorResponse("Ticket Status not found", HttpStatus.NOT_FOUND);
                if(createTicketDTO.getTicketState().equals(ticket.getTicketState().getTicketStateId()))
                    return ResponseService.generateErrorResponse("Selected state already set",HttpStatus.BAD_REQUEST);
                query = entityManager.createNativeQuery(Constant.GET_TICKET_STATUS_LINKED_WITH_TICKET_STATE);
                query.setParameter("ticketStateId", createTicketDTO.getTicketState());
                List<BigInteger> resultList = query.getResultList();
                // Convert BigInteger list to Long list
                List<Long> resultListLong = resultList.stream()
                        .map(BigInteger::longValue)  // Convert BigInteger to long
                        .collect(Collectors.toList());
                if(resultListLong.isEmpty())
                    return ResponseService.generateErrorResponse("No status is available for ticket state : "+ticketState.getTicketState(),HttpStatus.NOT_FOUND);
                if (!resultListLong.contains(createTicketDTO.getTicketStatus()))
                    return ResponseService.generateErrorResponse("Invalid Status selected for ticket State :" + ticketState.getTicketState(), HttpStatus.BAD_REQUEST);
                ticket.setTicketStatus(ticketStatus);
            }
            if(createTicketDTO.getAssigneeRole()!=null)
            {
                Role role=entityManager.find(Role.class,roleId);
            if(role==null)
                return ResponseService.generateErrorResponse("Invalid role specified",HttpStatus.NOT_FOUND);
            else if((!roleName.equals(Constant.roleAdmin))&&(!roleName.equals(Constant.roleServiceProvider)))
                return ResponseService.generateErrorResponse("Cannot assign ticket to : "+roleService.findRoleName(createTicketDTO.getAssigneeRole()),HttpStatus.NOT_FOUND);
            if(createTicketDTO.getAssignee()!=null)
            {
                if(roleName.equals(Constant.roleServiceProvider))
                {
                    ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, createTicketDTO.getAssignee());
                    if(serviceProvider==null)
                        return ResponseService.generateErrorResponse("Assignee not found",HttpStatus.NOT_FOUND);
                }
                else if(roleName.equals(Constant.roleAdmin))
                {
                    CustomAdmin customAdmin=entityManager.find(CustomAdmin.class,createTicketDTO.getAssignee());
                    if(customAdmin==null)
                        return ResponseService.generateErrorResponse("Assignee not found",HttpStatus.NOT_FOUND);
                }
                ticket.setAssignee(createTicketDTO.getAssignee());
                ticket.setAssigneeRole(role);
            }
            else
                return ResponseService.generateErrorResponse("Assignee id should be given",HttpStatus.NOT_FOUND);
            }
            if(createTicketDTO.getAssigneeRole()==null&&createTicketDTO.getAssignee()!=null)
                return ResponseService.generateErrorResponse("Specify the role",HttpStatus.BAD_REQUEST);
            if(createTicketDTO.getTargetCompletionDate()!=null)
            {
              if(sharedUtilityService.isInValidOrInPast(createTicketDTO.getTargetCompletionDate())==1)
              {
                  return ResponseService.generateErrorResponse("Target Completion date cannot be in past",HttpStatus.BAD_REQUEST);
              }
              if(sharedUtilityService.isInValidOrInPast(createTicketDTO.getTargetCompletionDate())==-1)
                  return ResponseService.generateErrorResponse("Invalid Date-Time",HttpStatus.BAD_REQUEST);
              ticket.setTargetCompletionDate(createTicketDTO.getTargetCompletionDate());
            }
            Order order=orderService.findOrderById(ticket.getOrder().getId());
            CustomOrderState orderState=entityManager.find(CustomOrderState.class,order.getId());
            query=entityManager.createNativeQuery(Constant.GET_ORDER_STATE_LINKED_WITH_TICKET);
            if(ticketState!=null) {
                query.setParameter("ticketStateId", createTicketDTO.getTicketState());
                Integer orderStateId = (Integer) query.getFirstResult();
                orderState.setOrderStateId(orderStateId);
                entityManager.merge(orderState);
            }
            entityManager.merge(ticket);
            return ResponseService.generateSuccessResponse("Ticket Updated",ticket,HttpStatus.OK);
        }
        catch (PersistenceException e)
        {
            return ResponseService.generateErrorResponse("Cannot find valid result : "+e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(Exception e)
         {
             return ResponseService.generateErrorResponse("Error updating ticket :"+e.getMessage(),HttpStatus.NOT_FOUND);
         }
    }
}
