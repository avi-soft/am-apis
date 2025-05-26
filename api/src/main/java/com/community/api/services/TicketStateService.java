package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.CreateTicketDto;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomAdmin;
import com.community.api.entity.CustomOrderState;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.CustomTicketState;
import com.community.api.entity.CustomTicketStatus;
import com.community.api.entity.CustomTicketType;
import com.community.api.entity.CustomWorkQuality;
import com.community.api.entity.Role;
import com.community.api.entity.TicketStateLinkage;
import com.community.api.services.exception.ExceptionHandlingService;
import com.mchange.rmi.NotAuthorizedException;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderItem;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
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
    protected TicketStateService ticketStateService;
    @Autowired
    protected ServiceProviderTicketService serviceProviderTicketService;
    @Autowired
    protected WorkQualityService workQualityService;
    @Autowired
    protected CatalogService catalogService;
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
    public void updateSpTicketAvailibility(CustomServiceProviderTicket ticket, CustomTicketState nextState, Long oldSp, Long newSp) throws Exception {
        try {
            log.info("HERE {}, {}",oldSp, newSp);
            if (oldSp != null && newSp != null && !oldSp.equals(newSp)) {
                ServiceProviderEntity exServiceProvider = entityManager.find(ServiceProviderEntity.class, oldSp);
                if (ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_TO_DO)) {
                    log.info("HERHEHREHRHEHR");
                    exServiceProvider.setTicketAssigned(exServiceProvider.getTicketAssigned() - 1);
                } else if (!ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_CLOSE)) {
                    exServiceProvider.setTicketPending(exServiceProvider.getTicketPending() - 1);
                }
                entityManager.merge(exServiceProvider);

                ServiceProviderEntity newServiceProvider = entityManager.find(ServiceProviderEntity.class, newSp);
                if(nextState != null) {
                    if (nextState.getTicketState().equals("TO-DO")) {
                        newServiceProvider.setTicketAssigned(newServiceProvider.getTicketAssigned() + 1);
                    }
                    if (nextState.getTicketState().equals("CLOSE")) {
                        newServiceProvider.setTicketCompleted(newServiceProvider.getTicketCompleted() + 1);
                    }
                    if (nextState.getTicketState().equals("IN-PROGRESS") && ticket.getTicketState().equals("TO-DO")) {
                        newServiceProvider.setTicketPending(newServiceProvider.getTicketPending() + 1);
                    }
                } else {
                    // Generally this is what going to run
                    newServiceProvider.setTicketAssigned(newServiceProvider.getTicketAssigned() + 1);
                }

                entityManager.merge(newServiceProvider);
            } else if(newSp != null) {
                ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, newSp);

                // TODO NEEDS TO CHECK THIS @RAMAN
                if(nextState != null) {
                    if (nextState.getTicketState().equals("TO-DO")) {
                        serviceProvider.setTicketAssigned(serviceProvider.getTicketAssigned() + 1);
                    }
                    if (nextState.getTicketState().equals("CLOSE")) {
                        serviceProvider.setTicketCompleted(serviceProvider.getTicketCompleted() + 1);
                    }
                    if (nextState.getTicketState().equals("IN-PROGRESS") && ticket.getTicketState().equals("TO-DO")) {
                        serviceProvider.setTicketPending(serviceProvider.getTicketPending() + 1);
                    }
                } else {
                    // Generally this is what going to run
                    serviceProvider.setTicketAssigned(serviceProvider.getTicketAssigned() + 1);
                }

                entityManager.merge(serviceProvider);
            }

            // Approve and close ticket flow.
            if(nextState != null) {
                if(nextState.getTicketStateId().equals(Constant.TICKET_STATE_IN_PROGRESS)) {
                    ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, ticket.getAssignee());
                    serviceProvider.setTicketAssigned(serviceProvider.getTicketAssigned()-1);
                    serviceProvider.setTicketPending(serviceProvider.getTicketPending()+1);

                    entityManager.merge(serviceProvider);
                } else if(nextState.getTicketStateId().equals(Constant.TICKET_STATE_CLOSE)) {
                    // which is true always but here a condition for better understanding of logic.
                    if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_REVIEW_TICKET)) {
                        ServiceProviderEntity reviewTicketServiceProvider = entityManager.find(ServiceProviderEntity.class, ticket.getAssignee());
                        reviewTicketServiceProvider.setTicketPending(reviewTicketServiceProvider.getTicketPending() - 1);
                        reviewTicketServiceProvider.setTicketCompleted(reviewTicketServiceProvider.getTicketCompleted() + 1);

                        if (ticket.getParentTicket().getIsComplete()) {
                            ServiceProviderEntity parentTicketServiceProvider = entityManager.find(ServiceProviderEntity.class, ticket.getParentTicket().getAssignee());
                            parentTicketServiceProvider.setTicketPending(parentTicketServiceProvider.getTicketPending() - 1);
                            parentTicketServiceProvider.setTicketCompleted(parentTicketServiceProvider.getTicketCompleted() + 1);

                            entityManager.merge(parentTicketServiceProvider);
                        }

                        entityManager.merge(reviewTicketServiceProvider);
                    } else {
                        ServiceProviderEntity assignee = entityManager.find(ServiceProviderEntity.class, ticket.getAssignee());
                        assignee.setTicketPending(assignee.getTicketPending() - 1);
                        assignee.setTicketCompleted(assignee.getTicketCompleted() + 1);

                        entityManager.merge(assignee);
                    }
                }
            }

        } catch(IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new Exception(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }

    }

    @Transactional
    public CustomServiceProviderTicket updateTicket(CreateTicketDto createTicketDTO, Long ticketId, String authHeader) throws Exception {
        try {
            if (createTicketDTO == null || (createTicketDTO.getTicketStatus() == null && createTicketDTO.getTicketState() == null && createTicketDTO.getTicketType() == null && createTicketDTO.getAssignee() == null && createTicketDTO.getAssigneeRole() == null && createTicketDTO.getTargetCompletionDate() == null)) {
                throw new IllegalArgumentException("At least one parameter is required to update the ticket");
            }

            if(createTicketDTO.getTicketType() != null) {
                throw new IllegalArgumentException("Ticket type of a ticket cannot be changed");
            }

            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            String roleNameToken = roleService.getRoleByRoleId(roleId).getRole_name();

            log.info("USER ID{}", tokenUserId);

            if (ticketId == null)
                throw new IllegalArgumentException("Ticket Id not provided");

            CustomServiceProviderTicket ticket = entityManager.find(CustomServiceProviderTicket.class, ticketId);

            if (ticket == null)
                throw new NotFoundException("Ticket not found");

            // the assignee not allowed to modify the ticket now.
            if( ticket.getAssignee() != null && ticket.getAssignee().equals(tokenUserId) && ticket.getAssigneeRole().getRole_id() == roleId && (ticket.getTicketId().equals(Constant.TICKET_STATE_IN_REVIEW) || ticket.getTicketId().equals(Constant.TICKET_STATE_CLOSE) || ticket.getTicketId().equals(Constant.TICKET_STATE_SUPPORT)) ){
                throw new NotAuthorizedException("Forbidden Access");
            }

            if (roleNameToken.equals(Constant.roleServiceProvider)) {
                if (!tokenUserId.equals(ticket.getAssignee())) {
                    throw new NotAuthorizedException("Forbidden Access");
                }
                if (createTicketDTO.getTargetCompletionDate() != null)
                    throw new NotAuthorizedException("Service Provider is not authorized to update completion date");
                if (createTicketDTO.getAssignee() != null || createTicketDTO.getAssigneeRole() != null)
                    throw new NotAuthorizedException("Service Provider is not authorized to update Assignee role or Assignee id");
                if (createTicketDTO.getTicketType() != null)
                    throw new NotAuthorizedException("Service Provider is not authorized to  update ticket type");
            }

            Query query = null;
            CustomTicketState ticketState = null;
            CustomTicketStatus ticketStatus = null;

            if (createTicketDTO.getTicketState() != null && createTicketDTO.getTicketStatus() != null) {
                ticketState = getTicketStateByTicketId(createTicketDTO.getTicketState());
                ticketStatus = ticketStatusService.getTicketStatusByTicketStatusId(createTicketDTO.getTicketStatus());

                if(!ticket.getAssignee().equals(tokenUserId)) {
                    throw new IllegalArgumentException("Forbidden Access");
                }
                if(ticket.getTicketState().equals(ticketState) && ticket.getTicketStatus().equals(ticketStatus)) {
                    throw new IllegalArgumentException("Already in the same state and status");
                }

                if (ticketState == null)
                    throw new NotFoundException("Ticket state not found");

                if(ticketStatus == null)
                    throw new NotFoundException("Ticket status not found");

                ticketStateService.verifyState(ticket.getTicketType(), ticket.getTicketState(), ticketState);
                ticketStatusService.verifyStatus(ticketState, ticketStatus, ticket.getTicketType());

                // TODO Understand canTransitTicket with more clarity @Raman
                if (!canTransitTicket(ticket, createTicketDTO.getTicketState(), roleNameToken, createTicketDTO.getTicketStatus()))
                    throw new IllegalArgumentException("Ticket cannot move to the selected state due to workflow restrictions.");

                if (createTicketDTO.getTicketStatus().equals(Constant.TICKET_STATUS_IN_REVIEW_HELP) || createTicketDTO.getTicketStatus().equals(Constant.TICKET_STATUS_OTHER)) {
                    if (createTicketDTO.getComment() == null || createTicketDTO.getComment().isEmpty() || createTicketDTO.getComment().trim().isEmpty()) {
                        throw new IllegalArgumentException("Comment is required");
                    }
                    ticket.setComment(createTicketDTO.getComment().trim());
                }

                // Automatically handles the creation of review ticket when ticket state changed to IN-REVIEW.
                if(ticketState.getTicketStateId().equals(Constant.TICKET_STATE_IN_REVIEW)) {
                    if(ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_MISCELLANEOUS_TICKET)) {
                        if(!ticket.getIsReviewRequired()) {
                            throw new IllegalArgumentException("Cannot create review ticket for this as review required for this is false.");
                        }
                    } else if(ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET)) {
                        CustomProduct customProduct = entityManager.find(CustomProduct.class, findProductFromItemAttribute(ticket.getOrder().getOrderItems().get(0)).getId() );
                        if(!customProduct.getIsReviewRequired()) {
                            throw new IllegalArgumentException("Cannot create review ticket for this as review required for this is false.");
                        }
                    }
                    serviceProviderTicketService.createReviewTicket(ticket);

                } else if(ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_REVIEW_TICKET) && ticketState.getTicketStateId().equals(Constant.TICKET_STATE_CLOSE)) {
                    if(createTicketDTO.getIsComplete() == null || createTicketDTO.getWorkQualityId() == null) {
                        throw new IllegalArgumentException("Is Complete and Work Quality is required to close a review ticket");
                    }
                    CustomWorkQuality workQuality = workQualityService.getWorkQualityByWorkQualityId(createTicketDTO.getWorkQualityId());
                    if(workQuality == null) {
                        throw new NotFoundException("Work Quality Not found with this Id");
                    }

                    CustomServiceProviderTicket parentTicket = ticket.getParentTicket();
                    if(createTicketDTO.getIsComplete()) {
                        parentTicket.setTicketState(ticketStateService.getTicketStateByTicketId(5L));
                        parentTicket.setTicketStatus(ticketStatusService.getTicketStatusByTicketStatusId(15L));
                        parentTicket.setIsComplete(createTicketDTO.getIsComplete());
                        parentTicket.setWorkQuality(workQuality);
                    } else {
                        parentTicket.setTicketState(ticketStateService.getTicketStateByTicketId(2L));
                        parentTicket.setTicketStatus(ticketStatusService.getTicketStatusByTicketStatusId(16L));
                        parentTicket.setIsComplete(createTicketDTO.getIsComplete());
                        parentTicket.setWorkQuality(workQuality);
                    }

                    entityManager.merge(parentTicket);
                } else if(ticketState.getTicketStateId().equals(Constant.TICKET_STATE_CLOSE)) {
                    if(ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_MISCELLANEOUS_TICKET)) {
                        if(ticket.getIsReviewRequired()) {
                            throw new IllegalArgumentException("Cannot close this ticket with creation of review ticket for this as review required for this.");
                        }
                    } else if(ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET)) {
                        CustomProduct customProduct = entityManager.find(CustomProduct.class, findProductFromItemAttribute(ticket.getOrder().getOrderItems().get(0)).getId() );
                        if(customProduct.getIsReviewRequired()) {
                            throw new IllegalArgumentException("Cannot close this ticket with creation of review ticket for this as review required for this.");
                        }
                    }
                } else if(ticketState.getTicketStateId().equals(Constant.TICKET_STATE_RETURNED)) {
                    List<Long> rejectedList = ticket.getRejectedBy();
                    rejectedList.add(ticket.getAssignee());
                    ticket.setRejectedBy(rejectedList);

                    ticket.setAssignee(null);
                    ticket.setAssigneeRole(null);
                }

                ticket.setTicketStatus(ticketStatus);
                ticket.setTicketState(ticketState);

            } else if(createTicketDTO.getTicketStatus() != null) {

                ticketStatus = ticketStatusService.getTicketStatusByTicketStatusId(createTicketDTO.getTicketStatus());
                if (ticketStatus == null)
                    throw new NotFoundException("Ticket Status not found");

                if(ticket.getTicketStatus().equals(ticketStatus)) {
                    throw new IllegalArgumentException("Already in the status");
                }

                ticketState = ticket.getTicketState();
                ticketStatusService.verifyStatus(ticketState, ticketStatus, ticket.getTicketType());

                if (createTicketDTO.getTicketStatus().equals(Constant.TICKET_STATUS_IN_REVIEW_HELP) || createTicketDTO.getTicketStatus().equals(Constant.TICKET_STATUS_OTHER)) {
                    if (createTicketDTO.getComment() == null || createTicketDTO.getComment().isEmpty() || createTicketDTO.getComment().trim().isEmpty()) {
                        throw new IllegalArgumentException("Comment is required");
                    }
                    ticket.setComment(createTicketDTO.getComment().trim());
                }

                ticket.setTicketStatus(ticketStatus);

            } else if(createTicketDTO.getTicketState() != null) {
                throw new IllegalArgumentException("Ticket State cannot be changed without status.");
            }

            if (createTicketDTO.getAssigneeRole() != null) {
                Role role = entityManager.find(Role.class, createTicketDTO.getAssigneeRole());
                if (role == null)
                    throw new NotFoundException("Invalid role id");
                else if ((!role.getRole_name().equals(Constant.roleAdmin)) && (!role.getRole_name().equals(Constant.roleServiceProvider)) && (!role.getRole_name().equals(Constant.roleSuperAdmin)))
                    throw new IllegalArgumentException("Cannot assign ticket to : " + roleService.findRoleName(createTicketDTO.getAssigneeRole()));
                if (createTicketDTO.getAssignee() != null) {
                    if (role.getRole_name().equals(Constant.roleServiceProvider)) {
                        ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, createTicketDTO.getAssignee());
                        if (serviceProvider == null)
                            throw new NotFoundException("Assignee not found");
                    } else if (role.getRole_name().equals(Constant.roleAdmin)) {
                        CustomAdmin customAdmin = entityManager.find(CustomAdmin.class, createTicketDTO.getAssignee());
                        if (customAdmin == null)
                            throw new NotFoundException("Assignee not found");
                    }

                    if(ticket.getAssignee() != null && ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_REVIEW_TICKET) && createTicketDTO.getAssignee().equals(ticket.getParentTicket().getAssignee())) {
                        throw new IllegalArgumentException("Cannot assign ticket to same who is assignee of its parent ticket");
                    } else if(ticket.getAssignee() == null && ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_REVIEW_TICKET) && createTicketDTO.getAssignee().equals(ticket.getParentTicket().getAssignee())) {
                        throw new IllegalArgumentException("Cannot assign ticket to parent assignee");
                    }

                    if(ticket.getAssignee() != null && ticket.getAssignee().equals(createTicketDTO.getAssignee())) {
                        throw new IllegalArgumentException("Already is the assignee");
                    }

                    if(ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_RETURNED)) {
                        if (ticketState != null && !ticketState.getTicketStateId().equals(Constant.TICKET_STATE_TO_DO)) {
                            throw new IllegalArgumentException("Cannot change the assignee from return state to this new state.");
                        } else {
                            ticketState = ticketStateService.getTicketStateByTicketId(Constant.TICKET_STATE_TO_DO);
                        }
                    }

                    if(!ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_TO_DO) && !ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_RETURNED) && !ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_SUPPORT)) {
                        throw new IllegalArgumentException("Not allowed to change the assignee of ticket when ticket not in todo, returned and support");
                    }

                    List<Long> rejectedBy = ticket.getRejectedBy();
                    for(Long rejectedUserId : rejectedBy) {
                        if(rejectedUserId.equals(createTicketDTO.getAssignee())) {
                            throw new IllegalArgumentException("Cannot assignee ticket to someone who already rejected the ticket.");
                        }
                    }
