package com.community.api.endpoint.avisoft.controller.Admin;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.entity.StateCode;
import com.community.api.services.DistrictService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/states",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
)
public class StateController {
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private ResponseService responseService;

    @RequestMapping(value = "get-states", method = RequestMethod.GET)
    public ResponseEntity<?> getStates(@RequestParam(defaultValue = "false", required = false) Boolean archived) {
        try {
            List<StateCode> names = districtService.findStateList(archived);
            return responseService.generateSuccessResponse("List Retrieved Successfully", names, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error retrieving list", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Authorize(value = {Constant.roleSuperAdmin})
    @RequestMapping(value = "add", method = RequestMethod.POST)
    public ResponseEntity<?> addStateToMasterList(@RequestBody StateCode stateCode) {
        try {
            return ResponseService.generateSuccessResponse("State added successfully to master data", districtService.addState(stateCode), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse("Cannot add state : " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Cannot add state : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Authorize(value = {Constant.roleSuperAdmin})
    @RequestMapping(value = "{stateId}/edit", method = RequestMethod.PUT)
    public ResponseEntity<?> addState(@PathVariable Integer stateId, @RequestBody StateCode stateCode) {
        try {
            StateCode state = districtService.getStateByStateId(stateId);
            if (state == null)
                return ResponseService.generateErrorResponse("State not found", HttpStatus.BAD_REQUEST);
            return ResponseService.generateSuccessResponse("State updated successfully to master data", districtService.editState(stateId, stateCode), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse("Cannot edit state : " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Cannot edit state : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Authorize(value = {Constant.roleSuperAdmin})
    @RequestMapping(value = "{stateId}/manage", method = RequestMethod.DELETE)
    public ResponseEntity<?> manage(@PathVariable Integer stateId, @RequestParam(defaultValue = "true") Boolean archive) {
        try {
            StateCode state = districtService.getStateByStateId(stateId);
            if (state == null)
                return ResponseService.generateErrorResponse("State not found", HttpStatus.BAD_REQUEST);
            return ResponseService.generateSuccessResponse("State archive status alterd successfully in master data", districtService.manageState(stateId, archive), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse("Cannot archive state : " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Cannot archive state : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

