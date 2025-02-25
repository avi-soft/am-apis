package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.CreateTicketDto;
import com.community.api.dto.CustomTicketWrapper;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CombinedOrderDTO;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomOrderState;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.CustomTicketState;
import com.community.api.entity.CustomTicketStatus;
import com.community.api.entity.CustomTicketType;
import com.community.api.entity.CustomerReferrer;
import com.community.api.entity.OrderCustomerDetailsDTO;
import com.community.api.entity.OrderStateRef;
import com.community.api.entity.Role;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.exception.ExceptionHandlingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderImpl;
import org.broadleafcommerce.core.order.service.OrderService;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private javax.sql.DataSource dataSource;

    @Autowired
    OrderService orderService;

    @Autowired
    JdbcTemplate jdbcTemplate;

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
    CustomerAddressFetcher addressFetcher;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private OrderDTOService orderDTOService;
    @Autowired
    private CustomerService customerService;

    // auto-assigner scheduled to execute at 7:30 AM
    @Scheduled(cron = "0 30 7 * * ?")
    @Transactional
    public void callApiAt7_30AM() {
        try {
            autoAssigner();
            logger.info("API called at 7:30 AM: ");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
        }
    }

    // auto-assigner scheduled to execute at 3:30 PM
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


    public List<Long> getAssignedTickets() throws IOException {
        List<Long> ticketList = new ArrayList<>();
        String scriptPathForAutoAssigner = "auto_assigner.sql";
        String sqlScript = new BufferedReader(
                new InputStreamReader(new ClassPathResource(scriptPathForAutoAssigner).getInputStream())
        ).lines().collect(Collectors.joining("\n"));
        // Execute the query using a callback to access the underlying Connection and Statement
        jdbcTemplate.execute((Connection connection) -> {
            try (Statement stmt = connection.createStatement()) {

                // Execute the PL/pgSQL block
                stmt.execute(sqlScript);

                // Define a regex pattern to extract the value after "Assigned Tickets:"
                Pattern pattern = Pattern.compile("Assigned Tickets:\\s*(\\{.*?\\}|<NULL>|[^,\\s]+)");

                // Retrieve all SQLWarnings (which include RAISE NOTICE messages)
                SQLWarning warning = stmt.getWarnings();
                while (warning != null) {
                    String message = warning.getMessage();

                    // Look for the "Assigned Tickets:" part in the warning message
                    Matcher matcher = pattern.matcher(message);
                    if (matcher.find()) {
                        String assignedTicketsValue = matcher.group(1);
                        System.out.println("Assigned Tickets Value: " + assignedTicketsValue);

                        // Check if the value is an array (in curly braces)
                        if (assignedTicketsValue.startsWith("{") && assignedTicketsValue.endsWith("}")) {
                            // Remove the curly braces and split the string by commas
                            String[] ticketIds = assignedTicketsValue.substring(1, assignedTicketsValue.length() - 1).split(",");
                            // Convert the split strings into a List of Longs

                            for (String ticketId : ticketIds) {
                                try {
                                    ticketList.add(Long.parseLong(ticketId.trim()));
                                } catch (NumberFormatException e) {
                                    // Handle the case where a value is not a valid Long
                                    System.err.println("Invalid ticket ID: " + ticketId);
                                }
                            }
                            System.out.println("Converted ticket IDs to List<Long>: " + ticketList);
                        }
                    }

                    // Move to the next warning if present
                    warning = warning.getNextWarning();
                }
            } catch (SQLException e) {
                // Handle SQL exceptions if necessary
                e.printStackTrace();
            }
            return null;  // No need to return anything here
        });

        // Now that the callback has completed, return the populated ticketList
        return ticketList;
    }








    public List<CustomTicketWrapper> autoAssigner() throws Exception {
        try {
            logger.info("AUTO-ASSIGNER");
            /*
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
            */

            // auto assigner will run only on those order which are in NEW STATE. (Later we can change this.)
            OrderStateRef orderStateRef = orderStateRefService.getOrderStateByOrderStateId(1);

            if (orderStateRef == null) {
                throw new IllegalArgumentException("No Order State Ref Found with id 1(NEW).");
            }

            // Fetch all the Orders for auto-assignment and handle the exception as well.
            List<CustomOrderState> customOrders = customOrderService.getCustomOrdersByOrderStateId(orderStateRef.getOrderStateId());
            System.out.println("size"+customOrders.size());
            if (customOrders.isEmpty()) {
                throw new IllegalArgumentException("No Orders to Assign");
            }

            // created a list which will keep the records of the tickets that are assigned by the auto-assigned.
            List<CustomTicketWrapper> assignedTickets = new ArrayList<>();

            /*
             RBTA logic- (ONLY FOR THOSE ORDERS WHOSE CUSTOMER OR USER IS BINDED WITH SOME SERVICE PROVIDER).
             This will traverse the orders one by one and see if their referee( primary binded sp) have the capacity to fulfill this ticket or not
              - If yes then it will be allocated to that Service Provider which are active and have a capacity to fulfill this ticket.
              - If no then it will be allocated to rest referees if any which are active and have a maximum bandwidth.
              - If it is not handled by the upper two cases then we try to allocate it to the creator of the product (However we are handling that case through adding the creator as the customer referee at the time of placing a order.) so there is no point of this logic but right now we have keep this logic as well in future we can comment out this code.
              - If it got placed then we change the order state from the un-assigned order to assigned order.
              - If not then we move this order to VDTA (that we react at once after RBTA is done).
              - Also there is a logic to give that order to the product creator but as of now as the product creator is already a referee of the customer so that condition is already been handled (as of now i am commenting that code but according to requirement of the client we can uncomment that part of RBTA.
            */
            randomBindingTicketAllocation(customOrders, assignedTickets);

            // Here we are fetching all the service provider who are approved and active.
            List<ServiceProviderEntity> availableServiceProvider = serviceProviderService.getActiveAndApprovedServiceProviders();

            /*
             VDTA logic- (FOR THOSE ORDERS WHICH ARE NOT ALLOCATED BY RBTA AND UNBINDED ORDERS).
             This will Fetch all the service Provider which are in active state and are approved.
             - We are bifurcating these serviceProvider in different ranks as according to the document we are allocating the document from Professional to Individual (Vertical Distribution) and From Rank inside the Professional again from 1a-1d and 2a-2d.
             - For the bifurcating we are using priority Queues as its more optimised way as for each rank we have to do horizontal allocating depending on the bandwidth of the service Provider.
             - So one by one we traverse the orders that are new state and starts from the vertical distribution and run the allocation algo for service providers in each rank.
             - From the Service Providers in the same rank we try to allocate the particular ticket the Service Provider who have the maximum capacity. and update the priority queue
             - If service Provider limit is reached then we remove the Service Provider from the Priority List.
            */
            verticalDistributionTicketAllocation(customOrders, availableServiceProvider, assignedTickets);

            return assignedTickets;
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            throw new Exception("Exception caught: " + exception.getMessage());
        }
    }

    public boolean allocateTicket(Order order, ServiceProviderEntity serviceProvider, CustomOrderState customOrderState, CustomCustomer customer, List<CustomTicketWrapper> assignedTickets) throws Exception {
        try {
            logger.info("PRIMARY REFERRER(SERVICE PROVIDER) ID: " + serviceProvider.getService_provider_id());
            if ((serviceProvider.getMaximumTicketSize() != null &&(serviceProvider.getIsActive().equals(true))&& serviceProvider.getTicketAssigned() + serviceProvider.getTicketPending() < serviceProvider.getMaximumTicketSize()) || (serviceProvider.getTicketAssigned() + serviceProvider.getTicketPending() < serviceProvider.getRanking().getMaximumTicketSize())) {
                // assign him the ticket
                // create a entry in serviceProvider ticket table where the info about which serviceProvider is linked with which ticket is stored.
                CreateTicketDto createTicketDto = new CreateTicketDto();
                createTicketDto.setTicketState(1L);
                createTicketDto.setTicketType(1L);
                createTicketDto.setTicketStatus(1L);
                createTicketDto.setAssignee(serviceProvider.getService_provider_id());
                createTicketDto.setAssigneeRole(4);
                CustomServiceProviderTicket ticket = createTicket(createTicketDto, (OrderImpl) order, serviceProvider, null, null);

                customOrderState.setOrderStateId(Constant.ORDER_STATE_ASSIGNED.getOrderStateId());
                entityManager.merge(customOrderState);

                // Increment the ticket assigned to the Service Provider
                serviceProviderService.serviceProviderTicketAssignedIncrement(serviceProvider);

                // Ticket Wrapper for the response in auto-assigner.
                CustomTicketWrapper wrapper = new CustomTicketWrapper();

                CustomOrderState orderState = entityManager.find(CustomOrderState.class, ticket.getOrder().getId());
                CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer.getId());
                OrderCustomerDetailsDTO customerDetailsDTO = new OrderCustomerDetailsDTO(customer.getId(), customer.getFirstName() + " " + customer.getLastName(), customer.getEmailAddress(), customCustomer.getMobileNumber(), addressFetcher.fetch(customer), customer.getUsername());
                CombinedOrderDTO orderDto = orderDTOService.wrapOrder(ticket.getOrder(), orderState, ticket, customerDetailsDTO);

                wrapper.customWrapDetails(ticket, orderDto);
                assignedTickets.add(wrapper);
                return true;
            } else {
                logger.info("Service Provider limit exceeded for the day - serviceProvider details: " + serviceProvider);
            }
            return false;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some Exception Caught: " + exception.getMessage());
        }
    }

    @Transactional
    public void randomBindingTicketAllocation(List<CustomOrderState> customOrders, List<CustomTicketWrapper> assignedTickets) throws Exception {
        try {
            logger.info("Random Binding Ticket Allocation (RBTA)");
            logger.info("Total Orders received by RBTA are: " + customOrders.size());

            boolean assigned;

            // Created a iterator that will iterator each order.
            Iterator<CustomOrderState> iterator = customOrders.iterator();
            while (iterator.hasNext()) {

                CustomOrderState customOrderState = iterator.next();
                assigned = false;

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonString = objectMapper.writeValueAsString(customOrderState);
                logger.info("order state logger: " + jsonString);

                // Fetch Order and customer from customOrderState and order respectively.
                Order order = orderService.findOrderById(customOrderState.getOrderId());
                CustomCustomer customer = entityManager.find(CustomCustomer.class, order.getCustomer().getId());

                // Fetch all the referees.
                List<CustomerReferrer> referrers = customer.getMyReferrer();
                logger.info("Customer whose id is: " + customer.getId() + " have a referrer list of size: " + referrers.size());

                if (referrers.isEmpty()) {
                    continue;
                }

                // PRIMARY BINDED LOGIC OF RANDOM BINDING TICKET ALLOCATION (RBTA)
                for (CustomerReferrer referrer : referrers) {
                    // Traverse the Referrers one by one
                    ServiceProviderEntity serviceProvider = referrer.getServiceProvider();

                    // Check if the referee is the primary Referee.
                    if (referrer.getPrimaryRef() != null && referrer.getPrimaryRef() == true && serviceProvider.getIsActive() != null && serviceProvider.getIsActive()) {
                        assigned = allocateTicket(order, serviceProvider, customOrderState, customer, assignedTickets);
                    }
                }

                // For the Remaining Referees
                if (!assigned) {
                    for (CustomerReferrer referrer : referrers) {
                        ServiceProviderEntity serviceProvider = referrer.getServiceProvider();
                        logger.info("REFERRER ID: " + serviceProvider.getService_provider_id());

                        assigned = allocateTicket(order, serviceProvider, customOrderState, customer, assignedTickets);
                    }
                }

                // If there is no one in referrer list of custom to whom we can assign this ticket then we will try to assign the ticket to the creator of the product.
                if (!assigned) {

                    logger.info("INSIDE THE CREATOR OF THE PRODUCT LOGIC OF RBTA");
                    Long productId = Long.parseLong(order.getOrderItems().get(0).getOrderItemAttributes().get("productId").getValue());
                    CustomProduct customProduct = productService.getCustomProductByCustomProductId(productId);

                    ServiceProviderEntity serviceProvider = serviceProviderService.getServiceProviderById(customProduct.getUserId());
                    allocateTicket(order, serviceProvider, customOrderState, customer, assignedTickets);
                }
            }
            logger.info("Total orders assigned by RBTA method is: " + assignedTickets.size());

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught: " + exception.getMessage());
        }
    }

    @Transactional
    public CustomServiceProviderTicket createTicket(CreateTicketDto createTicketDto, OrderImpl order, ServiceProviderEntity assignedTo, Integer creatorRoleId, Long creatorId) throws Exception {
        try {
            CustomServiceProviderTicket customServiceProviderTicket = new CustomServiceProviderTicket();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Set active start date to current date and time in "yyyy-MM-dd HH:mm:ss" format
            String formattedDate = dateFormat.format(new Date());
            Date createdDate = dateFormat.parse(formattedDate);
            if (createTicketDto.getTargetCompletionDate() != null && !(createTicketDto.getTargetCompletionDate().after(new Date()))) {
                ResponseService.generateErrorResponse("TARGET COMPLETION DATE MUST BE OF FUTURE", HttpStatus.NOT_FOUND);
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
            if(createTicketDto.getTicketType()==3)
                customServiceProviderTicket.setDesc(createTicketDto.getTask());
            if (creatorId != null && creatorRoleId != null) {
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
            if(createTicketDto.getAssigneeRole()==4)
            {
                ServiceProviderEntity serviceProvider=entityManager.find(ServiceProviderEntity.class,createTicketDto.getAssignee());
                serviceProvider.setTicketAssigned(serviceProvider.getTicketAssigned()+1);
                entityManager.merge(serviceProvider);
            }
            customServiceProviderTicket = entityManager.merge(customServiceProviderTicket);
            return customServiceProviderTicket;

        } catch (IllegalArgumentException illegalArgumentException) {
            throw new IllegalArgumentException("Illegal Exception Caught: " + illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
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
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some Exception Caught: " + exception.getMessage());
        }
    }

    public void bifurcateAvailableServiceProviders(List<ServiceProviderEntity> availableServiceProviders, PriorityQueue<ServiceProviderEntity> rank1a, PriorityQueue<ServiceProviderEntity> rank1b,
                                                   PriorityQueue<ServiceProviderEntity> rank1c,
                                                   PriorityQueue<ServiceProviderEntity> rank1d,
                                                   PriorityQueue<ServiceProviderEntity> rank2a,
                                                   PriorityQueue<ServiceProviderEntity> rank2b,
                                                   PriorityQueue<ServiceProviderEntity> rank2c,
                                                   PriorityQueue<ServiceProviderEntity> rank2d) throws Exception {

        // Loop through the list of available service providers
        for (ServiceProviderEntity serviceProvider : availableServiceProviders) {
            try {

                // Determine the rank of the service provider (this logic needs to be based on your use case)
                String rank = serviceProvider.getRanking().getRank_name(); // Assuming 'getRank' returns a rank name like "rank1a", "rank1b", etc.

                // Add the service provider to the corresponding priority queue based on the rank
                switch (rank) {
                    case "1a":
                        rank1a.offer(serviceProvider);
                        break;
                    case "1b":
                        rank1b.offer(serviceProvider);
                        break;
                    case "1c":
                        rank1c.offer(serviceProvider);
                        break;
                    case "1d":
                        rank1d.offer(serviceProvider);
                        break;
                    case "2a":
                        rank2a.offer(serviceProvider);
                        break;
                    case "2b":
                        rank2b.offer(serviceProvider);
                        break;
                    case "2c":
                        rank2c.offer(serviceProvider);
                        break;
                    case "2d":
                        rank2d.offer(serviceProvider);
                        break;
                    default:
                        // Handle cases where rank is unrecognized
                        break;
                }
            } catch (Exception exception) {
                exceptionHandlingService.handleException(exception);
                throw new Exception("Some Exception occured while bifurcation: " + exception.getMessage());
            }
        }
    }

    public boolean processRank(PriorityQueue<ServiceProviderEntity> rankedServiceProvider, Order order, List<CustomTicketWrapper> assignedTickets, CustomOrderState customOrderState) throws Exception {
        try {

            // while the rankedService Provider is not empty.
            while (!rankedServiceProvider.isEmpty()) {
                ServiceProviderEntity serviceProvider = rankedServiceProvider.poll();

                double bandwidth = 0.0;
                if(serviceProvider.getMaximumTicketSize() != null){
                    bandwidth = (double) (serviceProvider.getTicketAssigned() + serviceProvider.getTicketPending()) / serviceProvider.getMaximumTicketSize() * 100;
                } else {
                    bandwidth = (double) (serviceProvider.getTicketAssigned() + serviceProvider.getTicketPending()) / serviceProvider.getRanking().getMaximumTicketSize() * 100;
                }
                // if the capacity is reached then continue to next service provider.
                if (bandwidth >= 100.0) {
                    logger.info("Service Provider limit exceeded for the day - serviceProvider details: " + serviceProvider.getService_provider_id());
                    continue;
                }

                // assign him the ticket
                // create a entry in serviceProvider ticket table where the info about which serviceProvider is linked with which ticket is stored.
                CreateTicketDto createTicketDto = new CreateTicketDto();
                createTicketDto.setTicketState(1L);
                createTicketDto.setTicketType(1L);
                createTicketDto.setTicketStatus(1L);
                createTicketDto.setAssignee(serviceProvider.getService_provider_id());
                createTicketDto.setAssigneeRole(4);
                CustomServiceProviderTicket ticket = createTicket(createTicketDto, (OrderImpl) order, serviceProvider, null, null);

                customOrderState.setOrderStateId(Constant.ORDER_STATE_ASSIGNED.getOrderStateId());
                entityManager.merge(customOrderState);

                // updated service provider ticket assigned data in db
                serviceProviderService.serviceProviderTicketAssignedIncrement(serviceProvider);

                // updated service provider ticket assigned data in the PriorityQueue.
                rankedServiceProvider.offer(serviceProvider);

                CustomTicketWrapper wrapper = new CustomTicketWrapper();

                CustomOrderState orderState = entityManager.find(CustomOrderState.class, ticket.getOrder().getId());
                Customer customer = customerService.readCustomerById(ticket.getOrder().getCustomer().getId());
                CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer.getId());
                OrderCustomerDetailsDTO customerDetailsDTO = new OrderCustomerDetailsDTO(customer.getId(), customer.getFirstName() + " " + customer.getLastName(), customer.getEmailAddress(), customCustomer.getMobileNumber(), addressFetcher.fetch(customer), customer.getUsername());
                CombinedOrderDTO orderDto = orderDTOService.wrapOrder(ticket.getOrder(), orderState, ticket, customerDetailsDTO);

                wrapper.customWrapDetails(ticket, orderDto);
                assignedTickets.add(wrapper);

                logger.info("Order with id: " + order.getId() + " is assigned to Service Provider with id: " + serviceProvider.getService_provider_id() + " with ticket id: " + ticket.getTicketId());
                return true;
            }
            return false;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some Exception Caught: " + exception.getMessage());
        }
    }

    @Transactional
    public void verticalDistributionTicketAllocation(List<CustomOrderState> customOrders, List<ServiceProviderEntity> availableServiceProvider, List<CustomTicketWrapper> assignedTickets) throws Exception {
        try {
            logger.info("Vertical Distribution Ticket Allocation");
            logger.info("Total orders received for VDTA: " + customOrders.size());
            logger.info("Total Service Provider: " + availableServiceProvider.size());

            Iterator<CustomOrderState> iterator = customOrders.iterator();

            // Initialized the service provider with different ranks.
            PriorityQueue<ServiceProviderEntity> rank1a = new PriorityQueue<>(new ServiceProviderComparator());
            PriorityQueue<ServiceProviderEntity> rank1b = new PriorityQueue<>(new ServiceProviderComparator());
            PriorityQueue<ServiceProviderEntity> rank1c = new PriorityQueue<>(new ServiceProviderComparator());
            PriorityQueue<ServiceProviderEntity> rank1d = new PriorityQueue<>(new ServiceProviderComparator());
            PriorityQueue<ServiceProviderEntity> rank2a = new PriorityQueue<>(new ServiceProviderComparator());
            PriorityQueue<ServiceProviderEntity> rank2b = new PriorityQueue<>(new ServiceProviderComparator());
            PriorityQueue<ServiceProviderEntity> rank2c = new PriorityQueue<>(new ServiceProviderComparator());
            PriorityQueue<ServiceProviderEntity> rank2d = new PriorityQueue<>(new ServiceProviderComparator());
            bifurcateAvailableServiceProviders(availableServiceProvider, rank1a, rank1b, rank1c, rank1d, rank2a, rank2b, rank2c, rank2d);

            logger.info("Service Provider in rank1a: " + rank1a.size());
            logger.info("Service Provider in rank1b: " + rank1b.size());
            logger.info("Service Provider in rank1c: " + rank1c.size());
            logger.info("Service Provider in rank1d: " + rank1d.size());

            logger.info("Service Provider in rank2a: " + rank2a.size());
            logger.info("Service Provider in rank2b: " + rank2b.size());
            logger.info("Service Provider in rank2c: " + rank2c.size());
            logger.info("Service Provider in rank2d: " + rank2d.size());

            /*

            // For debugging purposes
            Iterator<ServiceProviderEntity> iterator2 = rank1d.iterator();
            while (!rank1d.isEmpty()) {
                ServiceProviderEntity serviceProvider = rank1d.poll();
                System.out.println("service_provider ticket assigned: " + serviceProvider.getTicketAssigned());
                double bandwidth = (double) (serviceProvider.getTicketAssigned() + serviceProvider.getTicketPending()) / serviceProvider.getRanking().getMaximumTicketSize() * 100;
                System.out.println("BANDWDTH : " + bandwidth );
                System.out.println(serviceProvider.getService_provider_id() + " - Name: " + serviceProvider.getFirst_name());
            }

            */

            // Iterator for traversing orders.
            while (iterator.hasNext()) {

                CustomOrderState customOrderState = iterator.next();

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonString = objectMapper.writeValueAsString(customOrderState);
                logger.info(jsonString);

                Order order = orderService.findOrderById(customOrderState.getOrderId());

                // created a switch statement which will execute in vertical order.
                switch (1) {
                    case 1:
                        if (!rank1a.isEmpty() && processRank(rank1a, order, assignedTickets, customOrderState)) {
                            iterator.remove();
                            break;
                        }
                    case 2:
                        if (!rank1b.isEmpty() && processRank(rank1b, order, assignedTickets, customOrderState)) {
                            iterator.remove();
                            break;
                        }
                    case 3:
                        if (!rank1c.isEmpty() && processRank(rank1c, order, assignedTickets, customOrderState)) {
                            iterator.remove();
                            break;
                        }
                    case 4:
                        if (!rank1d.isEmpty() && processRank(rank1d, order, assignedTickets, customOrderState)) {
                            iterator.remove();
                            break;
                        }
                    case 5:
                        if (!rank2a.isEmpty() && processRank(rank2a, order, assignedTickets, customOrderState)) {
                            iterator.remove();
                            break;
                        }
                    case 6:
                        if (!rank2b.isEmpty() && processRank(rank2b, order, assignedTickets, customOrderState)) {
                            iterator.remove();
                            break;
                        }
                    case 7:
                        if (!rank2c.isEmpty() && processRank(rank2c, order, assignedTickets, customOrderState)) {
                            iterator.remove();
                            break;
                        }
                    case 8:
                        if (!rank2d.isEmpty() && processRank(rank2d, order, assignedTickets, customOrderState)) {
                            iterator.remove();
                            break;
                        }
                    default:
                        break;
                }
            }
            logger.info("Total orders assigned by VDTA method is: " + assignedTickets.size());


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

    public List<CustomServiceProviderTicket> filterTicket(List<Long> states, List<Long> types, Long userId, Role role, Date dateFrom, Date dateTo, Long status) throws Exception {
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
            if (status != null) {
                CustomTicketStatus customTicketStatus = ticketStatusService.getTicketStatusByTicketStatusId(status);
                if (customTicketStatus == null)
                    throw new IllegalArgumentException("No ticket state found");
                jpql.append("AND c.ticketStatus = :status ");
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
            if (status != null) {
                CustomTicketStatus customTicketStatus = ticketStatusService.getTicketStatusByTicketStatusId(status);
                query.setParameter("status", customTicketStatus);
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

    // Comparator defined for the sorting of Service Provider in each rank depending on their capacity.
    public static class ServiceProviderComparator implements java.util.Comparator<ServiceProviderEntity> {
        @Override
        public int compare(ServiceProviderEntity sp1, ServiceProviderEntity sp2) {
            // Get the max ticket size from rank if max_ticket_size is not available
            Integer maxTicketSize1 = sp1.getMaximumTicketSize() != null
                    ? sp1.getMaximumTicketSize()
                    : sp1.getRanking().getMaximumTicketSize(); // Assuming getRanking() returns an object with max ticket size
            Integer maxTicketSize2 = sp2.getMaximumTicketSize() != null
                    ? sp2.getMaximumTicketSize()
                    : sp2.getRanking().getMaximumTicketSize(); // Assuming getRanking() returns an object with max ticket size

            // Avoid division by zero by ensuring maxTicketSize is not 0
            if (maxTicketSize1 == 0) maxTicketSize1 = 1;
            if (maxTicketSize2 == 0) maxTicketSize2 = 1;

            // Calculate bandwidth for both service providers
            double bandwidth1 = (double) (sp1.getTicketAssigned() + sp1.getTicketPending()) / maxTicketSize1 * 100;
            double bandwidth2 = (double) (sp2.getTicketAssigned() + sp2.getTicketPending()) / maxTicketSize2 * 100;

            // Sort by bandwidth (ascending order)
            return Double.compare(bandwidth1, bandwidth2); // for ascending order
        }
    }
}