//                    ticket.setAssignee(createTicketDTO.getAssignee());
                    ticket.setAssigneeRole(role);
                } else
                    throw new IllegalArgumentException("Assignee and role must be provided together.");
            }

            if (createTicketDTO.getAssigneeRole() == null && createTicketDTO.getAssignee() != null)
                throw new IllegalArgumentException("Assignee and role must be provided together.");

            if (createTicketDTO.getTargetCompletionDate() != null) {
                if (sharedUtilityService.isInValidOrInPast(createTicketDTO.getTargetCompletionDate()) == 1) {
                    throw new IllegalArgumentException("Target Completion date cannot be in past");
                }
                if (sharedUtilityService.isInValidOrInPast(createTicketDTO.getTargetCompletionDate()) == -1)
                    throw new IllegalArgumentException("Invalid Date-Time");

                if(ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET)) {
                    Product product = findProductFromItemAttribute(ticket.getOrder().getOrderItems().get(0));
                    if(!createTicketDTO.getTargetCompletionDate().after(product.getActiveStartDate()) && !createTicketDTO.getTargetCompletionDate().before(product.getActiveEndDate())) {
                        throw new IllegalArgumentException("Target Completion date must be between Product Open Date and Close Date.");
                    }
                }
                ticket.setTargetCompletionDate(createTicketDTO.getTargetCompletionDate());
            }

            // As only primary ticket will be linked to the order.
            if(ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET)) {
                Order order = orderService.findOrderById(ticket.getOrder().getId());
                order.getOrderAttributes().get("product_id");
                CustomOrderState orderState = entityManager.find(CustomOrderState.class, order.getId());
                query = entityManager.createNativeQuery(Constant.GET_ORDER_STATE_LINKED_WITH_TICKET);

                if (ticketState != null) {
                    query.setParameter("ticketStateId", createTicketDTO.getTicketState());
                    Integer orderStateId = (Integer) query.getFirstResult();
                    orderState.setOrderStateId(orderStateId);
                    entityManager.merge(orderState);
                }
            }

            Long newAssigneeId = createTicketDTO.getAssignee();
            Long oldAssigneeId = ticket.getAssignee();
            LocalDateTime localDateTime = LocalDateTime.now();
            Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            ticket.setModifiedDate(date);
            ticket.setModifierId(tokenUserId);
            ticket.setModifierRole(roleService.getRoleByRoleId(roleId));

            if(oldAssigneeId != null || newAssigneeId != null) {
                updateSpTicketAvailibility(ticket, ticketState, oldAssigneeId, newAssigneeId);
            }
            if(newAssigneeId != null) {
                ticket.setAssignee(newAssigneeId);
            }
            return entityManager.merge(ticket);

        } catch (PersistenceException persistenceException) {
            log.info("Inside persistence");
            exceptionHandlingService.handleException(persistenceException);
            throw new PersistenceException("Cannot find valid result : " + persistenceException.getMessage());
        } catch (IllegalArgumentException illegalArgumentException) {
            log.info("Inside illegal argument exception");
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (NotFoundException notFoundException) {
            log.info("Inside not found exception");
            exceptionHandlingService.handleException(notFoundException);
            throw new NotFoundException(notFoundException.getMessage());
        } catch (Exception exception) {
            log.info("Inside exception");
            exceptionHandlingService.handleException(exception);
            throw new Exception("Error updating ticket :" + exception.getMessage());
        }
    }

    public Boolean canTransitTicket(CustomServiceProviderTicket customServiceProviderTicket, Long ticketStateId, String roleName, Long customTicketStatus) throws Exception {
        try {

            CustomTicketState nextState = getTicketStateByTicketId(ticketStateId);
            CustomTicketStatus status = ticketStatusService.getTicketStatusByTicketStatusId(customTicketStatus);

            if(customServiceProviderTicket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET) ) {
                Long productId = Long.parseLong(customServiceProviderTicket.getOrder().getOrderItems().get(0).getOrderItemAttributes().get("productId").getValue());
                log.info("product id:{}", productId);

                if (!productId.equals(0L)) {
                    CustomProduct customProduct = entityManager.find(CustomProduct.class, productId);
                    if (customProduct == null)
                        throw new NotFoundException("Product linked with ticket not found");

                    // TODO - Need confirmation here that whether we should go ahead with reviewRequired in the product or it should be handled by admin. @Raman
                    if (customProduct.getIsReviewRequired().equals(false) && status.getTicketStatus().equals("FORM-COMPLETED-REVIEW")) {
                        throw new UnsupportedOperationException("Review is not required for this ticket");
                    }
                }
            }

            if(roleName.equals(Constant.roleServiceProvider)) {
                ticketStatusService.verifyStatus(nextState, status, customServiceProviderTicket.getTicketType());
            }

            // Instead of using switch we can handle this with linkage table.
            /*if (roleName.equals(Constant.roleServiceProvider)) {
                switch (customServiceProviderTicket.getTicketState().getTicketState()) {
                    case "TO-DO":
                        return nextState.getTicketState().equals("IN-PROGRESS") || nextState.getTicketState().equals("TO-DO");
                    case "IN-PROGRESS":
                        return nextState.getTicketState().equals("ON-HOLD") || nextState.getTicketState().equals("IN-REVIEW") || nextState.getTicketState().equals("IN-PROGRESS");
                    case "ON-HOLD":
                        return nextState.getTicketState().equals("IN-PROGRESS") || nextState.getTicketState().equals("ON-HOLD") || nextState.getTicketState().equals("IN-REVIEW");
                    case "IN-REVIEW":
                        return nextState.getTicketState().equals("CLOSE") || nextState.getTicketState().equals("IN-REVIEW");
                    case "CLOSE":
                        return nextState.getTicketState().equals("CLOSE");
                    default:
                        return false; // No transitions allowed from DONE
                }
            }*/

            // Admin logic
            if (roleName.equals(Constant.roleAdmin) || roleName.equals(Constant.roleSuperAdmin)) {
                // Admin can transition to any state except from close
                return !customServiceProviderTicket.getTicketState().getTicketState().equals("CLOSE");
            }
            return true; // Default: No transition allowed
        } catch (NotFoundException nfexception) {
            throw new Exception(nfexception.getMessage());
        } catch (Exception exception) {
            throw new Exception(exception.getMessage());
        }
    }

    public TicketStateLinkage verifyState(CustomTicketType ticketType, CustomTicketState ticketStateFrom, CustomTicketState ticketStateTo) throws Exception {
        try {
            if(ticketType == null || ticketStateFrom == null || ticketStateTo == null) {
                throw new IllegalArgumentException("Ticket Type, Ticket State From and Ticket State To cannot be NULL");
            }

            Long ticketTypeId = ticketType.getTicketTypeId();
            Long ticketStateFromId = ticketStateFrom.getTicketStateId();
            Long ticketStateToId = ticketStateTo.getTicketStateId();

            Query query = entityManager.createQuery(Constant.GET_TICKET_STATE_LINKAGE_BY_TICKET_TYPE_AND_TICKET_FROM_AND_TICKET, TicketStateLinkage.class);
            query.setParameter("ticketTypeId", ticketTypeId);
            query.setParameter("ticketStateIdFrom", ticketStateFromId);
            query.setParameter("ticketStateIdTo", ticketStateToId);

            List<TicketStateLinkage> ticketStateLinkageList = query.getResultList();
            if(ticketStateLinkageList == null || ticketStateLinkageList.isEmpty()) {
                throw new IllegalArgumentException("Linkage not Found between type and states.");
            }

            return ticketStateLinkageList.get(0);

        } catch(IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    public Product findProductFromItemAttribute(OrderItem orderItem) {
        Long productId = Long.parseLong(orderItem.getOrderItemAttributes().get("productId").getValue());
        Product product = catalogService.findProductById(productId);
        return product;
    }
}
