package com.community.api.endpoint.avisoft.controller.Qualification;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.entity.Qualification;
import com.community.api.services.QualificationService;
import com.community.api.services.ResponseService;
import com.community.api.services.SharedUtilityService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.community.api.component.Constant.FIND_ALL_QUALIFICATIONS_QUERY;

@RestController
@RequestMapping("/qualification")
public class QualificationController {
    protected ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private SharedUtilityService sharedUtilityService;
    private EntityManager entityManager;
    private ResponseService responseService;
    private QualificationService qualificationService;

    public QualificationController(EntityManager entityManager, ResponseService responseService, ExceptionHandlingImplement exceptionHandling, QualificationService qualificationService) {
        this.responseService = responseService;
        this.entityManager = entityManager;
        this.exceptionHandling = exceptionHandling;
        this.qualificationService = qualificationService;
    }

    @GetMapping("/get-all-qualifications")
    public ResponseEntity<?> getAllQualifications(@RequestParam(name = "major", required = false, defaultValue = "false") Boolean major, @RequestParam(defaultValue = "false", required = false) Boolean archived) throws Exception {
        try {
            TypedQuery<Qualification> query = entityManager.createQuery(FIND_ALL_QUALIFICATIONS_QUERY, Qualification.class);
            query.setParameter("archived", archived);
            List<Qualification> qualifications = query.getResultList();

            // Filter out qualifications safely
            List<Qualification> filteredQualifications = qualifications.stream()
//                .filter(q ->!q.getQualification_name().equalsIgnoreCase("MASTERS/POST_GRADUATION"))
                    .collect(Collectors.toList());

            if (filteredQualifications.isEmpty()) {
                return responseService.generateResponse(HttpStatus.OK, "Qualification List is Empty", filteredQualifications);
            }
            if (major.equals(true)) {
                Qualification bachelor = qualificationService.getQualificationByQualificationId(3);
                Qualification masters = qualificationService.getQualificationByQualificationId(4);
                filteredQualifications.add(bachelor);
                filteredQualifications.add(masters);
                filteredQualifications.sort(Comparator.comparingLong(Qualification::getQualification_id));
                int index = Math.min(7, filteredQualifications.size());
                filteredQualifications = filteredQualifications.subList(0, index);
            }
            return responseService.generateResponse(HttpStatus.OK, "Qualification List Retrieved Successfully", filteredQualifications);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Authorize(value = {Constant.roleSuperAdmin})
    @PostMapping("/add")
    public ResponseEntity<?> addQualification(@RequestBody Qualification qualification) throws Exception {
        try {
            Qualification addedQualification = qualificationService.addQualification(qualification);
            return responseService.generateResponse(HttpStatus.CREATED, "Qualification added successfully", addedQualification);
        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Authorize(value = {Constant.roleSuperAdmin})
    @PutMapping("/{qualificationId}/edit")
    public ResponseEntity<?> editQualification(@PathVariable Integer qualificationId, @RequestBody Qualification qualification) throws Exception {
        try {
            Qualification addedQualification = qualificationService.edit(qualificationId, qualification);
            return responseService.generateResponse(HttpStatus.OK, "Qualification edited successfully", addedQualification);
        } catch (NumberFormatException numberFormatException) {
            exceptionHandling.handleException(numberFormatException);
            return responseService.generateErrorResponse(numberFormatException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @Authorize(value = {Constant.roleSuperAdmin})
    @DeleteMapping("/{qualificationId}/manage")
    public ResponseEntity<?> manage(@PathVariable Integer qualificationId, @RequestParam(required = false, defaultValue = "FALSE") Boolean archive) throws Exception {
        try {
            Qualification addedQualification = entityManager.find(Qualification.class, qualificationId);
            if (addedQualification == null)
                return ResponseService.generateErrorResponse("Qualification not found", HttpStatus.NOT_FOUND);
            if (archive) {
                if (addedQualification.getArchived())
                    throw new IllegalArgumentException("Qualification already archived");
                else {
                    addedQualification.setArchived(true);
                    entityManager.merge(addedQualification);
                }
            } else {
                if (!addedQualification.getArchived())
                    throw new IllegalArgumentException("Qualification already unarchived");
                else {
                    addedQualification.setArchived(false);
                    entityManager.merge(addedQualification);
                }
            }
            return responseService.generateResponse(HttpStatus.OK, "Qualification status altered successfully", addedQualification);
        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong" + e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

