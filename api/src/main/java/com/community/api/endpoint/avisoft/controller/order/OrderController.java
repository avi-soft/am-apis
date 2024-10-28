package com.community.api.endpoint.avisoft.controller.order;
import com.community.api.component.Constant;
import com.community.api.dto.CustomProductWrapper;
import com.community.api.dto.PhysicalRequirementDto;
import com.community.api.dto.ReserveCategoryDto;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CombinedOrderDTO;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomOrderState;
import com.community.api.entity.CustomOrderStatus;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.OrderCustomerDetailsDTO;
import com.community.api.entity.OrderDTO;
import com.community.api.entity.OrderRequest;
import com.community.api.entity.OrderStateRef;
import com.community.api.services.CustomOrderService;
import com.community.api.services.CustomerAddressFetcher;
import com.community.api.services.OrderDTOService;
import com.community.api.services.OrderStatusByStateService;
import com.community.api.services.PhysicalRequirementDtoService;
import com.community.api.services.ReserveCategoryDtoService;
import com.community.api.services.ResponseService;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.SharedUtilityService;
import com.community.api.services.exception.ExceptionHandlingImplement;

import javassist.NotFoundException;

import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.order.service.OrderService;
import org.broadleafcommerce.core.web.order.OrderState;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping(value = "/orders",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
)
@RestController
public class OrderController
{
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
    private CustomerAddressFetcher addressFetcher;
    @Autowired
    private CustomOrderService customOrderService;
    @Autowired
    private ReserveCategoryDtoService reserveCategoryDtoService;
    @Autowired
    private PhysicalRequirementDtoService physicalRequirementDtoService;
    @Autowired
    private OrderDTOService orderDTOService;
    @Autowired
    private SharedUtilityService sharedUtilityService;
    @Autowired
    public void setEntityManager(EntityManager entityManager)
    {
        this.entityManager=entityManager;
    }
    @Transactional
    @RequestMapping(value = "get-order-history/{customerId}",method = RequestMethod.GET)
    public ResponseEntity<?> getOrderHistory(@PathVariable Long customerId, @RequestParam(defaultValue = "oldest-to-latest") String sort,@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "5") int limit)
    {
        try{
            CustomCustomer customCustomer=entityManager.find(CustomCustomer.class,customerId);
            if (customCustomer==null)
                throw new NotFoundException("Customer with the provided Id not found");
            if(customCustomer.getNumberOfOrders()==0)
                return ResponseService.generateErrorResponse("Order History Empty - No Orders placed", HttpStatus.OK);
            String orderNumber = "O-"+customerId+"%"; // Use % for wildcard search
            int startPosition=page*limit;
            Query query = entityManager.createNativeQuery(Constant.GET_ORDERS_USING_CUSTOMER_ID);
            query.setFirstResult(startPosition);
            query.setMaxResults(limit);
            query.setParameter("orderNumber", orderNumber);
            List<BigInteger> orders = query.getResultList();
            return generateCombinedDTO(orders,sort);
        }catch(NotFoundException e)
        {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } 
        catch (Exception e)
        {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error fetching order list", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Transactional
    @RequestMapping(value = "details/{orderId}",method = RequestMethod.GET)
    public ResponseEntity<?> showClubbedOrders(@PathVariable Long orderId) {
        try {
            Long id = Long.valueOf(orderId);
            if (id == null)
                return ResponseService.generateErrorResponse("Order Id not specified", HttpStatus.BAD_REQUEST);
            Order order = orderService.findOrderById(orderId);
            CustomOrderState orderState = entityManager.find(CustomOrderState.class, order.getId());
            OrderDTO orderDTO = null;
            if (order == null)
                throw new NullPointerException("Order not found");
            else {
                Customer customer=customerService.readCustomerById(order.getCustomer().getId());
                CustomCustomer customCustomer=entityManager.find(CustomCustomer.class,customer.getId());
                OrderCustomerDetailsDTO customerDetailsDTO=new OrderCustomerDetailsDTO(customer.getId(),customer.getFirstName()+" "+customer.getLastName(),customer.getEmailAddress(),customCustomer.getMobileNumber(),addressFetcher.fetch(customer),customer.getUsername());
                return ResponseService.generateSuccessResponse("order_details :", orderDTOService.wrapOrder(order, orderState, null,customerDetailsDTO), HttpStatus.OK);
            }
        }
           catch (NullPointerException e) {
                return ResponseService.generateErrorResponse("Order not found",HttpStatus.NOT_FOUND);
        }catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error retrieving Order Details", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Transactional
    @RequestMapping(value = "show-all-orders",method = RequestMethod.GET)
        public ResponseEntity<?> showClubbedOrders( @RequestParam(defaultValue = "9") Integer orderState,
                                                @RequestParam(defaultValue = "oldest-to-latest")String sort,
                                                @RequestParam(defaultValue = "0")int page,
                                                @RequestParam(defaultValue = "5")int limit) {
        try {
            sort = sort.toLowerCase();
            int startPosition = page * limit;
            List<BigInteger> orderIds = null;
            Query query = null;
            OrderStateRef orderStateRef=entityManager.find(OrderStateRef.class,orderState);
            if (orderState.equals(9)) {
                query = entityManager.createNativeQuery(Constant.GET_ALL_ORDERS);
                query.setFirstResult(startPosition);
                query.setMaxResults(limit);
                orderIds = query.getResultList();
            } else {
                if(orderStateRef==null)
                    return ResponseService.generateErrorResponse("Invalid orderState provided",HttpStatus.BAD_REQUEST);
                query = entityManager.createNativeQuery(Constant.SEARCH_ORDER_QUERY);
                query.setFirstResult(startPosition);
                query.setMaxResults(limit);
                query.setParameter("orderStateId",orderState);
            }
            orderIds = query.getResultList();
            return generateCombinedDTO(orderIds, sort);
        } catch (Exception e)
        {
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
    public ResponseEntity<?>generateCombinedDTO(List<BigInteger>orders,String sort)
    {
        try{
            Map<String,Object>orderMap=new HashMap<>();
            List<CombinedOrderDTO>orderDetails=new ArrayList<>();
            OrderDTO orderDTO=null;
            for(BigInteger orderId:orders) {
                Order order = orderService.findOrderById(orderId.longValue());
                CustomOrderState orderState = entityManager.find(CustomOrderState.class, order.getId());
                if (order != null) {
                    Customer customer=customerService.readCustomerById(order.getCustomer().getId());
                    CustomCustomer customCustomer=entityManager.find(CustomCustomer.class,customer.getId());
                    OrderCustomerDetailsDTO customerDetailsDTO=new OrderCustomerDetailsDTO(customer.getId(),customer.getFirstName()+" "+customer.getLastName(),customer.getEmailAddress(),customCustomer.getMobileNumber(),addressFetcher.fetch(customer),customer.getUsername());
                    orderDetails.add(orderDTOService.wrapOrder(order,orderState,null,customerDetailsDTO));
                }
            }
            if(sort.equals("latest-to-oldest"))
                sortOrdersByDate(orderDetails);
            else if(!sort.equals("oldest-to-latest"))
                return ResponseService.generateErrorResponse("Invalid sort option",HttpStatus.BAD_REQUEST);
            orderMap.put("orderList",orderDetails);
            return ResponseService.generateSuccessResponse("Orders",orderMap,HttpStatus.OK);
        }catch (Exception e)
        {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error fetching orders ", HttpStatus.INTERNAL_SERVER_ERROR);
        }}
    @Transactional
    @RequestMapping(value = "assign-order/{orderId}/{serviceProviderId}",method = RequestMethod.POST)
    public ResponseEntity<?>manuallyAssignOrder(@PathVariable Long orderId,@PathVariable Long serviceProviderId) {
        try {
            Order order = orderService.findOrderById(orderId);
            if(order==null)
                return ResponseService.generateErrorResponse("Order not found",HttpStatus.NOT_FOUND);
            CustomOrderState customOrderState=entityManager.find(CustomOrderState.class,order.getId());
            if(!customOrderState.getOrderStateId().equals(Constant.ORDER_STATE_UNASSIGNED.getOrderStateId()))
                return ResponseService.generateErrorResponse("Cannot assign this order manually as its status is : "+orderStatusByStateService.getOrderStateById(customOrderState.getOrderStateId()).getOrderStateName(),HttpStatus.UNPROCESSABLE_ENTITY);
            if (order == null)
                return ResponseService.generateErrorResponse("Order with the provided id not found", HttpStatus.NOT_FOUND);
            ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
            if (serviceProvider == null)
                return ResponseService.generateErrorResponse("Service Provider with the provided id not found", HttpStatus.NOT_FOUND);
            Query query = entityManager.createNativeQuery(Constant.GET_SP_ORDER_REQUEST);
            query.setParameter("orderId", orderId);
            query.setParameter("serviceProviderId", serviceProviderId);
            Integer queryResultCount=query.getResultList().size();
            if(queryResultCount==1)
            {
                return ResponseService.generateErrorResponse("Unable to assign order: This order request has been previously returned by the chosen service provider.",HttpStatus.BAD_REQUEST);
            }
            OrderRequest orderRequest = new OrderRequest();
            orderRequest.setOrderId(order.getId());
            orderRequest.setServiceProvider(serviceProvider);
            orderRequest.setRequestStatus("GENERATED");
            orderRequest.setGeneratedAt(LocalDateTime.now());
            orderRequest.setUpdatedAt(LocalDateTime.now());
            entityManager.persist(orderRequest);
            serviceProvider.getOrderRequests().add(orderRequest);
            order.setStatus(Constant.ORDER_STATUS_ASSIGNED);
            Integer orderStatusId=orderStatusByStateService.getOrderStatusByOrderStateId(Constant.ORDER_STATE_ASSIGNED.getOrderStateId()).get(0).getOrderStatusId();
            customOrderState.setOrderStatusId(orderStatusId);
            customOrderState.setOrderStateId(Constant.ORDER_STATE_ASSIGNED.getOrderStateId());
            entityManager.merge(customOrderState);
            entityManager.merge(order);
            entityManager.merge(serviceProvider);
            return ResponseService.generateSuccessResponse("Order Request Generated", orderRequest, HttpStatus.OK);
        }catch (Exception e)
        {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error assigning Request to Service Provider", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Transactional
    @GetMapping("/{orderId}/availableSp")
    public ResponseEntity<?>getEligibleSp(@PathVariable Long orderId,@RequestParam (defaultValue = "0") int page ,@RequestParam(defaultValue = "10") int limit)
    {
        try{
            List<ServiceProviderEntity>result=customOrderService.availableSp(orderId,page,limit);
            return ResponseService.generateSuccessResponse("List of available Sp",result,HttpStatus.OK);
        }catch (Exception e)
        {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error viewing SP List", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("find-order-status/{orderStateId}")
    public ResponseEntity<?>findOrderStatusByOrderState(@PathVariable Integer orderStateId)
    {
        try {
            OrderStateRef orderStateRef=entityManager.find(OrderStateRef.class,orderStateId);
            if(orderStateRef==null)
                return ResponseService.generateErrorResponse("Order State Id is invalid",HttpStatus.BAD_REQUEST);
            List<CustomOrderStatus>result=orderStatusByStateService.getOrderStatusByOrderStateId(orderStateId);
            if(result.isEmpty())
                return ResponseService.generateErrorResponse("No Results Found",HttpStatus.OK);
            return ResponseService.generateSuccessResponse("Order Statuses",result,HttpStatus.OK);
        }catch (Exception e)
        {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error in fetching status list : ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("find-order-state/{orderStatusId}")
    public ResponseEntity<?>findOrderStatebyStatusId(@PathVariable Integer orderStatusId)
    {
        try {
            CustomOrderStatus customOrderStatus=entityManager.find(CustomOrderStatus.class,orderStatusId);
            if(customOrderStatus==null)
                return ResponseService.generateErrorResponse("Order Status Id invalid",HttpStatus.BAD_REQUEST);
            OrderStateRef result=orderStatusByStateService.getOrderStateByOrderStatus(orderStatusId);
            return ResponseService.generateSuccessResponse("Order State Linked :",result,HttpStatus.OK);
        }catch (Exception e)
        {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error in fetching status list : ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}