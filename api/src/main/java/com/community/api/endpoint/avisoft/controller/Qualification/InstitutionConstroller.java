package com.community.api.endpoint.avisoft.controller.Qualification;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.entity.Institution;
import com.community.api.services.InstitutionService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@RestController
@RequestMapping("/institution")
public class InstitutionConstroller {
    protected ExceptionHandlingImplement exceptionHandling;
    private EntityManager entityManager;
    private ResponseService responseService;
    private InstitutionService institutionService;

    public InstitutionConstroller(EntityManager entityManager, ResponseService responseService, ExceptionHandlingImplement exceptionHandling, InstitutionService institutionService) {
        this.responseService = responseService;
        this.entityManager = entityManager;
        this.exceptionHandling = exceptionHandling;
        this.institutionService = institutionService;
    }

    @GetMapping("/get-all-institutions")
    public ResponseEntity<?> getAllInstitutions(@RequestParam(required = false, defaultValue = "false") Boolean archived) {
        try {
            TypedQuery<Institution> query = entityManager.createQuery(Constant.FIND_ALL_INSTITUTION_QUERY, Institution.class);
            query.setParameter("archived", archived);
            List<Institution> institutionList = query.getResultList();
            if (institutionList.isEmpty()) {
                return responseService.generateResponse(HttpStatus.OK, "Institution List is Empty", institutionList);
            }
            return responseService.generateResponse(HttpStatus.OK, "Institution List Retrieved Successfully", institutionList);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addInstitution(@RequestBody Institution institutions, @RequestHeader(value = "Authorization") String authHeader) throws Exception {
        try {
            Institution addedInstitutions = institutionService.addInstitutions(institutions, authHeader);
            return responseService.generateResponse(HttpStatus.CREATED, "Institution is added successfully", addedInstitutions);
        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{institutionId}")
    public ResponseEntity<?> updateInstitution(@PathVariable Long institutionId, @RequestBody Institution institution, @RequestHeader(value = "Authorization") String authHeader) {
        try {
            Institution updatedInstitution = institutionService.updateInstitution(institutionId, institution, authHeader);
            return responseService.generateResponse(HttpStatus.CREATED, "Institution is updated successfully", updatedInstitution);
        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Authorize(value = {Constant.roleSuperAdmin})
    @PutMapping("/{id}/manage")
    public ResponseEntity<?> manageInstitutionArchiveStatus(
            @PathVariable Long id,
            @RequestParam Boolean archive) {
        try {
            Institution institution = institutionService.manageInstitutionArchiveStatus(id, archive);
            String message = archive ? "Institution archived successfully" : "Institution unarchived successfully";
            return ResponseService.generateSuccessResponse(message, institution, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error updating institution status", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

