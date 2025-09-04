package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.entity.Institution;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
public class InstitutionService
{
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandlingService;
    @Autowired
    private InstitutionService institutionService;
    @Autowired
    private ResponseService responseService;
    @Autowired
    private JwtUtil jwtTokenUtil;
    @Autowired
    RoleService roleService;

    @Transactional
    public Institution addInstitutions(Institution institution, String authHeader) {
        try {
            String jwtToken = authHeader.substring(7);

            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);

            String role = roleService.getRoleByRoleId(roleId).getRole_name();
            List<Institution> savedInstitutions = new ArrayList<>();

            Institution institutionToBeSaved =new Institution();
            long id = findCount() + 1;
            if (institution.getInstitution_name() == null || institution.getInstitution_name().trim().isEmpty()) {
                throw new IllegalArgumentException("Institution name cannot be empty or consist only of whitespace");
            }
            if (institution.getInstitution_code() == null || institution.getInstitution_code().trim().isEmpty()) {
                throw new IllegalArgumentException("Institution code cannot be empty or consist only of whitespace");
            }

            if (institution.getInstitution_address() == null || institution.getInstitution_address().trim().isEmpty()) {
                throw new IllegalArgumentException("Institution address cannot be empty or consist only of whitespace");
            }
            if (!institution.getInstitution_address().matches("^[#a-zA-Z0-9].*")) {
                throw new IllegalArgumentException("Institution address must start with #, letter, or number");
            }

            if (institution.getInstitution_address().matches(".*[~`!@$%^*\\\\|;<>?].*")) {
                throw new IllegalArgumentException("Institution address contains invalid special characters");
            }

            if (institution.getInstitution_address().matches("^[()_\\-{}\\[\\]/\":&,. \n]+$")) {
                throw new IllegalArgumentException("Institution address cannot contain only special characters");
            }
            if (institution.getInstitution_address().matches("^[0-9]+$")) {
                throw new IllegalArgumentException("Institution address cannot contain only numbers");
            }

            if (!institution.getInstitution_name().matches("^[a-zA-Z][a-zA-Z ]*$")) {
                throw new IllegalArgumentException("Institution name cannot contain numeric values, special characters, or leading spaces");
            }
            if (!institution.getInstitution_code().matches("^[a-zA-Z][a-zA-Z ]*$")) {
                throw new IllegalArgumentException("Institution code cannot contain numeric values, special characters, or leading spaces");
            }

            List<Institution> institutions = getAllInstitutions();
            for (Institution existingInstitution : institutions) {
                if (existingInstitution.getInstitution_name().equalsIgnoreCase(institution.getInstitution_name())) {
                    throw new IllegalArgumentException("Duplicate name not allowed");
                }
                if (existingInstitution.getInstitution_code().equalsIgnoreCase(institution.getInstitution_code())) {
                    throw new IllegalArgumentException("Duplicate code not allowed");
                }
            }
            Query query = entityManager.createQuery("SELECT MAX(i.sortOrder) FROM Institution i WHERE i.sortOrder <> 1000000");
            Long sortOrder = (Long) query.getSingleResult();
            institutionToBeSaved.setInstitution_name(institution.getInstitution_name());
            institutionToBeSaved.setInstitution_address(institution.getInstitution_address());
            institutionToBeSaved.setInstitution_code(institution.getInstitution_code());
            institutionToBeSaved.setSortOrder(sortOrder+1);
            institutionToBeSaved.setCreated_by(role);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String now = LocalDateTime.now().format(formatter);
            institutionToBeSaved.setCreated_date(now);
            institutionToBeSaved.setArchived(false);
            entityManager.persist(institutionToBeSaved);

            return institutionToBeSaved;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw exception;
        }
    }

    public List<Institution> getAllInstitutions() {
        try {
            TypedQuery<Institution> query = entityManager.createQuery(Constant.FIND_ALL_INSTITUTION_QUERY, Institution.class);
            query.setParameter("archived", false); // or true, depending on what you want
            return query.getResultList();
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw exception;
        }
    }


    //need to be change here
    public long findCount() {
        try {
            String queryString = Constant.GET_INSTITUTION_COUNT;
            TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);
            return query.getSingleResult();
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw exception;
        }
    }

    @Transactional
    public Institution updateInstitution(Long institutionId, Institution institution,String authHeader){
        try {
            String jwtToken = authHeader.substring(7);

            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);

            String role = roleService.getRoleByRoleId(roleId).getRole_name();
            Institution institutionToUpdate = entityManager.find(Institution.class, institutionId);
            if (institutionToUpdate == null) {
                throw new IllegalArgumentException("Institution with id \" + institutionId + \" not found");
            }
            List<Institution> institutions = getAllInstitutions();
            if (Objects.nonNull(institution.getInstitution_name())) {
                if (!institution.getInstitution_name().matches("^[a-zA-Z][a-zA-Z ]*$")) {
                    throw new IllegalArgumentException("Institution name cannot contain numeric values, special characters or leading spaces");
                }
                for (Institution existingInstitution : institutions) {
                    if (existingInstitution.getInstitution_name().equalsIgnoreCase(institution.getInstitution_name()) && !existingInstitution.getInstitution_id().equals(institutionId)) {
                        throw new IllegalArgumentException("Duplicate name not allowed");
                    }
                }
                institutionToUpdate.setInstitution_name(institution.getInstitution_name());
            }
            if (Objects.nonNull(institution.getInstitution_code())) {
                if (!institution.getInstitution_code().matches("^[a-zA-Z][a-zA-Z ]*$")) {
                    throw new IllegalArgumentException("Institution code cannot contain numeric values, special characters or leading spaces");
                }
                for (Institution existingInstitution : institutions) {
                    if (existingInstitution.getInstitution_code().equalsIgnoreCase(institution.getInstitution_code()) && !existingInstitution.getInstitution_id().equals(institutionId)) {
                        throw new IllegalArgumentException("Duplicate code not allowed");
                    }
                }
                institutionToUpdate.setInstitution_code(institution.getInstitution_code());
            }
            if (Objects.nonNull(institution.getInstitution_address())) {
                if (!institution.getInstitution_address().matches("^[#a-zA-Z0-9].*")) {
                    throw new IllegalArgumentException("Institution address must start with #, letter, or number");
                }

                if (institution.getInstitution_address().matches(".*[~`!@$%^*\\\\|;<>?].*")) {
                    throw new IllegalArgumentException("Institution address contains invalid special characters");
                }

                if (institution.getInstitution_address().matches("^[()_\\-{}\\[\\]/\":&,. \n]+$")) {
                    throw new IllegalArgumentException("Institution address cannot contain only special characters");
                }
                if (institution.getInstitution_address().matches("^[0-9]+$")) {
                    throw new IllegalArgumentException("Institution address cannot contain only numbers");
                }
                institutionToUpdate.setInstitution_address(institution.getInstitution_address());
            }
            if (institution.getCreated_date() != null || institution.getCreated_by() != null) {
                throw new IllegalArgumentException("Created Date and Created By cannot be modified");
            }
            institutionToUpdate.setModified_by(role);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String now = LocalDateTime.now().format(formatter);
            institutionToUpdate.setModified_date(now);
            return entityManager.merge(institutionToUpdate);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw exception;
        }
    }
    @Transactional
    public Institution manageInstitutionArchiveStatus(Long id, Boolean archive) {
        try {
            Institution institution = entityManager.find(Institution.class, id);
            if (institution == null) {
                throw new IllegalArgumentException("Institution not found with id: " + id);
            }

            if (archive == null) {
                throw new IllegalArgumentException("Archive status must be provided (true/false)Archive status must be provided (true/false)");
            }

            if (institution.getArchived().equals(archive)) {
                throw new IllegalArgumentException("Institution already " + (archive ? "archived" : "unarchived"));
            }

            institution.setArchived(archive);
            entityManager.merge(institution);
            return institution;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw exception;
        }
    }

}
