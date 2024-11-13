package com.community.api.endpoint.Ticket;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.CreateTicketDto;
import com.community.api.dto.CustomProductWrapper;
import com.community.api.dto.CustomTicketWrapper;
import com.community.api.entity.CombinedOrderDTO;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomOrderState;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.CustomTicketState;
import com.community.api.entity.CustomTicketStatus;
import com.community.api.entity.CustomTicketType;
import com.community.api.entity.OrderCustomerDetailsDTO;
import com.community.api.entity.Privileges;
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
import jsinterop.annotations.JsOverlay;
import org.broadleafcommerce.common.persistence.Status;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.broadleafcommerce.core.catalog.domain.ProductOptionValueAdminPresentation.FieldOrder.order;

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
            serviceProviderTicketService.autoAssigner();
            return ResponseService.generateSuccessResponse("DONE TILL HERE",null, HttpStatus.OK);
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
            @RequestParam(value = "ticket_type", required = false) List<Long> type)
    {
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Set active start date to current date and time in "yyyy-MM-dd HH:mm:ss" format
            if(dateFrom != null) {
                String formattedDateFrom = dateFormat.format(dateFrom);
                dateFrom = dateFormat.parse(formattedDateFrom);
                if(dateTo == null) {
                    dateTo = dateFrom;
                }
            }
            if(dateTo != null) {
                String formattedDateTo = dateFormat.format(dateTo);
                dateTo = dateFormat.parse(formattedDateTo);
                if(dateFrom == null) {
                    dateFrom = dateTo;
                }
            }

            if(dateFrom != null && dateTo != null && dateTo.before(dateFrom)) {
                throw new IllegalArgumentException("createdDateFrom must be before createdDateTo");
            }

            String jwtToken = authHeader.substring(7);

            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Role role = roleService.getRoleByRoleId(roleId);

            Long userId = null;

            if (role.getRole_name().equals(Constant.SERVICE_PROVIDER)) {
                userId = jwtTokenUtil.extractId(jwtToken);
            }

            List<CustomServiceProviderTicket> tickets = serviceProviderTicketService.filterTicket(state, type, userId, role, dateFrom, dateTo);
            if (tickets.isEmpty()) {
                return ResponseService.generateErrorResponse("NO TICKETS FOUND WITH THE GIVEN CRITERIA", HttpStatus.NOT_FOUND);
            }

            List<CustomTicketWrapper> responses = new ArrayList<>();
            for (CustomServiceProviderTicket ticket : tickets) {

                if (ticket != null) {
                    CustomTicketWrapper wrapper = new CustomTicketWrapper();

                    CustomOrderState orderState = entityManager.find(CustomOrderState.class, ticket.getOrder().getId());
                    Customer customer = customerService.readCustomerById(ticket.getOrder().getCustomer().getId());
                    CustomCustomer customCustomer = entityManager.find(CustomCustomer.class,customer.getId());
                    OrderCustomerDetailsDTO customerDetailsDTO=new OrderCustomerDetailsDTO(customer.getId(),customer.getFirstName()+" "+customer.getLastName(),customer.getEmailAddress(),customCustomer.getMobileNumber(),addressFetcher.fetch(customer),customer.getUsername());
                    CombinedOrderDTO orderDto = orderDTOService.wrapOrder(ticket.getOrder(), orderState,ticket, customerDetailsDTO);

                    wrapper.customWrapDetailsGetAll(ticket, orderDto);
                    responses.add(wrapper);
                }
            }

            logger.info("Total tickets: " + responses.size());
            return ResponseService.generateSuccessResponse("Tickets Found", responses, HttpStatus.OK);

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
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

    }
}
