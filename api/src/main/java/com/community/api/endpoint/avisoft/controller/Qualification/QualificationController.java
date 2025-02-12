package com.community.api.endpoint.avisoft.controller.Qualification;

import com.community.api.entity.Qualification;
import com.community.api.services.QualificationService;
import com.community.api.services.ResponseService;
import com.community.api.services.SharedUtilityService;
import com.community.api.services.exception.*;
import com.community.api.utils.DocumentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.community.api.component.Constant.FIND_ALL_QUALIFICATIONS_QUERY;

@RestController
@RequestMapping("/qualification")
public class QualificationController {
    @Autowired
    private SharedUtilityService sharedUtilityService;

    private EntityManager entityManager;
    private ResponseService responseService;
    protected ExceptionHandlingImplement exceptionHandling;
    private QualificationService qualificationService;
    public QualificationController(EntityManager entityManager, ResponseService responseService, ExceptionHandlingImplement exceptionHandling, QualificationService qualificationService) {
        this.responseService=responseService;
        this.entityManager = entityManager;
        this.exceptionHandling=exceptionHandling;
        this.qualificationService = qualificationService;
    }

    @GetMapping("/get-all-qualifications")
    public ResponseEntity<?> getAllQualifications(@RequestParam(name = "major", required = false,defaultValue = "false")Boolean major) throws Exception {
        TypedQuery<Qualification> query = entityManager.createQuery(FIND_ALL_QUALIFICATIONS_QUERY, Qualification.class);
        List<Qualification> qualifications = query.getResultList();

        // Filter out qualifications safely
        List<Qualification> filteredQualifications = qualifications.stream()
                .filter(q -> !q.getQualification_name().equalsIgnoreCase("BACHELORS/GRADUATION") &&
                        !q.getQualification_name().equalsIgnoreCase("MASTERS/POST_GRADUATION"))
                .collect(Collectors.toList());

        if (filteredQualifications.isEmpty()) {
            return responseService.generateResponse(HttpStatus.OK, "Qualification List is Empty", filteredQualifications);
        }
        if(major.equals(true))
        {
            Qualification bachelor=qualificationService.getQualificationByQualificationId(3);
            Qualification masters=qualificationService.getQualificationByQualificationId(4);
            filteredQualifications.add(bachelor);
            filteredQualifications.add(masters);
            filteredQualifications.sort(Comparator.comparingLong(Qualification::getQualification_id));
           int index=Math.min(7,filteredQualifications.size());
           filteredQualifications=filteredQualifications.subList(0,index);
        }
            return responseService.generateResponse(HttpStatus.OK, "Qualification List Retrieved Successfully", filteredQualifications);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addQualification(@RequestBody Qualification qualification) throws Exception {
        try
        {
            Qualification addedQualification = qualificationService.addQualification(qualification);
            return responseService.generateResponse(HttpStatus.CREATED,"Qualification added successfully", addedQualification);
        }
        catch (IllegalArgumentException e) {
            return responseService.generateErrorResponse(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        catch (Exception e)
        {
            return ResponseService.generateErrorResponse("Something went wrong",HttpStatus.BAD_REQUEST);
        }
    }
}

