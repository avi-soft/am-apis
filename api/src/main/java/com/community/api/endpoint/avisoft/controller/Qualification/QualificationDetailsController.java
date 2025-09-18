package com.community.api.endpoint.avisoft.controller.Qualification;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.UpdateQualificationDto;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.ExternalUseToken;
import com.community.api.entity.QualificationDetails;
import com.community.api.services.QualificationDetailsService;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.exception.CustomerDoesNotExistsException;
import com.community.api.services.exception.EntityAlreadyExistsException;
import com.community.api.services.exception.EntityDoesNotExistsException;
import com.community.api.services.exception.ExaminationDoesNotExistsException;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/qualification-details")
public class QualificationDetailsController {
    protected QualificationDetailsService qualificationDetailsService;
    protected ExceptionHandlingImplement exceptionHandling;
    @Autowired
    EntityManager entityManager;
    private ResponseService responseService;
    private JwtUtil jwtTokenUtil;
    private RoleService roleService;

    public QualificationDetailsController(QualificationDetailsService qualificationDetailsService, ExceptionHandlingImplement exceptionHandling, ResponseService responseService, JwtUtil jwtTokenUtil, RoleService roleService) {
        this.qualificationDetailsService = qualificationDetailsService;
        this.exceptionHandling = exceptionHandling;
        this.responseService = responseService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.roleService = roleService;
    }

