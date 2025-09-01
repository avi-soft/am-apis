package com.community.api.endpoint.avisoft.controller.Admin;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.CommunicationRequest;
import com.community.api.endpoint.avisoft.controller.ServiceProviderActionController;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.Privileges;
import com.community.api.entity.Role;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.SharedUtilityService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.social.NotAuthorizedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/roles",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
)
public class RoleController {
    @Autowired
    ServiceProviderActionController serviceProviderActionController;
    @Autowired
    private RoleService roleService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private ResponseService responseService;
    @Autowired
    private SharedUtilityService sharedUtilityService;
    @Autowired
    private JwtUtil jwtTokenUtil;

    @PostMapping("add-role")
    public ResponseEntity<?> addRole(@RequestBody Role role) {
        try {
            return roleService.addRole(role);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error aadding role", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Authorize(value = {Constant.roleSuperAdmin, Constant.roleAdmin, Constant.roleServiceProvider})
    @GetMapping("/get-roles")
    public ResponseEntity<?> getRoles() {
        try {
            return responseService.generateSuccessResponse("Roles", roleService.findAllRoleList(), HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return responseService.generateErrorResponse("Some error fetching: " + exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Authorize(value = {Constant.roleSuperAdmin, Constant.roleAdmin})
    @GetMapping("/get-roles-to-assign-tickets")
    public ResponseEntity<?> getRolesToAssignTickets(@RequestHeader(value = "Authorization") String authHeader) {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);

            List<Role> rolesToReturn = new ArrayList<>();

            if (roleId == 1) {
                rolesToReturn = roleService.getRolesForSuperAdmin();  // Role ID 1
            } else if (roleId == 2) {
                rolesToReturn = roleService.getRolesForAdmin();       // Role ID 2
            } else {
                // You can decide how to handle unexpected roleIds
                return responseService.generateErrorResponse("Unauthorized role", HttpStatus.FORBIDDEN);
            }

            return responseService.generateSuccessResponse("Roles", rolesToReturn, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some error fetching: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Authorize(value = {Constant.roleSuperAdmin, Constant.roleAdmin, Constant.roleServiceProvider})
    @GetMapping("/get-roles-available")
    public ResponseEntity<?> getConditionalRoles(@RequestHeader(value = "Authorization") String authHeader) {
        try {
            return ResponseService.generateSuccessResponse("Available roles to assign are : ", roleService.getCondRoles(authHeader), HttpStatus.OK);
        } catch (NotAuthorizedException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Some Exception Occurred: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @Authorize(value = {Constant.roleSuperAdmin})
    @PostMapping("/change-role/{id}/{roleToBeId}")
    public ResponseEntity<?> changeRole(@RequestHeader(value = "Authorization") String authHeader, @PathVariable Long id, @PathVariable Integer roleToBeId) {
        try {
            ResponseEntity<?> response;
            ServiceProviderEntity user = entityManager.find(ServiceProviderEntity.class, id);
            if (user == null)
                return ResponseService.generateErrorResponse("User not found", HttpStatus.BAD_REQUEST);
            List<Role> allRoles = roleService.getCondRoles(authHeader);
            Role role = roleService.getRoleByRoleId(roleToBeId);
            Role prevRole = roleService.getRoleByRoleId(user.getRole());
            if (role == null)
                return ResponseService.generateErrorResponse("Invalid role Selected", HttpStatus.BAD_REQUEST);
            if (!allRoles.contains(role))
                return ResponseService.generateErrorResponse("Not authorized to set role to : " + role.getRole_name(), HttpStatus.BAD_REQUEST);
            if (user.getRole() == 1)
                return ResponseService.generateErrorResponse("Cannot change Super Admin's role", HttpStatus.BAD_REQUEST);
            if (roleToBeId == user.getRole())
                return ResponseService.generateErrorResponse("User already has role : " + role.getRole_name(), HttpStatus.OK);
            switch (roleToBeId) {
                case 1:
                    Privileges privileges = entityManager.find(Privileges.class, Constant.SUPER_ADMIN_PRIVILEGES);
                    if (privileges == null)
                        return ResponseService.generateErrorResponse("Privilege id 4 does not exist in DB", HttpStatus.NOT_FOUND);
                    user.setRole(roleToBeId);
                    user.getPrivileges().add(privileges);
                    user.setToken(null);
                    entityManager.merge(user);
                    response = ResponseService.generateSuccessResponse(user.getFirst_name() + " " + user.getLast_name() + " ID:" + user.getService_provider_id() + "'s role changed from " + prevRole.getRole_name() + " to " + role.getRole_name(), sharedUtilityService.serviceProviderDetailsMap(user, false), HttpStatus.OK);
                    break;
                case 2:
                    user.getPrivileges().clear();
                    user.setRole(roleToBeId);
                    //a static loop
                    for (int i = 1; i <= 31; i++) {
                        if (i == Constant.SUPER_ADMIN_PRIVILEGES)
                            continue;
                        privileges = entityManager.find(Privileges.class, i);
                        user.getPrivileges().add(privileges);
                    }
                    user.setToken(null);
                    entityManager.merge(user);
                    response = ResponseService.generateSuccessResponse(user.getFirst_name() + " " + user.getLast_name() + " ID:" + user.getService_provider_id() + "'s role changed from " + prevRole.getRole_name() + " to " + role.getRole_name(), sharedUtilityService.serviceProviderDetailsMap(user, false), HttpStatus.OK);
                    break;
                case 3:
                    user.getPrivileges().clear();
                    user.setRole(roleToBeId);
                    user.setToken(null);
                    entityManager.merge(user);
                    response = ResponseService.generateSuccessResponse(user.getFirst_name() + " " + user.getLast_name() + " ID:" + user.getService_provider_id() + "'s role changed from " + prevRole.getRole_name() + " to " + role.getRole_name(), sharedUtilityService.serviceProviderDetailsMap(user, false), HttpStatus.OK);
                    break;
                case 4:
                    user.getPrivileges().clear();
                    user.setRole(roleToBeId);
                    user.setToken(null);
                    entityManager.merge(user);
                    response = ResponseService.generateSuccessResponse(user.getFirst_name() + " " + user.getLast_name() + " ID:" + user.getService_provider_id() + "'s role changed from " + prevRole.getRole_name() + " to " + role.getRole_name(), sharedUtilityService.serviceProviderDetailsMap(user, false), HttpStatus.OK);
                    break;
                default:
                    response = ResponseService.generateErrorResponse("Invalid action", HttpStatus.BAD_REQUEST);
            }
            // Call communicate only if status is 200 OK
            if (response.getStatusCode() == HttpStatus.OK) {
                CommunicationRequest communicationRequest = new CommunicationRequest();
                communicationRequest.setSubject("Role Change");
                communicationRequest.setUserIds(id.toString());
                communicationRequest.setModes("1");
                communicationRequest.setContentText(
                        "Hello " + user.getFirst_name() + " " + user.getLast_name() + ",\n\n" +
                                "We would like to inform you that your role has been updated by the system administrator.\n\n" +
                                "Previous Role: " + prevRole.getRole_name() + "\n" +
                                "New Role: " + role.getRole_name() + "\n\n" +
                                "Please log in to the respective portal to continue with your updated responsibilities.\n\n" +
                                "Best regards,\n" +
                                "System Administrator"
                );

                communicateWithCustomersAsync(communicationRequest, roleToBeId, authHeader);

            }
            return response;
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Some error occurred" + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Async
    public void communicateWithCustomersAsync(CommunicationRequest communicationRequest, Integer roleToBeId, String authHeader) {
        try {
            ResponseEntity<?> response = serviceProviderActionController.communicateWithCustomersDummy(communicationRequest, roleToBeId, authHeader, true);
            System.out.println(response);
        } catch (Exception e) {
            // Log the error or handle it as necessary
        }
    }

}
