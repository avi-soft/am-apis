package com.community.api.endpoint.avisoft.controller.Qualification;

import com.community.api.component.Constant;
import com.community.api.entity.Institution;
import com.community.api.services.InstitutionService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.utils.DocumentType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

import static com.community.api.component.Constant.FIND_ALL_QUALIFICATIONS_QUERY;

@RestController
@RequestMapping("/institution")
public class InstitutionConstroller {
    private EntityManager entityManager;
    private ResponseService responseService;
    protected ExceptionHandlingImplement exceptionHandling;
    private InstitutionService institutionService;
    public InstitutionConstroller(EntityManager entityManager, ResponseService responseService, ExceptionHandlingImplement exceptionHandling, InstitutionService institutionService) {
        this.responseService=responseService;
        this.entityManager = entityManager;
        this.exceptionHandling=exceptionHandling;
        this.institutionService = institutionService;
    }

    @GetMapping("/get-all-institutions")
    public ResponseEntity<?> getAllInstitutions() {
        try
        {
            TypedQuery<Institution> query = entityManager.createQuery(Constant.FIND_ALL_INSTITUTION_QUERY, Institution.class);
            List<Institution> institutionList = query.getResultList();
            if(institutionList.isEmpty())
            {
                return responseService.generateResponse(HttpStatus.OK,"Institution List is Empty", institutionList);
            }
            return responseService.generateResponse(HttpStatus.OK,"Institution List Retrieved Successfully", institutionList);
        }
        catch (Exception exception)
        {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Something went wrong",HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addInstitution(@RequestBody List<Institution>  institutions, @RequestHeader(value = "Authorization") String authHeader) throws Exception {
        try
        {
            List<Institution> addedInstitutions = institutionService.addInstitutions(institutions,authHeader);
            return responseService.generateResponse(HttpStatus.CREATED,"Institution is added successfully", addedInstitutions);
        }
        catch (IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        catch (Exception e)
        {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong",HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/update/{institutionId}")
    public ResponseEntity<?> updateInstitution(@PathVariable Long institutionId, @RequestBody Institution institution, @RequestHeader(value = "Authorization")String authHeader)
    {
        try
        {
            Institution updatedInstitution= institutionService.updateInstitution(institutionId,institution,authHeader);
            return responseService.generateResponse(HttpStatus.CREATED,"Institution is updated successfully", updatedInstitution);
        }
        catch (IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        catch (Exception e)
        {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong",HttpStatus.BAD_REQUEST);
        }
    }
}

