package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.CreateTicketDto;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomAdmin;
import com.community.api.entity.CustomJobGroup;
import com.community.api.entity.CustomOrderState;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.CustomTicketState;
import com.community.api.entity.CustomTicketStatus;
import com.community.api.entity.ManualAssignmentDetails;
import com.community.api.entity.Role;
import com.community.api.services.exception.ExceptionHandlingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.NotFoundException;
import com.twilio.exception.ApiException;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.OrderService;
import org.hibernate.query.criteria.internal.expression.function.CurrentTimeFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.MethodNotAllowedException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
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

            if (!ticketStateList.isEmpty()) {
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
            throw new Exception(exception.getMessage());
        }
    }
    @Transactional
    public void updateSpTicketAvailibility(CustomServiceProviderTicket ticket,CustomTicketState nextState,Long oldSp,Long newSp)
    {
        if(oldSp!=newSp)
        {
            ServiceProviderEntity exServiceProvider=entityManager.find(ServiceProviderEntity.class,newSp);
            if(ticket.getTicketState().getTicketState().equals("TO-DO"))
            {
                exServiceProvider.setTicketAssigned(exServiceProvider.getTicketAssigned()-1);
            }
            else if(!ticket.getTicketState().getTicketState().equals("TO-DO")&&!ticket.getTicketState().getTicketState().equals("CLOSED"))
        {
            exServiceProvider.setTicketPending(exServiceProvider.getTicketPending()-1);
        }
            entityManager.merge(exServiceProvider);
        }
        ServiceProviderEntity serviceProvider=entityManager.find(ServiceProviderEntity.class,oldSp);
        if(nextState.getTicketState().equals("TO-DO"))
        {
            serviceProvider.setTicketAssigned(serviceProvider.getTicketAssigned()+1);
        }
        if(nextState.getTicketState().equals("CLOSE"))
        {
            serviceProvider.setTicketCompleted(serviceProvider.getTicketCompleted()+1);
        }
        if(nextState.getTicketState().equals("IN-PROGRESS")&&ticket.getTicketState().equals("TO-DO"))
        {
            serviceProvider.setTicketPending(serviceProvider.getTicketPending()+1);
        }
        entityManager.merge(serviceProvider);
    }
    @Transactional
    public ResponseEntity<?> updateTicket(CreateTicketDto createTicketDTO, Long ticketId, String authHeader) {
        try {
            if (createTicketDTO == null || (createTicketDTO.getTicketStatus() == null && createTicketDTO.getTicketState() == null && createTicketDTO.getTicketType() == null && createTicketDTO.getAssignee() == null && createTicketDTO.getAssigneeRole() == null && createTicketDTO.getTargetCompletionDate() == null)) {
                return ResponseService.generateErrorResponse("Atleast one parameter is required to update the ticket", HttpStatus.BAD_REQUEST);
            }
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            System.out.println("USER ID" + tokenUserId);
            String roleNameToken = roleService.getRoleByRoleId(roleId).getRole_name();
            CustomTicketState ticketState = null;
            if (ticketId == null)
                return ResponseService.generateErrorResponse("Ticket Id not provided", HttpStatus.BAD_REQUEST);
            CustomServiceProviderTicket ticket = entityManager.find(CustomServiceProviderTicket.class, ticketId);
            if (ticket == null)
                return ResponseService.generateErrorResponse("Ticket not found", HttpStatus.NOT_FOUND);
            if (roleNameToken.equals(Constant.roleServiceProvider)) {
                if (!tokenUserId.equals(ticket.getAssignee())) {
                    return ResponseService.generateErrorResponse("Forbidden Access", HttpStatus.UNAUTHORIZED);
                }
                if (createTicketDTO.getTargetCompletionDate() != null)
                    return ResponseService.generateErrorResponse("Service Provider is not authorized to update completion date", HttpStatus.UNAUTHORIZED);
                if (createTicketDTO.getAssignee() != null || createTicketDTO.getAssigneeRole() != null)
                    return ResponseService.generateErrorResponse("Service Provider is not authorized to update Assignee role or Assignee id", HttpStatus.UNAUTHORIZED);
                if (createTicketDTO.getTicketType() != null)
                    return ResponseService.generateErrorResponse("Service Provider is not authorized to  update ticket type", HttpStatus.UNAUTHORIZED);
            }
            if (roleNameToken.equals(Constant.roleServiceProvider) && !ticket.getAssignee().equals(tokenUserId))
                return ResponseService.generateErrorResponse("Not authorized to perform action on this ticket", HttpStatus.UNAUTHORIZED);
            if (createTicketDTO.getTicketState() != null) {
                ticketState = getTicketStateByTicketId(createTicketDTO.getTicketState());
                if (ticketState == null)
                    return ResponseService.generateErrorResponse("Ticket state not found", HttpStatus.NOT_FOUND);

                if(!canTransitTicket(ticket,createTicketDTO.getTicketState(),roleNameToken,createTicketDTO.getTicketStatus()))
                    return ResponseService.generateErrorResponse("Ticket cannot move to the selected state due to workflow restrictions.",HttpStatus.FORBIDDEN);

                ticket.setTicketState(ticketState);
            }
            if ((createTicketDTO.getTicketStatus() != null && createTicketDTO.getTicketState() == null) || (createTicketDTO.getTicketStatus() == null && createTicketDTO.getTicketState() != null))
                return ResponseService.generateErrorResponse("Ticket state and status must be provided together.", HttpStatus.BAD_REQUEST);
            Query query = null;
            if (createTicketDTO.getTicketStatus() != null) {
                CustomTicketStatus ticketStatus = ticketStatusService.getTicketStatusByTicketStatusId(createTicketDTO.getTicketStatus());
                if (ticketStatus == null)
                    return ResponseService.generateErrorResponse("Ticket Status not found", HttpStatus.NOT_FOUND);
                /*if(createTicketDTO.getTicketState().equals(ticket.getTicketState().getTicketStateId()))
                    return ResponseService.generateErrorResponse("Selected state already set",HttpStatus.BAD_REQUEST);*/
                query = entityManager.createNativeQuery(Constant.GET_TICKET_STATUS_LINKED_WITH_TICKET_STATE);
                query.setParameter("ticketStateId", createTicketDTO.getTicketState());
                List<BigInteger> resultList = query.getResultList();
                // Convert BigInteger list to Long list
                List<Long> resultListLong = resultList.stream()
                        .map(BigInteger::longValue)  // Convert BigInteger to long
                        .collect(Collectors.toList());
                if (resultListLong.isEmpty())
                    return ResponseService.generateErrorResponse("No status is available for ticket state : " + ticketState.getTicketState(), HttpStatus.NOT_FOUND);
                if (!resultListLong.contains(createTicketDTO.getTicketStatus()))
                    return ResponseService.generateErrorResponse("Invalid Status selected for ticket State :" + ticketState.getTicketState(), HttpStatus.BAD_REQUEST);
                ticket.setTicketStatus(ticketStatus);

                if (createTicketDTO.getTicketState().equals(Constant.TICKET_STATE_IN_REVIEW) && createTicketDTO.getTicketStatus().equals(Constant.TICKET_STATUS_IN_REVIEW_HELP)) {
                    if (createTicketDTO.getComment() == null || createTicketDTO.getComment().isEmpty()) {
                        return ResponseService.generateErrorResponse("Comment is required", HttpStatus.BAD_REQUEST);
                    }
                    ticket.setComment(createTicketDTO.getComment());
                }
            }
            if (createTicketDTO.getAssigneeRole() != null) {
                Role role = entityManager.find(Role.class, createTicketDTO.getAssigneeRole());
                if (role == null)
                    return ResponseService.generateErrorResponse("Invalid role id", HttpStatus.NOT_FOUND);
                else if ((!role.getRole_name().equals(Constant.roleAdmin)) && (!role.getRole_name().equals(Constant.roleServiceProvider)))
                    return ResponseService.generateErrorResponse("Cannot assign ticket to : " + roleService.findRoleName(createTicketDTO.getAssigneeRole()), HttpStatus.NOT_FOUND);
                if (createTicketDTO.getAssignee() != null) {
                    if (role.getRole_name().equals(Constant.roleServiceProvider)) {
                        ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, createTicketDTO.getAssignee());
                        if (serviceProvider == null)
                            return ResponseService.generateErrorResponse("Assignee not found", HttpStatus.NOT_FOUND);
                    } else if (role.getRole_name().equals(Constant.roleAdmin)) {
                        CustomAdmin customAdmin = entityManager.find(CustomAdmin.class, createTicketDTO.getAssignee());
                        if (customAdmin == null)
                            return ResponseService.generateErrorResponse("Assignee not found", HttpStatus.NOT_FOUND);
                    }
                    ticket.setAssignee(createTicketDTO.getAssignee());
                    ticket.setAssigneeRole(role);
                } else
                    return ResponseService.generateErrorResponse("Assignee and role must be provided together.", HttpStatus.NOT_FOUND);
            }
            if (createTicketDTO.getAssigneeRole() == null && createTicketDTO.getAssignee() != null)
                return ResponseService.generateErrorResponse("Assignee and role must be provided together.", HttpStatus.BAD_REQUEST);

            if (createTicketDTO.getTargetCompletionDate() != null) {
                if (sharedUtilityService.isInValidOrInPast(createTicketDTO.getTargetCompletionDate()) == 1) {
                    return ResponseService.generateErrorResponse("Target Completion date cannot be in past", HttpStatus.BAD_REQUEST);
                }
                if (sharedUtilityService.isInValidOrInPast(createTicketDTO.getTargetCompletionDate()) == -1)
                    return ResponseService.generateErrorResponse("Invalid Date-Time", HttpStatus.BAD_REQUEST);
                ticket.setTargetCompletionDate(createTicketDTO.getTargetCompletionDate());
            }
            Order order = orderService.findOrderById(ticket.getOrder().getId());
            CustomOrderState orderState = entityManager.find(CustomOrderState.class, order.getId());
            query = entityManager.createNativeQuery(Constant.GET_ORDER_STATE_LINKED_WITH_TICKET);
            if (ticketState != null) {
                query.setParameter("ticketStateId", createTicketDTO.getTicketState());
                Integer orderStateId = (Integer) query.getFirstResult();
                orderState.setOrderStateId(orderStateId);
                entityManager.merge(orderState);
            }
            Long newId=createTicketDTO.getAssignee();
            Long old=ticket.getAssignee();
            LocalDateTime localDateTime = LocalDateTime.now();
            Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            ticket.setModifiedDate(date);
            ticket.setModifierId(tokenUserId);
            ticket.setModifierRole(roleService.getRoleByRoleId(roleId));
            entityManager.merge(ticket);
            updateSpTicketAvailibility(ticket,ticketState,old,newId);
            return ResponseService.generateSuccessResponse("Ticket Updated", ticket, HttpStatus.OK);

        } catch (PersistenceException e) {
            return ResponseService.generateErrorResponse("Cannot find valid result : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch (NotFoundException notFoundException)
        {
            return ResponseService.generateErrorResponse(notFoundException.getMessage(),HttpStatus.NOT_FOUND);
        }
        catch (Exception e) {

            return ResponseService.generateErrorResponse("Error updating ticket :" + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    public Boolean canTransitTicket(CustomServiceProviderTicket customServiceProviderTicket, Long ticketStateId,String roleName, Long customTicketStatus) throws Exception {
        try {
            Long productId =Long.parseLong(customServiceProviderTicket.getOrder().getOrderItems().get(0).getOrderItemAttributes().get("productId").getValue());
            System.out.println("id:"+productId);
            int stateValue = Math.toIntExact(ticketStateId);
            CustomTicketState nextState = getTicketStateByTicketId(ticketStateId);
            CustomTicketStatus status=ticketStatusService.getTicketStatusByTicketStatusId(customTicketStatus);
            if (!productId.equals(0L)) {
                CustomProduct customProduct = entityManager.find(CustomProduct.class, productId);
                if(customProduct==null)
                    throw new NotFoundException("Product linked with ticket not found");
                if (customProduct.getIsReviewRequired().equals(false) && status.getTicketStatus().equals("FORM-COMPLETED-REVIEW")) {
                    throw new UnsupportedOperationException("Review is not required for this ticket");
                }
            }
            if (roleName.equals(Constant.roleServiceProvider)) {
                switch (customServiceProviderTicket.getTicketState().getTicketState()) {
                    case "TO-DO":
                        return nextState.getTicketState().equals("IN-PROGRESS")||nextState.getTicketState().equals("TO-DO");
                    case "IN-PROGRESS":
                        return nextState.getTicketState().equals("ON-HOLD") || nextState.getTicketState().equals("IN-REVIEW")||nextState.getTicketState().equals("IN-PROGRESS");
                    case "ON-HOLD":
                        return nextState.getTicketState().equals("IN-PROGRESS")||nextState.getTicketState().equals("ON-HOLD")||nextState.getTicketState().equals("IN-REVIEW");
                    case "IN-REVIEW":
                        return nextState.getTicketState().equals("CLOSE")||nextState.getTicketState().equals("IN-REVIEW");
                    case "CLOSE":
                        return nextState.getTicketState().equals("CLOSE");
                    default:
                        return false; // No transitions allowed from DONE
                }
            }

            // Admin logic
            if (roleName.equals(Constant.roleAdmin) || roleName.equals(Constant.roleSuperAdmin)) {
                // Admin can transition to any state except from close
                return !customServiceProviderTicket.getTicketState().getTicketState().equals("CLOSE");
            }
            return false; // Default: No transition allowed
        }catch (NotFoundException nfexception) {
            throw new Exception(nfexception.getMessage());
        } catch (Exception exception) {
            throw new Exception(exception.getMessage());
        }
    }
}
