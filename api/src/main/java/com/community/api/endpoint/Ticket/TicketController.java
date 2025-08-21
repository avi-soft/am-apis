package com.community.api.endpoint.Ticket;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.CreateTicketDto;
import com.community.api.dto.CustomTicketHistoryWrapper;
import com.community.api.dto.CustomTicketWrapper;
import com.community.api.dto.TicketDocumentWrapper;
import com.community.api.dto.TicketStatisticsDto;
import com.community.api.endpoint.avisoft.controller.ServiceProviderActionController;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CombinedOrderDTO;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomOrderState;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.CustomTicketHistory;
import com.community.api.entity.CustomTicketState;
import com.community.api.entity.CustomTicketStatus;
import com.community.api.entity.CustomTicketType;
import com.community.api.entity.OrderCustomerDetailsDTO;
import com.community.api.entity.Role;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.CustomerAddressFetcher;
import com.community.api.services.DocumentStorageService;
import com.community.api.services.EmailQueueService;
import com.community.api.services.FileService;
import com.community.api.services.OrderDTOService;
import com.community.api.services.ProductService;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.ServiceProviderTicketService;
import com.community.api.services.TicketHistoryService;
import com.community.api.services.TicketStateService;
import com.community.api.services.TicketStatusService;
import com.community.api.services.TicketTypeService;
import com.community.api.services.exception.ExceptionHandlingService;
import com.community.api.utils.ServiceProviderDocument;
import com.mchange.rmi.NotAuthorizedException;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.nio.file.AccessDeniedException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "/ticket-custom", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
public class TicketController {

    @Autowired
    ServiceProviderTicketService serviceProviderTicketService;

    @Autowired
    TicketStateService ticketStateService;

    @Autowired
    ServiceProviderServiceImpl serviceProviderService;

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
    CustomCustomerService customCustomerService;

    @Autowired
    FileService fileService;

    @Autowired
    DocumentStorageService documentStorageService;

    @Autowired
    HttpServletRequest request;

    @Autowired
    TicketHistoryService ticketHistoryService;

    @Autowired
    OrderDTOService orderDTOService;

    @Autowired
    CustomerAddressFetcher addressFetcher;

    @Autowired
    EmailQueueService emailQueueService;

    @Autowired
    ServiceProviderActionController serviceProviderActionController;

    @PersistenceContext
    EntityManager entityManager;

