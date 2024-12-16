package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.CreateTicketDto;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomOrderState;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.CustomTicketState;
import com.community.api.entity.CustomTicketStatus;
import com.community.api.entity.CustomTicketType;
import com.community.api.entity.CustomerReferrer;
import com.community.api.entity.OrderStateRef;
import com.community.api.entity.Role;
import com.community.api.entity.SuccessResponse;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.exception.ExceptionHandlingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderImpl;
import org.broadleafcommerce.core.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class ServiceProviderTicketService {
    private static final Logger logger = LoggerFactory.getLogger(ServiceProviderTicketService.class);

    @Autowired
    ServiceProviderServiceImpl serviceProviderService;

    @Autowired
    OrderStateRefService orderStateRefService;

    @Autowired
    CustomOrderService customOrderService;

    @Autowired
    OrderService orderService;

    @Autowired
    TicketStateService ticketStateService;

    @Autowired
    TicketTypeService ticketTypeService;

    @Autowired
    TicketStatusService ticketStatusService;

    @Autowired
    JwtUtil jwtTokenUtil;

    @Autowired
    ProductService productService;

    @Autowired
    RoleService roleService;

    @Autowired
    EntityManager entityManager;

    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    @Autowired
    private RestTemplate restTemplate;

    @Scheduled(cron = "0 39 16 * * ?")
    @Transactional
    public void callApiAt7_30AM() {
        try {
            autoAssigner();
            logger.info("API called at 7:30 AM: ");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
        }
    }

    @Scheduled(cron = "0 30 15 * * ?")
    @Transactional
    public void callApiAt3_30PM() {
        try {
            autoAssigner();
            logger.info("API called at 3:30 PM: ");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
        }
    }

    public List<Order> autoAssigner() throws Exception {
        try {
            logger.info("AUTO-ASSIGNER");
            ResponseEntity<?> responseEntity = serviceProviderService.searchServiceProviderBasedOnGivenFields(null, null, null, null, null, 3L);

            // Check if the response body is of type SuccessResponse
            List<Map<String, Object>> availableServiceProvider = null;
            if (responseEntity.getBody() instanceof SuccessResponse) {
                SuccessResponse successResponse = (SuccessResponse) responseEntity.getBody();

                // Extract the data (which should be a List<Map<String, Object>>)
                if (successResponse.getData() instanceof List<?>) {
                    availableServiceProvider = (List<Map<String, Object>>) successResponse.getData();
                }
            } else {
                throw new RuntimeException("Unable to fetch the available service provider as not getting SuccessResponse");
            }

            if (availableServiceProvider.isEmpty()) {
                throw new IllegalArgumentException("No Service Provider is in required State.");
            }

            logger.info("Available service Provider according to condition(in autoAssigner()) method: " + availableServiceProvider.size());

            OrderStateRef orderStateRef = orderStateRefService.getOrderStateByOrderStateId(1);
            // auto assigner will run only on those order which are in NEW STATE.

            if (orderStateRef == null) {
                throw new IllegalArgumentException("No Order State Ref Found with id 1(NEW).");
            }

            List<CustomOrderState> customOrders = customOrderService.getCustomOrdersByOrderStateId(orderStateRef.getOrderStateId());
            if (customOrders.isEmpty()) {
                throw new IllegalArgumentException("No Orders to Assign");
            }
            List<Order> assignedOrders = new ArrayList<>();

            randomBindingTicketAllocation(customOrders, assignedOrders);
            verticalDistributionTicketAllocation(customOrders, availableServiceProvider, assignedOrders);

            return assignedOrders;
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new IllegalArgumentException("Illegal Argument exception caught: " + illegalArgumentException.getMessage());
        } catch (Exception exception) {
            throw new Exception("Exception caught: " + exception.getMessage());
        }
    }

    @Transactional
    public void randomBindingTicketAllocation(List<CustomOrderState> customOrders, List<Order> assignedOrders) throws Exception {
        try {
            logger.info("Random Binding Ticket Allocation");
            logger.info("Total Orders received by RBTA are: " + customOrders.size());

            boolean assigned;

            Iterator<CustomOrderState> iterator = customOrders.iterator();
            while (iterator.hasNext()) {

                CustomOrderState customOrderState = iterator.next();
                assigned = false;

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonString = objectMapper.writeValueAsString(customOrderState);
                logger.info(jsonString);

                Order order = orderService.findOrderById(customOrderState.getOrderId());
                CustomCustomer customer = entityManager.find(CustomCustomer.class, order.getCustomer().getId());

                List<CustomerReferrer> referrers = customer.getMyReferrer();
                logger.info("Referrer list for customer: "+ customer + " is: " + referrers.size());

                if(referrers.isEmpty()) {
                    logger.info("Referrer List is empty for custom: " + customer);
                    continue;
                }

                for (CustomerReferrer referrer : referrers) {
                    ServiceProviderEntity serviceProvider = referrer.getServiceProvider();
                    logger.info("REFERRER ID: " + serviceProvider.getService_provider_id());

                    if (serviceProvider.getIsActive()) {

                        if( (serviceProvider.getMaximumTicketSize() != null && serviceProvider.getTicketAssigned() + serviceProvider.getTicketPending() < serviceProvider.getMaximumTicketSize()) || (serviceProvider.getTicketAssigned() + serviceProvider.getTicketPending() < serviceProvider.getRanking().getMaximumTicketSize()) ) {
                            // assign him the ticket
                            // create a entry in serviceProvider tickets tables where the info about which serviceProvider is linked with which ticket is stored.
                            CreateTicketDto createTicketDto = new CreateTicketDto();
                            createTicketDto.setTicketState(1L);
                            createTicketDto.setTicketType(1L);
                            createTicketDto.setTicketStatus(1L);
                            createTicketDto.setAssignee(serviceProvider.getService_provider_id());
                            createTicketDto.setAssigneeRole(4);
                            createTicket(createTicketDto, (OrderImpl) order, serviceProvider,null,null);

                            customOrderState.setOrderStateId(Constant.ORDER_STATE_ASSIGNED.getOrderStateId());
                            entityManager.merge(customOrderState);
                            serviceProviderService.serviceProviderTicketAssignedIncrement(serviceProvider);

                            assigned = true;
                            iterator.remove();
                            assignedOrders.add(order);
                            break;
                        } else {
                            logger.info("Service Provider limit exceeded for the day - serviceProvider details: " + serviceProvider);
                        }
                    }
                }

                // If there is no one in referrer list of custom to whom we can assign this ticket
                if (!assigned) {

                    logger.info("INSIDE THE CREATOR OF THE PRODUCT LOGIC OF RBTA");
                    Long productId = Long.parseLong(order.getOrderItems().get(0).getOrderItemAttributes().get("productId").getValue());
                    CustomProduct customProduct = productService.getCustomProductByCustomProductId(productId);

                    ServiceProviderEntity serviceProvider = serviceProviderService.getServiceProviderById(customProduct.getUserId());
                    if (serviceProvider.getIsActive()) {

                        if ( (serviceProvider.getMaximumTicketSize() != null && serviceProvider.getTicketAssigned() + serviceProvider.getTicketPending() < serviceProvider.getMaximumTicketSize()) || (serviceProvider.getTicketAssigned() + serviceProvider.getTicketPending() < serviceProvider.getRanking().getMaximumTicketSize()) ) {
                            // assign him the ticket
                            // create a entry in serviceProvider tickets tables where the info about which serviceProvider is linked with which ticket is stored.
                            CreateTicketDto createTicketDto = new CreateTicketDto();
                            createTicketDto.setTicketState(1L);
                            createTicketDto.setTicketType(1L);
                            createTicketDto.setTicketStatus(1L);
                            createTicketDto.setAssignee(serviceProvider.getService_provider_id());
                            createTicketDto.setAssigneeRole(4);
                            createTicket(createTicketDto, (OrderImpl) order, serviceProvider,null,null);

                            customOrderState.setOrderStateId(Constant.ORDER_STATE_ASSIGNED.getOrderStateId());
                            entityManager.merge(customOrderState);
                            serviceProviderService.serviceProviderTicketAssignedIncrement(serviceProvider);

                            iterator.remove();
                            assignedOrders.add(order);
                            break;
                        } else {
                            logger.info("Service Provider limit exceeded for the day serviceProvider details: " + serviceProvider);
                        }
                    }
                }
            }
            logger.info("Total orders assigned by RBTA method is: " + assignedOrders.size());

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught: " + exception.getMessage());
        }
    }

    @Transactional
    public CustomServiceProviderTicket createTicket(CreateTicketDto createTicketDto, OrderImpl order, ServiceProviderEntity assignedTo,Integer creatorRoleId,Long creatorId) throws Exception {
        try {
            CustomServiceProviderTicket customServiceProviderTicket = new CustomServiceProviderTicket();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Set active start date to current date and time in "yyyy-MM-dd HH:mm:ss" format
            String formattedDate = dateFormat.format(new Date());
            Date createdDate = dateFormat.parse(formattedDate);
            if (createTicketDto.getTargetCompletionDate() != null) {
                System.out.println("assigned date :"+createdDate);
                System.out.println( " tc date :"+createTicketDto.getTargetCompletionDate());
                if (!(createTicketDto.getTargetCompletionDate().after(new Date()))) {
                    ResponseService.generateErrorResponse("TARGET COMPLETION DATE MUST BE OF FUTURE", HttpStatus.NOT_FOUND);
                }
            } else {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(createdDate);
                calendar.add(Calendar.HOUR_OF_DAY, 4);
                Date newTargetDate = calendar.getTime();

                createTicketDto.setTargetCompletionDate(newTargetDate);
            }
            customServiceProviderTicket.setTicketAssignDate(createdDate);
            customServiceProviderTicket.setTargetCompletionDate(createTicketDto.getTargetCompletionDate());
            customServiceProviderTicket.setCreatedDate(createdDate);
            customServiceProviderTicket.setOrder(order);
            if(creatorId != null && creatorRoleId != null) {
                customServiceProviderTicket.setCreatorRole(roleService.getRoleByRoleId(creatorRoleId));
                customServiceProviderTicket.setUserId(creatorId);
            }
            customServiceProviderTicket.setModifiedDate(customServiceProviderTicket.getCreatedDate());
            Role role = roleService.getRoleByRoleId(createTicketDto.getAssigneeRole());
            customServiceProviderTicket.setAssigneeRole(role);

            if (assignedTo != null) {
                customServiceProviderTicket.setAssignee(assignedTo.getService_provider_id());
            }

            if (createTicketDto.getTicketState() != null) {
                CustomTicketState ticketState = ticketStateService.getTicketStateByTicketId(createTicketDto.getTicketState());
                customServiceProviderTicket.setTicketState(ticketState);
            } else {
                throw new IllegalArgumentException("Ticket State is mandatory field while creating a ticket");
            }

            if (createTicketDto.getTicketType() != null) {
                CustomTicketType ticketType = ticketTypeService.getTicketTypeByTicketTypeId(createTicketDto.getTicketType());
                customServiceProviderTicket.setTicketType(ticketType);
            } else {
                throw new IllegalArgumentException("Ticket Type is mandatory field while creating a ticket");
            }

            if (createTicketDto.getTicketStatus() != null) {
                CustomTicketStatus ticketStatus = ticketStatusService.getTicketStatusByTicketStatusId(createTicketDto.getTicketStatus());
                customServiceProviderTicket.setTicketStatus(ticketStatus);
            }

            customServiceProviderTicket = entityManager.merge(customServiceProviderTicket);
            return customServiceProviderTicket;

        } catch (IllegalArgumentException illegalArgumentException) {
            throw new IllegalArgumentException("Illegal Exception Caught: " + illegalArgumentException.getMessage());
        } catch (Exception exception) {
            throw new Exception(exception.getMessage());
        }
    }

    @Transactional
    public CustomServiceProviderTicket manualTicketCreation(CreateTicketDto createTicketDto) throws Exception {
        try {
            CustomServiceProviderTicket customServiceProviderTicket = new CustomServiceProviderTicket();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Set active start date to current date and time in "yyyy-MM-dd HH:mm:ss" format
            String formattedDate = dateFormat.format(new Date());
            Date createdDate = dateFormat.parse(formattedDate);

            if (createTicketDto.getTargetCompletionDate() != null) {
                dateFormat.parse(dateFormat.format(createTicketDto.getTargetCompletionDate()));
                if (!createTicketDto.getTargetCompletionDate().after(createdDate)) {
                    ResponseService.generateErrorResponse("TARGET COMPLETION DATE MUST BE OF FUTURE", HttpStatus.NOT_FOUND);
                }
                createTicketDto.setTargetCompletionDate(createTicketDto.getTargetCompletionDate());
            } else {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(createdDate);
                calendar.add(Calendar.HOUR_OF_DAY, 4);
                Date newTargetDate = calendar.getTime();

                createTicketDto.setTargetCompletionDate(newTargetDate);
            }

            customServiceProviderTicket.setTargetCompletionDate(createTicketDto.getTargetCompletionDate());
            customServiceProviderTicket.setCreatedDate(createdDate);

            CustomTicketState ticketState = ticketStateService.getTicketStateByTicketId(createTicketDto.getTicketState());
            CustomTicketType ticketType = ticketTypeService.getTicketTypeByTicketTypeId(createTicketDto.getTicketType());
            CustomTicketStatus ticketStatus = ticketStatusService.getTicketStatusByTicketStatusId(createTicketDto.getTicketStatus());

            customServiceProviderTicket.setTicketState(ticketState);
            customServiceProviderTicket.setTicketType(ticketType);
            customServiceProviderTicket.setTicketStatus(ticketStatus);

            customServiceProviderTicket = entityManager.merge(customServiceProviderTicket);
            return customServiceProviderTicket;
        } catch (Exception exception) {
            throw new Exception("Some Exception Caught: " + exception.getMessage());
        }
    }

    @Transactional
    public void verticalDistributionTicketAllocation(List<CustomOrderState> customOrders, List<Map<String, Object>> availableServiceProvider, List<Order> assignedOrders) throws Exception {
        try {
            logger.info("Vertical Distribution Ticket Allocation");
            logger.info("Total orders received for VDTA: " + customOrders.size());
            logger.info("Total Service Provider: " + availableServiceProvider.size());

            Iterator<CustomOrderState> iterator = customOrders.iterator();

            while (iterator.hasNext()) {

                CustomOrderState customOrderState = iterator.next();

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonString = objectMapper.writeValueAsString(customOrderState);
                logger.info(jsonString);

                Order order = orderService.findOrderById(customOrderState.getOrderId());

                for (Map<String, Object> serviceProviderMap : availableServiceProvider) {
                    ServiceProviderEntity serviceProvider = serviceProviderService.getServiceProviderById(Long.valueOf(serviceProviderMap.get("service_provider_id").toString()));

                    if (serviceProvider.getIsActive()) {
                        logger.info("sp id:" + serviceProvider.getService_provider_id());
                        if ( (serviceProvider.getMaximumTicketSize() != null && serviceProvider.getTicketAssigned() + serviceProvider.getTicketPending() < serviceProvider.getMaximumTicketSize()) || (serviceProvider.getTicketAssigned() + serviceProvider.getTicketPending() < serviceProvider.getRanking().getMaximumTicketSize()) ) {
                            logger.info("sp who are active and approved: " + serviceProvider.getService_provider_id());
                            CreateTicketDto createTicketDto = new CreateTicketDto();
                            createTicketDto.setTicketState(1L);
                            createTicketDto.setTicketType(1L);
                            createTicketDto.setTicketStatus(1L);
                            createTicketDto.setAssignee(serviceProvider.getService_provider_id());
                            createTicketDto.setAssigneeRole(4);
                            createTicket(createTicketDto, (OrderImpl) order, serviceProvider,null,null);

                            customOrderState.setOrderStateId(Constant.ORDER_STATE_ASSIGNED.getOrderStateId());
                            entityManager.merge(customOrderState);
                            serviceProviderService.serviceProviderTicketAssignedIncrement(serviceProvider);

                            iterator.remove();
                            assignedOrders.add(order);
                            break;
                        } else {
                            logger.info("sp who are active and approved: else " + serviceProvider.getService_provider_id());
                            logger.info("Service Provider limit exceeded for the day - serviceProvider details: " + serviceProvider);
                        }
                    }
                }
            }
            logger.info("Total orders assigned by VDTA method is: " + assignedOrders.size());


        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught: " + exception.getMessage());
        }
    }

    public List<CustomServiceProviderTicket> getAllTickets() throws Exception {
        try {
            String sql = "SELECT * FROM custom_service_provider_ticket";
            return entityManager.createNativeQuery(sql, CustomServiceProviderTicket.class).getResultList();
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught: " + exception.getMessage());
        }
    }

    public List<CustomServiceProviderTicket> filterTicket(List<Long> states, List<Long> types, Long userId, Role role, Date dateFrom, Date dateTo) throws Exception {
        try {
            // Initialize the JPQL query
            StringBuilder jpql = new StringBuilder("SELECT c FROM CustomServiceProviderTicket c ")
                    .append("WHERE 1=1 "); // Use this to simplify appending conditions

            // List to hold query parameters
            List<CustomTicketState> customTicketStates = new ArrayList<>();
            List<CustomTicketType> customTicketTypes = new ArrayList<>();

            // Conditionally build the query
            if (states != null && !states.isEmpty()) {
                for (Long id : states) {
                    CustomTicketState ticketState = ticketStateService.getTicketStateByTicketId(id);
                    if (ticketState == null) {
                        throw new IllegalArgumentException("NO TICKET STATE FOUND WITH THIS ID: " + id);
                    }
                    customTicketStates.add(ticketState);
                }
                jpql.append("AND c.ticketState IN :states ");
            }

            if (types != null && !types.isEmpty()) {
                for (Long id : types) {
                    CustomTicketType ticketType = ticketTypeService.getTicketTypeByTicketTypeId(id);
                    if (ticketType == null) {
                        throw new IllegalArgumentException("NO TICKET TYPE FOUND WITH THIS ID: " + id);
                    }
                    customTicketTypes.add(ticketType);
                }
                jpql.append("AND c.ticketType IN :types ");
            }

            if (dateFrom != null && dateTo != null) {
                jpql.append("AND c.createdDate >= :dateFrom AND c.createdDate <= :dateTo ");
            }

            if (userId != null && role != null) {
                jpql.append("AND c.assignee = :userId AND c.assigneeRole = :role ");
            }

            // Create the query with the final JPQL string
            TypedQuery<CustomServiceProviderTicket> query = entityManager.createQuery(jpql.toString(), CustomServiceProviderTicket.class);

            // Set parameters
            if (!customTicketStates.isEmpty()) {
                query.setParameter("states", customTicketStates);
            }
            if (!customTicketTypes.isEmpty()) {
                query.setParameter("types", customTicketTypes);
            }
            if (dateFrom != null && dateTo != null) {
                query.setParameter("dateFrom", dateFrom);
                query.setParameter("dateTo", dateTo);
            }
            if (userId != null && role != null) {
                query.setParameter("userId", userId);
                query.setParameter("role", role);
            }

            // Execute and return the result
            return query.getResultList();
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught: " + exception.getMessage());
        }
    }

    public CustomServiceProviderTicket fetchTicketByTicketId(Long ticketId) throws Exception {
        try {
            if (ticketId == null || ticketId <= 0) {
                throw new IllegalArgumentException("TicketId cannot be <=0 or null");
            }

            Query query = entityManager.createQuery(Constant.GET_CUSTOM_SERVICE_PROVIDER_TICKET_BY_TICKET_ID, CustomServiceProviderTicket.class);
            query.setParameter("ticketId", ticketId);
            List<CustomServiceProviderTicket> ticket = query.getResultList();

            if (!ticket.isEmpty()) {
                return ticket.get(0);
            } else {
                return null;
            }

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught: " + exception.getMessage());
        }
    }
}
