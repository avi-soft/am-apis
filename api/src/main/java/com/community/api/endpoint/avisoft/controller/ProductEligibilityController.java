package com.community.api.endpoint.avisoft.controller;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.EligibilityResult;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.Role;
import com.community.api.services.CartService;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/eligibility")
@CrossOrigin(origins = "*")
public class ProductEligibilityController {

    @Autowired
    ExceptionHandlingImplement exceptionHandlingImplement;
    @Autowired
    private CartService cartService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private JwtUtil jwtTokenUtil;
    @Autowired
    private RoleService roleService;

    @GetMapping("/check/{customerId}/{productId}")
    public ResponseEntity<?> checkEligibility(
            @PathVariable Long customerId,
            @PathVariable Long productId,
            @RequestParam(defaultValue = "false") boolean includeAllReasons,
            @RequestHeader(value = "Authorization") String authHeader) {

        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Role role = roleService.getRoleByRoleId(roleId);
            Long userId = null;

            if (!role.getRole_name().equals(Constant.roleUser) && !role.getRole_name().equalsIgnoreCase(Constant.ADMIN) && !role.getRole_name().equalsIgnoreCase(Constant.SUPER_ADMIN)) {
                throw new IllegalArgumentException("Not Authorized to check product eligibility for a customer");
            }
            userId = jwtTokenUtil.extractId(jwtToken);
            if (role.getRole_name().equals(Constant.roleUser) && !userId.equals(customerId)) {
                throw new IllegalArgumentException("Not Authorized to check product eligibility for a customer");
            }

            CustomCustomer customer = entityManager.find(CustomCustomer.class, customerId);
            CustomProduct product = entityManager.find(CustomProduct.class, productId);

            if (customer == null) {
                return ResponseService.generateErrorResponse("Customer not found", HttpStatus.BAD_REQUEST);
            }

            if (product == null) {
                return ResponseService.generateErrorResponse("Product not found", HttpStatus.BAD_REQUEST);
            }

            // Check eligibility
            EligibilityResult result = cartService.checkCustomerEligibilityDetailed(customer, product, includeAllReasons);

            // Create response
            Map<String, Object> response = new HashMap<>();
            response.put("customerId", customerId);
            response.put("productId", productId);

            switch (result.getStatus()) {
                case ELIGIBLE:
                    response.put("status", "ELIGIBLE");
                    response.put("message", "You are eligible for this product");
                    break;
                case ELIGIBLE_WITH_WARNINGS:
                    response.put("status", "ELIGIBLE_WITH_WARNINGS");
                    response.put("message", "You are eligible for this product, but please verify the following warnings");
                    response.put("warnings", result.getWarnings());
                    break;
                case NOT_ELIGIBLE:
                    response.put("status", "NOT_ELIGIBLE");
                    response.put("message", "You are not eligible for this product");
                    response.put("reasons", result.getReasons());
                    break;
            }
            return ResponseService.generateSuccessResponse("Eligibility is checked successfully", response, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            exceptionHandlingImplement.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandlingImplement.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}