package com.community.api.endpoint.avisoft.controller.WorkQualtiy;

import com.community.api.component.Constant;
import com.community.api.entity.CustomWorkQuality;
import com.community.api.services.ResponseService;
import com.community.api.services.WorkQualityService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class WorkQualtiyController {

    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    @Autowired
    WorkQualityService workQualityService;

    @GetMapping("/get-all-work-quality")
    public ResponseEntity<?> getAllWorkQualities() {
        try {
            List<CustomWorkQuality> customWorkQualityList = workQualityService.getAllWorkQuality();
            if (customWorkQualityList.isEmpty()) {
                return ResponseService.generateErrorResponse("NO WORK QUALITY IS FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("WORK QUALITY FOUND", customWorkQualityList, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-work-quality-by-work-quality-id/{workQualityId}")
    public ResponseEntity<?> getWorkQualityByWorkQualityId(@PathVariable Long workQualityId) {
        try {
            CustomWorkQuality workQuality = workQualityService.getWorkQualityByWorkQualityId(workQualityId);
            if (workQuality == null) {
                return ResponseService.generateErrorResponse("NO WORK QUALITY FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("WORK QUALITY FOUND", workQuality, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
