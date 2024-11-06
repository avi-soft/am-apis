package com.community.api.endpoint.avisoft.controller.Admin;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomAdmin;
import com.community.api.entity.Privileges;
import com.community.api.entity.Role;
import com.community.api.services.PrivilegeService;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

@RestController
@RequestMapping(value = "/privileges",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class PrivilegesController {
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private PrivilegeService privilegeService;
    @Autowired
    private ResponseService responseService;
    @Autowired
    private JwtUtil jwtTokenUtil;
    @Autowired
    private RoleService roleService;
    @Autowired
    private EntityManager entityManager;
    @Transactional
    @RequestMapping(value = "assign-privilege", method = RequestMethod.POST)
    public ResponseEntity<?> assignPrivilege(@RequestParam int privilege_id, @RequestParam List<Long> ids, @RequestParam int role_id) {
        try {
           return privilegeService.assignPrivilegeToMultipleUsers(privilege_id,ids,role_id);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error assigning privilege", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "remove-privilege", method = RequestMethod.PATCH)
    public ResponseEntity<?> removePrivilege(@RequestParam int privilege_id, @RequestParam Long id, @RequestParam int role_id) {
        try {
            return privilegeService.removePrivilege(privilege_id, id, role_id);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error removing ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    @RequestMapping(value = "create-privilege", method = RequestMethod.POST)
    public ResponseEntity<?> createPrivilege(@RequestBody Privileges privilege) {
        try {

            return privilegeService.createPrivilege(privilege);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error removing ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "get-privileges-by-id", method = RequestMethod.GET)
    public ResponseEntity<?> getAllPrivilegesOfUsers(@RequestParam Long userId, @RequestParam Integer roleId) {
        try {
            if ( roleId == null || userId==null) {
                return responseService.generateErrorResponse("Empty or invalid details", HttpStatus.BAD_REQUEST);
            }
            Role role = entityManager.find(Role.class, roleId);
            if (role == null)
                return responseService.generateErrorResponse("Specified role not found", HttpStatus.NOT_FOUND);

            if (role.getRole_name().equals("SERVICE_PROVIDER")) {
                ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, userId);
                if (serviceProvider == null) {
                    return responseService.generateErrorResponse("Service Provider with ID " + userId + " not found", HttpStatus.NOT_FOUND);
                }
                System.out.println(serviceProvider.getService_provider_id());
                List<Privileges>serviceProviderPrivileges= privilegeService.getServiceProviderPrivilege(userId);
                if(serviceProviderPrivileges.isEmpty())
                {
                    return responseService.generateSuccessResponse("Privilege list of Service Provider with id "+userId+" is empty",serviceProviderPrivileges,HttpStatus.OK);
                }
                return responseService.generateSuccessResponse("Privilege list of Service Provider with id "+userId+" is retrieved successfully",serviceProviderPrivileges,HttpStatus.OK);
            }

            else if(role.getRole_name().equals(Constant.ADMIN)) {
                CustomAdmin customAdmin = entityManager.find(CustomAdmin.class, userId);
                if (customAdmin == null) {
                    return responseService.generateErrorResponse("Custom Admin with ID " + userId + " not found", HttpStatus.NOT_FOUND);
                }
                if (customAdmin.getRole() == 2) {
                    List<Privileges> customAdminPrivileges = privilegeService.getCustomAdminPrivilege(userId);
                    if (customAdminPrivileges.isEmpty()) {
                        return responseService.generateSuccessResponse("Privilege list of Service Provider with id " + userId + " is emtpy", customAdminPrivileges, HttpStatus.OK);
                    }
                    return responseService.generateSuccessResponse("Privilege list of Custom Admin with id " + userId + " is retrieved successfully", customAdminPrivileges, HttpStatus.OK);
                }
                else{
                    return responseService.generateErrorResponse("Custom Admin with ID " + userId + " does not have "+ role.getRole_name()+" role", HttpStatus.BAD_REQUEST);
                }
            }
            else if(role.getRole_name().equals(Constant.SUPER_ADMIN)) {
                CustomAdmin customAdmin = entityManager.find(CustomAdmin.class, userId);
                if (customAdmin == null) {
                    return responseService.generateErrorResponse("Custom Admin with ID " + userId + " not found", HttpStatus.NOT_FOUND);
                }
                if (customAdmin.getRole() == 1) {
                    List<Privileges> customAdminPrivileges = privilegeService.getCustomAdminPrivilege(userId);
                    if (customAdminPrivileges.isEmpty()) {
                        return responseService.generateSuccessResponse("Privilege list of Service Provider with id " + userId + " is emtpy", customAdminPrivileges, HttpStatus.OK);
                    }
                    return responseService.generateSuccessResponse("Privilege list of Custom Admin with id " + userId + " is retrieved successfully", customAdminPrivileges, HttpStatus.OK);
                }
                else{
                    return responseService.generateErrorResponse("Custom Admin with ID " + userId + " does not have "+ role.getRole_name()+" role", HttpStatus.BAD_REQUEST);
                }
            }
            //else for adminServiceProvider
            CustomAdmin customAdmin = entityManager.find(CustomAdmin.class, userId);
            if (customAdmin == null) {
                return responseService.generateErrorResponse("Custom Admin with ID " + userId + " not found", HttpStatus.NOT_FOUND);
            }
            if (customAdmin.getRole() == 3) {
                List<Privileges> customAdminPrivileges = privilegeService.getCustomAdminPrivilege(userId);
                if (customAdminPrivileges.isEmpty()) {
                    return responseService.generateSuccessResponse("Privilege list of Service Provider with id " + userId + " is emtpy", customAdminPrivileges, HttpStatus.OK);
                }
                return responseService.generateSuccessResponse("Privilege list of Custom Admin with id " + userId + " is retrieved successfully", customAdminPrivileges, HttpStatus.OK);
            }
            else{
                return responseService.generateErrorResponse("Custom Admin with ID " + userId + " does not have "+ role.getRole_name()+" role", HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error retrieving list of privileges ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

