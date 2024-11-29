package com.community.api.endpoint.avisoft.controller.order;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.CreateTicketDto;
import com.community.api.dto.CustomTicketWrapper;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CombinedOrderDTO;
import com.community.api.entity.CustomAdmin;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomOrderState;
import com.community.api.entity.CustomOrderStatus;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.OrderCustomerDetailsDTO;
import com.community.api.entity.OrderDTO;
import com.community.api.entity.OrderStateRef;
import com.community.api.entity.Role;
import com.community.api.services.CustomOrderService;
import com.community.api.services.CustomerAddressFetcher;
import com.community.api.services.OrderDTOService;
import com.community.api.services.OrderStatusByStateService;
import com.community.api.services.PhysicalRequirementDtoService;
import com.community.api.services.ReserveCategoryDtoService;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.ServiceProviderTicketService;
import com.community.api.services.SharedUtilityService;
import com.community.api.services.exception.ExceptionHandlingImplement;

import javassist.NotFoundException;

import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderImpl;
import org.broadleafcommerce.core.order.service.OrderService;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RequestMapping(value = "/orders",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
)
@RestController
public class OrderController {
    private EntityManager entityManager;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ServiceProviderServiceImpl serviceProviderService;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private OrderStatusByStateService orderStatusByStateService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private ServiceProviderTicketService serviceProviderTicketService;
    @Autowired
    private CustomerAddressFetcher addressFetcher;
    @Autowired
    private CustomOrderService customOrderService;
    @Autowired
    private ReserveCategoryDtoService reserveCategoryDtoService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private PhysicalRequirementDtoService physicalRequirementDtoService;
    @Autowired
    private OrderDTOService orderDTOService;
    @Autowired
    private JwtUtil jwtTokenUtil;
    @Autowired
    private SharedUtilityService sharedUtilityService;

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    @RequestMapping(value = "get-order-history/{customerId}", method = RequestMethod.GET)
    public ResponseEntity<?> getOrderHistory(@RequestHeader(value = "Authorization") String authHeader, @PathVariable Long customerId, @RequestParam(defaultValue = "oldest-to-latest") String sort, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int limit) {
        try {
            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customerId);
            if (customCustomer == null)
                throw new NotFoundException("Customer with the provided Id not found");
            if (customCustomer.getNumberOfOrders() == 0)
                return ResponseService.generateErrorResponse("Order History Empty - No Orders placed", HttpStatus.OK);
            String orderNumber = "O-" + customerId + "%"; // Use % for wildcard search
            int startPosition = page * limit;
            String queryString = Constant.GET_ORDERS_USING_CUSTOMER_ID;
            if (sort.equals("latest-to-oldest"))
                queryString = queryString + " ORDER BY order_id DESC";
            Query query = entityManager.createNativeQuery(queryString);
            query.setFirstResult(startPosition);
            query.setMaxResults(limit);
            query.setParameter("orderNumber", orderNumber);
            List<BigInteger> orders = query.getResultList();
            return generateCombinedDTO(authHeader, orders, sort);
        } catch (NotFoundException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error fetching order list", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "details/{orderId}", method = RequestMethod.GET)
    public ResponseEntity<?> showDetails(@RequestHeader(value = "Authorization") String authHeader, @PathVariable Long orderId) {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Role role = roleService.getRoleByRoleId(roleId);
            Long id = Long.valueOf(orderId);
            if (id == null)
                return ResponseService.generateErrorResponse("Order Id not specified", HttpStatus.BAD_REQUEST);
            Order order = orderService.findOrderById(orderId);
            CustomOrderState orderState = entityManager.find(CustomOrderState.class, order.getId());
            OrderDTO orderDTO = null;
            if (order == null)
                throw new NullPointerException("Order not found");
            else {
                Customer customer = customerService.readCustomerById(order.getCustomer().getId());
                CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer.getId());
                CustomServiceProviderTicket customServiceProviderTicket = null;
                OrderCustomerDetailsDTO customerDetailsDTO = new OrderCustomerDetailsDTO(customer.getId(), customer.getFirstName() + " " + customer.getLastName(), customer.getEmailAddress(), customCustomer.getMobileNumber(), addressFetcher.fetch(customer), customer.getUsername());
                if (!role.getRole_name().equals(Constant.roleUser)) {
                    try {
                        Query query = entityManager.createNativeQuery(Constant.GET_PRIMARY_TICKET);
                        query.setParameter("orderId", orderId.longValue());
                        BigInteger tid = (BigInteger) query.getSingleResult();  // This will throw NoResultException if no result is found
                        customServiceProviderTicket = entityManager.find(CustomServiceProviderTicket.class, tid.longValue());
                    } catch (NoResultException e) {
                        // Handle the case where no result is found (e.g., log it or return null)
                        customServiceProviderTicket = null;
                    }
                }
                return ResponseService.generateSuccessResponse("order_details :", orderDTOService.wrapOrder(order, orderState, customServiceProviderTicket, customerDetailsDTO), HttpStatus.OK);
            }
        } catch (NullPointerException e) {
            return ResponseService.generateErrorResponse("Order not found", HttpStatus.NOT_FOUND);
        } catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error retrieving Order Details", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "show-all-orders", method = RequestMethod.GET)
    public ResponseEntity<?> showDetails(@RequestHeader(value = "Authorization") String authHeader,
                                         @RequestParam(defaultValue = "9") Integer orderState,
                                         @RequestParam(defaultValue = "oldest-to-latest") String sort,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "5") int limit) {
        try {
            sort = sort.toLowerCase();
            int startPosition = page * limit;
            List<BigInteger> orderIds = null;
            Query query = null;
            OrderStateRef orderStateRef = entityManager.find(OrderStateRef.class, orderState);
            if (orderState.equals(9)) {
                String queryString = Constant.GET_ALL_ORDERS;
                if (sort.equals("latest-to-oldest"))
                    queryString = queryString + " ORDER BY order_id DESC";
                query = entityManager.createNativeQuery(queryString);
                query.setFirstResult(startPosition);
                query.setMaxResults(limit);
                orderIds = query.getResultList();
            } else {
                if (orderStateRef == null)
                    return ResponseService.generateErrorResponse("Invalid orderState provided", HttpStatus.BAD_REQUEST);
                String queryString = Constant.SEARCH_ORDER_QUERY;
                if (sort.equals("latest-to-oldest"))
                    queryString = queryString + " ORDER BY order_id DESC";
                query = entityManager.createNativeQuery(queryString);
                query.setFirstResult(startPosition);
                query.setMaxResults(limit);
                query.setParameter("orderStateId", orderState);
            }
            orderIds = query.getResultList();
            return generateCombinedDTO(authHeader, orderIds, sort);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error Fetching order List", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void sortOrdersByDate(List<CombinedOrderDTO> orders) {
        Collections.sort(orders, new Comparator<CombinedOrderDTO>() {
            @Override
            public int compare(CombinedOrderDTO o1, CombinedOrderDTO o2) {
                return o2.getOrderDetails().getOrderPlacedDate().compareTo(o1.getOrderDetails().getOrderPlacedDate());
            }
        });
    }

    @Transactional
    public ResponseEntity<?> generateCombinedDTO(String authHeader, List<BigInteger> orders, String sort) {
        String jwtToken = authHeader.substring(7);
        Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
        Role role = roleService.getRoleByRoleId(roleId);
        try {
            Map<String, Object> orderMap = new HashMap<>();
            List<CombinedOrderDTO> orderDetails = new ArrayList<>();
            OrderDTO orderDTO = null;
            CustomServiceProviderTicket customServiceProviderTicket = null;
            for (BigInteger orderId : orders) {
                try {
                    Order order = orderService.findOrderById(orderId.longValue());
                    CustomOrderState orderState = entityManager.find(CustomOrderState.class, order.getId());
                    Customer customer = customerService.readCustomerById(order.getCustomer().getId());
                    CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer.getId());
                    OrderCustomerDetailsDTO customerDetailsDTO = new OrderCustomerDetailsDTO(customer.getId(), customer.getFirstName() + " " + customer.getLastName(), customer.getEmailAddress(), customCustomer.getMobileNumber(), addressFetcher.fetch(customer), customer.getUsername());
                    try {
                        Query query = entityManager.createNativeQuery(Constant.GET_PRIMARY_TICKET);
                        query.setParameter("orderId", order.getId());
                        BigInteger id = (BigInteger) query.getSingleResult();
                        // This will throw NoResultException if no result is found
                        customServiceProviderTicket = entityManager.find(CustomServiceProviderTicket.class, id.longValue());
                        System.out.println(customServiceProviderTicket);
                    } catch (NoResultException e) {
                        //the case where no result is found 
                        customServiceProviderTicket = null;
                    }
                    orderDetails.add(orderDTOService.wrapOrder(order, orderState, customServiceProviderTicket, customerDetailsDTO));
                } catch (NullPointerException e) {
                    continue;
                }
            }
            return ResponseService.generateSuccessResponse("Orders", orderDetails, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error fetching orders ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    //@Authorize(value = {Constant.roleSuperAdmin,Constant.roleAdmin})
    @RequestMapping(value = "assign-order/{orderId}", method = RequestMethod.POST)
    public ResponseEntity<?> manuallyAssignOrder(@PathVariable Long orderId, @RequestBody CreateTicketDto createTicketDto) {
        try {

            if (createTicketDto.getTicketType() == 1L) {
                Query query = entityManager.createNativeQuery(Constant.GET_PRIMARY_TICKET);
                query.setParameter("orderId", orderId);
                if (!query.getResultList().isEmpty()) {
                    return ResponseService.generateErrorResponse("Primary ticket already exists", HttpStatus.BAD_REQUEST);
                }
            }

            /*List<String> errorMessages = new ArrayList<>();
            for (Field field : createTicketDto.getClass().getDeclaredFields()) {
//                field.setAccessible(true); // Allow access to private fields
                Object value = field.get(createTicketDto); // Get the value of the field

                // Check if the field value is null or empty for specific types
                if (value == null) {
                    errorMessages.add(field.getName() + " cannot be null");
                }

                if (value instanceof String && ((String) value).isEmpty()) {
                    errorMessages.add(field.getName() + " cannot be empty");
                }
            }
            System.out.println(errorMessages);*/

            Role role = null;
            if (createTicketDto.getAssigneeRole() != null) {
                role = roleService.getRoleByRoleId(createTicketDto.getAssigneeRole());
                if (role == null) {
                    return ResponseService.generateErrorResponse("Invalid role", HttpStatus.BAD_REQUEST);
                }
            }

            /*if (!errorMessages.isEmpty()) {
                return ResponseService.generateErrorResponse("Cannot assign order : " + errorMessages, HttpStatus.BAD_REQUEST);
            }*/

            Order order = orderService.findOrderById(orderId);
            if (order == null) {
                return ResponseService.generateErrorResponse("Order with the provided id not found", HttpStatus.NOT_FOUND);
            }

            CustomOrderState customOrderState = entityManager.find(CustomOrderState.class, order.getId());
            ServiceProviderEntity serviceProvider = null;
            CustomAdmin customAdmin = null;
            if (!customOrderState.getOrderStateId().equals(Constant.ORDER_STATE_UNASSIGNED.getOrderStateId()) && !customOrderState.getOrderStateId().equals(Constant.ORDER_STATE_NEW.getOrderStateId())) {
                return ResponseService.generateErrorResponse("Cannot assign this order manually as its status is : " + orderStatusByStateService.getOrderStateById(customOrderState.getOrderStateId()).getOrderStateName(), HttpStatus.UNPROCESSABLE_ENTITY);
            }

            if (role.getRole_name().equals(Constant.roleServiceProvider)) {
                serviceProvider = entityManager.find(ServiceProviderEntity.class, createTicketDto.getAssignee());
                if (serviceProvider == null)
                    return ResponseService.generateErrorResponse("Service Provider with the provided id not found", HttpStatus.NOT_FOUND);

            } else if (role.getRole_name().equals(Constant.roleAdmin)) {
                customAdmin = entityManager.find(CustomAdmin.class, createTicketDto.getAssignee());
                if (customAdmin == null)
                    return ResponseService.generateErrorResponse("Admin with the provided id not found", HttpStatus.NOT_FOUND);
            }

            /*CreateTicketDto createTicketDto = new CreateTicketDto();
            createTicketDto.setAssignee(createTicketDto.getAssignee());
            createTicketDto.setTicketState(createTicketDto.getTicketState());
            createTicketDto.setTicketStatus(createTicketDto.getTicketStatus());
            createTicketDto.setAssigneeRole(role);
            createTicketDto.setTargetCompletionDate(createTicketDto.getTargetCompletionDate());
            createTicketDto.setTicketType(createTicketDto.getTicketType());*/

            CustomServiceProviderTicket customServiceProviderTicket = serviceProviderTicketService.createTicket(createTicketDto, (OrderImpl) order, serviceProvider);

            customOrderState.setOrderStateId(Constant.ORDER_STATE_ASSIGNED.getOrderStateId());
            entityManager.merge(customOrderState);

            Customer customer = customerService.readCustomerById(order.getCustomer().getId());
            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, order.getCustomer().getId());
            OrderCustomerDetailsDTO customerDetailsDTO = new OrderCustomerDetailsDTO(customer.getId(), customer.getFirstName() + " " + customer.getLastName(), customer.getEmailAddress(), customCustomer.getMobileNumber(), addressFetcher.fetch(customer), customer.getUsername());
            CombinedOrderDTO combinedOrderDTO = orderDTOService.wrapOrder(order, customOrderState, customServiceProviderTicket, customerDetailsDTO);

            CustomTicketWrapper wrapper = new CustomTicketWrapper();
            wrapper.customWrapDetails(customServiceProviderTicket, combinedOrderDTO);

            return ResponseService.generateSuccessResponse("Order Assigned", wrapper, HttpStatus.OK);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandling.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse("Error assigning Request to Service Provider : " + illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Error assigning Request to Service Provider : " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @GetMapping("/{orderId}/availableSp")
    public ResponseEntity<?> getEligibleSp(@PathVariable Long orderId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int limit) {
        try {
            List<ServiceProviderEntity> result = customOrderService.availableSp(orderId, page, limit);
            return ResponseService.generateSuccessResponse("List of available Sp", result, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error viewing SP List", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("find-order-status/{orderStateId}")
    public ResponseEntity<?> findOrderStatusByOrderState(@PathVariable Integer orderStateId) {
        try {
            OrderStateRef orderStateRef = entityManager.find(OrderStateRef.class, orderStateId);
            if (orderStateRef == null)
                return ResponseService.generateErrorResponse("Order State Id is invalid", HttpStatus.BAD_REQUEST);
            List<CustomOrderStatus> result = orderStatusByStateService.getOrderStatusByOrderStateId(orderStateId);
            if (result.isEmpty())
                return ResponseService.generateErrorResponse("No Results Found", HttpStatus.OK);
            return ResponseService.generateSuccessResponse("Order Statuses", result, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error in fetching status list : ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("find-order-state/{orderStatusId}")
    public ResponseEntity<?> findOrderStatebyStatusId(@PathVariable Integer orderStatusId) {
        try {
            CustomOrderStatus customOrderStatus = entityManager.find(CustomOrderStatus.class, orderStatusId);
            if (customOrderStatus == null)
                return ResponseService.generateErrorResponse("Order Status Id invalid", HttpStatus.BAD_REQUEST);
            OrderStateRef result = orderStatusByStateService.getOrderStateByOrderStatus(orderStatusId);
            return ResponseService.generateSuccessResponse("Order State Linked :", result, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error in fetching status list : ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}