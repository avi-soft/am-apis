package com.community.api.endpoint.avisoft.controller;

import com.community.api.dto.EligibilityResult;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomProduct;
import com.community.api.services.CartService;
import com.community.api.services.CartService.EligibilityStatus;
import com.community.api.services.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/eligibility")
@CrossOrigin(origins = "*")
public class ProductEligibilityController {

    @Autowired
    private CartService cartService;

    @Autowired
    private EntityManager entityManager;

    /**
     * Check customer eligibility for a product with detailed response
     * @param customerId Customer ID
     * @param productId Product ID
     * @param includeAllReasons Whether to include all reasons for ineligibility (default: false)
     * @return Detailed eligibility result
     */
    @GetMapping("/check/{customerId}/{productId}")
    public ResponseEntity<?> checkEligibility(
            @PathVariable Long customerId,
            @PathVariable Long productId,
            @RequestParam(defaultValue = "false") boolean includeAllReasons) {
        
        try {
            // Fetch customer and product
            CustomCustomer customer = entityManager.find(CustomCustomer.class, customerId);
            CustomProduct product = entityManager.find(CustomProduct.class, productId);

            if (customer == null) {
                return ResponseService.generateErrorResponse("Customer not found",HttpStatus.BAD_REQUEST);
            }

            if (product == null) {
                return ResponseService.generateErrorResponse("Product not found",HttpStatus.BAD_REQUEST);
            }

            // Check eligibility
            EligibilityResult result = cartService.checkCustomerEligibilityDetailed(customer, product, includeAllReasons);

            // Create response
            Map<String, Object> response = new HashMap<>();
            response.put("customerId",customerId);
            response.put("productId",productId);

            switch (result.getStatus()) {
                case ELIGIBLE:
                    response.put("status","ELIGIBLE");
                    response.put("message","You are eligible for this product");
                    break;
                case ELIGIBLE_WITH_WARNINGS:
                    response.put("status","ELIGIBLE_WITH_WARNINGS");
                    response.put("message","You are eligible for this product, but please verify the following warnings");
                    response.put("warnings",result.getWarnings());
                    break;
                case NOT_ELIGIBLE:
                    response.put("status","NOT_ELIGIBLE");
                    response.put("message","You are not eligible for this product");
                    response.put("reasons",result.getReasons());
                    break;
            }

            return ResponseService.generateSuccessResponse("Eligibility is checked successfully",response,HttpStatus.OK);

        }
        catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            return ResponseService.generateErrorResponse(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    /**
//     * Quick eligibility check - returns only status without detailed reasons
//     * @param customerId Customer ID
//     * @param productId Product ID
//     * @return Simple eligibility status
//     */
//    @GetMapping("/quick-check/{customerId}/{productId}")
//    public ResponseEntity<Map<String, Object>> quickEligibilityCheck(
//            @PathVariable Long customerId,
//            @PathVariable Long productId) {
//
//        Map<String, Object> response = new HashMap<>();
//
//        try {
//            CustomCustomer customer = entityManager.find(CustomCustomer.class, customerId);
//            CustomProduct product = entityManager.find(CustomProduct.class, productId);
//
//            if (customer == null) {
//                response.put("status", "ERROR");
//                response.put("message", "Customer not found");
//                return ResponseEntity.badRequest().body(response);
//            }
//
//            if (product == null) {
//                response.put("status", "ERROR");
//                response.put("message", "Product not found");
//                return ResponseEntity.badRequest().body(response);
//            }
//
//            EligibilityResult result = cartService.checkCustomerEligibilityDetailed(customer, product, false);
//
//            response.put("customerId", customerId);
//            response.put("productId", productId);
//            response.put("eligible", result.getStatus() != EligibilityStatus.NOT_ELIGIBLE);
//            response.put("status", result.getStatus().toString());
//            response.put("hasWarnings", result.getStatus() == EligibilityStatus.ELIGIBLE_WITH_WARNINGS);
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            response.put("status", "ERROR");
//            response.put("message", "An error occurred while checking eligibility: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
//    }
//
//    /**
//     * Batch eligibility check for multiple products
//     * @param customerId Customer ID
//     * @param request Batch request with product IDs
//     * @return Batch eligibility results
//     */
//    @PostMapping("/batch-check/{customerId}")
//    public ResponseEntity<BatchEligibilityResponse> batchEligibilityCheck(
//            @PathVariable Long customerId,
//            @RequestBody BatchEligibilityRequest request) {
//
//        BatchEligibilityResponse response = new BatchEligibilityResponse();
//        response.setCustomerId(customerId);
//
//        try {
//            CustomCustomer customer = entityManager.find(CustomCustomer.class, customerId);
//            if (customer == null) {
//                response.setStatus("ERROR");
//                response.setMessage("Customer not found");
//                return ResponseEntity.badRequest().body(response);
//            }
//
//            for (Long productId : request.getProductIds()) {
//                CustomProduct product = entityManager.find(CustomProduct.class, productId);
//
//                if (product == null) {
//                    ProductEligibilityResult productResult = new ProductEligibilityResult();
//                    productResult.setProductId(productId);
//                    productResult.setStatus("ERROR");
//                    productResult.setMessage("Product not found");
//                    response.getResults().add(productResult);
//                    continue;
//                }
//
//                EligibilityResult result = cartService.checkCustomerEligibilityDetailed(customer, product, request.isIncludeAllReasons());
//
//                ProductEligibilityResult productResult = new ProductEligibilityResult();
//                productResult.setProductId(productId);
//                productResult.setStatus(result.getStatus().toString());
//
//                switch (result.getStatus()) {
//                    case ELIGIBLE:
//                        productResult.setMessage("Eligible");
//                        break;
//                    case ELIGIBLE_WITH_WARNINGS:
//                        productResult.setMessage("Eligible with warnings");
//                        productResult.setWarnings(result.getWarnings());
//                        break;
//                    case NOT_ELIGIBLE:
//                        productResult.setMessage("Not eligible");
//                        productResult.setReasons(result.getReasons());
//                        break;
//                }
//
//                response.getResults().add(productResult);
//            }
//
//            response.setStatus("SUCCESS");
//            response.setMessage("Batch eligibility check completed");
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            response.setStatus("ERROR");
//            response.setMessage("An error occurred during batch eligibility check: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
//    }
//
//    /**
//     * Get eligibility summary for a customer and product
//     * @param customerId Customer ID
//     * @param productId Product ID
//     * @return Eligibility summary with counts
//     */
//    @GetMapping("/summary/{customerId}/{productId}")
//    public ResponseEntity<EligibilitySummary> getEligibilitySummary(
//            @PathVariable Long customerId,
//            @PathVariable Long productId) {
//
//        try {
//            CustomCustomer customer = entityManager.find(CustomCustomer.class, customerId);
//            CustomProduct product = entityManager.find(CustomProduct.class, productId);
//
//            if (customer == null || product == null) {
//                return ResponseEntity.badRequest()
//                        .body(new EligibilitySummary("ERROR", "Customer or product not found", 0, 0, 0, 0));
//            }
//
//            EligibilityResult result = cartService.checkCustomerEligibilityDetailed(customer, product, true);
//
//            EligibilitySummary summary = new EligibilitySummary();
//            summary.setCustomerId(customerId);
//            summary.setProductId(productId);
//            summary.setStatus(result.getStatus().toString());
//            summary.setTotalReasons(result.getReasons().size());
//            summary.setTotalWarnings(result.getWarnings().size());
//
//            // Count different types of issues
//            long qualificationIssues = result.getReasons().stream()
//                    .mapToLong(reason -> reason.toLowerCase().contains("qualification") ? 1 : 0)
//                    .sum();
//
//            long ageIssues = result.getReasons().stream()
//                    .mapToLong(reason -> reason.toLowerCase().contains("age") || reason.toLowerCase().contains("born") ? 1 : 0)
//                    .sum();
//
//            summary.setQualificationIssues((int) qualificationIssues);
//            summary.setAgeIssues((int) ageIssues);
//
//            switch (result.getStatus()) {
//                case ELIGIBLE:
//                    summary.setMessage("Customer is fully eligible for this product");
//                    break;
//                case ELIGIBLE_WITH_WARNINGS:
//                    summary.setMessage("Customer is eligible but needs to verify some details manually");
//                    break;
//                case NOT_ELIGIBLE:
//                    summary.setMessage("Customer is not eligible for this product");
//                    break;
//            }
//
//            return ResponseEntity.ok(summary);
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new EligibilitySummary("ERROR", "An error occurred: " + e.getMessage(), 0, 0, 0, 0));
//        }
//    }
}