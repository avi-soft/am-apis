package com.community.api.endpoint.avisoft.controller.Qualification;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.entity.BoardUniversity;
import com.community.api.services.BoardUniversityService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/board-university")
public class BoardUniversityController {
    protected ExceptionHandlingImplement exceptionHandling;
    private EntityManager entityManager;
    private ResponseService responseService;
    private BoardUniversityService boardUniversityService;

    public BoardUniversityController(EntityManager entityManager, ResponseService responseService, ExceptionHandlingImplement exceptionHandling, BoardUniversityService boardUniversityService) {
        this.responseService = responseService;
        this.entityManager = entityManager;
        this.exceptionHandling = exceptionHandling;
        this.boardUniversityService = boardUniversityService;
    }


    @GetMapping("/get-all-board-universities")
    public ResponseEntity<?> getAllBoardUniversities(@RequestParam(required = false, defaultValue = "false") Boolean archived) {
        try {
            TypedQuery<BoardUniversity> query = entityManager.createQuery(Constant.FIND_ALL_BOARD_UNIVERSITY_QUERY, BoardUniversity.class);
            query.setParameter("archived", archived);
            List<BoardUniversity> boardUniversityList = query.getResultList();
            if (boardUniversityList.isEmpty()) {
                return responseService.generateResponse(HttpStatus.OK, "Board or University List is Empty", boardUniversityList);
            }
            return responseService.generateResponse(HttpStatus.OK, "Board or University List Retrieved Successfully", boardUniversityList);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Authorize(value = {Constant.roleSuperAdmin})
    @PostMapping("/add")
    public ResponseEntity<?> addBoardUniversity(@RequestBody BoardUniversity boardUniversities, @RequestHeader(value = "Authorization") String authHeader) throws Exception {
        try {
            BoardUniversity addedBoardUniversities = boardUniversityService.addBoardUniversities(boardUniversities, authHeader);
            return responseService.generateResponse(HttpStatus.OK, "Board or University is added successfully", addedBoardUniversities);
        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Authorize(value = {Constant.roleSuperAdmin})
    @PutMapping("/update/{boardUniversityId}")
    public ResponseEntity<?> updateBoardUniversity(@PathVariable Long boardUniversityId, @RequestBody BoardUniversity boardUniversity, @RequestHeader(value = "Authorization") String authHeader) {
        try {
            BoardUniversity updatedBoardUniversity = boardUniversityService.updateBoardUniversity(boardUniversityId, boardUniversity, authHeader);
            return responseService.generateResponse(HttpStatus.CREATED, "Board or University is updated successfully", updatedBoardUniversity);
        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Authorize(value = {Constant.roleSuperAdmin})
    @DeleteMapping("/{id}/status")
    public ResponseEntity<?> manageUniversityStatus(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") Boolean archive) {
        try {
            BoardUniversity university = boardUniversityService.manageUni(id, archive);
            String message = archive ? "University activated successfully" : "University deactivated successfully";
            return responseService.generateSuccessResponse(message, university, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error updating university status", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
