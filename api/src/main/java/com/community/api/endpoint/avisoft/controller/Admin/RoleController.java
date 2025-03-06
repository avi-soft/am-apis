package com.community.api.endpoint.avisoft.controller.Admin;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.Privileges;
import com.community.api.entity.Role;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.SharedUtilityService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.services.exception.InvalidFileTypeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.ParameterResolutionDelegate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.social.NotAuthorizedException;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/roles",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class RoleController {
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
    public ResponseEntity<?> addRole(@RequestBody Role role)
    {
        try{
            return roleService.addRole(role);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error aadding role", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/get-roles")
    public ResponseEntity<?> getRoles() {
        try{
            return responseService.generateSuccessResponse("Roles",roleService.findAllRoleList(),HttpStatus.OK);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some error fetching: "+e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/get-roles-available")
    public ResponseEntity<?> getConditionalRoles(@RequestHeader(value = "Authorization") String authHeader)
    {
        try {
            return ResponseService.generateSuccessResponse("Available roles to assign are : ", roleService.getCondRoles(authHeader), HttpStatus.OK);
        }catch (NotAuthorizedException e)
        {
            return ResponseService.generateErrorResponse(e.getMessage(),HttpStatus.UNAUTHORIZED);
        }
    }
    @Transactional
    @Authorize(value = {Constant.roleServiceProvider,Constant.roleAdmin,Constant.roleSuperAdmin,Constant.roleAdminServiceProvider})
    @PostMapping("/change-role/{id}/{roleToBeId}")
    public ResponseEntity<?> changeRole(@RequestHeader(value = "Authorization") String authHeader,@PathVariable Long id,@PathVariable Integer roleToBeId)
    {
        try{
        ServiceProviderEntity user=entityManager.find(ServiceProviderEntity.class,id);
        if(user==null)
             return ResponseService.generateErrorResponse("User not found",HttpStatus.BAD_REQUEST);
        List<Role>allRoles= roleService.getCondRoles(authHeader);
        Role role=roleService.getRoleByRoleId(roleToBeId);
        Role prevRole=roleService.getRoleByRoleId(user.getRole());
        if(role==null)
            return ResponseService.generateErrorResponse("Invalid role Selected",HttpStatus.BAD_REQUEST);
        if(!allRoles.contains(role))
            return ResponseService.generateErrorResponse("Not authorized to set role to : "+role.getRole_name(),HttpStatus.BAD_REQUEST);
        if(user.getRole() == 1)
            return ResponseService.generateErrorResponse("Cannot change Super Admin's role",HttpStatus.BAD_REQUEST);
        if (roleToBeId==user.getRole())
            return ResponseService.generateErrorResponse("User already has role : "+role.getRole_name(),HttpStatus.BAD_REQUEST);
        switch (roleToBeId)
        {
            case 1:
                Privileges privileges=entityManager.find(Privileges.class, Constant.SUPER_ADMIN_PRIVILEGES);
                if (privileges==null)
                    return ResponseService.generateErrorResponse("Privilege id 4 does not exist in DB",HttpStatus.NOT_FOUND);
                user.setRole(roleToBeId);
                user.getPrivileges().add(privileges);
                user.setToken(null);
                entityManager.merge(user);
                return ResponseService.generateSuccessResponse(user.getFirst_name()+" "+user.getLast_name()+" ID:"+user.getService_provider_id()+"'s role changed from "+prevRole.getRole_name()+" to "+role.getRole_name(),sharedUtilityService.serviceProviderDetailsMap(user),HttpStatus.OK);
            case 2:
                user.getPrivileges().clear();
                user.setRole(roleToBeId);
                //a static loop
                for(int i=1;i<=31;i++)
                {
                    if(i==Constant.SUPER_ADMIN_PRIVILEGES)
                        continue;
                    privileges=entityManager.find(Privileges.class,i);
                    user.getPrivileges().add(privileges);
                }
                user.setToken(null);
                entityManager.merge(user);
                return ResponseService.generateSuccessResponse(user.getFirst_name()+" "+user.getLast_name()+" ID:"+user.getService_provider_id()+"'s role changed from "+prevRole.getRole_name()+" to "+role.getRole_name(),sharedUtilityService.serviceProviderDetailsMap(user),HttpStatus.OK);
            case 3:
                user.getPrivileges().clear();
                user.setRole(roleToBeId);
                user.setToken(null);
                entityManager.merge(user);
                return ResponseService.generateSuccessResponse(user.getFirst_name()+" "+user.getLast_name()+" ID:"+user.getService_provider_id()+"'s role changed from "+prevRole.getRole_name()+" to "+role.getRole_name(),sharedUtilityService.serviceProviderDetailsMap(user),HttpStatus.OK);
            case 4:
                user.getPrivileges().clear();
                user.setRole(roleToBeId);
                user.setToken(null);
                entityManager.merge(user);
                return ResponseService.generateSuccessResponse(user.getFirst_name()+" "+user.getLast_name()+" ID:"+user.getService_provider_id()+"'s role changed from "+prevRole.getRole_name()+" to "+role.getRole_name(),sharedUtilityService.serviceProviderDetailsMap(user),HttpStatus.OK);
            default:
                return ResponseService.generateErrorResponse("Invalid action",HttpStatus.BAD_REQUEST);

        }
    }catch (Exception exception)
        {
            return ResponseService.generateErrorResponse("Some error occured"+exception.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
