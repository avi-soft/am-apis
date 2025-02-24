package com.community.api.endpoint.avisoft.controller.product;

import com.community.api.component.Constant;
import com.community.api.entity.CustomProductRejectionStatus;
import com.community.api.entity.CustomTicketState;
import com.community.api.services.ProductRejectionStatusService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ProductRejectionStatusController {

    private final ExceptionHandlingService exceptionHandlingService;
    private final ProductRejectionStatusService productRejectionStatusService;

    @Autowired
    public ProductRejectionStatusController(ExceptionHandlingService exceptionHandlingService, ProductRejectionStatusService productRejectionStatusService) {
        this.exceptionHandlingService = exceptionHandlingService;
        this.productRejectionStatusService = productRejectionStatusService;
    }

    @GetMapping("/get-all-product-rejection-status")
    public ResponseEntity<?> getAllProductRejectionStatus(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            if(offset<0)
            {
                throw new IllegalArgumentException("Offset for pagination cannot be a negative number");
            }
            if(limit<=0)
            {
                throw new IllegalArgumentException("Limit for pagination cannot be a negative number or 0");
            }
            List<CustomProductRejectionStatus> allStatuses = productRejectionStatusService.getAllRejectionStatus();

            if (allStatuses.isEmpty()) {
                return ResponseService.generateErrorResponse("NO REJECTION STATUS IS FOUND", HttpStatus.NOT_FOUND);
            }

            // Calculate pagination details
            int totalItems = allStatuses.size();
            int totalPages = (int) Math.ceil((double) totalItems / limit);
            int fromIndex = offset * limit;
            int toIndex = Math.min(fromIndex + limit, totalItems);
            if (offset >= totalPages && offset != 0) {
                throw new IllegalArgumentException("No more rejection status available");
            }
            // Validate the requested offset
            if (fromIndex >= totalItems) {
                return ResponseService.generateErrorResponse("Page index out of range", HttpStatus.BAD_REQUEST);
            }

            List<CustomProductRejectionStatus> paginatedList = allStatuses.subList(fromIndex, toIndex);

            // Construct response
            Map<String, Object> response = new HashMap<>();
            response.put("rejectionStatuses", paginatedList);
            response.put("totalItems", totalItems);     
            response.put("totalPages", totalPages);
            response.put("currentPage", offset);

            return ResponseService.generateSuccessResponse("REJECTION STATUS IS FOUND",response, HttpStatus.OK);

        }
        catch (IllegalArgumentException exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse( exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
        catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/get-product-rejection-status-by-rejection-status-id/{rejectionStatusId}")
    public ResponseEntity<?> getProductRejectionStateByRejectionStatusId(@PathVariable Long rejectionStatusId) {
        try {
            CustomProductRejectionStatus customProductRejectionStatus = productRejectionStatusService.getAllRejectionStatusByRejectionStatusId(rejectionStatusId);
            if (customProductRejectionStatus == null) {
                return ResponseService.generateErrorResponse("NO REJECTION STATUS IS FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("REJECTION STATUS IF FOUND", customProductRejectionStatus, HttpStatus.OK);
        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + numberFormatException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
