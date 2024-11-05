package com.community.api.services;

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
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderImpl;
import org.broadleafcommerce.core.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    ProductService productService;

    @Autowired
    EntityManager entityManager;

    public void autoAssigner() throws Exception {
        try{
            // we are fetching SP who are in approved state (later we will do only active)
            List<Map<String, Object>> availableServiceProvider = (List<Map<String, Object>>) serviceProviderService.searchServiceProviderBasedOnGivenFields(null, null, null, null, null, 1L);
            System.out.println("*************" + availableServiceProvider.size());
            // later will do order which are in particular state. (write now just fetching which are in state 2).
            OrderStateRef orderStateRef = orderStateRefService.getOrderStateByOrderStateId(1);
            List<CustomOrderState> customOrders = customOrderService.getCustomOrdersByOrderStateId(orderStateRef.getOrderStateId());

            System.out.println("TILL1");
            randomBindingTicketAllocation(customOrders);
            System.out.println("TILL2");
            verticalDistributionTicketAllocation(customOrders, availableServiceProvider);
            System.out.println("TILL3");

        } catch (Exception exception){
            throw new Exception("Exception caught: " + exception.getMessage());
        }
    }

    public void randomBindingTicketAllocation(List<CustomOrderState> customOrders) {
        /*try {

            logger.info("Total Orders at the moments are: " + customOrders.size());

            List<Order> assignedOrders = new ArrayList<>();

            boolean assigned = false;
            for (CustomOrderState customOrderState : customOrders) {
                assigned = false;

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonString = objectMapper.writeValueAsString(customOrderState);
                logger.info(jsonString);

                Order order = orderService.findOrderById(customOrderState.getOrderId());
                CustomCustomer customer = entityManager.find(CustomCustomer.class, order.getCustomer().getId());
//                String customerString = objectMapper.writeValueAsString(customer);
//                logger.info(customerString);

//                Query query = entityManager.createQuery(Constant.GET_ORDERS_BY_ORDER_STATE_ID, CustomOrderState.class);
//                query.setParameter("orderStateId", orderStateRef.getOrderStateId());
//                List<CustomOrderState> orderState = query.getResultList();
                List<CustomerReferrer> referrers = customer.getMyReferrer();
                System.out.println("Referrer list:" + referrers.size());
                for (CustomerReferrer refferer : referrers) {
                    ServiceProviderEntity serviceProvider = refferer.getServiceProvider();
                    System.out.println("REFFEREER ID: " + serviceProvider.getService_provider_id());

                    if (serviceProvider.getIsActive()) {
                        // create a entry in serviceProvider tickets tables where the info about which serviceProvider is linked with which ticket is stored.
                        CreateTicketDto createTicketDto = new CreateTicketDto();
                        createTicketDto.setTicketState(1L);
                        createTicketDto.setTicketType(1L);
                        createTicketDto.setTicketStatus(1L);
                        createTicketDto.setAssignTo(serviceProvider.getService_provider_id());
                        CustomServiceProviderTicket serviceProviderTicket = createTicket(createTicketDto, (OrderImpl) order, serviceProvider);
                        serviceProviderTicket.setModifiedDate(new Date());
                        serviceProviderTicket.setTicketAssignDate(new Date());
                        assigned = true;
                        customOrders.remove(customOrderState);
                        assignedOrders.add(order);
                        break;
                    }

                }
                // now search for creator of product.
                if (!assigned) {
                    System.out.println("INSIDE THE CREATOR OF THE PRODUCT LOGIC OF RBTA");
                    Long productId = Long.parseLong(order.getOrderItems().get(0).getOrderItemAttributes().get("productId").getValue());
                    CustomProduct customProduct = productService.getCustomProductByCustomProductId(productId);

                    ServiceProviderEntity serviceProvider = serviceProviderService.getServiceProviderById(customProduct.getUserId());
                    if (serviceProvider.getIsActive()) {
                        // create a entry in serviceProvider tickets tables where the info about which serviceProvider is linked with which ticket is stored.
                        CreateTicketDto createTicketDto = new CreateTicketDto();
                        createTicketDto.setTicketState(1L);
                        createTicketDto.setTicketType(1L);
                        createTicketDto.setTicketStatus(1L);
                        createTicketDto.setAssignTo(serviceProvider.getService_provider_id());
                        CustomServiceProviderTicket serviceProviderTicket = createTicket(createTicketDto, (OrderImpl) order, serviceProvider);

                        System.out.println("ticket state: " + serviceProviderTicket.getTicketState().getTicketState());

                        serviceProviderTicket.setModifiedDate(new Date());
                        serviceProviderTicket.setTicketAssignDate(new Date());
                        serviceProviderTicket.setAssignTo(serviceProvider);
                        customOrders.remove(customOrderState);
                        assignedOrders.add(order);
                        break;
                    }
                }

                Long pid = Long.valueOf(order.getOrderItems().get(0).getOrderItemAttributes().get("productId").getValue());
                CustomProduct customProduct = entityManager.find(CustomProduct.class, pid);

            }

            logger.info("Total orders assigned by RBTA method is: " + assignedOrders.size());

        } catch (Exception exception) {
            System.out.println("Exception caught: " + exception.getMessage());
        }*/
    }

    @Transactional
    public CustomServiceProviderTicket createTicket(CreateTicketDto createTicketDto, OrderImpl order, ServiceProviderEntity assignedTo) throws Exception {
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
            customServiceProviderTicket.setModifiedDate(customServiceProviderTicket.getCreatedDate());

            if(assignedTo != null) {
                customServiceProviderTicket.setAssignTo(assignedTo);
            }

            if(createTicketDto.getTicketState() != null) {
                CustomTicketState ticketState = ticketStateService.getTicketStateByTicketId(createTicketDto.getTicketState());
                customServiceProviderTicket.setTicketState(ticketState);
            } else {
                throw new IllegalArgumentException("Ticket State is mandatory field while creating a ticket");
            }

            if(createTicketDto.getTicketType() != null) {
                CustomTicketType ticketType = ticketTypeService.getTicketTypeByTicketTypeId(createTicketDto.getTicketType());
                customServiceProviderTicket.setTicketType(ticketType);
            } else {
                throw new IllegalArgumentException("Ticket Type is mandatory field while creating a ticket");
            }

            if(createTicketDto.getTicketStatus()!=null){
                CustomTicketStatus ticketStatus = ticketStatusService.getTicketStatusByTicketStatusId(createTicketDto.getTicketStatus());
                customServiceProviderTicket.setTicketStatus(ticketStatus);
            }

            customServiceProviderTicket = entityManager.merge(customServiceProviderTicket);
            return customServiceProviderTicket;

        } catch (IllegalArgumentException illegalArgumentException) {
            throw new IllegalArgumentException("Illegal Exception Caught: " + illegalArgumentException.getMessage());
        } catch (Exception exception) {
            throw new Exception("Some Exception Caught: " + exception.getMessage());
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
//            customServiceProviderTicket.setOrder(order);

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


    public void verticalDistributionTicketAllocation(List<CustomOrderState> customOrders, List<Map<String, Object>> availableServiceProvider) throws Exception {
        try{
            System.out.println("INSIDE VDTA FOR Ticket Allocation");
            logger.info("Total orders recieved for VDTA: " + customOrders.size());
            logger.info("Total Service Provider: " + availableServiceProvider.size());

            List<Order> assignedOrders = new ArrayList<>();

            for (CustomOrderState customOrderState : customOrders) {

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonString = objectMapper.writeValueAsString(customOrderState);
                logger.info(jsonString);

                System.out.println("Order Found");
                Order order = orderService.findOrderById(customOrderState.getOrderId());
                CustomCustomer customer = entityManager.find(CustomCustomer.class, order.getCustomer().getId());
                System.out.println("Order Found1");
//                String customerString = objectMapper.writeValueAsString(customer);
//                logger.info(customerString);

//                Query query = entityManager.createQuery(Constant.GET_ORDERS_BY_ORDER_STATE_ID, CustomOrderState.class);
//                query.setParameter("orderStateId", orderStateRef.getOrderStateId());
//                List<CustomOrderState> orderState = query.getResultList();

                for (Map<String, Object> serviceProviderMap : availableServiceProvider) {
                    System.out.println("Order Found2");
                    ServiceProviderEntity serviceProvider = serviceProviderService.getServiceProviderById(Long.valueOf(serviceProviderMap.get("service_provider_id").toString()));

                    System.out.println("Order Found3");
                    if (serviceProvider.getIsActive()) {
                        // create a entry in serviceProvider tickets tables where the info about which serviceProvider is linked with which ticket is stored.
                        CreateTicketDto createTicketDto = new CreateTicketDto();
                        createTicketDto.setTicketState(1L);
                        createTicketDto.setTicketType(1L);
                        createTicketDto.setTicketStatus(1L);
                        createTicketDto.setAssignTo(serviceProvider.getService_provider_id());
                        CustomServiceProviderTicket serviceProviderTicket = createTicket(createTicketDto, (OrderImpl) order, serviceProvider);
                        serviceProviderTicket.setModifiedDate(new Date());
                        serviceProviderTicket.setTicketAssignDate(new Date());
                        customOrders.remove(customOrderState);
                        assignedOrders.add(order);

                        System.out.println("Order Found4");
                        // Link that ticket to the service provider and increment its ticketAllocated and everthing.
                        availableServiceProvider.remove(serviceProviderMap);
                        assignedOrders.add(order);
                        break;
                    }

                }

                System.out.println("Order Found5");
                Long pid = Long.valueOf(order.getOrderItems().get(0).getOrderItemAttributes().get("productId").getValue());
                CustomProduct customProduct = entityManager.find(CustomProduct.class, pid);
                System.out.println("Order Found6");
            }
            System.out.println("NICE ####################");
            logger.info("Total orders assigned by VDTA method is: " + assignedOrders.size());


        } catch (Exception exception) {
            throw new Exception("Exception caught: " + exception.getMessage());
        }
    }

}
