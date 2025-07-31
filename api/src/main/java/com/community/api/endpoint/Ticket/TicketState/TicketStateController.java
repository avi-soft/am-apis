package com.community.api.endpoint.Ticket.TicketState;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.CustomTicketState;
import com.community.api.entity.CustomTicketStatus;
import com.community.api.entity.Role;
import com.community.api.entity.ShortAccessToken;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.TicketStateService;
import com.community.api.services.TicketStatusService;
import com.community.api.services.TicketTypeService;
import com.community.api.services.exception.ExceptionHandlingService;
import com.community.api.utils.ServiceProviderDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class TicketStateController {

    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    @Autowired
    TicketStateService ticketStateService;

    @Autowired
    private TicketStatusService ticketStatusService;

    @Autowired
    private TicketTypeService ticketTypeService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    private RoleService roleService;

    @GetMapping("/get-all-ticket-states")
    public ResponseEntity<?> getAllTicketStates() {
        try {
            List<CustomTicketState> customTicketStateList = ticketStateService.getAllTicketState();
            if (customTicketStateList.isEmpty()) {
                return ResponseService.generateErrorResponse("NO TICKET STATE IS FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("TICKET STATES FOUND", customTicketStateList, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-ticket-state-by-ticket-state-id/{ticketStateId}")
    public ResponseEntity<?> getTicketStateByTicketStateId(@PathVariable Long ticketStateId) {
        try {
            CustomTicketState ticketState = ticketStateService.getTicketStateByTicketStateId(ticketStateId);
            if (ticketState == null) {
                return ResponseService.generateErrorResponse("NO TICKET STATE FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("TICKET STATE FOUND", ticketState, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-all-status")
    public ResponseEntity<?> getAllStatusForAState(@RequestParam("ticket_state_id") Long ticketStateId,
                                                   @RequestParam("ticket_type_id") Long ticketTypeId) {
        try {
            Query query = entityManager.createNativeQuery(Constant.GET_TICKET_STATUS_LINKED_WITH_TICKET_STATE);
            query.setParameter("ticketStateId", ticketStateId);
            query.setParameter("ticketTypeId", ticketTypeId);
            List<BigInteger> resultList = query.getResultList();
            // Convert BigInteger list to Long list
            List<Long> resultListLong = resultList.stream()
                    .map(BigInteger::longValue)  // Convert BigInteger to long
                    .collect(Collectors.toList());
            List<CustomTicketStatus> listOfStatuses = new ArrayList<>();
            for (Long statusId : resultListLong) {
                CustomTicketStatus customTicketStatus = ticketStatusService.getTicketStatusByTicketStatusId(statusId);
                listOfStatuses.add(customTicketStatus);
            }
            return ResponseService.generateSuccessResponse("Status List : ", listOfStatuses, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            return ResponseService.generateErrorResponse("Some error occurred while fetching : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-all-states")
    public ResponseEntity<?> getAllStatesForAState(@RequestParam("ticket_type_id") Long ticketTypeId,
                                                   @RequestParam("ticket_state_id_from") Long ticketStateIdFrom,
                                                   @RequestHeader(name = "Authorization") String authHeader) {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Role role = roleService.getRoleByRoleId(roleId);

            List<Long> roleIds = new ArrayList<>();
            if(role.getRole_name().equals(Constant.roleSuperAdmin)) {
                roleIds.add(1L);
                roleIds.add(2L);
                roleIds.add(4L);
            } else if(role.getRole_name().equals(Constant.roleAdmin)) {
                roleIds.add(2L);
                roleIds.add(4L);
            } else if(role.getRole_name().equals(Constant.SERVICE_PROVIDER)) {
                roleIds.add(4L);
            } else {
                return ResponseService.generateErrorResponse("Only SA, A, SP can use this API", HttpStatus.NOT_FOUND);
            }

            ticketTypeService.getTicketTypeByTicketTypeId(ticketTypeId); // checks whether the id is valid or not.
            ticketStateService.getTicketStateByTicketStateId(ticketStateIdFrom); // checks whether the id is valid or not.

            Query query = entityManager.createNativeQuery(Constant.GET_TICKET_STATE_LINKED_WITH_TICKET_STATE);
            query.setParameter("ticketStateIdFrom", ticketStateIdFrom);
            query.setParameter("roleIds", roleIds);
            query.setParameter("ticketTypeId", ticketTypeId);

            List<BigInteger> resultList = query.getResultList();

            // Convert BigInteger list to Long list
            List<Long> resultListLong = resultList.stream()
                    .map(BigInteger::longValue)  // Convert BigInteger to long
                    .collect(Collectors.toList());

            List<CustomTicketState> listOfStates = new ArrayList<>();
            for (Long stateId : resultListLong) {
                CustomTicketState customTicketState = ticketStateService.getTicketStateByTicketStateId(stateId);
                listOfStates.add(customTicketState);
            }
            return ResponseService.generateSuccessResponse("State List : ", listOfStates, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            return ResponseService.generateErrorResponse("Some error occurred while fetching : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @GetMapping("ticket/docs/preview-auth")
    public ResponseEntity<?> getTicketAuth(@RequestParam Long docId,
                                           @RequestHeader(name = "Authorization") String authHeader,
                                           HttpServletRequest request) {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            String ip = request.getRemoteAddr();

            ServiceProviderDocument document = entityManager.find(ServiceProviderDocument.class, docId);
            if (document == null) {
                return ResponseService.generateErrorResponse("Document not found", HttpStatus.NOT_FOUND);
            }
            Query query=entityManager.createNativeQuery("SELECT ticket_id from custom_service_provider_ticket where parent_ticket_id = :ticketId and assignee_user_id = :uid");
            query.setParameter("ticketId",document.getServiceProviderTicket().getTicketId());
            query.setParameter("uid",tokenUserId);
            System.out.println("parent ticket id is"+document.getServiceProviderTicket().getTicketId());
            System.out.println("uid is"+tokenUserId);
            Long assigneeId = document.getServiceProviderTicket().getAssignee();
            Long linkedTicketId =null;
            try {
                linkedTicketId = ((BigInteger) query.getSingleResult()).longValue();
            } catch (NoResultException e) {
                // No result found — linkedTicketId remains null
            }
            CustomServiceProviderTicket linkedTicket=null;
            if(linkedTicketId!=null) {
                linkedTicket = entityManager.find(CustomServiceProviderTicket.class, linkedTicketId);
            }
            if(linkedTicket!=null) {
                System.out.println(linkedTicket.getTicketId());
            }
            // Check if user is allowed
            boolean isAllowed =
                    roleId == 1 || // Super Admin
                            roleId == 2 || // Admin
                            tokenUserId.equals(assigneeId) || // Ticket assignee
                            (linkedTicket != null && linkedTicket.getParentTicket().equals(document.getServiceProviderTicket())); // Parent ticket reviewer

            if (!isAllowed) {
                return ResponseService.generateErrorResponse("Forbidden", HttpStatus.FORBIDDEN);
            }

            // Generate short-lived token
            String shortToken = jwtTokenUtil.generateShortLivedToken(
                    document.getServiceProviderEntity().getService_provider_id(),
                    document.getServiceProviderEntity().getRole(),
                    ip
            );

            ShortAccessToken shortAccessToken = new ShortAccessToken();
            shortAccessToken.setRole(document.getServiceProviderEntity().getRole());
            shortAccessToken.setUserId(document.getServiceProviderEntity().getService_provider_id());
            shortAccessToken.setToken(shortToken);
            shortAccessToken.setExpired(false);

            entityManager.persist(shortAccessToken);

            return ResponseService.generateSuccessResponse("Short lived token", shortToken, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseService.generateErrorResponse("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
