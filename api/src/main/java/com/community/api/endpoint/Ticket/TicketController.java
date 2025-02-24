package com.community.api.endpoint.Ticket;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.CreateTicketDto;
import com.community.api.dto.CustomTicketWrapper;
import com.community.api.entity.CombinedOrderDTO;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomOrderState;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.OrderCustomerDetailsDTO;
import com.community.api.entity.Role;
import com.community.api.services.CustomerAddressFetcher;
import com.community.api.services.OrderDTOService;
import com.community.api.services.ProductService;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.ServiceProviderTicketService;
import com.community.api.services.TicketStateService;
import com.community.api.services.TicketStatusService;
import com.community.api.services.TicketTypeService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.validation.constraints.Null;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping(value = "/ticket-custom", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
public class TicketController {

    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

    @Autowired
    ServiceProviderTicketService serviceProviderTicketService;

    @Autowired
    TicketStateService ticketStateService;

    @Autowired
    TicketStatusService ticketStatusService;

    @Autowired
    TicketTypeService ticketTypeService;

    @Autowired
    ProductService productService;

    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    @Autowired
    JwtUtil jwtTokenUtil;

    @Autowired
    RoleService roleService;

    @Autowired
    CustomerService customerService;

    @Autowired
    OrderDTOService orderDTOService;

    @Autowired
    CustomerAddressFetcher addressFetcher;

    @PersistenceContext
    EntityManager entityManager;

    @Transactional
    @PostMapping("/auto-assigner")
    public ResponseEntity<?> autoAssigner() {
        try{
           List<Long>resultList=serviceProviderTicketService.getAssignedTickets();
           List<CombinedOrderDTO>orderDTO=new ArrayList<>();
            for(Long id :resultList)
            {
                CustomServiceProviderTicket ticket=entityManager.find(CustomServiceProviderTicket.class,id);
                CustomOrderState orderState = entityManager.find(CustomOrderState.class, ticket.getOrder().getId());
                Customer customer = customerService.readCustomerById(ticket.getOrder().getCustomer().getId());
                CustomCustomer customCustomer = entityManager.find(CustomCustomer.class,customer.getId());
                OrderCustomerDetailsDTO customerDetailsDTO=new OrderCustomerDetailsDTO(customer.getId(),customer.getFirstName()+" "+customer.getLastName(),customer.getEmailAddress(),customCustomer.getMobileNumber(),addressFetcher.fetch(customer),customer.getUsername());
                CombinedOrderDTO orderDto = orderDTOService.wrapOrder(ticket.getOrder(), orderState,ticket, customerDetailsDTO);
               CombinedOrderDTO combinedOrderDTO= orderDTOService.wrapOrder(ticket.getOrder(),orderState,ticket,customerDetailsDTO);
               orderDTO.add(combinedOrderDTO);
            }
            return ResponseService.generateSuccessResponse("Orders assigned by auto-assigner", orderDTO, HttpStatus.OK);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse("Illegal Argument Exception Caught: " + illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException runtimeException) {
            exceptionHandlingService.handleException(runtimeException);
            return ResponseService.generateErrorResponse("Runtime Exception Caught: " + runtimeException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @GetMapping("/get-all-tickets")
    public ResponseEntity<?> retrieveTickets() {
        try{
            return ResponseService.generateSuccessResponse("Tickets Found", serviceProviderTicketService.getAllTickets(), HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @GetMapping("/get-ticket-by-ticket-id/{ticketId}")
    public ResponseEntity<?> retrieveTickets(@PathVariable(name = "ticketId") Long ticketId) {
        try {

            CustomServiceProviderTicket ticket = serviceProviderTicketService.fetchTicketByTicketId(ticketId);
            if (ticket == null) {
                return ResponseService.generateErrorResponse("NO TICKETS FOUND WITH THE GIVEN CRITERIA", HttpStatus.NOT_FOUND);
            }

            CustomTicketWrapper wrapper = new CustomTicketWrapper();

            CustomOrderState orderState = entityManager.find(CustomOrderState.class, ticket.getOrder().getId());
            Customer customer = customerService.readCustomerById(ticket.getOrder().getCustomer().getId());
            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class,customer.getId());
            OrderCustomerDetailsDTO customerDetailsDTO=new OrderCustomerDetailsDTO(customer.getId(),customer.getFirstName()+" "+customer.getLastName(),customer.getEmailAddress(),customCustomer.getMobileNumber(),addressFetcher.fetch(customer),customer.getUsername());
            CombinedOrderDTO orderDto = orderDTOService.wrapOrder(ticket.getOrder(), orderState,ticket, customerDetailsDTO);

            wrapper.customWrapDetails(ticket, orderDto);

            return ResponseService.generateSuccessResponse("Tickets Found", wrapper, HttpStatus.OK);

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @GetMapping("/filter-tickets")
    public ResponseEntity<?> getFilterTickets(
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestParam(value = "created_date_from", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date dateFrom,
            @RequestParam(value = "created_date_to", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date dateTo,
            @RequestParam(value = "ticket_state", required = false) List<Long> state,
            @RequestParam(value = "ticket_type", required = false) List<Long> type,
            @RequestParam(value = "ticket_status", required = false) Long status,
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "10") int limit)
    {
        try {
            if(offset<0)
            {
                throw new IllegalArgumentException("Offset for pagination cannot be a negative number");
            }
            if(limit<=0)
            {
                throw new IllegalArgumentException("Limit for pagination cannot be a negative number or 0");
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (dateFrom != null) {
                String formattedDateFrom = dateFormat.format(dateFrom);
                dateFrom = dateFormat.parse(formattedDateFrom);
                if (dateTo == null) {
                    dateTo = dateFrom;
                }
            }
            if (dateTo != null) {
                String formattedDateTo = dateFormat.format(dateTo);
                dateTo = dateFormat.parse(formattedDateTo);
                if (dateFrom == null) {
                    dateFrom = dateTo;
                }
            }
            if (dateFrom != null && dateTo != null && dateTo.before(dateFrom)) {
                throw new IllegalArgumentException("createdDateFrom must be before createdDateTo");
            }

            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Role role = roleService.getRoleByRoleId(roleId);
            Long userId = null;

            if (role.getRole_name().equals(Constant.SERVICE_PROVIDER)) {
                userId = jwtTokenUtil.extractId(jwtToken);
            }

            List<CustomServiceProviderTicket> tickets = serviceProviderTicketService.filterTicket(
                    state, type, userId, role, dateFrom, dateTo, status);

            int totalItems = tickets.size();
            int totalPages = (int) Math.ceil((double) totalItems / limit);

            if (offset < 0) {
                offset = 0;
            }
            if (offset >= totalPages && offset!=0) {
                throw new IllegalArgumentException("No more tickets available");
            }

            int fromIndex = offset * limit;
            int toIndex = Math.min(fromIndex + limit, totalItems);

            List<CustomServiceProviderTicket> paginatedTickets = (totalItems > 0) ? tickets.subList(fromIndex, toIndex) : new ArrayList<>();

            List<CustomTicketWrapper> responses = paginatedTickets.stream().map(ticket -> {
                CustomTicketWrapper wrapper = new CustomTicketWrapper();
                CustomOrderState orderState = entityManager.find(CustomOrderState.class, ticket.getOrder().getId());
                Customer customer = customerService.readCustomerById(ticket.getOrder().getCustomer().getId());
                CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer.getId());
                OrderCustomerDetailsDTO customerDetailsDTO = new OrderCustomerDetailsDTO(
                        customer.getId(),
                        customer.getFirstName() + " " + customer.getLastName(),
                        customer.getEmailAddress(),
                        customCustomer.getMobileNumber(),
                        addressFetcher.fetch(customer),
                        customer.getUsername());

                CombinedOrderDTO orderDto = orderDTOService.wrapOrder(ticket.getOrder(), orderState, ticket, customerDetailsDTO);
                wrapper.customWrapDetails(ticket, orderDto);
                return wrapper;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("tickets", responses);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", offset);

            logger.info("Total tickets: " + responses.size());
            return ResponseService.generateSuccessResponse("Tickets Found successfully",response,HttpStatus.OK);

        }
        catch (IllegalArgumentException exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse( exception.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    @PutMapping("/ticket/update/{ticketId}")
    @Authorize(value = {Constant.roleServiceProvider,Constant.roleAdmin,Constant.roleSuperAdmin})
    public ResponseEntity<?>updateTicketStateAndStatus(@RequestBody CreateTicketDto createTicketDto, @PathVariable Long ticketId, @RequestHeader(value = "authorization")String authHeader)
    {
        try{
            return ticketStateService.updateTicket(createTicketDto,ticketId,authHeader);
        }
        catch (Exception e)
        {
            exceptionHandlingService.handleException(e);
            return ResponseService.generateErrorResponse("Error updating ticket state :"+e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*@Transactional
    @PostMapping("/add")
    public ResponseEntity<?> createTicket(@RequestBody CreateTicketDto createTicketDto, @RequestHeader(value = "Authorization") String authHeader) {

        try {
            CustomServiceProviderTicket customServiceProviderTicket = new CustomServiceProviderTicket();

            if (createTicketDto.getTicketState() == null || createTicketDto.getTicketState() <= 0) {
                ResponseService.generateErrorResponse("TICKET STATE CANNOT BE NULL OR <= 0", HttpStatus.NOT_FOUND);
            }
            CustomTicketState ticketState = ticketStateService.getTicketStateByTicketId(createTicketDto.getTicketState());
            if (ticketState == null) {
                ResponseService.generateErrorResponse("TICKET STATE NOT FOUND WITH THIS ID", HttpStatus.NOT_FOUND);
            }
            customServiceProviderTicket.setTicketState(ticketState);

            if (createTicketDto.getTicketType() != null) {
                if (createTicketDto.getTicketType() <= 0) {
                    ResponseService.generateErrorResponse("TICKET TYPE CANNOT BE <= 0", HttpStatus.NOT_FOUND);
                }
            }
            CustomTicketType ticketType = ticketTypeService.getTicketTypeByTicketTypeId(createTicketDto.getTicketType());
            if (ticketType == null) {
                ResponseService.generateErrorResponse("TICKET STATE NOT FOUND WITH THIS ID", HttpStatus.NOT_FOUND);
            }
            customServiceProviderTicket.setTicketType(ticketType);

            if (createTicketDto.getTicketStatus() != null) {
                if (createTicketDto.getTicketStatus() <= 0) {
                    ResponseService.generateErrorResponse("TICKET STATUS CANNOT BE <= 0", HttpStatus.NOT_FOUND);
                }
                CustomTicketStatus ticketStatus = ticketStatusService.getTicketStatusByTicketStatusId(createTicketDto.getTicketStatus());
                if (ticketStatus == null) {
                    ResponseService.generateErrorResponse("TICKET STATUS NOT FOUND WITH THIS ID", HttpStatus.NOT_FOUND);
                }
                customServiceProviderTicket.setTicketStatus(ticketStatus);
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Set active start date to current date and time in "yyyy-MM-dd HH:mm:ss" format
            String formattedDate = dateFormat.format(new Date());
            Date createdDate = dateFormat.parse(formattedDate);

            if(createTicketDto.getTargetCompletionDate()!= null) {
                dateFormat.parse(dateFormat.format(createTicketDto.getTargetCompletionDate()));
                if(!createTicketDto.getTargetCompletionDate().after(createdDate)) {
                    ResponseService.generateErrorResponse("TARGET COMPLETION DATE MUST BE OF FUTURE", HttpStatus.NOT_FOUND);
                }
            }else{
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(createdDate);
                calendar.add(Calendar.HOUR_OF_DAY, 4);
                Date newTargetDate = calendar.getTime();

                createTicketDto.setTargetCompletionDate(newTargetDate);
            }

            customServiceProviderTicket.setTargetCompletionDate(createTicketDto.getTargetCompletionDate());
            customServiceProviderTicket.setCreatedDate(createdDate);

            Long creatorUserId = productService.getUserIdByToken(authHeader);
            customServiceProviderTicket.setUserId(creatorUserId);

            customServiceProviderTicket = entityManager.merge(customServiceProviderTicket);
            return ResponseService.generateSuccessResponse("TICKET CREATED SUCCESSFULLY", customServiceProviderTicket,HttpStatus.OK);

        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }*/
}
