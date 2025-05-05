package com.community.api.endpoint.avisoft.controller.Subject;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.AddSubjectDto;
import com.community.api.entity.CustomSubject;
import com.community.api.entity.Role;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.SubjectService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.validation.Valid;
import java.util.List;

@RestController
public class SubjectController {

    private final ExceptionHandlingService exceptionHandlingService;
    private final SubjectService subjectService;
    private final RoleService roleService;
    private final JwtUtil jwtTokenUtil;
    private final EntityManager entityManager;

    @Autowired
    public SubjectController(ExceptionHandlingService exceptionHandlingService, SubjectService subjectService, RoleService roleService, JwtUtil jwtTokenUtil, EntityManager entityManager) {
        this.exceptionHandlingService = exceptionHandlingService;
        this.subjectService = subjectService;
        this.roleService = roleService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.entityManager = entityManager;
    }

    @PostMapping("/add-subject")
    public ResponseEntity<?> addSubject(@Valid @RequestBody AddSubjectDto addSubjectDto, @RequestHeader(value = "Authorization") String authHeader) {
        try{
            if(!subjectService.validiateAuthorization(authHeader)) {
                return ResponseService.generateErrorResponse("NOT AUTHORIZED TO ADD A SUBJECT", HttpStatus.UNAUTHORIZED);
            }

            subjectService.validateAddSubjectDto(addSubjectDto);
            String jwtToken = authHeader.substring(7);

            Long creatorId = jwtTokenUtil.extractId(jwtToken);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Role creatorRole = roleService.getRoleByRoleId(roleId);

            CustomSubject customSubject = subjectService.saveSubject(addSubjectDto, creatorId, creatorRole);

            if(customSubject == null) {
                return ResponseService.generateErrorResponse("SOMETHING WENT WRONG", HttpStatus.BAD_REQUEST);
            }
            return ResponseService.generateSuccessResponse("SUCCESSFULLY ADDED", customSubject, HttpStatus.OK);
        } catch (MethodArgumentNotValidException methodArgumentNotValidException) {
            exceptionHandlingService.handleException(methodArgumentNotValidException);
            return ResponseService.generateErrorResponse(  "Method Argument Not Valid Exception Caught: " + methodArgumentNotValidException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(  "Illegal Argument Exception Caught: " + illegalArgumentException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-all-subject")
    public ResponseEntity<?> getAllSubject() {
        try {
            List<CustomSubject> subjectList = subjectService.getAllSubject();
            if (subjectList.isEmpty()) {
                return ResponseService.generateErrorResponse("NO SUBJECT FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("SUBJECTS FOUND", subjectList, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-subject-by-id/{subjectIdString}")
    public ResponseEntity<?> getSubjectById(@PathVariable String subjectIdString) {
        try {
            Long subjectId = Long.parseLong(subjectIdString);
            CustomSubject subject = subjectService.getSubjectBySubjectId(subjectId);
            if (subject == null) {
                return ResponseService.generateErrorResponse("NO SUBJECT FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("SUBJECT FOUND", subject, HttpStatus.OK);
        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse("Invalid SubjectId: " + numberFormatException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/remove-subject-by-id/{subjectIdString}")
    public ResponseEntity<?> removeSubjectBySubjectId(@PathVariable String subjectIdString) {
        try {

            Long subjectId = Long.parseLong(subjectIdString);
            CustomSubject subject = subjectService.getSubjectBySubjectId(subjectId);
            if (subject == null) {
                return ResponseService.generateErrorResponse("NO SUBJECT FOUND", HttpStatus.NOT_FOUND);
            }
            subjectService.removeSubjectById(subject);
            return ResponseService.generateSuccessResponse("SUBJECT SUCCESSFULLY ARCHIVED", subject, HttpStatus.OK);
        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse("Invalid SubjectId: " + numberFormatException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-subjects-by-steam-id/{streamId}")
    public ResponseEntity<?> getSubjectsByStream(@PathVariable Long streamId) {
        try {
            List<CustomSubject> subjectList = subjectService.getSubjectsByStreamIds(streamId);
            if (subjectList.isEmpty()) {
                return ResponseService.generateSuccessResponse("LIST OF SUBJECTS IS EMPTY IN STREAM WITH ID "+ streamId, subjectList,HttpStatus.OK);
            }
            return ResponseService.generateSuccessResponse("SUBJECTS FOUND IN STREAM WITH ID "+ streamId,subjectList, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
