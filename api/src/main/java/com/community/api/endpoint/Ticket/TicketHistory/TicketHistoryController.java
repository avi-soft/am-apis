package com.community.api.endpoint.Ticket.TicketHistory;

import com.community.api.component.Constant;
import com.community.api.dto.CustomTicketWrapper;
import com.community.api.entity.CombinedOrderDTO;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomOrderState;
import com.community.api.entity.CustomTicketHistory;
import com.community.api.entity.OrderCustomerDetailsDTO;
import com.community.api.services.ResponseService;
import com.community.api.services.TicketHistoryService;
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

@RestController
@RequestMapping(value = "/ticket-custom", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
public class TicketHistoryController {

    @Autowired
    TicketHistoryService ticketHistoryService;

    @Transactional
    @GetMapping("/get-ticketHistory-by-ticket-id/{ticketId}")
    public ResponseEntity<?> retrieveTicketHistory(@PathVariable(name = "ticketId") Long ticketId) {
        try {

            CustomTicketHistory ticketHistory = ticketHistoryService.fetchTicketHistoryByTicketId(ticketId);
            if (ticketHistory == null) {
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
}