    @PostMapping("/add/{id}")
    public ResponseEntity<?> addQualificationDetail(@PathVariable Long id, @Valid @RequestBody QualificationDetails qualificationDetails, @RequestParam(value = "boardUniversityOthers", required = false) String boardUniversityOthers, @RequestParam(value = "streamOthers", required = false) String streamOthers, @RequestParam(value = "qualificationOthers", required = false) String qualificationOthers, @RequestParam(value = "institutionOthers", required = false) String institutionOthers, @RequestHeader(value = "Authorization") String authHeader, @RequestHeader(value = "extAuth", required = false) String extAuth, @RequestParam(required = false) Boolean extUp) throws EntityAlreadyExistsException, ExaminationDoesNotExistsException, CustomerDoesNotExistsException {
        String role = null;
        try {

            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long userId = jwtTokenUtil.extractId(jwtToken);
            if (roleId == 5 && !userId.equals(id)) {
                return ResponseService.generateErrorResponse("Forbidden", HttpStatus.FORBIDDEN);
            }
            if ((extUp != null && extUp) && roleId == 4) {
                if (id == null)
                    return ResponseService.generateErrorResponse("Id not provided", HttpStatus.NOT_FOUND);
                CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, id);
                if (customCustomer == null)
                    return ResponseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
                ExternalUseToken externalUseToken = entityManager.find(ExternalUseToken.class, userId);
                if (externalUseToken == null || externalUseToken.getToken() == null || externalUseToken.getToken().isEmpty())
                    return ResponseService.generateSuccessResponse("Forbidden Access", "role", HttpStatus.UNAUTHORIZED);
                if (jwtTokenUtil.extractId(externalUseToken.getToken()).equals(id))
                    roleId = 5;
                else
                    return ResponseService.generateSuccessResponse("Forbidden Access", "role", HttpStatus.UNAUTHORIZED);
            } else if ((extUp != null && extUp) && (roleId == 1 || roleId == 2)) {
                jwtToken = authHeader.substring(7);
                roleId = 5;
            }
            role = roleService.getRoleByRoleId(roleId).getRole_name();
            QualificationDetails newQualificationDetails = qualificationDetailsService.addQualificationDetails(id, qualificationDetails, boardUniversityOthers, streamOthers, qualificationOthers, institutionOthers, roleId, role);
            return ResponseService.generateSuccessResponse("Qualification Details is added successfully for " + role, newQualificationDetails, HttpStatus.CREATED);
        } catch (CustomerDoesNotExistsException e) {
            exceptionHandling.handleException(e);
            if (role.equalsIgnoreCase(Constant.SERVICE_PROVIDER)) {
                return ResponseService.generateErrorResponse("Service Provider does not exist", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateErrorResponse("Customer does not exist", HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (EntityAlreadyExistsException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Qualification already exists", HttpStatus.BAD_REQUEST);
        } catch (ExaminationDoesNotExistsException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Qualification does not exist", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<?> getQualificationDetailsById(@PathVariable Long id, @RequestHeader(value = "Authorization") String authHeader, @RequestParam(required = false, defaultValue = "false") Boolean ext) throws CustomerDoesNotExistsException, EntityDoesNotExistsException {
        String role = null;
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long userId = jwtTokenUtil.extractId(jwtToken);
            if (roleId == 5 && !userId.equals(id)) {
                return ResponseService.generateErrorResponse("Forbidden", HttpStatus.FORBIDDEN);
            }
            if ((ext) && roleId == 4) {
                if (id == null)
                    return ResponseService.generateErrorResponse("Id not provided", HttpStatus.NOT_FOUND);
                CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, id);
                if (customCustomer == null)
                    return ResponseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
                ExternalUseToken externalUseToken = entityManager.find(ExternalUseToken.class, userId);
                if (externalUseToken == null || externalUseToken.getToken() == null || externalUseToken.getToken().isEmpty())
                    return ResponseService.generateSuccessResponse("Forbidden Access", "role", HttpStatus.FORBIDDEN);
                if (jwtTokenUtil.extractId(externalUseToken.getToken()).equals(id))
                    roleId = 5;
                else
                    return ResponseService.generateSuccessResponse("Forbidden Access", "role", HttpStatus.FORBIDDEN);
            }
            /*else
            {
                roleId=5;
            }*/
            role = roleService.getRoleByRoleId(roleId).getRole_name();
            List<Map<String, Object>> qualificationDetails = qualificationDetailsService.getQualificationDetailsByCustomerId(id, role);


            if (qualificationDetails.isEmpty()) {
                return ResponseService.generateSuccessResponse("Qualification Details list is empty for " + role, qualificationDetails, HttpStatus.OK);
            }
            return ResponseService.generateSuccessResponse("Qualification Details are found for " + role, qualificationDetails, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (CustomerDoesNotExistsException e) {
            exceptionHandling.handleException(e);
            if (role.equalsIgnoreCase(Constant.SERVICE_PROVIDER)) {
                return ResponseService.generateErrorResponse("Service Provider does not exist", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateErrorResponse("Customer does not exist", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}/{qualificationDetailId}")
    public ResponseEntity<?> deleteQualificationDetailById(@PathVariable Long id, @PathVariable Long qualificationDetailId, @RequestHeader(value = "Authorization") String authHeader, @RequestParam(required = false) Boolean extUpdate) throws EntityDoesNotExistsException, CustomerDoesNotExistsException {
        String role = null;
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long userId = jwtTokenUtil.extractId(jwtToken);
            if (roleId == 5 && !userId.equals(id)) {
                return ResponseService.generateErrorResponse("Forbidden", HttpStatus.FORBIDDEN);
            }
            if (extUpdate != null && extUpdate && roleId == 4) {
                if (id == null)
                    return ResponseService.generateErrorResponse("Id not provided", HttpStatus.NOT_FOUND);
                CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, id);
                if (customCustomer == null)
                    return ResponseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
                ExternalUseToken externalUseToken = entityManager.find(ExternalUseToken.class, userId);
                if (externalUseToken == null || externalUseToken.getToken() == null || externalUseToken.getToken().isEmpty())
                    return ResponseService.generateSuccessResponse("Forbidden Access", "role", HttpStatus.FORBIDDEN);
                if (jwtTokenUtil.extractId(externalUseToken.getToken()).equals(id))
                    roleId = 5;
                else
                    return ResponseService.generateSuccessResponse("Forbidden Access", "role", HttpStatus.FORBIDDEN);
            }
//            else
//            {
//                roleId=5;
//            }
            role = roleService.getRoleByRoleId(roleId).getRole_name();
            QualificationDetails qualificationDetailsToDelete = qualificationDetailsService.deleteQualificationDetail(id, qualificationDetailId, role);
            return responseService.generateResponse(HttpStatus.OK, "Qualification Detail is deleted successfully for " + role, qualificationDetailsToDelete);
        } catch (CustomerDoesNotExistsException e) {
            exceptionHandling.handleException(e);
            if (role.equalsIgnoreCase(Constant.SERVICE_PROVIDER)) {
                return ResponseService.generateErrorResponse("Service Provider does not exist", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateErrorResponse("Customer does not exist", HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (EntityDoesNotExistsException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Qualification Details does not exist", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @PutMapping("/update/{id}/{qualificationDetailId}")
    public ResponseEntity<?> updateQualificationDetailById(@PathVariable Long id, @PathVariable Long qualificationDetailId, @Valid @RequestBody UpdateQualificationDto qualification, @RequestParam(value = "boardUniversityOthers", required = false) String boardUniversityOthers, @RequestParam(value = "streamOthers", required = false) String streamOthers, @RequestParam(value = "qualificationOthers", required = false) String qualificationOthers, @RequestParam(value = "institutionOthers", required = false) String institutionOthers, @RequestHeader(value = "Authorization") String authHeader, @RequestHeader(value = "extAuth", required = false) String extAuth, @RequestParam(required = false) Boolean extUp) throws EntityDoesNotExistsException, EntityAlreadyExistsException, ExaminationDoesNotExistsException, CustomerDoesNotExistsException {
        String role = null;
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long userId = jwtTokenUtil.extractId(jwtToken);
            if (roleId == 5 && !userId.equals(id)) {
                return ResponseService.generateErrorResponse("Forbidden", HttpStatus.FORBIDDEN);
            }
            if ((extUp != null && extUp) && roleId == 4) {
                if (id == null)
                    return ResponseService.generateErrorResponse("Id not provided", HttpStatus.NOT_FOUND);
                CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, id);
                if (customCustomer == null)
                    return ResponseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
                ExternalUseToken externalUseToken = entityManager.find(ExternalUseToken.class, userId);
                if (externalUseToken == null || externalUseToken.getToken() == null || externalUseToken.getToken().isEmpty())
                    return ResponseService.generateSuccessResponse("Forbidden Access", "role", HttpStatus.UNAUTHORIZED);
                if (jwtTokenUtil.extractId(externalUseToken.getToken()).equals(id))
                    roleId = 5;
                else
                    return ResponseService.generateSuccessResponse("Forbidden Access", "role", HttpStatus.UNAUTHORIZED);
            } else if ((extUp != null && extUp) && (roleId == 1 || roleId == 2)) {
                jwtToken = authHeader.substring(7);
                roleId = 5;
            }
//            else
//            {
//                roleId=5;
//            }
            role = roleService.getRoleByRoleId(roleId).getRole_name();
            QualificationDetails qualificationDetailsToUpdate = qualificationDetailsService.updateQualificationDetail(id, qualificationDetailId, qualification, boardUniversityOthers, streamOthers, qualificationOthers, institutionOthers, roleId, role);
            if (qualificationDetailsToUpdate.getSubject_ids() != null && !qualificationDetailsToUpdate.getSubject_ids().contains(54L)) {
                qualificationDetailsToUpdate.setOtherSubjects(new ArrayList<String>());
            } else {
                qualificationDetailsToUpdate.setOtherSubjects(qualification.getOtherSubjects());
            }
            entityManager.merge(qualificationDetailsToUpdate);
            return responseService.generateResponse(HttpStatus.OK, "Qualification Detail is updated successfully for " + role, qualificationDetailsToUpdate);
        } catch (CustomerDoesNotExistsException e) {
            exceptionHandling.handleException(e);
            if (role.equalsIgnoreCase(Constant.SERVICE_PROVIDER)) {
                return ResponseService.generateErrorResponse("Service Provider does not exist", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateErrorResponse("Customer does not exist", HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (ExaminationDoesNotExistsException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Qualification does not exist", HttpStatus.NOT_FOUND);
        } catch (EntityDoesNotExistsException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Qualification Details does not exist", HttpStatus.NOT_FOUND);
        } catch (EntityAlreadyExistsException e) {
            return ResponseService.generateErrorResponse("Qualification already exists", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handlesValidationErrors(MethodArgumentNotValidException exception) {
        HttpStatus status;
        List<String> errors = exception.getBindingResult().getFieldErrors()
                .stream().map(FieldError::getDefaultMessage).collect(Collectors.toList());
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("message", errors);
        status = HttpStatus.BAD_REQUEST;
        responseData.put("status_code", 400);
        responseData.put("status", status);
        return ResponseEntity.status(status).body(responseData);
    }

}