package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.dto.CreateTicketDto;
import com.community.api.dto.CustomTicketWrapper;
import com.community.api.endpoint.avisoft.controller.ServiceProviderActionController;
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
import com.community.api.utils.ServiceProviderDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderImpl;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.order.service.OrderService;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ServiceProviderTicketService {

    @Autowired
    ServiceProviderServiceImpl serviceProviderService;

    @Autowired
    OrderStateRefService orderStateRefService;

    @Autowired
    CatalogService catalogService;
    @Autowired
    CustomOrderService customOrderService;
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
    private OrderDTOService orderDTOService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private ServiceProviderActionController serviceProviderActionController;

    // auto-assigner scheduled to execute at 7:30 AM
    @Scheduled(cron = "0 30 7 * * ?")
    @Transactional
    public void callApiAt7_30AM() {
        try {
            autoAssigner();
            log.info("API called at 7:30 AM");
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
            log.info("API called at 3:30 PM");
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
                        log.info("Assigned Tickets Value: {}", assignedTicketsValue);

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
                                    log.error("Invalid ticket ID: {}", ticketId);
                                }
                            }
                            log.info("Converted ticket IDs to List<Long>: {}", ticketList);
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
            log.info("AUTO-ASSIGNER");

            // Here we are fetching all the service provider who are approved and active.
            List<ServiceProviderEntity> availableServiceProvider = serviceProviderService.getActiveAndApprovedServiceProviders();
            log.info("Available Service provider before the start of auto-assigner: {}", availableServiceProvider.size());

            // created a list which will keep the records of the tickets that are assigned by the auto-assigned.
            List<CustomTicketWrapper> assignedTickets = new ArrayList<>();

            primaryTicketNormalFlow(availableServiceProvider, assignedTickets);
            log.info("------------------------------------------------------------------------");
            log.info("Available Service provider after the completion of normal primary ticket flow: {}", availableServiceProvider.size());
            log.info("------------------------------------------------------------------------");
            rejectionAndReviewTicketLogic(availableServiceProvider, assignedTickets);

            return assignedTickets;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught: " + exception.getMessage());
        }
    }

    public void primaryTicketNormalFlow(List<ServiceProviderEntity> availableServiceProvider, List<CustomTicketWrapper> assignedTickets) throws Exception {
        try {
            // auto assigner will run only on those order which are in NEW STATE. (Later we can change this.)
            OrderStateRef orderStateRef = orderStateRefService.getOrderStateByOrderStateId(1);

            if (orderStateRef == null) {
                throw new IllegalArgumentException("No Order State Ref Found with id 1(NEW).");
            }

            // Fetch all the Orders for auto-assignment and handle the exception as well.
            List<CustomOrderState> customOrders = customOrderService.getCustomOrdersByOrderStateId(orderStateRef.getOrderStateId());
            log.info("Order which are in new state have size of: {}", customOrders.size());

            if (customOrders.isEmpty()) {
                return;
            }

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

        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught: " + exception.getMessage());
        }
    }

    /*public void rejectedTicketLogic(List<CustomTicketWrapper> assignedTickets) {
        try {

            List<Long> stateIdList = new ArrayList<>();
            stateIdList.add(Constant.TICKET_STATE_RETURNED);

            List<Long> typeIdList = new ArrayList<>();
            typeIdList.add(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET);
            typeIdList.add(Constant.TICKET_TYPE_ID_OF_REVIEW_TICKET);

            // Firstly we fetch all the tickets which are in return state.
            List<CustomServiceProviderTicket> tickets = filterTicket(stateIdList, typeIdList, null, null, null, null, null, null, null);
            log.info("ticket recieved for auto-assignment: {}", tickets.size());

            randomBindingTicketAllocationForTickets(tickets, assignedTickets);

            // Here we are fetching all the service provider who are approved and active.
            List<ServiceProviderEntity> availableServiceProvider = serviceProviderService.getActiveAndApprovedServiceProviders();

            verticalDistributionTicketAllocationForTickets(tickets, availableServiceProvider, assignedTickets);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new IllegalArgumentException(exception.getMessage());
        }
    }*/

    public void rejectionAndReviewTicketLogic(List<ServiceProviderEntity> availableServiceProvider, List<CustomTicketWrapper> assignedTickets) {
        try {

            List<CustomTicketState> ticketStateList = new ArrayList<>();
            CustomTicketState ticketStateToDo = ticketStateService.getTicketStateByTicketStateId(Constant.TICKET_STATE_TO_DO);
            CustomTicketState ticketStateReturned = ticketStateService.getTicketStateByTicketStateId(Constant.TICKET_STATE_RETURNED);

            ticketStateList.add(ticketStateToDo);
            ticketStateList.add(ticketStateReturned);

            List<CustomTicketType> ticketTypeList = new ArrayList<>();
            CustomTicketType ticketTypeReviewTicket = ticketTypeService.getTicketTypeByTicketTypeId(Constant.TICKET_TYPE_ID_OF_REVIEW_TICKET);
            CustomTicketType ticketTypePrimaryTicket = ticketTypeService.getTicketTypeByTicketTypeId(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET);
            ticketTypeList.add(ticketTypeReviewTicket);
            ticketTypeList.add(ticketTypePrimaryTicket);

            // Initialize the JPQL query
            StringBuilder jpql = new StringBuilder("SELECT c FROM CustomServiceProviderTicket c ")
                    .append("WHERE 1=1 ")
                    .append("AND c.assignee IS NULL ")
                    .append("AND c.ticketState IN :states ")
                    .append("AND c.ticketType IN :types ")
                    .append("AND (c.ticketType.id <> 2 OR (c.ticketType.id = 2 AND c.parentTicket.ticketType.id = 1))");

            // Create the query with the final JPQL string
            TypedQuery<CustomServiceProviderTicket> query = entityManager.createQuery(jpql.toString(), CustomServiceProviderTicket.class);

            query.setParameter("states", ticketStateList);
            query.setParameter("types", ticketTypeList);

            // Firstly we fetch all the tickets which are in return state.
            List<CustomServiceProviderTicket> tickets = query.getResultList();
            log.info("ticket received for auto-assignment: {}", tickets.size());

            randomBindingTicketAllocationForTickets(tickets, assignedTickets);

            // Here we are fetching all the service provider who are approved and active.
            log.info("available service provider: {}", availableServiceProvider.size());
            verticalDistributionTicketAllocationForTickets(tickets, availableServiceProvider, assignedTickets);

        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new IllegalArgumentException(exception.getMessage());
        }
    }

    public boolean allocateTicket(Order order, ServiceProviderEntity serviceProvider, CustomOrderState customOrderState, CustomCustomer customer, List<CustomTicketWrapper> assignedTickets) throws Exception {
        try {
            log.info("PRIMARY REFERRER(SERVICE PROVIDER) ID: {}", serviceProvider.getService_provider_id());
            if ((serviceProvider.getMaximumTicketSize() != null && (serviceProvider.getIsActive().equals(true)) && serviceProvider.getTicketAssigned() + serviceProvider.getTicketPending() < serviceProvider.getMaximumTicketSize()) || (serviceProvider.getTicketAssigned() + serviceProvider.getTicketPending() < serviceProvider.getRanking().getMaximumTicketSize())) {
                // assign him the ticket
                // create a entry in serviceProvider ticket table where the info about which serviceProvider is linked with which ticket is stored.
                CreateTicketDto createTicketDto = new CreateTicketDto();
                createTicketDto.setTicketState(1L);
                createTicketDto.setTicketType(1L);
                createTicketDto.setTicketStatus(0L);
                createTicketDto.setAssignee(serviceProvider.getService_provider_id());
                createTicketDto.setAssigneeRole(4);
                CustomServiceProviderTicket ticket = createTicket(createTicketDto, (OrderImpl) order, serviceProvider.getService_provider_id(), serviceProvider.getRole(), null, null);

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

                wrapper.customWrapDetails(ticket, orderDto, entityManager);
                assignedTickets.add(wrapper);
                return true;
            } else {
                log.info("Service Provider limit exceeded for the day - serviceProvider details: {}", serviceProvider);
            }
            return false;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some Exception Caught: " + exception.getMessage());
        }
    }

    @Transactional
    public boolean reallocateTicket(Order order, ServiceProviderEntity serviceProvider, CustomServiceProviderTicket ticket, CustomOrderState customOrderState, CustomCustomer customer, List<CustomTicketWrapper> assignedTickets) throws Exception {
        try {
            log.info("Referrer(SERVICE PROVIDER) with id: {}", serviceProvider.getService_provider_id());
            if ((serviceProvider.getMaximumTicketSize() != null && (serviceProvider.getIsActive().equals(true)) && serviceProvider.getTicketAssigned() + serviceProvider.getTicketPending() < serviceProvider.getMaximumTicketSize()) || (serviceProvider.getTicketAssigned() + serviceProvider.getTicketPending() < serviceProvider.getRanking().getMaximumTicketSize())) {

                // Set ticket state to TO-DO.
                CustomTicketState ticketState = ticketStateService.getTicketStateByTicketStateId(1L);
                ticket.setTicketState(ticketState);

                CustomTicketStatus ticketStatus = ticketStatusService.getTicketStatusByTicketStatusId(0L);
                ticket.setTicketStatus(ticketStatus);

                ticketStatusService.verifyStatus(ticketState, ticketStatus, ticket.getTicketType());

                // setting assignee and its role.
                ticket.setAssignee(serviceProvider.getService_provider_id());
                Role assigneeRole = roleService.getRoleByRoleId(4);
                ticket.setAssigneeRole(assigneeRole);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Set active start date to current date and time in "yyyy-MM-dd HH:mm:ss" format
                String formattedDate = dateFormat.format(new Date());
                Date currentDate = dateFormat.parse(formattedDate);

                ticket.setModifiedDate(currentDate);
                ticket.setTicketAssignDate(currentDate);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(currentDate);
                Date newTargetDate = null;

                Product product = findProductFromItemAttribute(order.getOrderItems().get(0));

                if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_REVIEW_TICKET)) {
                    calendar.add(Calendar.HOUR_OF_DAY, 2);
                    newTargetDate = calendar.getTime();


                } else if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET)) {
                    calendar.add(Calendar.HOUR_OF_DAY, 4);
                    newTargetDate = calendar.getTime();

                } else {
                    throw new IllegalArgumentException("Cannot perform action on miscellaneous ticket");
                }

                if (!newTargetDate.before(product.getActiveEndDate())) {
                    log.info("cannot assign ticket at the target completion date is after or equal to application close date.");
                    return false;
                }
                ticket.setTargetCompletionDate(newTargetDate);

                entityManager.merge(ticket);

                customOrderState.setOrderStateId(Constant.ORDER_STATE_ASSIGNED.getOrderStateId());
                entityManager.merge(customOrderState);

                // Increment the ticket assigned to the Service Provider
                serviceProviderService.serviceProviderTicketAssignedIncrement(serviceProvider);

                // Ticket Wrapper for the response in auto-assigner.
                CustomTicketWrapper wrapper = new CustomTicketWrapper();

                if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_REVIEW_TICKET)) {
                    wrapper.customWrapDetails(ticket, null, entityManager);
                } else {
                    CustomOrderState orderState = entityManager.find(CustomOrderState.class, ticket.getOrder().getId());
                    CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer.getId());
                    OrderCustomerDetailsDTO customerDetailsDTO = new OrderCustomerDetailsDTO(customer.getId(), customer.getFirstName() + " " + customer.getLastName(), customer.getEmailAddress(), customCustomer.getMobileNumber(), addressFetcher.fetch(customer), customer.getUsername());
                    CombinedOrderDTO orderDto = orderDTOService.wrapOrder(ticket.getOrder(), orderState, ticket, customerDetailsDTO);
                    wrapper.customWrapDetails(ticket, orderDto, entityManager);
                }

                assignedTickets.add(wrapper);
                return true;

            } else {
                log.info("Service Provider limit exceeded for the day - serviceProvider details: {}", serviceProvider);
            }
            return false;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some Exception Caught: " + exception.getMessage());
        }
    }

    // TODO REJECTED LIST LOGIC IS PENDING. @RAMAN.
    @Transactional
    public void randomBindingTicketAllocation(List<CustomOrderState> customOrders, List<CustomTicketWrapper> assignedTickets) throws Exception {
        try {
            log.info("Random Binding Ticket Allocation (RBTA)");
            log.info("Total Orders received by RBTA are: {}", customOrders.size());

            boolean assigned;

            // Created a iterator that will iterator each order.
            Iterator<CustomOrderState> iterator = customOrders.iterator();
            while (iterator.hasNext()) {

                CustomOrderState customOrderState = iterator.next();
                assigned = false;

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonString = objectMapper.writeValueAsString(customOrderState);
                log.info("order state log: {}", jsonString);

                // Fetch Order and customer from customOrderState and order respectively.
                Order order = orderService.findOrderById(customOrderState.getOrderId());
                CustomCustomer customer = entityManager.find(CustomCustomer.class, order.getCustomer().getId());

                // Fetch all the referees.
                List<CustomerReferrer> referrers = customer.getMyReferrer();
                log.info("Customer whose id is: {} have a referrer list of size: {}", customer.getId(), referrers.size());

                if (referrers.isEmpty()) {
                    continue;
                }

                // PRIMARY BINDED LOGIC OF RANDOM BINDING TICKET ALLOCATION (RBTA)
                for (CustomerReferrer referrer : referrers) {
                    // Traverse the Referrers one by one
                    ServiceProviderEntity serviceProvider = referrer.getServiceProvider();

                    // Check if the referee is the primary Referee.
                    if (referrer.getPrimaryRef() != null && referrer.getPrimaryRef() && serviceProvider.getIsActive() != null && (serviceProvider.getRole() == 4 || serviceProvider.getRole() == 2) && (serviceProvider.getApproved() != null && serviceProvider.getApproved())) {
                        assigned = allocateTicket(order, serviceProvider, customOrderState, customer, assignedTickets);
                    } else {
                        log.info("Either the service provider is not primary referee or not active nor approved or neither admin or service provider with id {}", serviceProvider.getService_provider_id());
                    }
                    if (assigned) {
                        iterator.remove();
                        break;
                    }
                }

                // For the Remaining Referees
                if (!assigned) {
                    for (CustomerReferrer referrer : referrers) {
                        ServiceProviderEntity serviceProvider = referrer.getServiceProvider();
                        log.info("REFERRER ID: {}", serviceProvider.getService_provider_id());

                        if (serviceProvider.getIsActive() != null && (serviceProvider.getRole() == 4 || serviceProvider.getRole() == 2) && (serviceProvider.getApproved() != null && serviceProvider.getApproved())) {
                            assigned = allocateTicket(order, serviceProvider, customOrderState, customer, assignedTickets);
                        } else {
                            log.info("Either the service provider is not active nor approved or neither admin or service provider with id {}", serviceProvider.getService_provider_id());
                        }
                        if (assigned) {
                            iterator.remove();
                            break;
                        }
                    }
                }

                // If there is no one in referrer list of custom to whom we can assign this ticket then we will try to assign the ticket to the creator of the product.
                if (!assigned) {

                    log.info("Inside the Creator of the Product logic OF RBTA");
                    Long productId = Long.parseLong(order.getOrderItems().get(0).getOrderItemAttributes().get("productId").getValue());
                    CustomProduct customProduct = productService.getCustomProductByCustomProductId(productId);

                    ServiceProviderEntity serviceProvider = serviceProviderService.getServiceProviderById(customProduct.getUserId());

                    if (serviceProvider.getIsActive() != null && (serviceProvider.getRole() == 4 || serviceProvider.getRole() == 2) && (serviceProvider.getApproved() != null && serviceProvider.getApproved())) {
                        assigned = allocateTicket(order, serviceProvider, customOrderState, customer, assignedTickets);
                    } else {
                        log.info("Either the service provider is not active nor approved or neither admin or service provider with id {}", serviceProvider.getService_provider_id());
                    }
                    if (assigned) {
                        iterator.remove();
                        break;
                    }
                }
            }
            log.info("Total orders assigned by RBTA method is: {}", assignedTickets.size());

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught: " + exception.getMessage());
        }
    }

    @Transactional
    public void randomBindingTicketAllocationForTickets(List<CustomServiceProviderTicket> tickets, List<CustomTicketWrapper> assignedTickets) throws Exception {
        try {
            log.info("Random Binding Ticket Allocation (RBTA)");
            log.info("Total Tickets received by RBTA are: {}", tickets.size());

            boolean assigned;

            // Created a iterator that will iterator each ticket.
            Iterator<CustomServiceProviderTicket> iterator = tickets.iterator();
            while (iterator.hasNext()) {

                CustomServiceProviderTicket ticket = iterator.next();
                assigned = false;

                log.info("On ticket with id: {}", ticket.getTicketId());

                // Fetch Order and customer from primary ticket or review ticket.
                Order order = null;
                CustomCustomer customer = null;

                if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET)) {
                    order = ticket.getOrder();
                    customer = entityManager.find(CustomCustomer.class, order.getCustomer().getId());
                } else if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_REVIEW_TICKET)) {
                    order = ticket.getParentTicket().getOrder();
                    customer = entityManager.find(CustomCustomer.class, order.getCustomer().getId());
                } else {
                    throw new IllegalArgumentException("Cannot found order or customer for this type of ticket.");
                }

                CustomOrderState customOrderState = entityManager.find(CustomOrderState.class, order.getId());
                // Fetch all the referees.
                List<CustomerReferrer> referrers = customer.getMyReferrer();
                log.info("Customer whose id is: {} have a referrer list of size: {}", customer.getId(), referrers.size());

                if (referrers.isEmpty()) {
                    continue;
                }

                // PRIMARY BINDED LOGIC OF RANDOM BINDING TICKET ALLOCATION (RBTA)
                for (CustomerReferrer referrer : referrers) {
                    // Traverse the Referrers one by one
                    ServiceProviderEntity serviceProvider = referrer.getServiceProvider();

                    // Check if the referee is the primary Referee.
                    if (referrer.getPrimaryRef() != null && referrer.getPrimaryRef() == true && serviceProvider.getIsActive() != null && serviceProvider.getIsActive() && serviceProvider.getApproved() != null && serviceProvider.getApproved() && !ticket.getRejectedBy().contains(serviceProvider.getService_provider_id()) && (serviceProvider.getRole() == 4 || serviceProvider.getRole() == 2)) {

                        log.info("Primary Referrer !!");
                        if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_REVIEW_TICKET) && Objects.equals(ticket.getParentTicket().getAssignee(), referrer.getId())) {
                            log.info("cannot assign ticket to assignee of parent ticket");
                        } else {
                            assigned = reallocateTicket(order, serviceProvider, ticket, customOrderState, customer, assignedTickets);
                            if (assigned) {
                                iterator.remove();
                                break;
                            }
                        }
                    } else {
                        log.info("Either the service provider is not primary referee or not active nor approved or neither admin or service provider with id: {}", serviceProvider.getService_provider_id());
                    }
                }

                // For the Remaining Referees
                if (!assigned) {
                    for (CustomerReferrer referrer : referrers) {
                        ServiceProviderEntity serviceProvider = referrer.getServiceProvider();
                        log.info("Other Referrer !!");

                        // check that it should not assigned to any sp who already rejected the ticket.
                        if (serviceProvider.getIsActive() != null && serviceProvider.getIsActive() && serviceProvider.getApproved() != null && serviceProvider.getApproved() && !ticket.getRejectedBy().contains(serviceProvider.getService_provider_id()) && (serviceProvider.getRole() == 4 || serviceProvider.getRole() == 2)) {

                            if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_REVIEW_TICKET) && Objects.equals(ticket.getParentTicket().getAssignee(), referrer.getId())) {
                                log.info("cannot assign ticket to assignee of parent ticket");
                            } else {
                                assigned = reallocateTicket(order, serviceProvider, ticket, customOrderState, customer, assignedTickets);
                                if (assigned) {
                                    iterator.remove();
                                    break;
                                }
                            }
                        } else {
                            log.info("Either the service provider is not active nor approved or neither admin or service provider with id: {}", serviceProvider.getService_provider_id());
                        }
                    }
                }

                // If there is no one in referrer list of custom to whom we can assign this ticket then we will try to assign the ticket to the creator of the product.
                if (!assigned) {

                    log.info("Inside the Creator of the Product Logic of RBTA");
                    Long productId = Long.parseLong(order.getOrderItems().get(0).getOrderItemAttributes().get("productId").getValue());
                    CustomProduct customProduct = productService.getCustomProductByCustomProductId(productId);

                    ServiceProviderEntity serviceProvider = serviceProviderService.getServiceProviderById(customProduct.getUserId());

                    if (serviceProvider.getIsActive() != null && serviceProvider.getIsActive() && serviceProvider.getApproved() != null && serviceProvider.getApproved() && (serviceProvider.getRole() == 4 || serviceProvider.getRole() == 2) && !ticket.getRejectedBy().contains(serviceProvider.getService_provider_id())) {
                        if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_REVIEW_TICKET) && Objects.equals(ticket.getParentTicket().getAssignee(), serviceProvider.getService_provider_id())) {
                            log.info("cannot assign ticket to assignee of parent ticket");
                        } else {
                            assigned = reallocateTicket(order, serviceProvider, ticket, customOrderState, customer, assignedTickets);
                            if (assigned) {
                                iterator.remove();
                                break;
                            }
                        }
                    } else {
                        log.info("Either the service provider is not active nor approved or neither admin or service provider with id: {}", serviceProvider.getService_provider_id());
                    }
                }

            }
            log.info("Total tickets re-assigned by RBTA method is: {}", assignedTickets.size());

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught: " + exception.getMessage());
        }
    }

    @Transactional
    public CustomServiceProviderTicket createTicket(CreateTicketDto createTicketDto, OrderImpl order, Long assignedUserTo, Integer assignedRoleId, Integer creatorRoleId, Long creatorId) throws Exception {
        try {
            CustomServiceProviderTicket customServiceProviderTicket = new CustomServiceProviderTicket();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Set active start date to current date and time in "yyyy-MM-dd HH:mm:ss" format
            String formattedDate = dateFormat.format(new Date());
            Date createdDate = dateFormat.parse(formattedDate);

            // If target date is provided then it's fine else we give 4 hours to complete the ticket.
            log.info("date {}", createTicketDto.getTargetCompletionDate());
            if (createTicketDto.getTargetCompletionDate() != null) {
                if (!createTicketDto.getTargetCompletionDate().after(createdDate)) {
                    throw new IllegalArgumentException("TARGET COMPLETION DATE MUST BE OF FUTURE");
                }
            } else {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(createdDate);
                calendar.add(Calendar.HOUR_OF_DAY, 4);
                Date newTargetDate = calendar.getTime();

                createTicketDto.setTargetCompletionDate(newTargetDate);
            }

            // Setting up the dates
            customServiceProviderTicket.setCreatedDate(createdDate);
            customServiceProviderTicket.setTicketAssignDate(createdDate);
            customServiceProviderTicket.setModifiedDate(createdDate);
            customServiceProviderTicket.setTargetCompletionDate(createTicketDto.getTargetCompletionDate());

            customServiceProviderTicket.setOrder(order);

            if (createTicketDto.getTicketType() == 3) {
                customServiceProviderTicket.setDesc(createTicketDto.getTask());
            }

            if (creatorId != null && creatorRoleId != null) {
                customServiceProviderTicket.setCreatorRole(roleService.getRoleByRoleId(creatorRoleId));
                customServiceProviderTicket.setUserId(creatorId);
            }

            Role role = roleService.getRoleByRoleId(assignedRoleId);
            customServiceProviderTicket.setAssigneeRole(role);
            customServiceProviderTicket.setAssignee(assignedUserTo);

            CustomTicketState ticketState = null;
            if (createTicketDto.getTicketState() != null) {
                ticketState = ticketStateService.getTicketStateByTicketStateId(createTicketDto.getTicketState());
                customServiceProviderTicket.setTicketState(ticketState);
            } else {
                throw new IllegalArgumentException("Ticket State is mandatory field while creating a ticket");
            }

            CustomTicketType ticketType = null;
            if (createTicketDto.getTicketType() != null) {
                ticketType = ticketTypeService.getTicketTypeByTicketTypeId(createTicketDto.getTicketType());
                customServiceProviderTicket.setTicketType(ticketType);
            } else {
                throw new IllegalArgumentException("Ticket Type is mandatory field while creating a ticket");
            }

            CustomTicketStatus ticketStatus = null;
            if (createTicketDto.getTicketStatus() != null) {
                ticketStatus = ticketStatusService.getTicketStatusByTicketStatusId(createTicketDto.getTicketStatus());
                customServiceProviderTicket.setTicketStatus(ticketStatus);
            } else {
                ticketStatus = ticketStatusService.getTicketStatusByTicketStatusId(0L); // By Default set to To-Do Status.
                customServiceProviderTicket.setTicketStatus(ticketStatus);
            }

            ticketStatusService.verifyStatus(ticketState, ticketStatus, ticketType);

            if (createTicketDto.getAssigneeRole() == 4) {
                ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, createTicketDto.getAssignee());
                serviceProvider.setTicketAssigned(serviceProvider.getTicketAssigned() + 1);
                entityManager.merge(serviceProvider);
            }
            customServiceProviderTicket = entityManager.merge(customServiceProviderTicket);
            return customServiceProviderTicket;

        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException("Illegal Exception Caught: " + illegalArgumentException.getMessage());
        } catch (PersistenceException persistenceException) {
            exceptionHandlingService.handleException(persistenceException);
            throw new Exception(persistenceException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    @Transactional
    public CustomServiceProviderTicket createReviewTicket(CustomServiceProviderTicket parentTicket, CreateTicketDto createTicketDto, List<MultipartFile> files, Long tokenUserId, Role tokenRole) throws Exception {
        try {

            CustomServiceProviderTicket reviewTicket = new CustomServiceProviderTicket();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Set active start date to current date and time in "yyyy-MM-dd HH:mm:ss" format
            String formattedDate = dateFormat.format(new Date());
            Date createdDate = dateFormat.parse(formattedDate);

            // Setting up the dates
            reviewTicket.setCreatedDate(createdDate);
//            customServiceProviderTicket.setTicketAssignDate(createdDate);
            reviewTicket.setModifiedDate(createdDate);
//            customServiceProviderTicket.setTargetCompletionDate(createTicketDto.getTargetCompletionDate());
            reviewTicket.setComment(parentTicket.getComment());
            reviewTicket.setParentTicket(parentTicket);

            CustomTicketState ticketState = ticketStateService.getTicketStateByTicketStateId(1L); // Sate (to-do)
            CustomTicketStatus ticketStatus = ticketStatusService.getTicketStatusByTicketStatusId(0L); // Status with state linkage (to-do)
            CustomTicketType ticketType = ticketTypeService.getTicketTypeByTicketTypeId(2L); // Review Ticket

            reviewTicket.setTicketState(ticketState);
            reviewTicket.setTicketStatus(ticketStatus);
            reviewTicket.setTicketType(ticketType);
            reviewTicket.setComment(createTicketDto.getComment());

            // If there exists some files then upload them as well.
            reviewTicket = entityManager.merge(reviewTicket);
            if (files != null) {

                Set<ServiceProviderDocument> serviceProviderDocument = ticketStateService.updateTicketDocument(files, reviewTicket, tokenUserId, tokenRole);
                reviewTicket.setServiceProviderDocuments(serviceProviderDocument);

            }
            entityManager.merge(reviewTicket);
            return reviewTicket;

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

            CustomTicketState ticketState = ticketStateService.getTicketStateByTicketStateId(createTicketDto.getTicketState());
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

    public void bifurcateAvailableServiceProviders(List<ServiceProviderEntity> availableServiceProviders,
                                                   PriorityQueue<ServiceProviderEntity> professionalRank1,
                                                   PriorityQueue<ServiceProviderEntity> professionalRank2,
                                                   PriorityQueue<ServiceProviderEntity> professionalRank3,
                                                   PriorityQueue<ServiceProviderEntity> professionalRank4,
                                                   PriorityQueue<ServiceProviderEntity> individualRank1,
                                                   PriorityQueue<ServiceProviderEntity> individualRank2,
                                                   PriorityQueue<ServiceProviderEntity> individualRank3,
                                                   PriorityQueue<ServiceProviderEntity> individualRank4) throws Exception {

        // Loop through the list of available service providers
        for (ServiceProviderEntity serviceProvider : availableServiceProviders) {
            try {

                // Determine the rank of the service provider (this logic needs to be based on your use case)
                String rank = serviceProvider.getRanking().getRank_id().toString(); // Assuming 'getRank' returns a rank name like "professionalRank1", "professionalRank2", etc.

                // Add the service provider to the corresponding priority queue based on the rank
                switch (rank) {
                    case "1":
                        professionalRank1.offer(serviceProvider);
                        break;
                    case "2":
                        professionalRank2.offer(serviceProvider);
                        break;
                    case "3":
                        professionalRank3.offer(serviceProvider);
                        break;
                    case "4":
                        professionalRank4.offer(serviceProvider);
                        break;
                    case "5":
                        individualRank1.offer(serviceProvider);
                        break;
                    case "6":
                        individualRank2.offer(serviceProvider);
                        break;
                    case "7":
                        individualRank3.offer(serviceProvider);
                        break;
                    case "8":
                        individualRank4.offer(serviceProvider);
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
                if (serviceProvider.getMaximumTicketSize() != null) {
                    bandwidth = (double) (serviceProvider.getTicketAssigned() + serviceProvider.getTicketPending()) / serviceProvider.getMaximumTicketSize() * 100;
                } else {
                    bandwidth = (double) (serviceProvider.getTicketAssigned() + serviceProvider.getTicketPending()) / serviceProvider.getRanking().getMaximumTicketSize() * 100;
                }
                // if the capacity is reached then continue to next service provider.
                if (bandwidth >= 100.0) {
                    log.info("Service Provider limit exceeded for the day - serviceProvider details: {}", serviceProvider.getService_provider_id());
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
                CustomServiceProviderTicket ticket = createTicket(createTicketDto, (OrderImpl) order, serviceProvider.getService_provider_id(), serviceProvider.getRole(), null, null);

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

                wrapper.customWrapDetails(ticket, orderDto, entityManager);
                assignedTickets.add(wrapper);

                log.info("Order with id: {} is assigned to Service Provider with id: {} with ticket id: {}", order.getId(), serviceProvider.getService_provider_id(), ticket.getTicketId());
                return true;
            }
            return false;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some Exception Caught: " + exception.getMessage());
        }
    }

    @Transactional
    public boolean processRankForTickets(PriorityQueue<ServiceProviderEntity> rankedServiceProvider, Order order, List<CustomTicketWrapper> assignedTickets, CustomServiceProviderTicket ticket) throws Exception {
        try {

            // while the rankedService Provider is not empty.
            while (!rankedServiceProvider.isEmpty()) {
                ServiceProviderEntity serviceProvider = rankedServiceProvider.poll();
                log.info("trying to assign ticket to sp with id: {}", serviceProvider.getService_provider_id());
                double bandwidth = 0.0;
                if (serviceProvider.getMaximumTicketSize() != null) {
                    bandwidth = (double) (serviceProvider.getTicketAssigned() + serviceProvider.getTicketPending()) / serviceProvider.getMaximumTicketSize() * 100;
                } else {
                    bandwidth = (double) (serviceProvider.getTicketAssigned() + serviceProvider.getTicketPending()) / serviceProvider.getRanking().getMaximumTicketSize() * 100;
                }
                // if the capacity is reached then continue to next service provider.
                if (bandwidth >= 100.0) {
                    log.info("Service Provider limit exceeded for the day - serviceProvider details: {}", serviceProvider.getService_provider_id());
                    continue;
                }

                CustomCustomer readCustomer = entityManager.find(CustomCustomer.class, order.getCustomer().getId());
                CustomOrderState customOrderState = entityManager.find(CustomOrderState.class, order.getId());

                if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_REVIEW_TICKET) && Objects.equals(ticket.getParentTicket().getAssignee(), serviceProvider.getService_provider_id())) {
                    log.info("cannot assign ticket to assignee of parent ticket: {}", serviceProvider.getService_provider_id());
                    continue;
                } else {
                    // assign him the ticket
                    Boolean assigned = reallocateTicket(order, serviceProvider, ticket, customOrderState, readCustomer, assignedTickets);
                    if (!assigned) {
                        throw new IllegalArgumentException("Not able to assigned ticket");
                    }
                }

                customOrderState.setOrderStateId(Constant.ORDER_STATE_ASSIGNED.getOrderStateId());
                entityManager.merge(customOrderState);

                // updated service provider ticket assigned data in db
                serviceProviderService.serviceProviderTicketAssignedIncrement(serviceProvider);

                // updated service provider ticket assigned data in the PriorityQueue.
                rankedServiceProvider.offer(serviceProvider);

                CustomTicketWrapper wrapper = new CustomTicketWrapper();

                if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_REVIEW_TICKET)) {
                    wrapper.customWrapDetails(ticket, null, entityManager);
                } else {
                    CustomOrderState orderState = entityManager.find(CustomOrderState.class, ticket.getOrder().getId());
                    Customer customer = customerService.readCustomerById(ticket.getOrder().getCustomer().getId());
                    CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer.getId());
                    OrderCustomerDetailsDTO customerDetailsDTO = new OrderCustomerDetailsDTO(customer.getId(), customer.getFirstName() + " " + customer.getLastName(), customer.getEmailAddress(), customCustomer.getMobileNumber(), addressFetcher.fetch(customer), customer.getUsername());
                    CombinedOrderDTO orderDto = orderDTOService.wrapOrder(ticket.getOrder(), orderState, ticket, customerDetailsDTO);

                    wrapper.customWrapDetails(ticket, orderDto, entityManager);
                }

                assignedTickets.add(wrapper);

                log.info("Order with id: {} is assigned to Service Provider with id: {} , with ticket id: {}", order.getId(), serviceProvider.getService_provider_id(), ticket.getTicketId());
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
            log.info("Vertical Distribution Ticket Allocation");
            log.info("Total orders received for VDTA: {}", customOrders.size());
            log.info("Total Service Provider: {}", availableServiceProvider.size());

            Iterator<CustomOrderState> iterator = customOrders.iterator();

            // Initialized the service provider with different ranks.
            PriorityQueue<ServiceProviderEntity> professionalRank1 = new PriorityQueue<>(new ServiceProviderComparator());
            PriorityQueue<ServiceProviderEntity> professionalRank2 = new PriorityQueue<>(new ServiceProviderComparator());
            PriorityQueue<ServiceProviderEntity> professionalRank3 = new PriorityQueue<>(new ServiceProviderComparator());
            PriorityQueue<ServiceProviderEntity> professionalRank4 = new PriorityQueue<>(new ServiceProviderComparator());
            PriorityQueue<ServiceProviderEntity> individualRank1 = new PriorityQueue<>(new ServiceProviderComparator());
            PriorityQueue<ServiceProviderEntity> individualRank2 = new PriorityQueue<>(new ServiceProviderComparator());
            PriorityQueue<ServiceProviderEntity> individualRank3 = new PriorityQueue<>(new ServiceProviderComparator());
            PriorityQueue<ServiceProviderEntity> individualRank4 = new PriorityQueue<>(new ServiceProviderComparator());
            bifurcateAvailableServiceProviders(availableServiceProvider, professionalRank1, professionalRank2, professionalRank3, professionalRank4, individualRank1, individualRank2, individualRank3, individualRank4);

            log.info("Service Provider in professionalRank1: {}", professionalRank1.size());
            log.info("Service Provider in professionalRank2: {}", professionalRank2.size());
            log.info("Service Provider in professionalRank3: {}", professionalRank3.size());
            log.info("Service Provider in professionalRank4: {}", professionalRank4.size());

            log.info("Service Provider in individualRank1: {}", individualRank1.size());
            log.info("Service Provider in individualRank2: {}", individualRank2.size());
            log.info("Service Provider in individualRank3: {}", individualRank3.size());
            log.info("Service Provider in individualRank4: {}", individualRank4.size());


            /*// For debugging purposes
            Iterator<ServiceProviderEntity> iterator2 = professionalRank4.iterator();
            while (!professionalRank4.isEmpty()) {
                ServiceProviderEntity serviceProvider = professionalRank4.poll();
                logger.info("service_provider ticket assigned: " + serviceProvider.getTicketAssigned());
                double bandwidth = (double) (serviceProvider.getTicketAssigned() + serviceProvider.getTicketPending()) / serviceProvider.getRanking().getMaximumTicketSize() * 100;
                logger.info("BANDWDTH : " + bandwidth );
                logger.info(serviceProvider.getService_provider_id() + " - Name: " + serviceProvider.getFirst_name());
            }*/


            // Iterator for traversing orders.
            while (iterator.hasNext()) {

                CustomOrderState customOrderState = iterator.next();

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonString = objectMapper.writeValueAsString(customOrderState);
                log.info(jsonString);

                Order order = orderService.findOrderById(customOrderState.getOrderId());

                // created a switch statement which will execute in vertical order.
                switch (1) {
                    case 1:
                        if (!professionalRank1.isEmpty() && processRank(professionalRank1, order, assignedTickets, customOrderState)) {
                            iterator.remove();
                            break;
                        }
                    case 2:
                        if (!professionalRank2.isEmpty() && processRank(professionalRank2, order, assignedTickets, customOrderState)) {
                            iterator.remove();
                            break;
                        }
                    case 3:
                        if (!professionalRank3.isEmpty() && processRank(professionalRank3, order, assignedTickets, customOrderState)) {
                            iterator.remove();
                            break;
                        }
                    case 4:
                        if (!professionalRank4.isEmpty() && processRank(professionalRank4, order, assignedTickets, customOrderState)) {
                            iterator.remove();
                            break;
                        }
                    case 5:
                        if (!individualRank1.isEmpty() && processRank(individualRank1, order, assignedTickets, customOrderState)) {
                            iterator.remove();
                            break;
                        }
                    case 6:
                        if (!individualRank2.isEmpty() && processRank(individualRank2, order, assignedTickets, customOrderState)) {
                            iterator.remove();
                            break;
                        }
                    case 7:
                        if (!individualRank3.isEmpty() && processRank(individualRank3, order, assignedTickets, customOrderState)) {
                            iterator.remove();
                            break;
                        }
                    case 8:
                        if (!individualRank4.isEmpty() && processRank(individualRank4, order, assignedTickets, customOrderState)) {
                            iterator.remove();
                            break;
                        }
                    default:
                        break;
                }
            }
            log.info("Total orders assigned by VDTA method is: {}", assignedTickets.size());

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught: " + exception.getMessage());
        }
    }

    @Transactional
    public void verticalDistributionTicketAllocationForTickets(List<CustomServiceProviderTicket> tickets, List<ServiceProviderEntity> availableServiceProvider, List<CustomTicketWrapper> assignedTickets) throws Exception {
        try {
            log.info("Vertical Distribution Ticket Allocation");
            log.info("Total tickets received for VDTA: {}", tickets.size());
            log.info("Total Service Provider: {}", availableServiceProvider.size());

            Iterator<CustomServiceProviderTicket> iterator = tickets.iterator();

            // Initialized the service provider with different ranks.
            PriorityQueue<ServiceProviderEntity> professionalRank1 = new PriorityQueue<>(new ServiceProviderComparator());
            PriorityQueue<ServiceProviderEntity> professionalRank2 = new PriorityQueue<>(new ServiceProviderComparator());
            PriorityQueue<ServiceProviderEntity> professionalRank3 = new PriorityQueue<>(new ServiceProviderComparator());
            PriorityQueue<ServiceProviderEntity> professionalRank4 = new PriorityQueue<>(new ServiceProviderComparator());
            PriorityQueue<ServiceProviderEntity> individualRank1 = new PriorityQueue<>(new ServiceProviderComparator());
            PriorityQueue<ServiceProviderEntity> individualRank2 = new PriorityQueue<>(new ServiceProviderComparator());
            PriorityQueue<ServiceProviderEntity> individualRank3 = new PriorityQueue<>(new ServiceProviderComparator());
            PriorityQueue<ServiceProviderEntity> individualRank4 = new PriorityQueue<>(new ServiceProviderComparator());
            bifurcateAvailableServiceProviders(availableServiceProvider, professionalRank1, professionalRank2, professionalRank3, professionalRank4, individualRank1, individualRank2, individualRank3, individualRank4);

            log.info("Service Provider in professional Rank 1: {}", professionalRank1.size());
            log.info("Service Provider in professional Rank 2: {}", professionalRank2.size());
            for (ServiceProviderEntity serviceProvider : professionalRank2) {
                log.info("service_provider in professional Rank 2: {}", serviceProvider.getService_provider_id());
            }
            log.info("Service Provider in professional Rank 3: {}", professionalRank3.size());
            log.info("Service Provider in professional Rank 4: {}", professionalRank4.size());

            log.info("Service Provider in individual Rank 1: {}", individualRank1.size());
            log.info("Service Provider in individual Rank 2: {}", individualRank2.size());
            log.info("Service Provider in individual Rank 3: {}", individualRank3.size());
            log.info("Service Provider in individual Rank 4: {}", individualRank4.size());

            // Iterator for traversing orders.
            while (iterator.hasNext()) {

                CustomServiceProviderTicket ticket = iterator.next();

//                ObjectMapper objectMapper = new ObjectMapper();
//                String jsonString = objectMapper.writeValueAsString(customOrderState);
//                logger.info(jsonString);

                Order order = null;

                if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET)) {
                    order = orderService.findOrderById(ticket.getOrder().getId());
                } else if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_REVIEW_TICKET)) {
                    order = orderService.findOrderById(ticket.getParentTicket().getOrder().getId());
                } else {
                    throw new IllegalArgumentException("auto-assigner cannot perform action on this type of ticket.");
                }

                // created a switch statement which will execute in vertical order.
                switch (1) {
                    case 1:
                        if (!professionalRank1.isEmpty() && processRankForTickets(professionalRank1, order, assignedTickets, ticket)) {
                            iterator.remove();
                            break;
                        }
                    case 2:
                        if (!professionalRank2.isEmpty() && processRankForTickets(professionalRank2, order, assignedTickets, ticket)) {
                            iterator.remove();
                            break;
                        }
                    case 3:
                        if (!professionalRank3.isEmpty() && processRankForTickets(professionalRank3, order, assignedTickets, ticket)) {
                            iterator.remove();
                            break;
                        }
                    case 4:
                        if (!professionalRank4.isEmpty() && processRankForTickets(professionalRank4, order, assignedTickets, ticket)) {
                            iterator.remove();
                            break;
                        }
                    case 5:
                        if (!individualRank1.isEmpty() && processRankForTickets(individualRank1, order, assignedTickets, ticket)) {
                            iterator.remove();
                            break;
                        }
                    case 6:
                        if (!individualRank2.isEmpty() && processRankForTickets(individualRank2, order, assignedTickets, ticket)) {
                            iterator.remove();
                            break;
                        }
                    case 7:
                        if (!individualRank3.isEmpty() && processRankForTickets(individualRank3, order, assignedTickets, ticket)) {
                            iterator.remove();
                            break;
                        }
                    case 8:
                        if (!individualRank4.isEmpty() && processRankForTickets(individualRank4, order, assignedTickets, ticket)) {
                            iterator.remove();
                            break;
                        }
                    default:
                        break;
                }
            }
            log.info("Total orders assigned by VDTA method is: {}", assignedTickets.size());


        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught: " + exception.getMessage());
        }
    }

    public List<CustomServiceProviderTicket> getAllTickets() throws Exception {
        try {
            String sql = "SELECT * FROM custom_service_provider_ticket WHERE archived = false ORDER BY modified_date DESC NULLS LAST";
            return entityManager.createNativeQuery(sql, CustomServiceProviderTicket.class).getResultList();
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught: " + exception.getMessage());
        }
    }

    public List<CustomServiceProviderTicket> filterTicket(List<Long> states, List<Long> types, Long userId, Role role, Date dateFrom, Date dateTo, List<Long> statuses, List<Long> assigneeUserIds, Boolean dueInThreeDays, Boolean archived, Boolean overdue) throws Exception {
        try {
            // Initialize the JPQL query
            StringBuilder jpql = new StringBuilder("SELECT c FROM CustomServiceProviderTicket c ")
                    .append("WHERE 1=1 "); // Use this to simplify appending conditions

            // List to hold query parameters
            List<CustomTicketState> customTicketStates = new ArrayList<>();
            List<CustomTicketType> customTicketTypes = new ArrayList<>();
            List<CustomTicketStatus> customTicketStatuses = new ArrayList<>();

            // Conditionally build the query
            if (states != null && !states.isEmpty()) {
                for (Long id : states) {
                    CustomTicketState ticketState = ticketStateService.getTicketStateByTicketStateId(id);
                    if (ticketState == null) {
                        throw new IllegalArgumentException("NO TICKET STATE FOUND WITH THIS ID: " + id);
                    }
                    customTicketStates.add(ticketState);
                }
                jpql.append("AND c.ticketState IN :states ");
            }

            if (statuses != null && !statuses.isEmpty()) {
                for (Long id : statuses) {
                    CustomTicketStatus ticketStatus = ticketStatusService.getTicketStatusByTicketStatusId(id);
                    if (ticketStatus == null) {
                        throw new IllegalArgumentException("No ticket status found with ID: " + id);
                    }
                    customTicketStatuses.add(ticketStatus);
                }
                jpql.append("AND c.ticketStatus IN :statuses ");
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
                jpql.append("AND FUNCTION('DATE',c.createdDate) >= FUNCTION('DATE',:dateFrom) AND FUNCTION('DATE',c.createdDate) <= FUNCTION('DATE',:dateTo) ");
            } else if (dateFrom != null) {
                jpql.append("AND FUNCTION('DATE',c.createdDate) >= FUNCTION('DATE',:dateFrom)");
            } else if (dateTo != null) {
                jpql.append("AND FUNCTION('DATE',c.createdDate) <= FUNCTION('DATE',:dateTo) ");
            }

            if (userId != null && role != null) {
                jpql.append("AND c.assignee = :userId AND c.assigneeRole = :role ");
            }

            if (assigneeUserIds != null && !assigneeUserIds.isEmpty()) {
                jpql.append("AND c.assignee IN :assigneeUserIds ");
            }

            if (dueInThreeDays != null) {
                jpql.append(" AND c.targetCompletionDate BETWEEN :now AND :threeDaysLater ");
                if (states == null) {
                    CustomTicketState ticketState = ticketStateService.getTicketStateByTicketStateId(Constant.TICKET_STATE_CLOSE);
                    if (ticketState == null) {
                        throw new IllegalArgumentException("NO TICKET STATE FOUND WITH THIS ID: " + Constant.TICKET_STATE_CLOSE);
                    }
                    customTicketStates.add(ticketState);
                    jpql.append("AND c.ticketState NOT IN :states ");
                }
            }

            if(overdue != null) {
                if(overdue) {
                    jpql.append(" AND c.targetCompletionDate < :now ");
                } else {
                    jpql.append(" AND c.targetCompletionDate >= :now ");
                }
                if (states == null) {
                    CustomTicketState ticketState = ticketStateService.getTicketStateByTicketStateId(Constant.TICKET_STATE_CLOSE);
                    if (ticketState == null) {
                        throw new IllegalArgumentException("NO TICKET STATE FOUND WITH THIS ID: " + Constant.TICKET_STATE_CLOSE);
                    }
                    customTicketStates.add(ticketState);
                    jpql.append("AND c.ticketState NOT IN :states ");
                }
            }

            if (archived != null) {
                jpql.append("AND c.archived = :archived ");
            }

            jpql.append("ORDER BY modified_date DESC NULLS LAST ");
            // Create the query with the final JPQL string
            TypedQuery<CustomServiceProviderTicket> query = entityManager.createQuery(jpql.toString(), CustomServiceProviderTicket.class);

            // Set parameters
            if (!customTicketStates.isEmpty()) {
                query.setParameter("states", customTicketStates);
            }

            if (!customTicketStatuses.isEmpty()) {
                query.setParameter("statuses", customTicketStatuses);
            }

            if (!customTicketTypes.isEmpty()) {
                query.setParameter("types", customTicketTypes);
            }
            if (assigneeUserIds != null && !assigneeUserIds.isEmpty()) {
                query.setParameter("assigneeUserIds", assigneeUserIds);
            }

            if (dateFrom != null && dateTo != null) {
                query.setParameter("dateFrom", dateFrom);
                query.setParameter("dateTo", dateTo);
            } else if (dateFrom != null) {
                query.setParameter("dateFrom", dateFrom);
            } else if (dateTo != null) {
                query.setParameter("dateTo", dateTo);
            }
            if (userId != null && role != null) {
                query.setParameter("userId", userId);
                query.setParameter("role", role);
            }

            if (dueInThreeDays != null) {
                Date now = new Date(); // current time

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(now);
                calendar.add(Calendar.DATE, 3); // add 3 days

                Date threeDaysLater = calendar.getTime();

                query.setParameter("now", now);
                query.setParameter("threeDaysLater", threeDaysLater);
            }

            if(overdue != null) {
                Date now = new Date(); // current time
                log.info("current date is: {}", now);
                query.setParameter("now", now);
            }

            if (archived != null) {
                query.setParameter("archived", archived);
            }

            // Execute and return the result
            return query.getResultList();
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught: " + exception.getMessage());
        }
    }

    @Transactional
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

    @Transactional
    public CustomServiceProviderTicket fetchTicketByOrderId(Long orderId) throws Exception {
        try {
            if (orderId == null || orderId <= 0) {
                throw new IllegalArgumentException("OrderId cannot be <=0 or null");
            }
            OrderImpl order = entityManager.find(OrderImpl.class, orderId);

            Query query = entityManager.createQuery(Constant.GET_CUSTOM_SERVICE_PROVIDER_TICKET_BY_ORDER_ID, CustomServiceProviderTicket.class);
            query.setParameter("orderId", order);
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

    @Transactional
    public List<CustomServiceProviderTicket> fetchChildTicketByTicketId(Long parentTicketId) throws Exception {
        try {
            if (parentTicketId == null || parentTicketId <= 0) {
                throw new IllegalArgumentException("parentTicketId cannot be <=0 or null");
            }

            CustomServiceProviderTicket parentTicket = entityManager.find(CustomServiceProviderTicket.class, parentTicketId);
            Query query = entityManager.createQuery(Constant.GET_CUSTOM_SERVICE_PROVIDER_TICKET_BY_PARENT_TICKET_ID, CustomServiceProviderTicket.class);
            query.setParameter("parentTicketId", parentTicket);
            List<CustomServiceProviderTicket> ticket = query.getResultList();

            if (!ticket.isEmpty()) {
                return ticket;
            } else {
                return null;
            }

        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new Exception(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught: " + exception.getMessage());
        }
    }

    @Transactional
    public CustomServiceProviderTicket deleteTicketLogic(CustomServiceProviderTicket ticket, ServiceProviderEntity tokenServiceProvider) throws Exception {
        try {
            if (ticket == null) {
                throw new IllegalArgumentException("Ticket is null");
            }

            // check if already archived
            if (ticket.getArchived() != null && ticket.getArchived()) {
                throw new IllegalArgumentException("Ticket is already Archived.");
            }

            // According to logic only Review and miscellaneous ticket are allowed to be archived.
            if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET)) {
                throw new IllegalArgumentException("Cannot archive Primary ticket directly.");
            }

            if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_REVIEW_TICKET)) {
                CustomServiceProviderTicket parentTicket = ticket.getParentTicket();
                if (parentTicket == null) {
                    throw new IllegalArgumentException("Parent ticket for a Review is null.");
                }

                CustomTicketState updatedTicketState = ticketStateService.getTicketStateByTicketStateId(Constant.TICKET_STATE_IN_PROGRESS);
                parentTicket.setTicketState(updatedTicketState);
                deleteTicket(ticket, tokenServiceProvider);

            } else if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_MISCELLANEOUS_TICKET)) {
                // find the review ticket associated with this miscellaneous ticket
                List<CustomServiceProviderTicket> linkedTickets = fetchChildTicketByTicketId(ticket.getTicketId());

                // archive this ticket as well
                deleteTicket(ticket, tokenServiceProvider);
            } else {
                throw new IllegalArgumentException("Ticket contains Invalid Ticket Type.");
            }

            return ticket;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught: " + exception.getMessage());
        }
    }

    @Transactional
    public CustomServiceProviderTicket deleteTicket(CustomServiceProviderTicket ticket, ServiceProviderEntity tokenServiceProvider) throws Exception {
        try {
            if (ticket == null) {
                throw new IllegalArgumentException("Ticket is null");
            }

            Role tokenRole = roleService.getRoleByRoleId(tokenServiceProvider.getRole());
            if(tokenRole == null) {
                throw new IllegalArgumentException("Could not found token role.");
            }

            // find all the review ticket which are associated with this ticket and archive them as well.
            List<CustomServiceProviderTicket> childTickets = fetchChildTicketByTicketId(ticket.getTicketId());
            if (childTickets != null) {
                for (CustomServiceProviderTicket childTicket : childTickets) {
                    if (childTicket.getAssignee() != null && childTicket.getAssigneeRole() != null) {

                        ServiceProviderEntity assigneeServiceProvider = serviceProviderService.getServiceProviderById(childTicket.getAssignee());

                        if (assigneeServiceProvider == null) {
                            throw new IllegalArgumentException("Assignee not found with the assignee Id.");
                        }

                        if (childTicket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_TO_DO)) {
                            assigneeServiceProvider.setTicketAssigned(assigneeServiceProvider.getTicketAssigned() - 1);
                        } else if (childTicket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_CLOSE)) {
                            assigneeServiceProvider.setTicketCompleted(assigneeServiceProvider.getTicketCompleted() - 1);
                        } else {
                            assigneeServiceProvider.setTicketPending(assigneeServiceProvider.getTicketPending() - 1);
                        }
                        serviceProviderActionController.sendArchiveTicketMail(assigneeServiceProvider, tokenServiceProvider, childTicket);
                    } else { // Returned ticket case also handled here.
                        log.info("child ticket with id: {} has no assignee to update its ticket stats.", childTicket.getTicketId());
                    }
                    log.info("child ticket id: {}", childTicket.getTicketId());
                    childTicket.setArchived(true);
                    childTicket.setModifierId(tokenServiceProvider.getService_provider_id());
                    childTicket.setModifierRole(tokenRole);
                    childTicket.setModifiedDate(new Date());
                    entityManager.merge(childTicket);
                }
            }

            // Updating main ticket.
            if (ticket.getAssignee() != null && ticket.getAssigneeRole() != null) {

                ServiceProviderEntity assigneeServiceProvider = serviceProviderService.getServiceProviderById(ticket.getAssignee());

                if (assigneeServiceProvider == null) {
                    throw new IllegalArgumentException("Assignee not found with the assignee Id.");
                }

                if (ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_TO_DO)) {
                    assigneeServiceProvider.setTicketAssigned(assigneeServiceProvider.getTicketAssigned() - 1);
                } else if (ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_CLOSE)) {
                    assigneeServiceProvider.setTicketCompleted(assigneeServiceProvider.getTicketCompleted() - 1);
                } else {
                    assigneeServiceProvider.setTicketPending(assigneeServiceProvider.getTicketPending() - 1);
                }
                serviceProviderActionController.sendArchiveTicketMail(assigneeServiceProvider, tokenServiceProvider, ticket);
            } else { // Returned ticket case also handled here.
                log.info("Ticket with id: {} has no assignee to update its ticket stats.", ticket.getTicketId());
            }

            ticket.setArchived(true);
            ticket.setModifierRole(tokenRole);
            ticket.setModifierId(tokenServiceProvider.getService_provider_id());
            ticket.setModifiedDate(new Date());
            return entityManager.merge(ticket);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new Exception(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught: " + exception.getMessage());
        }
    }

    public Product findProductFromItemAttribute(OrderItem orderItem) {
        Long productId = Long.parseLong(orderItem.getOrderItemAttributes().get("productId").getValue());
        Product product = catalogService.findProductById(productId);
        return product;
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
