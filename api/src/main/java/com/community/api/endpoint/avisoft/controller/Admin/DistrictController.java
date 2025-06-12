package com.community.api.endpoint.avisoft.controller.Admin;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.entity.Districts;
import com.community.api.entity.ErrorResponse;
import com.community.api.entity.StateCode;
import com.community.api.entity.SuccessResponse;
import com.community.api.services.DistrictService;
import com.community.api.services.PrivilegeService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping(value = "/districts",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class DistrictController {
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ResponseService responseService;
    @RequestMapping(value = "get-districts", method = RequestMethod.GET)
    public ResponseEntity<?> getDistricts(@RequestParam String state_code,@RequestParam(required = false,defaultValue = "false")Boolean archived) {
        try {
            if(state_code==null)
                return responseService.generateErrorResponse("Empty value for State Code passed",HttpStatus.BAD_REQUEST);
            List<Districts> names= districtService.findDistrictsByStateCode(state_code,archived);
            if(names.isEmpty()) {
                return responseService.generateErrorResponse("No data found",HttpStatus.OK);
            }
            return responseService.generateSuccessResponse("List retrieved successfully",names,HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error retrieving list", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @RequestMapping(value = "get-all-districts", method = RequestMethod.GET)
    public ResponseEntity<?> getAll(@RequestParam(required = false,defaultValue = "false")Boolean archived) {
        try {
            List<Districts> names= districtService.findAllDistricts(archived);
            if(names.isEmpty()) {
                return responseService.generateErrorResponse("No data found",HttpStatus.OK);
            }
            return responseService.generateSuccessResponse("List retrieved successfully",names,HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error retrieving list", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @RequestMapping(value = "get-state-by-district/{districtId}", method = RequestMethod.GET)
    public ResponseEntity<?> getStateLinkedToDistrict(@PathVariable Integer districtId) {
        try {
            if (districtId == null)
                return ResponseService.generateErrorResponse("District Id needed", HttpStatus.BAD_REQUEST);
            Districts district = entityManager.find(Districts.class, districtId);
            if (district == null)
                return ResponseService.generateErrorResponse("District not found", HttpStatus.OK);
            Query query = entityManager.createQuery("SELECT s FROM StateCode s WHERE s.state_code = :code", StateCode.class);
            query.setParameter("code", district.getState_code());
            return ResponseService.generateSuccessResponse("State found", query.getResultList().get(0), HttpStatus.OK);
        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error retrieving state", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Authorize(value ={Constant.roleSuperAdmin})
    @RequestMapping(value = "add", method = RequestMethod.POST)
    public ResponseEntity<?> addStateToMasterList(@RequestParam Integer stateId,@RequestBody Districts districts) {
        try {
            return ResponseService.generateSuccessResponse("State added successfully to master data",districtService.addDistrict(districts,stateId),HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse("Cannot add district : "+e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Cannot add district : "+e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Authorize(value ={Constant.roleSuperAdmin})
    @RequestMapping(value = "{districtId}/edit", method = RequestMethod.PUT)
    public ResponseEntity<?> addState(@PathVariable Integer districtId, @RequestBody Districts district) {
        try {
            Districts districtToEdit =entityManager.find(Districts.class,districtId);
            if(district==null)
                return ResponseService.generateErrorResponse("District not found",HttpStatus.BAD_REQUEST);
            return ResponseService.generateSuccessResponse("District updated successfully to master data",districtService.editDistrict(districtId,district),HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse("Cannot edit district : "+e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Cannot edit district : "+e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Authorize(value ={Constant.roleSuperAdmin})
    @RequestMapping(value = "{districtId}/manage", method = RequestMethod.DELETE)
    public ResponseEntity<?> manage(@PathVariable Integer districtId,@RequestParam(defaultValue = "true") Boolean archive) {
        try {
            Districts district =entityManager.find(Districts.class,districtId);
            if(district==null)
                return ResponseService.generateErrorResponse("District not found",HttpStatus.BAD_REQUEST);
            return ResponseService.generateSuccessResponse("District archive status altered successfully in master data",districtService.manageDistrict(districtId,archive),HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse("Cannot archive district : "+e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Cannot archive district : "+e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
