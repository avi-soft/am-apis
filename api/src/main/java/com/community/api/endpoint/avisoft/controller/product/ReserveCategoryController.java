package com.community.api.endpoint.avisoft.controller.product;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.entity.CustomReserveCategory;
import com.community.api.services.ReserveCategoryService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReserveCategoryController {
    private final ExceptionHandlingService exceptionHandlingService;
    private final ReserveCategoryService reserveCategoryService;

    @Autowired
    public ReserveCategoryController(ExceptionHandlingService exceptionHandlingService, ReserveCategoryService reserveCategoryService) {
        this.exceptionHandlingService = exceptionHandlingService;
        this.reserveCategoryService = reserveCategoryService;
    }

    @GetMapping("/get-all-reserve-category")
    public ResponseEntity<?> getAllReserveCategory(@RequestParam(required = false, defaultValue = "false") Boolean archived) {
        try {

            List<CustomReserveCategory> authorities = reserveCategoryService.getAllReserveCategory(archived);

            if (authorities.isEmpty()) {
                return ResponseService.generateErrorResponse("No Reserve Category Found", HttpStatus.OK);
            }
            return ResponseService.generateSuccessResponse("Reserve Categories Found", authorities, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("get-reserve-category-by-id/{reserveCategoryId}")
    public ResponseEntity<?> getReserveCategoryById(@PathVariable Long reserveCategoryId) {
        try {
            CustomReserveCategory customReserveCategory = reserveCategoryService.getReserveCategoryById(reserveCategoryId);
            if (customReserveCategory == null) {
                return ResponseService.generateErrorResponse("No Reserve Category Found", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("Reserve Category Found", customReserveCategory, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Authorize(value = {Constant.roleSuperAdmin})
    @PostMapping("reserve-category/add")
    public ResponseEntity<?> addReserveCategory(@RequestBody CustomReserveCategory reserveCategory) {
        try {
            return ResponseService.generateSuccessResponse(
                    "Reserve category added successfully",
                    reserveCategoryService.addReserveCategory(reserveCategory),
                    HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            exceptionHandlingService.handleException(e);
            return ResponseService.generateErrorResponse(
                    "Cannot add reserve category: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            return ResponseService.generateErrorResponse(
                    "Cannot add reserve category: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Authorize(value = {Constant.roleSuperAdmin})
    @PutMapping("reserve-category/{reserveCategoryId}/edit")
    public ResponseEntity<?> editReserveCategory(
            @PathVariable Long reserveCategoryId,
            @RequestBody CustomReserveCategory reserveCategory) {
        try {
            CustomReserveCategory existingCategory = reserveCategoryService.getReserveCategoryById(reserveCategoryId);
            if (existingCategory == null) {
                return ResponseService.generateErrorResponse(
                        "Reserve category not found",
                        HttpStatus.BAD_REQUEST);
            }
            return ResponseService.generateSuccessResponse(
                    "Reserve category updated successfully",
                    reserveCategoryService.editReserveCategory(reserveCategoryId, reserveCategory),
                    HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            exceptionHandlingService.handleException(e);
            return ResponseService.generateErrorResponse(
                    "Cannot edit reserve category: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            return ResponseService.generateErrorResponse(
                    "Cannot edit reserve category: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Authorize(value = {Constant.roleSuperAdmin})
    @DeleteMapping("reserve-category/{reserveCategoryId}/manage")
    public ResponseEntity<?> manageReserveCategory(
            @PathVariable Long reserveCategoryId,
            @RequestParam(defaultValue = "true") Boolean archive) {
        try {
            CustomReserveCategory existingCategory = reserveCategoryService.getReserveCategoryById(reserveCategoryId);
            if (existingCategory == null) {
                return ResponseService.generateErrorResponse(
                        "Reserve category not found",
                        HttpStatus.BAD_REQUEST);
            }
            return ResponseService.generateSuccessResponse(
                    "Reserve category archive status altered successfully",
                    reserveCategoryService.manageReserveCategory(reserveCategoryId, archive),
                    HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            exceptionHandlingService.handleException(e);
            return ResponseService.generateErrorResponse(
                    "Cannot archive reserve category: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            return ResponseService.generateErrorResponse(
                    "Cannot archive reserve category: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
