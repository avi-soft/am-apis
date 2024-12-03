package com.community.api.endpoint.Ticket.TicketHistory;

import com.community.api.component.Constant;
import com.community.api.dto.CustomTicketHistoryWrapper;
import com.community.api.dto.CustomTicketWrapper;
import com.community.api.entity.CombinedOrderDTO;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomOrderState;
import com.community.api.entity.CustomTicketHistory;
import com.community.api.entity.OrderCustomerDetailsDTO;
import com.community.api.services.ResponseService;
import com.community.api.services.TicketHistoryService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/ticket-custom", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
public class TicketHistoryController {

    @Autowired
    TicketHistoryService ticketHistoryService;

    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    @Transactional
    @GetMapping("/get-ticketHistory-by-ticket-id/{ticketId}")
    public ResponseEntity<?> retrieveTicketHistory(@PathVariable(name = "ticketId") Long ticketId) {
        try {

            List<CustomTicketHistory> ticketHistory = ticketHistoryService.fetchTicketHistoryByTicketId(ticketId);
            List<CustomTicketHistoryWrapper> result = new ArrayList<>();

            for (int i=0; i<ticketHistory.size(); i++) {
                CustomTicketHistory customTicketHistory = ticketHistory.get(i);
                CustomTicketHistoryWrapper wrapper = new CustomTicketHistoryWrapper();
                wrapper.customWrapDetails(customTicketHistory);
                result.add(wrapper);
            }

            return ResponseService.generateSuccessResponse("Tickets History Found", result, HttpStatus.OK);

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
