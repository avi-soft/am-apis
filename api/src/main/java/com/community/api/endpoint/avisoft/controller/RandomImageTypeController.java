package com.community.api.endpoint.avisoft.controller;

import com.community.api.entity.RandomImageType;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.utils.DocumentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
@RestController
@RequestMapping("/random-image-type")
public class RandomImageTypeController
{
    @Autowired
    public EntityManager entityManager;
    @Autowired
    public ExceptionHandlingImplement exceptionHandlingImplement;

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllDocuments() {
        try {
            List<RandomImageType> randomImageTypes;

            TypedQuery<RandomImageType> query = entityManager.createQuery("SELECT dt FROM RandomImageType dt", RandomImageType.class);
            randomImageTypes=query.getResultList();

            if (randomImageTypes.isEmpty()) {
                return ResponseService.generateErrorResponse("Random image type list is empty", HttpStatus.OK);
            }

            return ResponseService.generateSuccessResponse("Random image types retrieved successfully", randomImageTypes, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }  catch (Exception e) {
            exceptionHandlingImplement.handleException(e);
            return ResponseService.generateErrorResponse("Error retrieving Document Types", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
