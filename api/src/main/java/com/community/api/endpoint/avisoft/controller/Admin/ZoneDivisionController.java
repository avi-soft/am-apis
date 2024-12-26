package com.community.api.endpoint.avisoft.controller.Admin;

import com.community.api.entity.Zone;
import com.community.api.services.ResponseService;
import com.community.api.services.ZoneDivisionService;
import io.swagger.models.auth.In;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.persistence.NoResultException;

@Controller
@RequestMapping("/zone")
public class ZoneDivisionController {

    @Autowired
    private ZoneDivisionService zoneDivisionService;

    @GetMapping("/divisions/{zoneId}")
    public ResponseEntity<?> getDivisionByZone(@PathVariable Integer zoneId) {
        try {
            return ResponseService.generateSuccessResponse("Divisions : ", zoneDivisionService.getDivisionsByZoneId(zoneId), HttpStatus.OK);
        } catch (NoResultException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (NotFoundException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping("/all-zones")
    public ResponseEntity<?> getDivisionByZone() {
        try {
            return ResponseService.generateSuccessResponse("All Zones:", zoneDivisionService.getAllZones(), HttpStatus.OK);
        } catch (Exception exception) {
            return ResponseService.generateErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/findByDivision/{divisionId}")
    public ResponseEntity<?> getLInkedZone(@PathVariable Integer divisionId)
    {
        try{
            return ResponseService.generateSuccessResponse("Linked Zone : ",zoneDivisionService.findDivisionsLinkedZone(divisionId),HttpStatus.OK);
        } catch (NotFoundException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (NoResultException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.OK);
        }
    }
}
