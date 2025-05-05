package com.community.api.endpoint.Ticket.TicketState;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.dto.CreateTicketDto;
import com.community.api.endpoint.Ticket.TicketStatus.TicketStatusController;
import com.community.api.entity.CustomTicketState;
import com.community.api.entity.CustomTicketStatus;
import com.community.api.entity.ManualAssignmentDetails;
import com.community.api.services.ResponseService;
import com.community.api.services.TicketStateService;
import com.community.api.services.TicketStatusService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
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
    private EntityManager entityManager;

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
            CustomTicketState ticketState = ticketStateService.getTicketStateByTicketId(ticketStateId);
            if (ticketState == null) {
                return ResponseService.generateErrorResponse("NO TICKET STATE FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("TICKET STATE FOUND", ticketState, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/get-all-status/{ticketStateId}")
    public ResponseEntity<?>getAllStatusForAState(@PathVariable Long ticketStateId) {
        try {
            Query query = entityManager.createNativeQuery(Constant.GET_TICKET_STATUS_LINKED_WITH_TICKET_STATE);
            query.setParameter("ticketStateId", ticketStateId);
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
            return ResponseService.generateSuccessResponse("Status List : ",listOfStatuses,HttpStatus.OK);
        }catch (Exception e)
        {
            exceptionHandlingService.handleException(e);
            return ResponseService.generateErrorResponse("Some error occured while fetching : "+e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
