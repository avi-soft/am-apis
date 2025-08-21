package com.community.api.endpoint.avisoft.controller.order;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.CreateTicketDto;
import com.community.api.dto.CustomTicketWrapper;
import com.community.api.dto.SPDto;
import com.community.api.endpoint.avisoft.controller.ServiceProviderActionController;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CombinedOrderDTO;
import com.community.api.dto.OrderStateGroupDto;

import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomOrderState;
import com.community.api.entity.CustomOrderStatus;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.CustomTicketState;
import com.community.api.entity.CustomTicketStatus;
import com.community.api.entity.CustomTicketType;
import com.community.api.entity.OrderCustomerDetailsDTO;
import com.community.api.entity.OrderDTO;
import com.community.api.entity.OrderStateRef;
import com.community.api.entity.RazorpayDetails;
import com.community.api.entity.Refunds;
import com.community.api.entity.Role;
import com.community.api.services.CustomOrderService;
import com.community.api.services.CustomerAddressFetcher;
import com.community.api.services.OrderDTOService;
import com.community.api.services.OrderStateRefService;
import com.community.api.services.OrderStatusByStateService;
import com.community.api.services.PhysicalRequirementDtoService;
import com.community.api.services.ProductService;
import com.community.api.services.ReserveCategoryDtoService;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.ServiceProviderTicketService;
import com.community.api.services.SharedUtilityService;
import com.community.api.services.TicketStateService;
import com.community.api.services.TicketStatusService;
import com.community.api.services.TicketTypeService;
import com.community.api.services.exception.ExceptionHandlingImplement;

import javassist.NotFoundException;