    @Transactional
    @PostMapping("/auto-assigner")
    @Authorize(value = {Constant.roleAdmin, Constant.roleSuperAdmin})
    public ResponseEntity<?> autoAssigner() {
        try {
            /*List<Long> resultList = serviceProviderTicketService.getAssignedTickets();
            List<CombinedOrderDTO> orderDTO = new ArrayList<>();
            for (Long id : resultList) {
                CustomServiceProviderTicket ticket = entityManager.find(CustomServiceProviderTicket.class, id);
                CustomOrderState orderState = entityManager.find(CustomOrderState.class, ticket.getOrder().getId());
                Customer customer = customerService.readCustomerById(ticket.getOrder().getCustomer().getId());
                CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer.getId());
                OrderCustomerDetailsDTO customerDetailsDTO = new OrderCustomerDetailsDTO(customer.getId(), customer.getFirstName() + " " + customer.getLastName(), customer.getEmailAddress(), customCustomer.getMobileNumber(), addressFetcher.fetch(customer), customer.getUsername());
                CombinedOrderDTO orderDto = orderDTOService.wrapOrder(ticket.getOrder(), orderState, ticket, customerDetailsDTO);
                CombinedOrderDTO combinedOrderDTO = orderDTOService.wrapOrder(ticket.getOrder(), orderState, ticket, customerDetailsDTO);
                orderDTO.add(combinedOrderDTO);
            }*/

            List<CustomTicketWrapper> assignedTickets = serviceProviderTicketService.autoAssigner();

            return ResponseService.generateSuccessResponse("Orders and Tickets assigned by auto-assigner", assignedTickets, HttpStatus.OK);
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
    @GetMapping("/get-all-ticket-stats")
    @Authorize(value = {Constant.roleServiceProvider, Constant.roleAdmin, Constant.roleSuperAdmin})
    public ResponseEntity<?> retrieveAllTicketsStatistics() {
        try {

            List<TicketStatisticsDto> response = new ArrayList();
            List<Long> ticketTypes = new ArrayList<>();
            List<CustomServiceProviderTicket> tickets = new ArrayList<>();
            List<Long> rejectedState = new ArrayList<>();
            rejectedState.add(6L);

            // PRIMARY TICKET
            ticketTypes.add(1L);
            tickets = serviceProviderTicketService.filterTicket(null, ticketTypes, null, null, null, null, null, null, null, null, null);

            TicketStatisticsDto primaryTicketStats = new TicketStatisticsDto();
            CustomTicketType ticketType = ticketTypeService.getTicketTypeByTicketTypeId(1L);
            primaryTicketStats.setTicketType(ticketType);
            primaryTicketStats.setTotal(tickets.size());

            tickets = serviceProviderTicketService.filterTicket(rejectedState, ticketTypes, null, null, null, null, null, null, null, null, null);
            primaryTicketStats.setRejected(tickets.size());
            tickets = serviceProviderTicketService.filterTicket(null, ticketTypes, null, null, null, null, null, null, true, null, null);
            primaryTicketStats.setDueInThreeDays(tickets.size());

            response.add(primaryTicketStats);
            // REVIEW TICKET
            ticketTypes.set(0, 2L);
            tickets = serviceProviderTicketService.filterTicket(null, ticketTypes, null, null, null, null, null, null, null, null, null);

            TicketStatisticsDto reviewTicketStats = new TicketStatisticsDto();
            ticketType = ticketTypeService.getTicketTypeByTicketTypeId(2L);
            reviewTicketStats.setTicketType(ticketType);
            reviewTicketStats.setTotal(tickets.size());

            tickets = serviceProviderTicketService.filterTicket(rejectedState, ticketTypes, null, null, null, null, null, null, null, null, null);
            reviewTicketStats.setRejected(tickets.size());
            tickets = serviceProviderTicketService.filterTicket(null, ticketTypes, null, null, null, null, null, null, true, null, null);
            reviewTicketStats.setDueInThreeDays(tickets.size());
            response.add(reviewTicketStats);

            // MISCELLANEOUS TICKET
            ticketTypes.set(0, 3L);
            tickets = serviceProviderTicketService.filterTicket(null, ticketTypes, null, null, null, null, null, null, null, null, null);

            TicketStatisticsDto miscellaneousTicketStats = new TicketStatisticsDto();
            ticketType = ticketTypeService.getTicketTypeByTicketTypeId(3L);
            miscellaneousTicketStats.setTicketType(ticketType);
            miscellaneousTicketStats.setTotal(tickets.size());

            tickets = serviceProviderTicketService.filterTicket(rejectedState, ticketTypes, null, null, null, null, null, null, null, null, null);
            miscellaneousTicketStats.setRejected(tickets.size());
            tickets = serviceProviderTicketService.filterTicket(null, ticketTypes, null, null, null, null, null, null, true, null, null);
            miscellaneousTicketStats.setDueInThreeDays(tickets.size());
            response.add(miscellaneousTicketStats);

            return ResponseService.generateSuccessResponse("Tickets Found", response, HttpStatus.OK);

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @GetMapping("/get-all-tickets")
    public ResponseEntity<?> retrieveTickets() {
        try {
            return ResponseService.generateSuccessResponse("Tickets Found", serviceProviderTicketService.getAllTickets(), HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @GetMapping("/get-ticket-by-ticket-id/{ticketId}")
    @Authorize(value = {Constant.roleServiceProvider, Constant.roleAdmin, Constant.roleSuperAdmin})
    public ResponseEntity<?> retrieveTickets(@PathVariable(name = "ticketId") Long ticketId,
                                             @RequestHeader(value = "Authorization") String authHeader) {
        try {

            CustomServiceProviderTicket ticket = serviceProviderTicketService.fetchTicketByTicketId(ticketId);
            if (ticket == null) {
                return ResponseService.generateErrorResponse("NO TICKETS FOUND.", HttpStatus.NOT_FOUND);
            }

            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Role tokenRole = roleService.getRoleByRoleId(roleId);

            if (ticket.getArchived() && !tokenRole.getRole_name().equals(Constant.SUPER_ADMIN) && !tokenRole.getRole_name().equals(Constant.ADMIN)) {
                return ResponseService.generateErrorResponse("Forbidden Access.", HttpStatus.FORBIDDEN);
            }

            Set<TicketDocumentWrapper> ticketDocumentWrapperSet = new HashSet<>();
            for (ServiceProviderDocument document : ticket.getServiceProviderDocuments()) {
                TicketDocumentWrapper ticketDocumentWrapper = new TicketDocumentWrapper();
                String fileUrl = fileService.getFileUrl(documentStorageService.encrypt(document.getFilePath()), request);
                ticketDocumentWrapper.wrapDetails(document, fileUrl, request);

                ticketDocumentWrapperSet.add(ticketDocumentWrapper);
            }

            List<CustomTicketHistory> ticketHistoryList = ticketHistoryService.fetchTicketHistoryByTicketId(ticket.getTicketId());

            List<CustomTicketHistoryWrapper> customTicketHistoryWrapperList = new ArrayList<>();
            CustomTicketWrapper wrapper = new CustomTicketWrapper();
            for (CustomTicketHistory ticketHistory : ticketHistoryList) {
                CustomTicketHistoryWrapper customTicketHistoryWrapper = new CustomTicketHistoryWrapper();

                List<TicketDocumentWrapper> ticketHistoryDocumentWrapperList = new ArrayList<>();
                for (ServiceProviderDocument document : ticketHistory.getServiceProviderDocuments()) {
                    TicketDocumentWrapper ticketDocumentWrapper = new TicketDocumentWrapper();
                    String fileUrl = fileService.getFileUrl(documentStorageService.encrypt(document.getFilePath()), request);
                    ticketDocumentWrapper.wrapDetails(document, fileUrl, request);

                    ticketHistoryDocumentWrapperList.add(ticketDocumentWrapper);
                }

                customTicketHistoryWrapper.customWrapDetails(ticketHistory, ticketHistoryDocumentWrapperList, entityManager);
                customTicketHistoryWrapperList.add(customTicketHistoryWrapper);
            }

            if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET)) {
                CustomOrderState orderState = entityManager.find(CustomOrderState.class, ticket.getOrder().getId());
                Customer customer = customerService.readCustomerById(ticket.getOrder().getCustomer().getId());
                CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer.getId());
                OrderCustomerDetailsDTO customerDetailsDTO = new OrderCustomerDetailsDTO(customer.getId(), customer.getFirstName() + " " + customer.getLastName(), customer.getEmailAddress(), customCustomer.getMobileNumber(), addressFetcher.fetch(customer), customer.getUsername());
                CombinedOrderDTO orderDto = orderDTOService.wrapOrder(ticket.getOrder(), orderState, ticket, customerDetailsDTO);
                wrapper.customWrapDetails(ticket, orderDto, entityManager, customTicketHistoryWrapperList, ticketDocumentWrapperSet);
            } else if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_REVIEW_TICKET) && ticket.getParentTicket().getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET)) {
                CustomOrderState orderState = entityManager.find(CustomOrderState.class, ticket.getParentTicket().getOrder().getId());
                Customer customer = customerService.readCustomerById(ticket.getParentTicket().getOrder().getCustomer().getId());
                CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer.getId());
                OrderCustomerDetailsDTO customerDetailsDTO = new OrderCustomerDetailsDTO(customer.getId(), customer.getFirstName() + " " + customer.getLastName(), customer.getEmailAddress(), customCustomer.getMobileNumber(), addressFetcher.fetch(customer), customer.getUsername());
                CombinedOrderDTO orderDto = orderDTOService.wrapOrder(ticket.getParentTicket().getOrder(), orderState, ticket.getParentTicket(), customerDetailsDTO);
                wrapper.customWrapDetails(ticket, orderDto, entityManager, customTicketHistoryWrapperList, ticketDocumentWrapperSet);
            } else {
                wrapper.customWrapDetails(ticket, null, entityManager, customTicketHistoryWrapperList, ticketDocumentWrapperSet);
            }

            return ResponseService.generateSuccessResponse("Tickets Found", wrapper, HttpStatus.OK);

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @GetMapping("/filter-tickets")
    @Authorize(value = {Constant.roleServiceProvider, Constant.roleAdmin, Constant.roleSuperAdmin})
    public ResponseEntity<?> getFilterTickets(
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestParam(value = "created_date_from", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFrom,
            @RequestParam(value = "created_date_to", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateTo,
            @RequestParam(value = "ticket_state", required = false) List<Long> ticket_state,
            @RequestParam(value = "ticket_type", required = false) List<Long> ticket_type,
            @RequestParam(value = "ticket_status", required = false) List<Long> ticket_status,
            @RequestParam(value = "assignee_user_ids", required = false) List<Long> assigneeUserIds,
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "30") int limit,
            @RequestParam(value = "personal", required = false) Boolean personal,
            @RequestParam(value = "due_in_three_days", required = false) Boolean dueInThreeDays,
            @RequestParam(value = "overdue", required = false) Boolean overdue,
            @RequestParam(value = "archived", defaultValue = "false") Boolean archived,
            @RequestParam(value = "sort_order", required = false, defaultValue = "DESC") String sortOrder) {
        try {

            if (offset < 0) {
                throw new IllegalArgumentException("Offset for pagination cannot be a negative number");
            }
            if (limit <= 0) {
                throw new IllegalArgumentException("Limit for pagination cannot be a negative number or 0");
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (dateFrom != null) {
                String formattedDateFrom = dateFormat.format(dateFrom);
                dateFrom = dateFormat.parse(formattedDateFrom);
//                if (dateTo == null) {
//                    dateTo = new Date();
//                }
            }
            if (dateTo != null) {
                String formattedDateTo = dateFormat.format(dateTo);
                dateTo = dateFormat.parse(formattedDateTo);
//                if (dateFrom == null) {
//                    dateFrom = dateTo;
//                }
            }
            if (dateFrom != null && dateTo != null && dateTo.before(dateFrom)) {
                throw new IllegalArgumentException("createdDateFrom must be before createdDateTo");
            }

            if(dueInThreeDays!= null && overdue != null) {
                throw new IllegalArgumentException("Due in three days and overdue filter cannot be passed at once");
            }

            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Role role = roleService.getRoleByRoleId(roleId);
            Long userId = null;

            if (role.getRole_name().equals(Constant.SERVICE_PROVIDER)) {
                userId = jwtTokenUtil.extractId(jwtToken);
                if (archived) {
                    throw new IllegalArgumentException("Forbidden Access");
                }
                if(dueInThreeDays != null || overdue != null) {
                    throw new IllegalArgumentException("Forbidden Access");
                }
            } else {
                // by default showing list of all the tickets.
                if (personal != null && personal) {
                    userId = jwtTokenUtil.extractId(jwtToken);
                }
            }

            List<CustomServiceProviderTicket> tickets = serviceProviderTicketService.filterTicket(
                    ticket_state, ticket_type, userId, role, dateFrom, dateTo, ticket_status, assigneeUserIds, dueInThreeDays, archived, overdue);

            if ("ASC".equalsIgnoreCase(sortOrder)) {
                tickets.sort(
                        Comparator.comparing(
                                CustomServiceProviderTicket::getModifiedDate,
                                Comparator.nullsFirst(Comparator.naturalOrder())
                        )
                );
            } else {
                tickets.sort(
                        Comparator.comparing(
                                CustomServiceProviderTicket::getModifiedDate,
                                Comparator.nullsLast(Comparator.reverseOrder())
                        )
                );
            }

            int totalItems = tickets.size();
            int totalPages = (int) Math.ceil((double) totalItems / limit);

            if (offset < 0) {
                offset = 0;
            }
            if (offset >= totalPages && offset != 0) {
                throw new IllegalArgumentException("No more tickets available");
            }

            int fromIndex = offset * limit;
            int toIndex = Math.min(fromIndex + limit, totalItems);

            List<CustomServiceProviderTicket> paginatedTickets = (totalItems > 0) ? tickets.subList(fromIndex, toIndex) : new ArrayList<>();

            List<CustomTicketWrapper> responses = paginatedTickets.stream().map(ticket -> {
                CustomTicketWrapper wrapper = new CustomTicketWrapper();
                if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET)) {
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
                    wrapper.customWrapDetails(ticket, orderDto, entityManager);
                } else if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_REVIEW_TICKET) && ticket.getParentTicket().getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET)) {
                    CustomOrderState orderState = entityManager.find(CustomOrderState.class, ticket.getParentTicket().getOrder().getId());
                    Customer customer = customerService.readCustomerById(ticket.getParentTicket().getOrder().getCustomer().getId());
                    CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer.getId());
                    OrderCustomerDetailsDTO customerDetailsDTO = new OrderCustomerDetailsDTO(
                            customer.getId(),
                            customer.getFirstName() + " " + customer.getLastName(),
                            customer.getEmailAddress(),
                            customCustomer.getMobileNumber(),
                            addressFetcher.fetch(customer),
                            customer.getUsername());

                    CombinedOrderDTO orderDto = orderDTOService.wrapOrder(ticket.getParentTicket().getOrder(), orderState, ticket, customerDetailsDTO);
                    wrapper.customWrapDetails(ticket, orderDto, entityManager);
                } else if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_REVIEW_TICKET) && ticket.getParentTicket().getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_MISCELLANEOUS_TICKET)) {
                    wrapper.customWrapDetails(ticket, null, entityManager);
                } else {
                    wrapper.customWrapDetails(ticket, null, entityManager);
                }

                return wrapper;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("tickets", responses);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", offset);

            log.info("Total tickets: {}", responses.size());
            if (responses.isEmpty()) {
                return ResponseService.generateSuccessResponse("Ticket Not Found with provided constraints.", response, HttpStatus.OK);
            }
            return ResponseService.generateSuccessResponse("Tickets Found successfully", response, HttpStatus.OK);

        } catch (IllegalArgumentException exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/ticket/update/{ticketId}")
    @Authorize(value = {Constant.roleServiceProvider, Constant.roleAdmin, Constant.roleSuperAdmin})
    public ResponseEntity<?> updateTicket(@ModelAttribute CreateTicketDto createTicketDto, @PathVariable Long ticketId, @RequestParam(value = "files", required = false) List<MultipartFile> files, @RequestHeader(value = "authorization") String authHeader) {
        try {

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseService.generateErrorResponse("Authorization header is missing or invalid.", HttpStatus.UNAUTHORIZED);
            }

            CustomServiceProviderTicket ticket = ticketStateService.updateTicket(createTicketDto, files, ticketId, authHeader);
            if (ticket == null || ticket.getArchived()) {
                return ResponseService.generateErrorResponse("NO TICKETS FOUND.", HttpStatus.NOT_FOUND);
            }

            CustomTicketWrapper wrapper = new CustomTicketWrapper();

            if (ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET)) {

                CustomOrderState orderState = entityManager.find(CustomOrderState.class, ticket.getOrder().getId());
                Customer customer = customerService.readCustomerById(ticket.getOrder().getCustomer().getId());
                CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer.getId());
                OrderCustomerDetailsDTO customerDetailsDTO = new OrderCustomerDetailsDTO(customer.getId(), customer.getFirstName() + " " + customer.getLastName(), customer.getEmailAddress(), customCustomer.getMobileNumber(), addressFetcher.fetch(customer), customer.getUsername());
                CombinedOrderDTO orderDto = orderDTOService.wrapOrder(ticket.getOrder(), orderState, ticket, customerDetailsDTO);
                wrapper.customWrapDetails(ticket, orderDto, entityManager);
            } else {
                wrapper.customWrapDetails(ticket, null, entityManager);
            }

            return ResponseService.generateSuccessResponse("Tickets Updated successfully", wrapper, HttpStatus.OK);

        } catch (NotFoundException notAuthorizedException) {
            exceptionHandlingService.handleException(notAuthorizedException);
            return ResponseService.generateErrorResponse(notAuthorizedException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (NotAuthorizedException notAuthorizedException) {
            exceptionHandlingService.handleException(notAuthorizedException);
            return ResponseService.generateErrorResponse(notAuthorizedException.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            return ResponseService.generateErrorResponse("Error updating ticket state :" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @PutMapping("/ticket/dummy-update/{ticketId}")
    @Authorize(value = {Constant.roleServiceProvider, Constant.roleAdmin, Constant.roleSuperAdmin})
    public ResponseEntity<?> closeTicket(@PathVariable Long ticketId, @RequestHeader(value = "authorization") String authHeader) {
        try {

            CustomServiceProviderTicket ticket = entityManager.find(CustomServiceProviderTicket.class, ticketId);
            if (ticket == null) {
                return ResponseService.generateErrorResponse("NO TICKETS FOUND WITH THE GIVEN CRITERIA", HttpStatus.NOT_FOUND);
            }

            ServiceProviderEntity assignee = entityManager.find(ServiceProviderEntity.class, ticket.getAssignee());
            HashMap<String, Integer> dataMap = new HashMap<>();
            dataMap.put("ticket_pending_before_update", assignee.getTicketPending());
            dataMap.put("ticket_completed_before_update", assignee.getTicketCompleted().intValue());

            CustomTicketState ticketState = ticketStateService.getTicketStateByTicketStateId(5L);
            ticket.setTicketState(ticketState);
            entityManager.merge(ticket);

            ServiceProviderEntity updatedAssignee = entityManager.find(ServiceProviderEntity.class, ticket.getAssignee());
//            dataMap.put("ticket_pending_after_update", updatedAssignee.getTicketPending());
//            dataMap.put("ticket_completed_after_update", updatedAssignee.getTicketCompleted().intValue());

            return ResponseService.generateSuccessResponse("Tickets Updated successfully", dataMap, HttpStatus.OK);

        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            return ResponseService.generateErrorResponse("Error updating ticket state :" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @PostMapping("/add")
    public ResponseEntity<?> createTicket(@ModelAttribute CreateTicketDto createTicketDto, @RequestParam(value = "files", required = false) List<MultipartFile> files, @RequestHeader(value = "Authorization") String authHeader) {

        try {

            log.info("dto is: {}", createTicketDto);
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long userId = jwtTokenUtil.extractId(jwtToken);

            Role role = roleService.getRoleByRoleId(roleId);
            if (!role.getRole_name().equals(Constant.roleSuperAdmin) && !role.getRole_name().equals(Constant.roleAdmin)) {
                return ResponseService.generateErrorResponse("Forbidden Access", HttpStatus.FORBIDDEN);
            }

            CustomServiceProviderTicket customServiceProviderTicket = new CustomServiceProviderTicket();

            CustomTicketState ticketState = null;
            if (createTicketDto.getTicketState() != null) {
                if (createTicketDto.getTicketState() <= 0) {
                    return ResponseService.generateErrorResponse("TICKET STATE CANNOT BE NULL OR <= 0", HttpStatus.NOT_FOUND);
                }
                ticketState = ticketStateService.getTicketStateByTicketStateId(createTicketDto.getTicketState());

                if (ticketState == null) {
                    return ResponseService.generateErrorResponse("TICKET STATE NOT FOUND WITH THIS ID", HttpStatus.NOT_FOUND);
                }
            } else {
                // Default state.
                ticketState = ticketStateService.getTicketStateByTicketStateId(1L);

                if (ticketState == null) {
                    return ResponseService.generateErrorResponse("TICKET STATE NOT FOUND WITH THIS ID", HttpStatus.NOT_FOUND);
                }
            }

            CustomTicketType ticketType = null;
            if (createTicketDto.getTicketType() != null) {

                if (createTicketDto.getTicketType() <= 0) {
                    return ResponseService.generateErrorResponse("TICKET TYPE CANNOT BE <= 0", HttpStatus.NOT_FOUND);
                }

                ticketType = ticketTypeService.getTicketTypeByTicketTypeId(createTicketDto.getTicketType());
                if (ticketType == null) {
                    return ResponseService.generateErrorResponse("TICKET STATE NOT FOUND WITH THIS ID", HttpStatus.NOT_FOUND);
                }
                if (!ticketType.getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_MISCELLANEOUS_TICKET)) {
                    return ResponseService.generateErrorResponse("Only Ticket Type Miscellaneous can be created w/o linkage of order or parent ticket", HttpStatus.BAD_REQUEST);
                }
                customServiceProviderTicket.setTicketType(ticketType);

                if (ticketType.getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_MISCELLANEOUS_TICKET)) {
                    if (createTicketDto.getIsReviewRequired() == null) {
                        return ResponseService.generateErrorResponse("Review required is mandatory for the Miscellaneous Ticket.", HttpStatus.BAD_REQUEST);
                    } else {
                        customServiceProviderTicket.setIsReviewRequired(createTicketDto.getIsReviewRequired());
                    }
                }

            } else {
                // Default.
                ticketType = ticketTypeService.getTicketTypeByTicketTypeId(3L);
                if (ticketType == null) {
                    return ResponseService.generateErrorResponse("TICKET STATE NOT FOUND WITH THIS ID", HttpStatus.NOT_FOUND);
                }
                customServiceProviderTicket.setTicketType(ticketType);

                if (ticketType.getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_MISCELLANEOUS_TICKET)) {
                    if (createTicketDto.getIsReviewRequired() == null) {
                        return ResponseService.generateErrorResponse("Review required is mandatory for the Miscellaneous Ticket.", HttpStatus.BAD_REQUEST);
                    } else {
                        customServiceProviderTicket.setIsReviewRequired(createTicketDto.getIsReviewRequired());
                    }
                }
            }

            CustomTicketStatus ticketStatus = null;
            if (createTicketDto.getTicketStatus() != null) {
                if (createTicketDto.getTicketStatus() <= 0) {
                    return ResponseService.generateErrorResponse("TICKET STATUS CANNOT BE <= 0", HttpStatus.NOT_FOUND);
                }
                ticketStatus = ticketStatusService.getTicketStatusByTicketStatusId(createTicketDto.getTicketStatus());
                if (ticketStatus == null) {
                    return ResponseService.generateErrorResponse("TICKET STATUS NOT FOUND WITH THIS ID", HttpStatus.NOT_FOUND);
                }

                ticketStatusService.verifyStatus(ticketState, ticketStatus, ticketType);
                customServiceProviderTicket.setTicketStatus(ticketStatus);
                customServiceProviderTicket.setTicketState(ticketState);
            } else {
                // Default.
                ticketStatus = ticketStatusService.getTicketStatusByTicketStatusId(0L);
                if (ticketStatus == null) {
                    return ResponseService.generateErrorResponse("TICKET STATUS NOT FOUND WITH THIS ID", HttpStatus.NOT_FOUND);
                }

                ticketStatusService.verifyStatus(ticketState, ticketStatus, ticketType);
                customServiceProviderTicket.setTicketStatus(ticketStatus);
                customServiceProviderTicket.setTicketState(ticketState);
            }

            // Validation of Dates.
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Set active start date to current date and time in "yyyy-MM-dd HH:mm:ss" format
            String formattedDate = dateFormat.format(new Date());
            Date createdDate = dateFormat.parse(formattedDate);

            customServiceProviderTicket.setCreatedDate(createdDate);
            customServiceProviderTicket.setModifiedDate(createdDate);
            if (createTicketDto.getAssignee() != null) {
                customServiceProviderTicket.setTicketAssignDate(createdDate);
            }

            // Entering Creator details.
            customServiceProviderTicket.setUserId(userId);
            customServiceProviderTicket.setCreatorRole(role);
            customServiceProviderTicket.setModifierId(userId);
            customServiceProviderTicket.setModifierRole(role);

            // validation of title and task
            if (createTicketDto.getTitle() == null || createTicketDto.getTitle().trim().isEmpty() || createTicketDto.getTask() == null || createTicketDto.getTask().trim().isEmpty()) {
                return ResponseService.generateErrorResponse("Title and task for miscellaneous ticket cannot be null or empty", HttpStatus.NOT_FOUND);
            }
            customServiceProviderTicket.setTitle(createTicketDto.getTitle().trim());
            customServiceProviderTicket.setDesc(createTicketDto.getTask().trim());


            ServiceProviderEntity assignee = null;

            // validation for assignee and assignee role and handling the auto-handling the bandwidth of individual.
            if (createTicketDto.getAssignee() != null && createTicketDto.getAssigneeRole() != null) {
                Role assigneeRole = roleService.getRoleByRoleId(createTicketDto.getAssigneeRole());

                // Validating target completion date.
                if (createTicketDto.getTargetCompletionDate() != null) {
                    dateFormat.parse(dateFormat.format(createTicketDto.getTargetCompletionDate()));
                    if (!createTicketDto.getTargetCompletionDate().after(createdDate)) {
                        return ResponseService.generateErrorResponse("TARGET COMPLETION DATE MUST BE OF FUTURE", HttpStatus.NOT_FOUND);
                    }
                } else {
                    return ResponseService.generateErrorResponse("TARGET COMPLETION DATE CANNOT BE NULL.", HttpStatus.NOT_FOUND);
                }
                customServiceProviderTicket.setTargetCompletionDate(createTicketDto.getTargetCompletionDate());

                if (assigneeRole == null) {
                    return ResponseService.generateErrorResponse("Assignee role Not Found with given role id", HttpStatus.NOT_FOUND);
                }

                if (assigneeRole.getRole_name().equals(Constant.roleSuperAdmin) && role.getRole_name().equals(Constant.roleSuperAdmin)) {
                    assignee = serviceProviderService.getServiceProviderById(createTicketDto.getAssignee());
                    if (assignee == null) {
                        return ResponseService.generateErrorResponse("No Assignee Found with given Id.", HttpStatus.NOT_FOUND);
                    }

                    customServiceProviderTicket.setAssignee(assignee.getService_provider_id());
                    customServiceProviderTicket.setAssigneeRole(assigneeRole);

                    assignee.setTicketAssigned(assignee.getTicketAssigned() + 1);
                    entityManager.merge(assignee);
                } else if (assigneeRole.getRole_name().equals(Constant.roleAdmin)) {
                    assignee = serviceProviderService.getServiceProviderById(createTicketDto.getAssignee());
                    if (assignee == null) {
                        return ResponseService.generateErrorResponse("No Assignee Found with given Id.", HttpStatus.NOT_FOUND);
                    }

                    customServiceProviderTicket.setAssignee(assignee.getService_provider_id());
                    customServiceProviderTicket.setAssigneeRole(assigneeRole);

                    assignee.setTicketAssigned(assignee.getTicketAssigned() + 1);
                    entityManager.merge(assignee);
                } else if (assigneeRole.getRole_name().equals(Constant.roleAdminServiceProvider)) {
                    assignee = serviceProviderService.getServiceProviderById(createTicketDto.getAssignee());
                    if (assignee == null) {
                        return ResponseService.generateErrorResponse("No Assignee Found with given Id.", HttpStatus.NOT_FOUND);
                    }

                    customServiceProviderTicket.setAssignee(assignee.getService_provider_id());
                    customServiceProviderTicket.setAssigneeRole(assigneeRole);

                    assignee.setTicketAssigned(assignee.getTicketAssigned() + 1);
                    entityManager.merge(assignee);
                } else if (assigneeRole.getRole_name().equals(Constant.roleServiceProvider)) {
                    assignee = serviceProviderService.getServiceProviderById(createTicketDto.getAssignee());
                    if (assignee == null) {
                        return ResponseService.generateErrorResponse("No Assignee Found with given Id.", HttpStatus.NOT_FOUND);
                    }

                    customServiceProviderTicket.setAssignee(assignee.getService_provider_id());
                    customServiceProviderTicket.setAssigneeRole(assigneeRole);

                    assignee.setTicketAssigned(assignee.getTicketAssigned() + 1);
                    entityManager.merge(assignee);
                } else {
                    return ResponseService.generateErrorResponse("Cannot Assignee Ticket to this Role", HttpStatus.NOT_FOUND);
                }

            } else if (createTicketDto.getAssigneeRole() != null || createTicketDto.getAssignee() != null) {
                return ResponseService.generateErrorResponse("Assignee and Assignee Role must be provided together", HttpStatus.NOT_FOUND);
            } else if (createTicketDto.getTargetCompletionDate() != null) {
                return ResponseService.generateErrorResponse("Target Completion Date must be provided with assignee and assignee role not alone.", HttpStatus.NOT_FOUND);
            }

            customServiceProviderTicket = entityManager.merge(customServiceProviderTicket);

            if(assignee != null) {
                serviceProviderActionController.sendTicketAllocationMail(assignee, customServiceProviderTicket);
            }

            if (files != null) {
                Set<ServiceProviderDocument> serviceProviderDocument = ticketStateService.updateTicketDocument(files, customServiceProviderTicket, userId, role);
                customServiceProviderTicket.setServiceProviderDocuments(serviceProviderDocument);
                entityManager.merge(customServiceProviderTicket);

                // get the latest entry from the ticket history table.
                CustomTicketHistory ticketHistory = ticketHistoryService.fetchTicketHistoryByTicketId(customServiceProviderTicket.getTicketId()).get(0);

                List<Long> previousTicketDocumentIds = new ArrayList<>();
                for (ServiceProviderDocument serviceProviderDocumentClone : customServiceProviderTicket.getServiceProviderDocuments()) {
                    previousTicketDocumentIds.add(serviceProviderDocumentClone.getDocumentId());
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

            }

            CustomTicketWrapper wrapper = new CustomTicketWrapper();
            wrapper.customWrapDetails(customServiceProviderTicket, null, entityManager);
            return ResponseService.generateSuccessResponse("TICKET CREATED SUCCESSFULLY", wrapper, HttpStatus.OK);

        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @DeleteMapping("/delete/{ticketId}")
    @Authorize(value = {Constant.roleAdmin, Constant.roleSuperAdmin})
    public ResponseEntity<?> deleteTicket(@PathVariable Long ticketId, @RequestHeader(value = "authorization") String authHeader) {
        try {

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseService.generateErrorResponse("Authorization header is missing or invalid.", HttpStatus.UNAUTHORIZED);
            }

            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Role tokenRole = roleService.getRoleByRoleId(roleId);

            if (!tokenRole.getRole_name().equals(Constant.ADMIN) && !tokenRole.getRole_name().equals(Constant.SUPER_ADMIN)) {
                throw new IllegalArgumentException("Forbidden Access");
            }

            ServiceProviderEntity serviceProvider = serviceProviderService.getServiceProviderById(tokenUserId);
            if (serviceProvider == null) {
                throw new IllegalArgumentException("No Admin or SuperAdmin found with this userId.");
            } else if (serviceProvider.getIsArchived()) {
                throw new IllegalArgumentException("Admin or SuperAdmin is suspended.");
            }

            if (ticketId == null) {
                throw new IllegalArgumentException("Ticket Id not provided");
            }
            CustomServiceProviderTicket ticket = entityManager.find(CustomServiceProviderTicket.class, ticketId);
            if (ticket == null) {
                throw new IllegalArgumentException("Ticket not Found.");
            }

            if (ticket.getArchived()) {
                throw new IllegalArgumentException("Ticket already Archived");
            }
            serviceProviderTicketService.deleteTicketLogic(ticket, serviceProvider);

            return ResponseService.generateSuccessResponse("TICKET ARCHIVED SUCCESSFULLY", ticket, HttpStatus.OK);

        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/ticket-allocation-mail")
    @Authorize(value = {Constant.roleAdmin, Constant.roleSuperAdmin})
    public ResponseEntity<?> ticketAllocationMail(@RequestHeader(value = "authorization") String authHeader) {
        try {

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseService.generateErrorResponse("Authorization header is missing or invalid.", HttpStatus.UNAUTHORIZED);
            }

            String jwtToken = authHeader.substring(7);
            Integer tokenRoleId = jwtTokenUtil.extractRoleId(jwtToken);
            Role tokenRole = roleService.getRoleByRoleId(tokenRoleId);

            if (!tokenRole.getRole_name().equals(Constant.ADMIN) && !tokenRole.getRole_name().equals(Constant.SUPER_ADMIN)) {
                throw new AccessDeniedException("Forbidden Access");
            }

            List<Map<String, Long>> response = ticketStateService.ticketAllocationMail();
            if(!response.isEmpty()) {
                return ResponseService.generateSuccessResponse("Mail Sent Successfully", response, HttpStatus.OK);
            } else {
                return ResponseService.generateSuccessResponse("No recipient to send Mail", response, HttpStatus.OK);
            }

        } catch (AccessDeniedException accessDeniedException) {
            exceptionHandlingService.handleException(accessDeniedException);
            return ResponseService.generateErrorResponse(accessDeniedException.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
