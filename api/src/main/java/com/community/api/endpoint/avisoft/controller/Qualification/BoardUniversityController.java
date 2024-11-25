package com.community.api.endpoint.avisoft.controller.Qualification;

import com.community.api.component.Constant;
import com.community.api.entity.BoardUniversity;
import com.community.api.services.BoardUniversityService;
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
@RequestMapping("/board-university")
public class BoardUniversityController {
    private EntityManager entityManager;
    private ResponseService responseService;
    protected ExceptionHandlingImplement exceptionHandling;
    private BoardUniversityService boardUniversityService;
    public BoardUniversityController(EntityManager entityManager, ResponseService responseService, ExceptionHandlingImplement exceptionHandling, BoardUniversityService boardUniversityService) {
        this.responseService=responseService;
        this.entityManager = entityManager;
        this.exceptionHandling=exceptionHandling;
        this.boardUniversityService = boardUniversityService;
    }


    @GetMapping("/get-all-board-universities")

    public ResponseEntity<?> getAllBoardUniversities() {
        try
        {
            TypedQuery<BoardUniversity> query = entityManager.createQuery(Constant.FIND_ALL_BOARD_UNIVERSITY_QUERY, BoardUniversity.class);
            List<BoardUniversity> boardUniversityList = query.getResultList();
            if(boardUniversityList.isEmpty())
            {
                return responseService.generateResponse(HttpStatus.OK,"Board or University List is Empty", boardUniversityList);
            }
            return responseService.generateResponse(HttpStatus.OK,"Board or University List Retrieved Successfully", boardUniversityList);
        }
        catch (Exception exception)
        {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Something went wrong",HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addBoardUniversity(@RequestBody List<BoardUniversity>  boardUniversities,@RequestHeader(value = "Authorization") String authHeader) throws Exception {
        try
        {
            List<BoardUniversity> addedBoardUniversities = boardUniversityService.addBoardUniversities(boardUniversities,authHeader);
            return responseService.generateResponse(HttpStatus.CREATED,"Board or University is added successfully", addedBoardUniversities);
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

    @PatchMapping("/update/{boardUniversityId}")
    public ResponseEntity<?> updateBoardUniversity(@PathVariable Long boardUniversityId,@RequestBody BoardUniversity boardUniversity,@RequestHeader(value = "Authorization")String authHeader)
    {
        try
        {
            BoardUniversity updatedBoardUniversity= boardUniversityService.updateBoardUniversity(boardUniversityId,boardUniversity,authHeader);
            return responseService.generateResponse(HttpStatus.CREATED,"Board or University is updated successfully", updatedBoardUniversity);
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
