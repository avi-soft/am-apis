package com.community.api.endpoint.avisoft.controller.Admin;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.entity.Role;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.SanitizerService;
import com.community.api.services.ServiceProviderLanguageService;
import com.community.api.services.SharedUtilityService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Map;

// TODO- here adding the authorization manually as annotation didn't work
@RestController
@RequestMapping(value = "/service-provider-language",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
)
public class ServiceProviderLanguageController {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private ServiceProviderLanguageService languageService;
    @Autowired
    private ResponseService responseService;
    @Autowired
    private SharedUtilityService sharedUtilityService;
    @Autowired
    private SanitizerService sanitizerService;
    @Autowired
    private JwtUtil jwtTokenUtil;
    @Autowired
    private RoleService roleService;

    @Transactional
    @PostMapping("add-language")
    private ResponseEntity<?> addLanguage(@RequestBody Map<String, Object> serviceProviderLanguage, @RequestHeader(value = "Authorization") String authHeader) {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Role tokenRole = roleService.getRoleByRoleId(roleId);
            if(!tokenRole.getRole_name().equals(Constant.SERVICE_PROVIDER) && !tokenRole.getRole_name().equals(Constant.SUPER_ADMIN) && !tokenRole.getRole_name().equals(Constant.ADMIN)) {
                return responseService.generateErrorResponse("Forbidden Access", HttpStatus.FORBIDDEN);
            }

            if (!sharedUtilityService.validateInputMap(serviceProviderLanguage).equals(SharedUtilityService.ValidationResult.SUCCESS)) {
                return ResponseService.generateErrorResponse("Invalid Request Body", HttpStatus.UNPROCESSABLE_ENTITY);
            }
            serviceProviderLanguage = sanitizerService.sanitizeInputMap(serviceProviderLanguage);
            return languageService.addLanguage(serviceProviderLanguage);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return responseService.generateErrorResponse("Error adding language to list", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("get-languages")
    private ResponseEntity<?> getLanguages(@RequestHeader(value = "Authorization") String authHeader) {
        try {

            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Role tokenRole = roleService.getRoleByRoleId(roleId);
            if(!tokenRole.getRole_name().equals(Constant.SERVICE_PROVIDER) && !tokenRole.getRole_name().equals(Constant.SUPER_ADMIN) && !tokenRole.getRole_name().equals(Constant.ADMIN)) {
                return responseService.generateErrorResponse("Forbidden Access", HttpStatus.FORBIDDEN);
            }
            return responseService.generateSuccessResponse("List Fetched Successfully", languageService.findAllLanguageList(), HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return responseService.generateErrorResponse("Error adding language to list", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
