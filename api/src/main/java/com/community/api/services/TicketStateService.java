package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.CreateTicketDto;
import com.community.api.dto.ServiceProviderReRankingScoreDto;
import com.community.api.endpoint.avisoft.controller.ServiceProviderActionController;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomOrderState;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.CustomTicketHistory;
import com.community.api.entity.CustomTicketState;
import com.community.api.entity.CustomTicketStatus;
import com.community.api.entity.CustomTicketType;
import com.community.api.entity.CustomWorkQuality;
import com.community.api.entity.Earnings;
import com.community.api.entity.EmailQueue;
import com.community.api.entity.Role;
import com.community.api.entity.TicketStateLinkage;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.exception.ExceptionHandlingService;
import com.community.api.utils.ServiceProviderDocument;
import com.mchange.rmi.NotAuthorizedException;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
    protected TicketHistoryService ticketHistoryService;
    @Autowired
    protected SharedUtilityService sharedUtilityService;
    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;
    @Autowired
    ServiceProviderServiceImpl serviceProviderService;
    @Autowired
    ServiceProviderReRankingScoreService serviceProviderReRankingScoreService;
    @Autowired
    EmailQueueService emailQueueService;
    @Autowired
    ServiceProviderActionController serviceProviderActionController;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private EarningService earningService;


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

    public CustomTicketState getTicketStateByTicketStateId(Long ticketStateId) throws Exception {
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
    public CustomServiceProviderTicket updateTicket(CreateTicketDto createTicketDTO, List<MultipartFile> files, Long ticketId, String authHeader) throws Exception {
        try {

            if (createTicketDTO == null || (createTicketDTO.getTicketStatus() == null && createTicketDTO.getTicketState() == null && createTicketDTO.getTicketType() == null && createTicketDTO.getAssignee() == null && createTicketDTO.getAssigneeRole() == null && createTicketDTO.getTargetCompletionDate() == null)) {
                throw new IllegalArgumentException("At least one parameter is required to update the ticket");
            }

            if (createTicketDTO.getTicketType() != null) {
                throw new IllegalArgumentException("Ticket type of a ticket cannot be changed");
            }

            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Role tokenRole = roleService.getRoleByRoleId(roleId);
            String roleNameToken = tokenRole.getRole_name();

            ServiceProviderEntity tokenServiceProvider = entityManager.find(ServiceProviderEntity.class, tokenUserId);

            if (ticketId == null)
                throw new IllegalArgumentException("Ticket Id not provided");

            CustomServiceProviderTicket ticket = entityManager.find(CustomServiceProviderTicket.class, ticketId);

            if (ticket == null || ticket.getArchived())
                throw new NotFoundException("Ticket not found");

            // the assignee not allowed to modify the ticket now.
            if (ticket.getAssignee() != null && ticket.getAssignee().equals(tokenUserId) && ticket.getAssigneeRole().getRole_id() == roleId && (ticket.getTicketId().equals(Constant.TICKET_STATE_IN_REVIEW) || ticket.getTicketId().equals(Constant.TICKET_STATE_CLOSE) || ticket.getTicketId().equals(Constant.TICKET_STATE_SUPPORT))) {
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

            // Assignee change logic
            ServiceProviderEntity serviceProvider = null;
            if (createTicketDTO.getAssigneeRole() != null) {
                Role role = entityManager.find(Role.class, createTicketDTO.getAssigneeRole());
                if (role == null)
                    throw new NotFoundException("Invalid role id");
                else if ((!role.getRole_name().equals(Constant.roleAdmin)) && (!role.getRole_name().equals(Constant.roleServiceProvider)) && (!role.getRole_name().equals(Constant.roleSuperAdmin)))
                    throw new IllegalArgumentException("Cannot assign ticket to : " + roleService.findRoleName(createTicketDTO.getAssigneeRole()));
                if (createTicketDTO.getAssignee() != null) {
                    serviceProvider = entityManager.find(ServiceProviderEntity.class, createTicketDTO.getAssignee());
                    if (serviceProvider == null || serviceProvider.getRole() != role.getRole_id()) {
                        throw new NotFoundException("Assignee not found");
                    }

                    if (createTicketDTO.getTargetCompletionDate() == null) {
                        throw new IllegalArgumentException("target completion date is mandatory when changing the assignee");
                    }

                    if (ticket.getAssignee() != null && ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_REVIEW_TICKET) && createTicketDTO.getAssignee().equals(ticket.getParentTicket().getAssignee())) {
                        throw new IllegalArgumentException("Cannot assign ticket to same who is assignee of its parent ticket");
                    } else if (ticket.getAssignee() == null && ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_REVIEW_TICKET) && createTicketDTO.getAssignee().equals(ticket.getParentTicket().getAssignee())) {
                        throw new IllegalArgumentException("Cannot assign ticket to parent assignee");
                    }

                    if (ticket.getAssignee() != null && ticket.getAssignee().equals(createTicketDTO.getAssignee())) {
                        throw new IllegalArgumentException("Already is the assignee");
                    } else if (ticket.getAssignee() != null) {
                        ServiceProviderEntity existingAssignee = entityManager.find(ServiceProviderEntity.class, ticket.getAssignee());

                        if (existingAssignee == null) {
                            throw new IllegalArgumentException("Not able to find the previous assignee");
                        }
                        if (ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_TO_DO)) {
                            existingAssignee.setTicketAssigned(existingAssignee.getTicketAssigned() - 1);
                            serviceProvider.setTicketAssigned(serviceProvider.getTicketAssigned() + 1);
                        } else if (ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_CLOSE)) {
                            existingAssignee.setTicketCompleted(existingAssignee.getTicketCompleted() - 1);
                            serviceProvider.setTicketCompleted(serviceProvider.getTicketCompleted() + 1);
                        } else if (!ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_RETURNED)) {
                            existingAssignee.setTicketPending(existingAssignee.getTicketPending() - 1);
                            serviceProvider.setTicketPending(serviceProvider.getTicketPending() + 1);
                        }
                        entityManager.merge(existingAssignee);
                        entityManager.merge(serviceProvider);
                    } else {
                        if (ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_TO_DO)) {
                            serviceProvider.setTicketAssigned(serviceProvider.getTicketAssigned() + 1);
                        } else if (ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_CLOSE)) {
                            serviceProvider.setTicketCompleted(serviceProvider.getTicketCompleted() + 1);
                        } else if (!ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_RETURNED)) {
                            serviceProvider.setTicketPending(serviceProvider.getTicketPending() + 1);
                        }
                        entityManager.merge(serviceProvider);
                    }

                    if (tokenRole.getRole_name().equals(Constant.roleAdmin) && !role.getRole_name().equals(Constant.roleServiceProvider) && !Objects.equals(tokenUserId, createTicketDTO.getAssignee())) {
                        throw new IllegalArgumentException("Admin can only assign ticket to service provider");
                    }

                    List<Long> rejectedBy = ticket.getRejectedBy();
                    for (Long rejectedUserId : rejectedBy) {
                        if (rejectedUserId.equals(createTicketDTO.getAssignee())) {
                            throw new IllegalArgumentException("Cannot assignee ticket to someone who already rejected the ticket.");
                        }
                    }
                    ticket.setAssignee(createTicketDTO.getAssignee());
                    ticket.setAssigneeRole(role);
                    if (ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_RETURNED) && createTicketDTO.getTargetCompletionDate() != null && createTicketDTO.getAssignee() != null && createTicketDTO.getAssigneeRole() != null) {
                        CustomTicketState customTicketState = entityManager.find(CustomTicketState.class, Constant.TICKET_STATE_TO_DO);
                        if (customTicketState == null) {
                            throw new IllegalArgumentException("Ticket state with id " + Constant.TICKET_STATE_TO_DO + "not found");
                        }
                        ticket.setTicketState(customTicketState);
                        if(ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET)) {
                            CustomOrderState orderState = entityManager.find(CustomOrderState.class, ticket.getOrder().getId());

                            if(orderState == null) {
                                throw new IllegalArgumentException("No Order found with this primary ticket.");
                            }
                            orderState.setOrderStateId(4);
                            orderState.setModifiedDate(new Date());
                            orderState.setModifierUserId(tokenUserId);
                            orderState.setModifierRole(tokenRole);
                            entityManager.merge(orderState);
                        }
                    }
                } else
                    throw new IllegalArgumentException("Assignee and role must be provided together.");
            }

            if (createTicketDTO.getAssigneeRole() == null && createTicketDTO.getAssignee() != null)
                throw new IllegalArgumentException("Assignee and role must be provided together.");

            // STATE CHANGE LOGIC.
            CustomTicketState ticketState = null;
            CustomTicketStatus ticketStatus = null;

            ServiceProviderEntity existingServiceProvider = null;
            if (createTicketDTO.getTicketState() != null && createTicketDTO.getTicketStatus() != null) {
                ticketState = getTicketStateByTicketStateId(createTicketDTO.getTicketState());
                ticketStatus = ticketStatusService.getTicketStatusByTicketStatusId(createTicketDTO.getTicketStatus());

                if(ticketState.getTicketStateId().equals(Constant.TICKET_STATE_RETURNED)) {
                    existingServiceProvider = entityManager.find(ServiceProviderEntity.class, ticket.getAssignee());
                }
                if (ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_SUPPORT)) {
                    if (createTicketDTO.getComment() == null || createTicketDTO.getComment().isEmpty() || createTicketDTO.getComment().trim().isEmpty()) {
                        throw new IllegalArgumentException("Comment is required");
                    }
                    ticket.setComment(createTicketDTO.getComment().trim());
                }

                // Change order state to in-progress.
                if (ticketState.getTicketStateId().equals(Constant.TICKET_STATE_IN_PROGRESS) && ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET)) {
                    CustomOrderState orderState = entityManager.find(CustomOrderState.class, ticket.getOrder().getId());
                    orderState.setOrderStateId(6);
                    orderState.setModifiedDate(new Date());
                    orderState.setModifierUserId(tokenUserId);
                    orderState.setModifierRole(tokenRole);
                    entityManager.merge(orderState);
                } else if (ticketState.getTicketStateId().equals(Constant.TICKET_STATE_CLOSE) && ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET)) {
                    CustomOrderState orderState = entityManager.find(CustomOrderState.class, ticket.getOrder().getId());
                    orderState.setOrderStateId(7);
                    orderState.setModifiedDate(new Date());
                    orderState.setModifierUserId(tokenUserId);
                    orderState.setModifierRole(tokenRole);
                    entityManager.merge(orderState);

                    // Earning section
                    earningService.createEarning(ticket);
                } else if (ticketState.getTicketStateId().equals(Constant.TICKET_STATE_RETURNED) && ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET)) {
                    CustomOrderState orderState = entityManager.find(CustomOrderState.class, ticket.getOrder().getId());
                    orderState.setOrderStateId(3);
                    orderState.setModifiedDate(new Date());
                    orderState.setModifierUserId(tokenUserId);
                    orderState.setModifierRole(tokenRole);
                    entityManager.merge(orderState);
                }

                // Commented to allow super admin and admin to do what ever they want within the flow.
                /*if(!ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_SUPPORT) && !ticket.getAssignee().equals(tokenUserId)) {
                    if(!ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_ON_HOLD)) {
                        throw new IllegalArgumentException("Forbidden Access");
                    }
                }*/
                if (ticket.getTicketState().equals(ticketState) && ticket.getTicketStatus().equals(ticketStatus)) {
                    throw new IllegalArgumentException("Already in the same state and status");
                }

                if (ticketState == null)
                    throw new NotFoundException("Ticket state not found");

                if (ticketStatus == null)
                    throw new NotFoundException("Ticket status not found");

                ticketStateService.verifyState(roleService.getRoleByRoleId(roleId), ticket.getTicketType(), ticket.getTicketState(), ticketState);
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
                if (ticketState.getTicketStateId().equals(Constant.TICKET_STATE_IN_REVIEW)) {
                    if (createTicketDTO.getComment() == null || createTicketDTO.getComment().trim().isEmpty()) {
                        throw new IllegalArgumentException("Comment is mandatory for a ticket to close and in-review");
                    }
//                    ticket.setComment(createTicketDTO.getComment().trim());
                    if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_MISCELLANEOUS_TICKET)) {
                        if (!ticket.getIsReviewRequired()) {
                            throw new IllegalArgumentException("Cannot create review ticket for this as review required for this is false.");
                        }
                    } else if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET)) {
                        CustomProduct customProduct = entityManager.find(CustomProduct.class, findProductFromItemAttribute(ticket.getOrder().getOrderItems().get(0)).getId());
                        if (!customProduct.getIsReviewRequired()) {
                            throw new IllegalArgumentException("Cannot create review ticket for this as review required for this is false.");
                        }
                    }
                    serviceProviderTicketService.createReviewTicket(ticket, createTicketDTO, files, tokenUserId, tokenRole);

                } else if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_REVIEW_TICKET) && ticketState.getTicketStateId().equals(Constant.TICKET_STATE_CLOSE)) {
                    if (createTicketDTO.getIsComplete() == null || createTicketDTO.getWorkQualityId() == null) {
                        throw new IllegalArgumentException("Is Complete and Work Quality is required to close a review ticket");
                    }
                    CustomWorkQuality workQuality = workQualityService.getWorkQualityByWorkQualityId(createTicketDTO.getWorkQualityId());
                    if (workQuality == null) {
                        throw new NotFoundException("Work Quality Not found with this Id");
                    }
                    Long workQualityId = workQuality.getWorkQualityId();
                    Long workQualityScore = 0L;
                    if (workQualityId == 1) {
                        workQualityScore = Constant.REVIEW_TICKET_FEEDBACK_HIGH;
                    } else if (workQualityId == 3) {
                        workQualityScore = Constant.REVIEW_TICKET_FEEDBACK_LOW;
                    }

                    if (createTicketDTO.getComment() == null || createTicketDTO.getComment().trim().isEmpty()) {
                        throw new IllegalArgumentException("Comment is mandatory for a ticket to close and in-review");
                    }

                    CustomServiceProviderTicket parentTicket = ticket.getParentTicket();
                    ServiceProviderEntity parentTicketAssignee = null;
                    if (parentTicket.getAssignee() != null) {
                        parentTicketAssignee = serviceProviderService.getServiceProviderById(parentTicket.getAssignee());
                    }

                    parentTicket.setComment(createTicketDTO.getComment().trim());
                    if (createTicketDTO.getIsComplete()) {
                        parentTicket.setTicketState(ticketStateService.getTicketStateByTicketStateId(Constant.TICKET_STATE_CLOSE));
                        parentTicket.setTicketStatus(ticketStatusService.getTicketStatusByTicketStatusId(15L));
                        parentTicket.setIsComplete(createTicketDTO.getIsComplete());
                        parentTicket.setWorkQuality(workQuality);

                        // Change order state in case of ticket completes. (in case of primary ticket only).
                        if (parentTicket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET)) {
                            CustomOrderState orderState = entityManager.find(CustomOrderState.class, parentTicket.getOrder().getId());
                            orderState.setOrderStateId(7);
                            orderState.setModifiedDate(new Date());
                            orderState.setModifierUserId(tokenUserId);
                            orderState.setModifierRole(tokenRole);
                            entityManager.merge(orderState);

                            // Earning section
                            earningService.createEarning(parentTicket);
                        }

                        if (parentTicketAssignee != null) {
                            ServiceProviderReRankingScoreDto serviceProviderReRankingScoreDto = new ServiceProviderReRankingScoreDto();
                            serviceProviderReRankingScoreDto.setReviewTicketStatusScore(Constant.REVIEW_TICKET_STATUS_SUCCESS);
                            serviceProviderReRankingScoreDto.setReviewTicketFeedbackScore(workQualityScore);
//                            parentTicketAssignee.setReviewTicketStatusScore(parentTicketAssignee.getReviewTicketStatusScore() + Constant.REVIEW_TICKET_STATUS_SUCCESS);
//                            parentTicketAssignee.setReviewTicketFeedbackScore(parentTicketAssignee.getReviewTicketFeedbackScore() + workQualityScore);

                            if (parentTicket.getTargetCompletionDate() != null) {
                                if (parentTicket.getTargetCompletionDate().before(parentTicket.getModifiedDate())) {
                                    serviceProviderReRankingScoreDto.setTimeCompletionScore(Constant.TIME_COMPLETION_FAIL);
//                                    parentTicketAssignee.setTimeCompletionScore(parentTicketAssignee.getTimeCompletionScore() + Constant.TIME_COMPLETION_FAIL);
                                } else {
                                    serviceProviderReRankingScoreDto.setTimeCompletionScore(Constant.TIME_COMPLETION_SUCCESS);
//                                    parentTicketAssignee.setTimeCompletionScore(parentTicketAssignee.getTimeCompletionScore() + Constant.TIME_COMPLETION_SUCCESS);
                                }
                            }

                            if (parentTicketAssignee.getTicketPending() == 0) {
                                throw new IllegalArgumentException("Ticket pending of assignee is 0 (value cannot be < 0 )");
                            }
                            parentTicketAssignee.setTicketPending(parentTicketAssignee.getTicketPending() - 1);
                            parentTicketAssignee.setTicketCompleted(parentTicketAssignee.getTicketCompleted() + 1);

                            serviceProviderReRankingScoreService.updateServiceProviderReRankingScore(parentTicketAssignee, serviceProviderReRankingScoreDto);
                            entityManager.merge(parentTicketAssignee);
                        }
                    } else {
                        parentTicket.setTicketState(ticketStateService.getTicketStateByTicketStateId(Constant.TICKET_STATE_IN_PROGRESS));
                        parentTicket.setTicketStatus(ticketStatusService.getTicketStatusByTicketStatusId(16L));
                        parentTicket.setIsComplete(createTicketDTO.getIsComplete());
                        parentTicket.setWorkQuality(workQuality);

                        if (parentTicketAssignee != null) {

                            ServiceProviderReRankingScoreDto serviceProviderReRankingScoreDto = new ServiceProviderReRankingScoreDto();
                            serviceProviderReRankingScoreDto.setReviewTicketStatusScore(Constant.REVIEW_TICKET_STATUS_FAIL);
                            serviceProviderReRankingScoreDto.setReviewTicketFeedbackScore(workQualityScore);
//                            parentTicketAssignee.setReviewTicketStatusScore(parentTicketAssignee.getReviewTicketStatusScore() + Constant.REVIEW_TICKET_STATUS_FAIL);
//                            parentTicketAssignee.setReviewTicketFeedbackScore(parentTicketAssignee.getReviewTicketFeedbackScore() + workQualityScore);

                            // Won't Update time completion score as it does not matter the ticket is not complete yet.
                            /*if(parentTicket.getTargetCompletionDate() != null) {
                                if(parentTicket.getTargetCompletionDate().before(parentTicket.getModifiedDate())) {
                                    parentTicketAssignee.setTimeCompletionScore(parentTicketAssignee.getTimeCompletionScore() + Constant.TIME_COMPLETION_FAIL);
                                } else {
                                    parentTicketAssignee.setTimeCompletionScore(parentTicketAssignee.getTimeCompletionScore() + Constant.TIME_COMPLETION_SUCCESS);
                                }
                            }*/

                            serviceProviderReRankingScoreService.updateServiceProviderReRankingScore(parentTicketAssignee, serviceProviderReRankingScoreDto);
                            entityManager.merge(parentTicketAssignee);
                        }
                    }

                    if (files != null) {
                        Set<ServiceProviderDocument> serviceProviderDocument = ticketStateService.updateTicketDocument(files, parentTicket, tokenUserId, tokenRole);
                        parentTicket.setServiceProviderDocuments(serviceProviderDocument);
                    }
                    entityManager.merge(parentTicket);

                } else if (ticketState.getTicketStateId().equals(Constant.TICKET_STATE_CLOSE)) {
                    if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_MISCELLANEOUS_TICKET)) {
                        if (ticket.getIsReviewRequired()) {
                            throw new IllegalArgumentException("Cannot close this ticket without creation of review ticket for this as review required for this is true.");
                        }
                    } else if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET)) {
                        CustomProduct customProduct = entityManager.find(CustomProduct.class, findProductFromItemAttribute(ticket.getOrder().getOrderItems().get(0)).getId());
                        if (customProduct.getIsReviewRequired() && !ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_SUPPORT) && !ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_ON_HOLD)) {
                            throw new IllegalArgumentException("Cannot close this ticket without creation of review ticket for this as review required for this is true.");
                        }
                    }
                    if (ticket.getAssignee() != null) {

                        ServiceProviderReRankingScoreDto serviceProviderReRankingScoreDto = new ServiceProviderReRankingScoreDto();

                        ServiceProviderEntity assignee = serviceProviderService.getServiceProviderById(ticket.getAssignee());
                        if (ticket.getTargetCompletionDate().before(new Date())) {
                            serviceProviderReRankingScoreDto.setTimeCompletionScore(Constant.TIME_COMPLETION_FAIL);
//                            assignee.setTimeCompletionScore(assignee.getTimeCompletionScore() + Constant.TIME_COMPLETION_FAIL);
                        } else {
                            serviceProviderReRankingScoreDto.setTimeCompletionScore(Constant.TIME_COMPLETION_SUCCESS);
//                            assignee.setTimeCompletionScore(assignee.getTimeCompletionScore() + Constant.TIME_COMPLETION_SUCCESS);
                        }

                        if (assignee.getTicketPending() == 0) {
                            throw new IllegalArgumentException("Ticket pending of assignee is 0 (value cannot be < 0 )");
                        }
                        assignee.setTicketPending(assignee.getTicketPending() - 1);
                        assignee.setTicketCompleted(assignee.getTicketCompleted() + 1);

                        serviceProviderReRankingScoreService.updateServiceProviderReRankingScore(assignee, serviceProviderReRankingScoreDto);
                        entityManager.merge(assignee);
                    }
                    if (createTicketDTO.getComment() == null || createTicketDTO.getComment().trim().isEmpty()) {
                        throw new IllegalArgumentException("Comment is mandatory");
                    }
                    ticket.setComment(createTicketDTO.getComment().trim());
                } else if (ticketState.getTicketStateId().equals(Constant.TICKET_STATE_SUPPORT)) {
                    if (createTicketDTO.getComment() == null || createTicketDTO.getComment().trim().isEmpty()) {
                        throw new IllegalArgumentException("Comment is mandatory for a ticket to close, in-review and support.");
                    }
                    ticket.setComment(createTicketDTO.getComment().trim());
                } else if (ticketState.getTicketStateId().equals(Constant.TICKET_STATE_RETURNED)) {
                    List<Long> rejectedList = ticket.getRejectedBy();
                    rejectedList.add(ticket.getAssignee());
                    ticket.setRejectedBy(rejectedList);

                    ticket.setAssignee(null);
                    ticket.setAssigneeRole(null);
                }

                if (!ticketState.getTicketStateId().equals(Constant.TICKET_STATE_IN_REVIEW) && !ticketState.getTicketStateId().equals(Constant.TICKET_STATE_SUPPORT) && !(ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_SUPPORT) && createTicketDTO.getTicketState().equals(Constant.TICKET_STATE_IN_PROGRESS)) && !ticketState.getTicketStateId().equals(Constant.TICKET_STATE_CLOSE) && files != null) {
                    throw new IllegalArgumentException("Files can only be uploaded when state changes to Review, Close and Support or From Support.");
                }

                ticket.setTicketStatus(ticketStatus);
                ticket.setTicketState(ticketState);

            } else if (createTicketDTO.getTicketStatus() != null) {

                ticketStatus = ticketStatusService.getTicketStatusByTicketStatusId(createTicketDTO.getTicketStatus());
                if (ticketStatus == null)
                    throw new NotFoundException("Ticket Status not found");

                if (ticket.getTicketStatus().equals(ticketStatus)) {
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

            } else if (createTicketDTO.getTicketState() != null) {
                throw new IllegalArgumentException("Ticket State cannot be changed without status.");
            }

            if (createTicketDTO.getTargetCompletionDate() != null) {
                if (sharedUtilityService.isInValidOrInPast(createTicketDTO.getTargetCompletionDate()) == 1) {
                    throw new IllegalArgumentException("Target Completion date cannot be in past");
                }
                if (sharedUtilityService.isInValidOrInPast(createTicketDTO.getTargetCompletionDate()) == -1)
                    throw new IllegalArgumentException("Invalid Date-Time");

                if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET)) {
                    Product product = findProductFromItemAttribute(ticket.getOrder().getOrderItems().get(0));

                    /*log.info("product active open date is: {}", product.getActiveStartDate());
                    log.info("product active end date is: {}", product.getActiveEndDate());*/

                    if (!createTicketDTO.getTargetCompletionDate().after(product.getActiveStartDate()) || !createTicketDTO.getTargetCompletionDate().before(product.getActiveEndDate())) {
                        throw new IllegalArgumentException("Target Completion date must be between Product Open Date and Close Date.");
                    }
                } else if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_REVIEW_TICKET) && ticket.getParentTicket().getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET)) {
                    Product product = findProductFromItemAttribute(ticket.getParentTicket().getOrder().getOrderItems().get(0));
//                    if (!createTicketDTO.getTargetCompletionDate().after(product.getActiveStartDate()) && !createTicketDTO.getTargetCompletionDate().before(product.getActiveEndDate())) {
//                        throw new IllegalArgumentException("Target Completion date must be between Product Open Date and 4 days after Close Date.");
//                    }

                    Date target = createTicketDTO.getTargetCompletionDate();
                    Date start = product.getActiveStartDate();
                    Date endPlus4Days = Date.from(
                            product.getActiveEndDate().toInstant().plus(4, java.time.temporal.ChronoUnit.DAYS)
                    );
                    /*log.info("product active open date is: {}", product.getActiveStartDate());
                    log.info("product active end date is: {}", product.getActiveEndDate());
                    log.info("product new active endDate is: {}", endPlus4Days);*/

                    if (target.before(start) || target.after(endPlus4Days)) {
                        throw new IllegalArgumentException("Target Completion date must be between Product Open Date and 4 days after Close Date.");
                    }

                }
                ticket.setTargetCompletionDate(createTicketDTO.getTargetCompletionDate());
            }

            // As only primary ticket will be linked to the order.
            /*if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET)) {
                Order order = orderService.findOrderById(ticket.getOrder().getId());
                order.getOrderAttributes().get("product_id");
                CustomOrderState orderState = entityManager.find(CustomOrderState.class, order.getId());
                query = entityManager.createNativeQuery(Constant.GET_ORDER_STATE_LINKED_WITH_TICKET);

                if (ticketState != null) {
                    query.setParameter("ticketStateId", createTicketDTO.getTicketState());
                    Integer orderStateId = (Integer) query.getFirstResult();
                    orderState.setOrderStateId(orderStateId);
                    orderState.setModifiedDate(new Date());
                    orderState.setModifierUserId(tokenUserId);
                    orderState.setModifierRole(tokenRole);
                    entityManager.merge(orderState);
                }
            }*/

            LocalDateTime localDateTime = LocalDateTime.now();
            Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            ticket.setModifiedDate(date);
            ticket.setModifierId(tokenUserId);
            ticket.setModifierRole(tokenRole);

            if (createTicketDTO.getComment() != null && !createTicketDTO.getComment().trim().isEmpty()) {
                ticket.setComment(createTicketDTO.getComment().trim());
            }

            // If there exists some files then upload them as well.
            if (files != null) {

                log.info("INSIDE FILES IS NOT EMPTY");
                Set<ServiceProviderDocument> serviceProviderDocument = updateTicketDocument(files, ticket, tokenUserId, tokenRole);
                ticket.setServiceProviderDocuments(serviceProviderDocument);
                ticket = entityManager.merge(ticket);

                // get the latest entry from the ticket history table.
                CustomTicketHistory ticketHistory = ticketHistoryService.fetchTicketHistoryByTicketId(ticket.getTicketId()).get(0);

                List<Long> previousTicketDocumentIds = new ArrayList<>();
                for (ServiceProviderDocument serviceProviderDocumentCopy : ticket.getServiceProviderDocuments()) {
                    previousTicketDocumentIds.add(serviceProviderDocumentCopy.getDocumentId());
                }

                log.info("old ticket file size is: {}", previousTicketDocumentIds.size());
                log.info("ticket history is: {}", ticketHistory.getTicketHistoryId());

                Set<ServiceProviderDocument> clonedDocuments = new HashSet<>();
                for (Long previousDocumentId : previousTicketDocumentIds) {
                    ServiceProviderDocument oldDocument = entityManager.find(ServiceProviderDocument.class, previousDocumentId);
                    oldDocument.setTicketHistory(ticketHistory);
                    oldDocument.setServiceProviderTicket(null);
                    clonedDocuments.add(oldDocument);
                    entityManager.persist(oldDocument); // or merge if needed
                }
                ticketHistory.setServiceProviderDocuments(clonedDocuments);
                entityManager.merge(ticketHistory);
            } else {
                ticket = entityManager.merge(ticket);
            }

            if(createTicketDTO.getAssigneeRole() != null && createTicketDTO.getAssigneeRole() != null) {
                serviceProviderActionController.sendTicketAllocationMail(serviceProvider, ticket);
            }

            // log.info("existing sp id: {}", existingServiceProvider.getService_provider_id());
            if(createTicketDTO.getTicketState() != null && createTicketDTO.getTicketState().equals(Constant.TICKET_STATE_RETURNED)) {
                serviceProviderActionController.sendTicketRejectionMail(existingServiceProvider, tokenServiceProvider ,ticket);
            }

            return ticket;

        } catch (PersistenceException persistenceException) {
            exceptionHandlingService.handleException(persistenceException);
            throw new PersistenceException("Cannot find valid result : " + persistenceException.getMessage());
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (NotFoundException notFoundException) {
            exceptionHandlingService.handleException(notFoundException);
            throw new NotFoundException(notFoundException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new RuntimeException("Error updating ticket :" + exception.getMessage());
        }
    }

    @Transactional
    public Set<ServiceProviderDocument> updateTicketDocument(List<MultipartFile> files, CustomServiceProviderTicket ticket, Long tokenUserId, Role role) throws Exception {
        try {

            List<Integer> fileTypes = new ArrayList<>();
            fileTypes.add(Constant.DOCUMENT_TYPE_TICKET_DOCUMENT_ID);
            // Grouping of list of files w.r.t document type here (document_type is file_type which is naming convention issue).
            Map<Integer, List<MultipartFile>> groupedFiles = new HashMap<>();
            for (int i = 0; i < files.size(); i++) {
                Integer fileTypeId = fileTypes.get(0); // here fileType id meaning documentTypeId
                MultipartFile file = files.get(i);
                groupedFiles.computeIfAbsent(fileTypeId, k -> new ArrayList<>()).add(file);
            }
            log.info("group document size is: {}", groupedFiles.size());
            log.info("group document files size is: {}", groupedFiles.get(32).size());

            // Keep track of documents to be saved
            Set<ServiceProviderDocument> serviceProviderDocumentToSave = new HashSet<>();
            Map<String, Object> responseData = serviceProviderService.updateServiceProviderTicketDocument(groupedFiles, tokenUserId, null, null, null, null, role.getRole_name(), null, ticket, serviceProviderDocumentToSave);
            log.info("FILES DATA: {}", responseData);

            return serviceProviderDocumentToSave;

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
            throw new Exception("Issue in File Server.");
        }
    }

    public Boolean canTransitTicket(CustomServiceProviderTicket customServiceProviderTicket, Long ticketStateId, String roleName, Long customTicketStatus) throws Exception {
        try {

            CustomTicketState nextState = getTicketStateByTicketStateId(ticketStateId);
            CustomTicketStatus status = ticketStatusService.getTicketStatusByTicketStatusId(customTicketStatus);

            if (customServiceProviderTicket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET)) {
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

            if (roleName.equals(Constant.roleServiceProvider)) {
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

            /*// Admin logic
            if (roleName.equals(Constant.roleAdmin) || roleName.equals(Constant.roleSuperAdmin)) {
                // Admin can transition to any state except from close
                return !customServiceProviderTicket.getTicketState().getTicketState().equals("CLOSE");
            }*/
            return true; // Default: No transition allowed
        } catch (NotFoundException notFoundexception) {
            throw new Exception(notFoundexception.getMessage());
        } catch (Exception exception) {
            throw new Exception(exception.getMessage());
        }
    }

    public TicketStateLinkage verifyState(Role role, CustomTicketType ticketType, CustomTicketState ticketStateFrom, CustomTicketState ticketStateTo) throws Exception {
        try {
            if (ticketType == null || ticketStateFrom == null || ticketStateTo == null) {
                throw new IllegalArgumentException("Ticket Type, Ticket State From and Ticket State To cannot be NULL");
            }

            Long ticketTypeId = ticketType.getTicketTypeId();
            Long ticketStateFromId = ticketStateFrom.getTicketStateId();
            Long ticketStateToId = ticketStateTo.getTicketStateId();
            Integer roleId = role.getRole_id();
            List<Integer> roleIds = new ArrayList<>();
            if (roleId.equals(2) || roleId.equals(1)) {
                roleIds.add(2);
                roleIds.add(4);
            } else {
                roleIds.add(4);
            }
            Query query = entityManager.createQuery(Constant.GET_TICKET_STATE_LINKAGE_BY_TICKET_TYPE_AND_TICKET_FROM_AND_TICKET, TicketStateLinkage.class);
            query.setParameter("ticketTypeId", ticketTypeId);
            query.setParameter("ticketStateIdFrom", ticketStateFromId);
            query.setParameter("ticketStateIdTo", ticketStateToId);
            query.setParameter("roleId", roleIds);

            List<TicketStateLinkage> ticketStateLinkageList = query.getResultList();
            if (ticketStateLinkageList == null || ticketStateLinkageList.isEmpty()) {
                throw new IllegalArgumentException("Linkage not Found between type and states.");
            }

            return ticketStateLinkageList.get(0);

        } catch (IllegalArgumentException illegalArgumentException) {
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

    // Schedules separated as No EntityManager with actual transaction available for current thread exception came.
    @Scheduled(cron = "0 50 7,15 * * *") // // 7:50 AM and 3:50 PM every day
    public void ticketAllocationMailScheduler() {
        transactionTemplate.execute(status -> {
            try {
                ticketAllocationMail(); // Now it runs inside a transaction
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    @Transactional
    public List<Map<String, Long>> ticketAllocationMail() throws Exception {
        try {

            List<Map<String, Long>> response = new ArrayList<>();
            List<EmailQueue> emailQueues = emailQueueService.getAllEmailQueue();

            for (EmailQueue emailQueue : emailQueues) {
                log.info("called for id: {}", emailQueue.getId());
                Map<String, Long> mailDetails = new HashMap<>();
                Long userId = emailQueue.getUserId();
                Role role = emailQueue.getRole();

//                Role role = roleService.getRoleByRoleId(roleId);
                CustomServiceProviderTicket ticket = null;
                if(emailQueue.getTicket() != null) {
                    ticket = emailQueue.getTicket();

                    if(ticket == null) {
                        log.info("Ticket not found with id: {}", ticket.getTicketId());
                        continue;
                    } else if (ticket.getArchived()) {
                        log.info("Ticket is archived with id: {}", ticket.getTicketId());
                        continue;
                    }

                } else {
                    log.info("Ticket is null");
                    continue;
                }

                /*if (role.getRole_name().equals(Constant.USER)) {
                    CustomCustomer customCustomer = customCustomerService.findCustomCustomerById(userId) ;
                    Customer customer = customerService.readCustomerById(userId);
                    if (customer == null) {
                        log.info("customer not found with id: {}", customer.getId());
                        continue;
                    }
//                    if(customer.get) {
//                        log.info("customer is archived with id: {}", customer.getId());
//                        continue;
//                    }

                    serviceProviderActionController.sendTicketAllocationMail(customer, ticket);

                } */
                if (role.getRole_name().equals(Constant.ADMIN) || role.getRole_name().equals(Constant.SUPER_ADMIN) || role.getRole_name().equals(Constant.SERVICE_PROVIDER)) {

                    ServiceProviderEntity serviceProvider = serviceProviderService.getServiceProviderById(userId);
                    if(serviceProvider == null) {
                        log.info("service provider not found with id: {}", userId);
                        continue;
                    }
                    if(serviceProvider.getIsArchived()) {
                        log.info("Service provider is archived with id: {}", serviceProvider.getService_provider_id());
                        continue;
                    }

                    serviceProviderActionController.sendTicketAllocationMail(serviceProvider, ticket);
                    mailDetails.put("ticket_id", ticket.getTicketId());
                    mailDetails.put("assignee_id", serviceProvider.getService_provider_id());
                    response.add(mailDetails);

                    emailQueue.setArchived(true);
                    entityManager.merge(emailQueue);
                    entityManager.flush();

                } else {
                    log.info("Invalid Role");
                }
            }
            return response;

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

}
