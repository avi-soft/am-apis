package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomAdmin;
import com.community.api.entity.Privileges;
import com.community.api.entity.Role;
import com.community.api.services.exception.ExceptionHandlingImplement;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PrivilegeService {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private ResponseService responseService;
    @Autowired
    JwtUtil jwtTokenUtil;
    @Autowired
    RoleService roleService;

    @Transactional
    public ResponseEntity<?> assignPrivilegeToMultipleUsers(@RequestParam Integer privilege_id, @RequestParam List<Long> ids, @RequestParam Integer role_id) {
        try {
            if (privilege_id == null || ids == null || ids.isEmpty() || role_id == null) {
                return responseService.generateErrorResponse("Empty or invalid details", HttpStatus.BAD_REQUEST);
            }

            Privileges privilege = entityManager.find(Privileges.class, privilege_id);
            if (privilege == null)
                return responseService.generateErrorResponse("Privilege not found", HttpStatus.NOT_FOUND);

            Role role = entityManager.find(Role.class, role_id);
            if (role == null)
                return responseService.generateErrorResponse("Specified role not found", HttpStatus.NOT_FOUND);

            List<ServiceProviderEntity> serviceProviderEntities=new ArrayList<>();
            List<CustomAdmin> customAdminList=new ArrayList<>();
            for (Long id : ids) {
                if (role.getRole_name().equals("SERVICE_PROVIDER"))
                {
                    ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, id);
                    if (serviceProvider == null) {
                        return responseService.generateErrorResponse("Service Provider with ID " + id + " not found", HttpStatus.NOT_FOUND);
                    }
                    List<Privileges> spPrivileges = serviceProvider.getPrivileges();
                    if (!spPrivileges.contains(privilege)) {
                        spPrivileges.add(privilege);
                        serviceProvider.setPrivileges(spPrivileges);
                        entityManager.merge(serviceProvider);
                    }
                    serviceProviderEntities.add(serviceProvider);
                }
                else if (role.getRole_name().equals(Constant.ADMIN))
                {
                    CustomAdmin customAdmin = entityManager.find(CustomAdmin.class, id);
                    if (customAdmin == null) {
                        return responseService.generateErrorResponse("Custom Admin with ID " + id + " not found", HttpStatus.NOT_FOUND);
                    }
                    if (customAdmin.getRole() ==2) {
                        List<Privileges> customAdminPrivileges = customAdmin.getPrivileges();
                        if (!customAdminPrivileges.contains(privilege)) {
                            customAdminPrivileges.add(privilege);
                            customAdmin.setPrivileges(customAdminPrivileges);
                            entityManager.merge(customAdmin);
                        }
                        customAdminList.add(customAdmin);
                    }
                    else{
                       return responseService.generateErrorResponse("Custom Admin with ID " + id + " does not have "+ role.getRole_name()+" role", HttpStatus.BAD_REQUEST);
                    }
                }
                else if(role.getRole_name().equals(Constant.SUPER_ADMIN) ) {
                    CustomAdmin customAdmin = entityManager.find(CustomAdmin.class, id);
                    if (customAdmin == null) {
                        return responseService.generateErrorResponse("Custom Admin with ID " + id + " not found", HttpStatus.NOT_FOUND);
                    }
                    if (customAdmin.getRole() ==1) {
                        List<Privileges> customAdminPrivileges = customAdmin.getPrivileges();
                        if (!customAdminPrivileges.contains(privilege)) {
                            customAdminPrivileges.add(privilege);
                            customAdmin.setPrivileges(customAdminPrivileges);
                            entityManager.merge(customAdmin);
                        }
                        customAdminList.add(customAdmin);
                    }
                    else{
                        return responseService.generateErrorResponse("Custom Admin with ID " + id + " does not have "+ role.getRole_name()+" role", HttpStatus.BAD_REQUEST);
                    }
                }
                else if(role.getRole_name().equals(Constant.roleAdminServiceProvider))
                {
                    CustomAdmin customAdmin = entityManager.find(CustomAdmin.class, id);
                    if (customAdmin == null) {
                        return responseService.generateErrorResponse("Custom Admin with ID " + id + " not found", HttpStatus.NOT_FOUND);
                    }
                    if (customAdmin.getRole() ==3) {
                        List<Privileges> customAdminPrivileges = customAdmin.getPrivileges();
                        if (!customAdminPrivileges.contains(privilege)) {
                            customAdminPrivileges.add(privilege);
                            customAdmin.setPrivileges(customAdminPrivileges);
                            entityManager.merge(customAdmin);
                        }
                        customAdminList.add(customAdmin);
                    }
                    else{
                        return responseService.generateErrorResponse("Custom Admin with ID " + id + " does not have "+ role.getRole_name()+" role", HttpStatus.BAD_REQUEST);
                    }
                }
                else{
                    return responseService.generateErrorResponse("No valid records found for given details", HttpStatus.NOT_FOUND);
                }
            }
            if (role.getRole_name().equals("SERVICE_PROVIDER")) {
                return responseService.generateSuccessResponse("Privilege assigned successfully to the specified users", serviceProviderEntities, HttpStatus.OK);
            }
                return responseService.generateSuccessResponse("Privilege assigned successfully to the specified users", customAdminList, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error assigning privilege", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<?> removePrivilege(@RequestParam Integer privilege_id, @RequestParam Long id, @RequestParam Integer role_id) {
        try {
            if (privilege_id == null || id == null || role_id == null) {
                return responseService.generateErrorResponse("Empty details", HttpStatus.BAD_REQUEST);
            }
            Privileges privilege = entityManager.find(Privileges.class, privilege_id);
            if (privilege == null)
                return responseService.generateErrorResponse("Privilege not found", HttpStatus.NOT_FOUND);
            Role role = entityManager.find(Role.class, role_id);
            if (role == null)
                return responseService.generateErrorResponse("Specified role not found", HttpStatus.NOT_FOUND);
            if (role.getRole_name().equals("SERVICE_PROVIDER")) {
                ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, id);
                if (serviceProvider == null) {
                    return responseService.generateErrorResponse("Error removing privilege : " + role.getRole_name().equals("SERVICE_PROVIDER") + " not found", HttpStatus.NOT_FOUND);
                }
                List<Privileges> spPrivileges = serviceProvider.getPrivileges();
                if (spPrivileges.contains(privilege))
                    spPrivileges.remove(privilege);
                else
                    return responseService.generateErrorResponse("Privilege not assigned", HttpStatus.UNAUTHORIZED);
                serviceProvider.setPrivileges(spPrivileges);
                entityManager.merge(serviceProvider);
                return responseService.generateSuccessResponse("Privilege removed successfully",serviceProvider, HttpStatus.OK);
            } else
                return responseService.generateErrorResponse("No records found for given details", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error removing ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<?> createPrivilege(Privileges privilege) {
        try {
            if (privilege.getPrivilege_name() == null || privilege.getDescription() == null)
                return responseService.generateErrorResponse("Incomplete details", HttpStatus.BAD_REQUEST);
                int count=(int)findCount();
                privilege.setPrivilege_id(++count);
                entityManager.persist(privilege);
            return responseService.generateSuccessResponse("Privilege created successfully",privilege, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error removing ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    public long findCount() {
        String queryString = Constant.GET_PRIVILEGES_COUNT;
        TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);
        return query.getSingleResult();
    }
    public List<Integer> getPrivilege(Long userId) {
        try {

            Query query = entityManager.createNativeQuery(Constant.SERVICE_PROVIDER_PRIVILEGE);
            query.setParameter("serviceProviderId", userId);

            return query.getResultList();

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return Collections.emptyList();
        }
    }

    public List<Integer> getCustomAdminPrivilegeQuery(Long customAdminId)
    {
        try
        {
            Query query= entityManager.createNativeQuery(Constant.CUSTOM_ADMIN_PRIVILEGE);
            query.setParameter("custom_admin_id", customAdminId);
            return query.getResultList();
        }
        catch (Exception e)
        {
            exceptionHandling.handleException(e);
            return Collections.emptyList();
        }
    }
    public List<Privileges> findAllPrivilegeList() {
        TypedQuery<Privileges> query = entityManager.createQuery(Constant.GET_ALL_PRIVILEGES,Privileges.class);
        return query.getResultList();
    }
    public List<Privileges> getServiceProviderPrivilege(Long userId) {
        try {
            List<Integer>listOfPrivilegeId=getPrivilege(userId);
            List<Privileges>listOfPrivileges=new ArrayList<>();
            for(int privilege_id:listOfPrivilegeId)
            {
                Privileges privilege=entityManager.find(Privileges.class,privilege_id);
                if(privilege!=null)
                    listOfPrivileges.add(privilege);
            }
            return listOfPrivileges;

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return Collections.emptyList();
        }
    }

    public List<Privileges> getCustomAdminPrivilege(Long customAdminId)
    {
        try
        {
            List<Integer>listOfPrivilegeId=getCustomAdminPrivilegeQuery(customAdminId);
            List<Privileges>listOfPrivileges=new ArrayList<>();
            for(int  privilege_id:listOfPrivilegeId)
            {
                Privileges privileges= entityManager.find(Privileges.class,privilege_id);
                if(privileges!=null)
                {
                    listOfPrivileges.add(privileges);
                }
            }
            return listOfPrivileges;
        }
        catch (Exception e)
        {
            exceptionHandling.handleException(e);
            return Collections.emptyList();
        }
    }

    public boolean checkAuthority( String authHeader,String privilegeToAuthorize)
    {
        String jwtToken = authHeader.substring(7);
        Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
        String role = roleService.getRoleByRoleId(roleId).getRole_name();
        Long userId = null;
        if (role.equals(Constant.SERVICE_PROVIDER))
        {
            userId = jwtTokenUtil.extractId(jwtToken);
            List<Privileges> privileges = getServiceProviderPrivilege(userId);

            for (Privileges privilege : privileges) {
                if (privilege.getPrivilege_name().equals(privilegeToAuthorize)) {
                    return true;
                }
            }
        }
        else if(role.equals(Constant.SUPER_ADMIN)|| role.equals(Constant.ADMIN) || role.equals(Constant.roleAdminServiceProvider))
        {
            userId=jwtTokenUtil.extractId(jwtToken);
            List<Privileges> privileges = getCustomAdminPrivilege(userId);
            for(Privileges privilege : privileges)
            {
                if(privilege.getPrivilege_name().equals(privilegeToAuthorize))
                {
                    return true;
                }
            }
        }
        return false;
    }
}
