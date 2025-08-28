package com.community.api.endpoint.avisoft.controller.Admin;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.services.ResponseService;
import com.community.api.services.SanitizerService;
import com.community.api.services.SharedUtilityService;
import com.community.api.services.SkillService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.Map;

@RestController
@RequestMapping("/service-provider-skill")
public class SkillController {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private SkillService skillService;
    @Autowired
    private ResponseService responseService;
    @Autowired
    private SharedUtilityService sharedUtilityService;
    @Autowired
    private SanitizerService sanitizerService;

    @Transactional
    @Authorize(value = {Constant.roleAdmin, Constant.roleSuperAdmin, Constant.roleServiceProvider})
    @PostMapping("/add-skill")
    public ResponseEntity<?> addSkill(@RequestBody Map<String, Object> skill) {
        try {
            if (!sharedUtilityService.validateInputMap(skill).equals(SharedUtilityService.ValidationResult.SUCCESS)) {
                return ResponseService.generateErrorResponse("Invalid Request Body", HttpStatus.UNPROCESSABLE_ENTITY);
            }
            skill = sanitizerService.sanitizeInputMap(skill);
            return skillService.addSkill(skill);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandling.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return responseService.generateErrorResponse("Error saving skill : " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-skills")
    @Authorize(value = {Constant.roleAdmin, Constant.roleSuperAdmin, Constant.roleServiceProvider})
    public ResponseEntity<?> getSkillList() {
        try {
            return responseService.generateSuccessResponse("List Fetched Successfully", skillService.findAllSkillList(), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return responseService.generateErrorResponse("Error saving skill : " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