import lombok.extern.slf4j.Slf4j;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderImpl;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.order.service.OrderService;
import org.broadleafcommerce.core.web.order.OrderState;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequestMapping(value = "/orders", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
@RestController
public class OrderController {

    @Autowired
    protected CatalogService catalogService;
    @Autowired
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
    private TicketTypeService ticketTypeService;
    @Autowired
    private TicketStateService ticketStateService;
    @Autowired
    private TicketStatusService ticketStatusService;
    @Autowired
    private OrderDTOService orderDTOService;
    @Autowired
    private JwtUtil jwtTokenUtil;
    @Autowired
    private SharedUtilityService sharedUtilityService;
    @Autowired
    private OrderStateRefService orderStateRefService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ServiceProviderActionController serviceProviderActionController;

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

/*    @Transactional
    @RequestMapping(value = "get-order-history/{customerId}", method = RequestMethod.GET)
    public ResponseEntity<?> getOrderHistory(@RequestHeader(value = "Authorization") String authHeader, @PathVariable Long customerId, @RequestParam(defaultValue = "oldest-to-latest") String sort, @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "30") int limit) {
        try {
            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customerId);
            if (customCustomer == null)
                throw new NotFoundException("Customer with the provided Id not found");
            if (customCustomer.getNumberOfOrders() == 0)
                return ResponseService.generateErrorResponse("Order History Empty - No Orders placed", HttpStatus.OK);
            // Validate parameters first
            if (offset < 0) {
                return ResponseService.generateErrorResponse("Offset for pagination cannot be a negative number", HttpStatus.BAD_REQUEST);
            }
            if (limit <= 0) {
                return ResponseService.generateErrorResponse("Limit for pagination cannot be a negative number or 0", HttpStatus.BAD_REQUEST);
            }

            BigInteger totalItems;
            BigInteger totalPages;
            String orderNumber = "O-" + customerId + "%";
            Query countQuery = entityManager.createNativeQuery(
                    "SELECT COUNT(*) FROM blc_order o WHERE o.order_number LIKE :orderNumber and tax_override is null");
            countQuery.setParameter("orderNumber", orderNumber);
            totalItems = (BigInteger) countQuery.getSingleResult();
            totalPages = BigInteger.valueOf((int) Math.ceil((double) totalItems.intValue() / limit));

            if (offset >= totalPages.intValue() && offset != 0) {
                return ResponseService.generateErrorResponse("No Orders Available", HttpStatus.BAD_REQUEST);
            }


            int startPosition = offset * limit;
            String queryString = Constant.GET_ORDERS_USING_CUSTOMER_ID;
            if (sort.equals("latest-to-oldest"))
                queryString = queryString + " ORDER BY order_id DESC";
            Query query = entityManager.createNativeQuery(queryString);
            query.setFirstResult(startPosition);
            query.setMaxResults(limit);
            query.setParameter("orderNumber", orderNumber);
            List<BigInteger> orders = query.getResultList();
            return generateCombinedDTO(authHeader, orders, sort, totalItems.intValue(), totalPages.intValue(), offset);
        } catch (NotFoundException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error fetching order list", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/

    @Transactional
    @RequestMapping(value = "get-order-history/{customerId}", method = RequestMethod.GET)
    public ResponseEntity<?> getOrderHistory(@RequestHeader(value = "Authorization") String authHeader, @PathVariable Long customerId, @RequestParam(defaultValue = "DESC") String sortOrder, @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "30") int limit, @RequestParam(value = "date_to", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateTo, @RequestParam(value = "date_from", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFrom, @RequestParam(value = "order_state", required = false) List<Integer> orderStateIds, @RequestParam(value = "product_name", required = false) String productName) {

        try {

            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);

            // Validate customer
            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customerId);
            if (customCustomer == null) throw new NotFoundException("Customer with the provided Id not found");

            if (!tokenUserId.equals(customerId)) {
                return ResponseService.generateErrorResponse("Forbidden Access", HttpStatus.FORBIDDEN);
            }

            if (customCustomer.getNumberOfOrders() == 0)
                return ResponseService.generateErrorResponse("Order History Empty - No Orders placed", HttpStatus.OK);

            if (offset < 0 || limit <= 0)
                return ResponseService.generateErrorResponse("Offset or Limit invalid", HttpStatus.BAD_REQUEST);

            if (orderStateIds != null && !orderStateIds.isEmpty()) {
                for (Integer id : orderStateIds) {
                    if (!isOrderStateIdValid(id)) {
                        return ResponseService.generateErrorResponse("Invalid Order State ID: " + id, HttpStatus.BAD_REQUEST);
                    }
                }
            }

            // Build base query with joins and dynamic conditions
            String baseQuery = "FROM blc_order o " + "JOIN order_state os ON o.order_id = os.order_id " + "WHERE o.customer_id = :customerId AND o.tax_override IS NULL";

            if (orderStateIds != null && !orderStateIds.isEmpty()) {
                baseQuery += " AND os.order_state_id IN (:orderStateIds)";
            }

            if (dateFrom != null && dateTo != null) {
                baseQuery += " AND CAST(o.submit_date AS DATE) BETWEEN :dateFrom AND :dateTo";
            } else if (dateFrom != null) {
                baseQuery += " AND CAST(o.submit_date AS DATE) >= :dateFrom";
            } else if (dateTo != null) {
                baseQuery += " AND CAST(o.submit_date AS DATE) <= :dateTo";
            }

            if (productName != null && !productName.trim().isEmpty()) {
                baseQuery += " AND LOWER(o.name) LIKE LOWER(:productName)";
            }

            // Count query
            String countQueryStr = "SELECT COUNT(*) " + baseQuery;
            Query countQuery = entityManager.createNativeQuery(countQueryStr);
            countQuery.setParameter("customerId", customerId);


            if (orderStateIds != null && !orderStateIds.isEmpty()) {
                countQuery.setParameter("orderStateIds", orderStateIds);
            }
            if (dateFrom != null) {
                countQuery.setParameter("dateFrom", new java.sql.Date(dateFrom.getTime()));
            }
            if (dateTo != null) {
                countQuery.setParameter("dateTo", new java.sql.Date(dateTo.getTime()));
            }
            if (productName != null && !productName.trim().isEmpty()) {
                countQuery.setParameter("productName", "%" + productName.trim() + "%");
            }

            BigInteger totalItems = (BigInteger) countQuery.getSingleResult();
            BigInteger totalPages = BigInteger.valueOf((int) Math.ceil((double) totalItems.intValue() / limit));

            if (offset >= totalPages.intValue() && offset != 0) {
                return ResponseService.generateErrorResponse("No Orders Available", HttpStatus.BAD_REQUEST);
            }

            // Data query
            String dataQueryStr = "SELECT o.order_id " + baseQuery;
            if ("DESC".equalsIgnoreCase(sortOrder)) {
                dataQueryStr += " ORDER BY o.date_updated DESC";
            } else {
                dataQueryStr += " ORDER BY o.date_updated ASC";
            }

            Query dataQuery = entityManager.createNativeQuery(dataQueryStr);
            dataQuery.setFirstResult(offset * limit);
            dataQuery.setMaxResults(limit);
            dataQuery.setParameter("customerId", customerId);

            if (orderStateIds != null && !orderStateIds.isEmpty()) {
                dataQuery.setParameter("orderStateIds", orderStateIds);
            }
            if (dateFrom != null) {
                dataQuery.setParameter("dateFrom", new java.sql.Date(dateFrom.getTime()));
            }
            if (dateTo != null) {
                dataQuery.setParameter("dateTo", new java.sql.Date(dateTo.getTime()));
            }
            if (productName != null && !productName.trim().isEmpty()) {
                dataQuery.setParameter("productName", "%" + productName.trim() + "%");
            }

            List<BigInteger> orders = dataQuery.getResultList();

            return generateCombinedDTO(authHeader, orders, sortOrder, totalItems.intValue(), totalPages.intValue(), offset);

        } catch (NotFoundException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error fetching order list", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Authorize(value = {Constant.roleAdmin, Constant.roleSuperAdmin})
    @Transactional
    @RequestMapping(value = "show-all-orders", method = RequestMethod.GET)
    public ResponseEntity<?> showDetails(@RequestHeader(value = "Authorization") String authHeader, @RequestParam(value = "order_state", required = false) List<Integer> orderStateIds, @RequestParam(defaultValue = "DESC") String sortOrder, @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "30") int limit, @RequestParam(value = "date_to", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateTo, @RequestParam(value = "date_from", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFrom, @RequestParam(value = "product_name", required = false) String productName) {
        try {
            if (offset < 0 || limit <= 0) {
                throw new IllegalArgumentException("Offset or Limit invalid");
            }

            int offset1 = offset * limit;

//            if (orderStateIds != null && orderStateIds.contains(9)) {
//                orderStateIds.clear();
//                orderStateIds.addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 0, 999));
//            }
            // Validate order state IDs
            if (orderStateIds != null && !orderStateIds.isEmpty()) {
                for (Integer id : orderStateIds) {
                    OrderStateRef ref = entityManager.find(OrderStateRef.class, id);
                    if (ref == null) {
                        return ResponseService.generateErrorResponse("Invalid Order State ID: " + id, HttpStatus.BAD_REQUEST);
                    }
                }
            }

            // Build dynamic base query
            String baseQuery = "FROM blc_order o JOIN order_state os ON o.order_id = os.order_id WHERE o.tax_override IS NULL";

            if (orderStateIds != null && !orderStateIds.isEmpty()) {
                baseQuery += " AND os.order_state_id IN (:orderStateIds)";
            }

            if (dateFrom != null && dateTo != null) {
                baseQuery += " AND CAST(o.submit_date AS DATE) BETWEEN :dateFrom AND :dateTo";
            } else if (dateFrom != null) {
                baseQuery += " AND CAST(o.submit_date AS DATE) >= :dateFrom";
            } else if (dateTo != null) {
                baseQuery += " AND CAST(o.submit_date AS DATE) <= :dateTo";
            }

            if (productName != null && !productName.trim().isEmpty()) {
                baseQuery += " AND LOWER(o.name) LIKE LOWER(:productName)";
            }

            //  Count Query
            String countQueryStr = "SELECT COUNT(*) " + baseQuery;
            Query countQuery = entityManager.createNativeQuery(countQueryStr);

            if (orderStateIds != null && !orderStateIds.isEmpty()) {
                countQuery.setParameter("orderStateIds", orderStateIds);
            }
            if (dateFrom != null) {
                countQuery.setParameter("dateFrom", new java.sql.Date(dateFrom.getTime()));
            }
            if (dateTo != null) {
                countQuery.setParameter("dateTo", new java.sql.Date(dateTo.getTime()));
            }
            if (productName != null && !productName.trim().isEmpty()) {
                countQuery.setParameter("productName", "%" + productName.trim() + "%");
            }

            BigInteger totalItems = (BigInteger) countQuery.getSingleResult();
            int totalPages = (int) Math.ceil(totalItems.doubleValue() / limit);

            if (offset >= totalPages && offset != 0) {
                throw new IllegalArgumentException("No more orders available");
            }

            // Data Query
            String dataQueryStr = "SELECT o.order_id " + baseQuery;
            dataQueryStr += "DESC".equalsIgnoreCase(sortOrder) ? " ORDER BY o.date_updated DESC" : " ORDER BY o.date_updated ASC";

            Query query = entityManager.createNativeQuery(dataQueryStr);
            query.setFirstResult(offset1);
            query.setMaxResults(limit);

            if (orderStateIds != null && !orderStateIds.isEmpty()) {
                query.setParameter("orderStateIds", orderStateIds);
            }
            if (dateFrom != null) {
                query.setParameter("dateFrom", new java.sql.Date(dateFrom.getTime()));
            }
            if (dateTo != null) {
                query.setParameter("dateTo", new java.sql.Date(dateTo.getTime()));
            }
            if (productName != null && !productName.trim().isEmpty()) {
                query.setParameter("productName", "%" + productName.trim() + "%");
            }

            @SuppressWarnings("unchecked") List<BigInteger> orderIds = query.getResultList();

            // Generate DTO and build response
            List<CombinedOrderDTO> orderDetails = generateCombinedDTOForOrders(authHeader, orderIds, sortOrder);

            Map<String, Object> response = new HashMap<>();
            response.put("orders", orderDetails);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", offset);

            return ResponseService.generateSuccessResponse("Orders are fetched", response, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error Fetching Order List", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @GetMapping("/details/{orderId}")
    public ResponseEntity<?> getOrderByOrderId(@PathVariable Long orderId, @RequestHeader(value = "Authorization") String authHeader) {
        try {
            if (orderId == null)
                return ResponseService.generateErrorResponse("Order id is required", HttpStatus.BAD_REQUEST);
            Order order = orderService.findOrderById(orderId);
            if (order == null) return ResponseService.generateErrorResponse("Order Not found", HttpStatus.NOT_FOUND);

            CustomOrderState orderState = entityManager.find(CustomOrderState.class, order.getId());
            Customer customer = customerService.readCustomerById(order.getCustomer().getId());
            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer.getId());

            OrderCustomerDetailsDTO customerDetailsDTO = new OrderCustomerDetailsDTO(customer.getId(), customer.getFirstName() + " " + customer.getLastName(), customer.getEmailAddress(), customCustomer != null ? customCustomer.getMobileNumber() : null, addressFetcher.fetch(customer), customer.getUsername());

            CustomServiceProviderTicket customServiceProviderTicket = getServiceProviderTicket(order.getId());

            CombinedOrderDTO dto = orderDTOService.wrapOrder(order, orderState, customServiceProviderTicket, customerDetailsDTO);
            if (dto != null) {
                return ResponseService.generateSuccessResponse("Order details", dto, HttpStatus.OK);
            }
            return null;
        } catch (Exception exception) {
            return ResponseService.generateErrorResponse("Some error occured", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public List<CombinedOrderDTO> generateCombinedDTOForOrders(String authHeader, List<BigInteger> orders, String sort) {
        String jwtToken = authHeader.substring(7);
        Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
        Role role = roleService.getRoleByRoleId(roleId);

        List<CombinedOrderDTO> orderDetails = new ArrayList<>();

        for (BigInteger orderId : orders) {
            try {

                Order order = orderService.findOrderById(orderId.longValue());
                if (order == null) {
                    continue;
                }

                CustomOrderState orderState = entityManager.find(CustomOrderState.class, order.getId());
                Customer customer = customerService.readCustomerById(order.getCustomer().getId());

                if (customer == null) {
                    continue;
                }

                CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer.getId());

                OrderCustomerDetailsDTO customerDetailsDTO = new OrderCustomerDetailsDTO(customer.getId(), customer.getFirstName() + " " + customer.getLastName(), customer.getEmailAddress(), customCustomer != null ? customCustomer.getMobileNumber() : null, addressFetcher.fetch(customer), customer.getUsername());

                CustomServiceProviderTicket customServiceProviderTicket = getServiceProviderTicket(order.getId());

                CombinedOrderDTO dto = orderDTOService.wrapOrder(order, orderState, customServiceProviderTicket, customerDetailsDTO);
                if (dto != null) {
                    orderDetails.add(dto);
                }

            } catch (Exception exception) {
                exceptionHandling.handleException(exception);
            }
        }

        return orderDetails;
    }

    private void logDebugQuery(String message, String queryStr) {
        try {
            BigInteger count = (BigInteger) entityManager.createNativeQuery(queryStr).getSingleResult();
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
        }
    }

    private CustomServiceProviderTicket getServiceProviderTicket(Long orderId) {
        try {
            Query query = entityManager.createNativeQuery(Constant.GET_PRIMARY_TICKET);
            query.setParameter("orderId", orderId);

            @SuppressWarnings("unchecked") List<BigInteger> ticketIds = query.getResultList();
            if (!ticketIds.isEmpty()) {
                return entityManager.find(CustomServiceProviderTicket.class, ticketIds.get(0).longValue());
            }
        } catch (NoResultException noResultException) {
            exceptionHandling.handleException(noResultException);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
        }
        return null;
    }

    @Transactional
    public ResponseEntity<?> generateCombinedDTO(String authHeader, List<BigInteger> orders, String sort, long totalItems, long totalPages, long offset) {
        String jwtToken = authHeader.substring(7);
        Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
        Role role = roleService.getRoleByRoleId(roleId);
        try {
            System.out.println("Order size is" + orders.size());
            Map<String, Object> orderMap = new HashMap<>();
            List<CombinedOrderDTO> orderDetails = new ArrayList<>();
            OrderDTO orderDTO = null;
            CustomServiceProviderTicket customServiceProviderTicket = null;
            for (BigInteger orderId : orders) {
                try {
                    System.out.println("start");
                    Order order = orderService.findOrderById(orderId.longValue());
                    if (order.getTaxOverride())//a way to archive old orders
                        continue;
                    CustomOrderState orderState = entityManager.find(CustomOrderState.class, order.getId());
                    Customer customer = customerService.readCustomerById(order.getCustomer().getId());
                    CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer.getId());
                    OrderCustomerDetailsDTO customerDetailsDTO = new OrderCustomerDetailsDTO(customer.getId(), customer.getFirstName() + " " + customer.getLastName(), customer.getEmailAddress(), customCustomer.getMobileNumber(), addressFetcher.fetch(customer), customer.getUsername());
                    try {
                        Query query = entityManager.createNativeQuery(Constant.GET_PRIMARY_TICKET);
                        query.setParameter("orderId", order.getId());
                        System.out.println("order-id" + order.getId());
                        Integer id = query.getFirstResult(); //@TODO-multiple enteries
                        // This will throw NoResultException if no result is found
                        customServiceProviderTicket = entityManager.find(CustomServiceProviderTicket.class, id.longValue());
                        System.out.println(customServiceProviderTicket);
                        orderDetails.add(orderDTOService.wrapOrderCustomer(order, orderState, customServiceProviderTicket, customerDetailsDTO));
                    } catch (NoResultException e) {
                        //the case where no result is found
                        customServiceProviderTicket = null;
                    }

                    System.out.println("end");
                } catch (NullPointerException e) {
                    exceptionHandling.handleException(e);
                    continue;
                }
            }
            Map<String, Object> response = new HashMap<>();
            response.put("orders", orderDetails);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", offset);
            return ResponseService.generateSuccessResponse("Orders", response, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error fetching orders ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "get-eligible-sp", method = RequestMethod.GET)
    public ResponseEntity<?> getEligibleSp(@RequestParam(required = false) Long ticketId) {
        try {
            CustomServiceProviderTicket ticket = entityManager.find(CustomServiceProviderTicket.class, ticketId);
            if (ticket == null) return ResponseService.generateErrorResponse("Ticket not found", HttpStatus.NOT_FOUND);
            List<Long> rejectedBy = ticket.getRejectedBy();
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<ServiceProviderEntity> cq = cb.createQuery(ServiceProviderEntity.class);
            Root<ServiceProviderEntity> root = cq.from(ServiceProviderEntity.class);
            Predicate condition = cb.equal(root.get("isArchived"), false);
            Predicate notRejected = rejectedBy.isEmpty() ? cb.conjunction() : cb.not(root.get("service_provider_id").in(rejectedBy));
            cq.where(condition, notRejected);
            TypedQuery<ServiceProviderEntity> query = entityManager.createQuery(cq);
            List<ServiceProviderEntity> serviceProviderEntities = query.getResultList();
            List<SPDto> result = new ArrayList<>();
            for (ServiceProviderEntity sp : serviceProviderEntities) {
                SPDto spDto = new SPDto();
                spDto.setName(sp.getFirst_name() + " " + sp.getLast_name());
                spDto.setSpId(sp.getService_provider_id());
                result.add(spDto);
            }
            return ResponseService.generateSuccessResponse("Available service providers", result, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    @Authorize(value = {Constant.roleSuperAdmin, Constant.roleAdmin})
    @RequestMapping(value = "reassign-ticket/{ticketId}", method = RequestMethod.POST)
    public ResponseEntity<?> reassignTicket(@PathVariable Long ticketId, @RequestParam(required = true) Long id, @RequestHeader(value = "Authorization") String authHeader) {
        try {
            CustomServiceProviderTicket ticket = entityManager.find(CustomServiceProviderTicket.class, ticketId);
            ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, id);
            if (serviceProvider == null)
                return ResponseService.generateErrorResponse("Service Provider not found", HttpStatus.BAD_REQUEST);
            if (serviceProvider.getRole() == 1)
                return ResponseService.generateErrorResponse("Cannot assign ticket to Super admin", HttpStatus.BAD_REQUEST);
            if (ticket.getRejectedBy().contains(id))
                return ResponseService.generateErrorResponse("Cannot assign : Ticket has been already returned by selected SP", HttpStatus.BAD_REQUEST);
            else ticket.setAssignee(id);
            entityManager.merge(ticket);
            return ResponseService.generateSuccessResponse("Ticket assigned", ticket, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Error assigning ticket", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @Authorize(value = {Constant.roleSuperAdmin, Constant.roleAdmin})
    @RequestMapping(value = "assign-order/{orderId}", method = RequestMethod.POST)
    public ResponseEntity<?> manuallyAssignOrder(@PathVariable Long orderId, @RequestBody CreateTicketDto createTicketDto, @RequestHeader(value = "Authorization") String authHeader) {
        try {

            List<String> deleteLogs = new ArrayList<>();

            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);

            Query query = entityManager.createNativeQuery(Constant.GET_PRIMARY_TICKET);
            query.setParameter("orderId", orderId);

            CustomTicketType ticketType = ticketTypeService.getTicketTypeByTicketTypeId(1L);
            CustomTicketState ticketState = ticketStateService.getTicketStateByTicketStateId(1L);
            CustomTicketStatus ticketStatus = ticketStatusService.getTicketStatusByTicketStatusId(0L);
            createTicketDto.setTicketType(1L);
            createTicketDto.setTicketState(1L);
            createTicketDto.setTicketStatus(0L);

            if (createTicketDto.getTicketType() == 1L) {
                if (!query.getResultList().isEmpty()) {
                    return ResponseService.generateErrorResponse("Primary ticket already exists", HttpStatus.BAD_REQUEST);
                }
            }

            Role role = null;
            if (createTicketDto.getAssigneeRole() != null) {
                role = roleService.getRoleByRoleId(createTicketDto.getAssigneeRole());
                if (role == null) {
                    return ResponseService.generateErrorResponse("Invalid role", HttpStatus.BAD_REQUEST);
                }
            }

            Order order = orderService.findOrderById(orderId);
            if (order == null) {
                return ResponseService.generateErrorResponse("Order with the provided id not found", HttpStatus.NOT_FOUND);
            }

            CustomOrderState customOrderState = entityManager.find(CustomOrderState.class, order.getId());
            if (!customOrderState.getOrderStateId().equals(Constant.ORDER_STATE_NEW.getOrderStateId())) {
                throw new IllegalArgumentException("Order can only be allowed to allocate at new state.");
            }

            if (createTicketDto.getTargetCompletionDate() != null) {
                if (sharedUtilityService.isInValidOrInPast(createTicketDto.getTargetCompletionDate()) == 1)
                    return ResponseService.generateErrorResponse("Target completion date cannot be in past", HttpStatus.BAD_REQUEST);

                Product product = findProductFromItemAttribute(order.getOrderItems().get(0));
                if (createTicketDto.getTargetCompletionDate().after(product.getActiveEndDate()) || createTicketDto.getTargetCompletionDate().before(product.getActiveStartDate()) || !createTicketDto.getTargetCompletionDate().after(new Date())) {
                    log.info(String.valueOf(createTicketDto.getTargetCompletionDate()));
                    log.info(String.valueOf(product.getActiveStartDate()));
                    log.info(String.valueOf(product.getActiveEndDate()));
                    log.info(String.valueOf(product.getId()));
                    return ResponseService.generateErrorResponse("Target completion date must be b/w product open date and close data and after current date.", HttpStatus.BAD_REQUEST);
                }
                log.info("hello");

            } else {
                return ResponseService.generateErrorResponse("Target Completion date is mandatory", HttpStatus.BAD_REQUEST);
            }

            ServiceProviderEntity serviceProvider = null;

            Long assignedUserId;
            Integer assignedRoleId;

            if (role.getRole_name().equals(Constant.roleServiceProvider)) {
                serviceProvider = entityManager.find(ServiceProviderEntity.class, createTicketDto.getAssignee());
                assignedUserId = serviceProvider.getService_provider_id();
                assignedRoleId = role.getRole_id();
                if (serviceProvider == null) {
                    return ResponseService.generateErrorResponse("Service Provider with the provided id not found", HttpStatus.NOT_FOUND);
                }
                if (serviceProvider.getApproved() == false || serviceProvider.getApproved() == null) {
                    return ResponseService.generateErrorResponse("Service Provider with the provided id is not Approved", HttpStatus.NOT_FOUND);
                }
            } else if (role.getRole_name().equals(Constant.roleAdmin) || role.getRole_name().equals(Constant.roleSuperAdmin)) {
                serviceProvider = entityManager.find(ServiceProviderEntity.class, createTicketDto.getAssignee());
                assignedUserId = serviceProvider.getService_provider_id();
                assignedRoleId = role.getRole_id();
                serviceProvider.setTicketAssigned(serviceProvider.getTicketAssigned() + 1);
                entityManager.merge(serviceProvider);

                if (serviceProvider == null)
                    return ResponseService.generateErrorResponse("Admin with the provided id not found", HttpStatus.NOT_FOUND);
            } else {
                return ResponseService.generateErrorResponse("Unknown role for ticket creation", HttpStatus.NOT_FOUND);
            }

            CustomServiceProviderTicket customServiceProviderTicket = serviceProviderTicketService.createTicket(createTicketDto, (OrderImpl) order, assignedUserId, assignedRoleId, roleId, tokenUserId);

            if(createTicketDto.getAssigneeRole() != null && createTicketDto.getAssigneeRole() != null) {
                serviceProviderActionController.sendTicketAllocationMail(serviceProvider, customServiceProviderTicket);
            }
            customOrderState.setOrderStateId(Constant.ORDER_STATE_ASSIGNED.getOrderStateId());
            entityManager.merge(customOrderState);

            Customer customer = customerService.readCustomerById(order.getCustomer().getId());
            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, order.getCustomer().getId());
            OrderCustomerDetailsDTO customerDetailsDTO = new OrderCustomerDetailsDTO(customer.getId(), customer.getFirstName() + " " + customer.getLastName(), customer.getEmailAddress(), customCustomer.getMobileNumber(), addressFetcher.fetch(customer), customer.getUsername());
            CombinedOrderDTO combinedOrderDTO = orderDTOService.wrapOrder(order, customOrderState, customServiceProviderTicket, customerDetailsDTO);

            CustomTicketWrapper wrapper = new CustomTicketWrapper();
            wrapper.customWrapDetails(customServiceProviderTicket, combinedOrderDTO, entityManager);

            return ResponseService.generateSuccessResponse("Order Assigned", wrapper, HttpStatus.OK);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandling.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @GetMapping("/{orderId}/availableSp")
    public ResponseEntity<?> getEligibleSp(@PathVariable Long orderId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "30") int limit) {
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
            if (result.isEmpty()) return ResponseService.generateErrorResponse("No Results Found", HttpStatus.OK);
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

    public Product findProductFromItemAttribute(OrderItem orderItem) {
        Long productId = Long.parseLong(orderItem.getOrderItemAttributes().get("productId").getValue());
        Product product = catalogService.findProductById(productId);
        return product;
    }

    @GetMapping("get-all-order-state")
    public ResponseEntity<?> findAllOrderState() {
        try {
            List<OrderStateRef> orderStates = orderStateRefService.getAllOrderState();
            if (orderStates == null || orderStates.isEmpty()) {
                return ResponseService.generateErrorResponse("No order states found.", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("Order States :", orderStates, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("An error occurred while fetching order states.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isOrderStateIdValid(Integer orderStateId) {
        String sql = "SELECT COUNT(*) FROM order_state_ref WHERE order_state_id = :id";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("id", orderStateId);
        BigInteger count = (BigInteger) query.getSingleResult();
        return count.intValue() > 0;
    }

    @GetMapping("get-all-grouped-order-state")
    public ResponseEntity<?> getGroupedOrderState() {
        List<OrderStateGroupDto> groupedStates = new ArrayList<>();

        groupedStates.add(new OrderStateGroupDto("New", Arrays.asList(1, 0, 3)));
        groupedStates.add(new OrderStateGroupDto("In Progress", Arrays.asList(2, 4, 6, 8)));
        groupedStates.add(new OrderStateGroupDto("Fulfilled", Collections.singletonList(7)));
        groupedStates.add(new OrderStateGroupDto("Canceled", Arrays.asList(999, 5, 9)));
        groupedStates.add(new OrderStateGroupDto("Refund", Arrays.asList(10, 11)));

        return ResponseService.generateSuccessResponse("Order States :", groupedStates, HttpStatus.OK);
    }
    /*@Authorize(value = {Constant.roleSuperAdmin, Constant.roleAdmin})
    @PutMapping("manage-refund")
    public ResponseEntity<?> manageRefunds() {

    }*/

    @Authorize(value = {Constant.roleUser})
    @PostMapping("/request-refund/{orderIdString}")
    public ResponseEntity<?>requestRefund(@PathVariable Long orderId,
                                          @RequestParam(value = "refund_amount", defaultValue = "0.0") Double refundAmount,
                                          @RequestHeader(value = "Authorization") String authHeader)
    {
        String jwtToken = authHeader.substring(7);
        Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
        Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
        Role role = roleService.getRoleByRoleId(roleId);
        if(orderId==null)
            return ResponseService.generateErrorResponse("Order id not provided",HttpStatus.BAD_REQUEST);
        Order order=orderService.findOrderById(orderId);
        if(!order.getCustomer().getId().equals(tokenUserId))
        {
            return ResponseService.generateErrorResponse("Forbidden",HttpStatus.FORBIDDEN);
        }
        OrderStateRef orderStateRef=entityManager.find(OrderStateRef.class,12);
        CustomOrderState orderState=entityManager.find(CustomOrderState.class,orderId);
        if(orderState.getOrderStateId()==12)
            return ResponseService.generateErrorResponse("Order already requested for cancellation",HttpStatus.BAD_REQUEST);
        else if(orderState.getOrderStateId()==9)
            return ResponseService.generateErrorResponse("Order already cancelled",HttpStatus.BAD_REQUEST);
        if(orderState.getOrderStateId()>1)
            return ResponseService.generateErrorResponse("Order cannot be cancelled as it has already been processed",HttpStatus.BAD_REQUEST);
        orderState.setOrderStateId(12);
        entityManager.merge(orderState);
        return ResponseService.generateErrorResponse("Order processed for cancellation",HttpStatus.OK);
    }

    @Authorize(value = {Constant.roleSuperAdmin, Constant.roleAdmin})
    @PutMapping("cancel-order/{orderIdString}")
    public ResponseEntity<?> cancelOrder(@PathVariable String orderIdString,
                                         @RequestParam(value = "refund_amount", defaultValue = "0.0") Double refundAmount,
                                         @RequestHeader(value = "Authorization") String authHeader) {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Role role = roleService.getRoleByRoleId(roleId);

            ServiceProviderEntity serviceProvider = serviceProviderService.getServiceProviderById(tokenUserId);
            if(serviceProvider == null) {
                throw new IllegalArgumentException("Token user id is not found.");
            }

            Long orderId = Long.parseLong(orderIdString);

            Order order = orderService.findOrderById(orderId);
            if (order == null) {
                throw new IllegalArgumentException("No order found with this id.");
            }

            List<CustomOrderState> orderStateList = customOrderService.getCustomOrderStateByOrderId(orderId);
            if (orderStateList.isEmpty()) {
                throw new IllegalArgumentException("No order state found with this order.");
            } else if (orderStateList.size() > 1) {
                throw new IllegalArgumentException("Multiple order state found with this order.");
            }

            CustomOrderState customOrderState = orderStateList.get(0);
            if(customOrderState.getOrderStateId().equals(Constant.ORDER_STATE_CANCELLED.getOrderStateId())) {
                throw new IllegalArgumentException("Order state is already been cancelled.");
            }

            if(customOrderState.getOrderStateId().equals(Constant.ORDER_STATE_REFUND_SUCCESS.getOrderStateId()) || customOrderState.getOrderStateId().equals(Constant.ORDER_STATE_REFUND_FAIL.getOrderStateId())) {
                throw new IllegalArgumentException("Order cannot be cancelled when refund is failed or success");
            } else if(customOrderState.getOrderStateId().equals(Constant.ORDER_STATE_COMPLETED.getOrderStateId()) ) {
                throw new IllegalArgumentException("Order cannot be cancelled when order is completed.");
            }

            OrderItem orderItem = order.getOrderItems().get(0);

            // Here we need to add a max amount as well depending on what customer paid.
            if(refundAmount < 0.0 || refundAmount > order.getTotal().doubleValue()) {
                throw new IllegalArgumentException("Refund Amount cannot be < 0 and greater than actual amount of the product.");
            }

            // Updating archived ticket logic.
            CustomServiceProviderTicket ticket = serviceProviderTicketService.fetchTicketByOrderId(orderId);
            if(ticket != null) {
                serviceProviderTicketService.deleteTicket(ticket, serviceProvider);
            }
            customOrderService.updateOrderState(customOrderState, Constant.ORDER_STATE_CANCELLED, refundAmount, tokenUserId, role);
            Refunds refunds=new Refunds();
            refunds.setOrderId(orderId);
            refunds.setRefundAmount(refundAmount);
            refunds.setGeneratedAt(new Date());
            refunds.setModifiedAt(new Date());
            try
            {
                RazorpayDetails razorpayDetails=entityManager.find(RazorpayDetails.class,orderId);
                refunds.setPaymentId(razorpayDetails.getRazorpayPaymentId());
            }
            catch (Exception e)
            {
                return ResponseService.generateErrorResponse("Error processing refund",HttpStatus.INTERNAL_SERVER_ERROR);
            }
            entityManager.persist(refunds);
            return ResponseService.generateSuccessResponse("Updated order", getOrderByOrderId(orderId, authHeader).getBody(), HttpStatus.OK);

        } catch (NumberFormatException numberFormatException) {
            exceptionHandling.handleException(numberFormatException);
            return ResponseService.generateErrorResponse("Number format exception: " + numberFormatException.getMessage(), HttpStatus.BAD_REQUEST);
        }  catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandling.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse("Illegal Argument Exception: " + illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Something Went Wrong: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}