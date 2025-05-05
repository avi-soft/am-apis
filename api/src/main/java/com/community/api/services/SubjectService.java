package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.AddStreamDto;
import com.community.api.dto.AddSubjectDto;
import com.community.api.entity.CustomStream;
import com.community.api.entity.CustomSubject;
import com.community.api.entity.Role;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class SubjectService {
    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    @Autowired
    JwtUtil jwtTokenUtil;

    @Autowired
    RoleService roleService;

    public Boolean validiateAuthorization(String authHeader) throws Exception {
        try {
            String jwtToken = authHeader.substring(7);

            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            String role = roleService.getRoleByRoleId(roleId).getRole_name();

            if (role.equals(Constant.SUPER_ADMIN) || role.equals(Constant.ADMIN)) {
                return true;
            }
            return false;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("ERRORS WHILE VALIDATING AUTHORIZATION: " + exception.getMessage() + "\n");
        }
    }

    public void addSubjectToStream(Long streamId, Long subjectId) {
        CustomStream stream = entityManager.find(CustomStream.class, streamId);
        CustomSubject subject = entityManager.find(CustomSubject.class, subjectId);

        if (stream != null && subject != null) {
            stream.getSubjects().add(subject);
            entityManager.merge(stream);
        }
    }

    public List<CustomSubject> getAllSubject() {
        try {

            List<CustomSubject> subjectList = entityManager.createQuery(Constant.GET_ALL_SUBJECT, CustomSubject.class).getResultList();
            return subjectList;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return Collections.emptyList();
        }
    }
    public List<CustomSubject> getSubjectsByStreamIds(Long streamId) {
        try {

            String jpql = """
                SELECT s FROM CustomStream cs 
                JOIN cs.subjects s 
                WHERE cs.streamId = :streamId""";

            List<CustomSubject> subjects = entityManager.createQuery(jpql, CustomSubject.class)
                    .setParameter("streamId", streamId)
                    .getResultList();
            return subjects;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return Collections.emptyList();
        }
    }

    public CustomSubject getSubjectBySubjectId(Long subjectId) {
        try {

            Query query = entityManager.createQuery(Constant.GET_SUBJECT_BY_SUBJECT_ID, CustomSubject.class);
            query.setParameter("subjectId", subjectId);
            List<CustomSubject> subject = query.getResultList();

            if (!subject.isEmpty()) {
                if(subject.get(0).getArchived() == 'Y'){
                    throw new IllegalArgumentException("Subject is already Archived");
                }
                return subject.get(0);
            }
            return null;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException("Illegal Exception Caught: " + illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new IllegalArgumentException("Exception Caught: " + exception.getMessage());
        }
    }

    public List<CustomSubject> getSubjectBySubjectName(String subjectName) throws Exception {
        try {

            Query query = entityManager.createQuery(Constant.GET_SUBJECT_BY_SUBJECT_NAME, CustomSubject.class);
            query.setParameter("subjectName", subjectName);
            List<CustomSubject> subject = query.getResultList();

            return subject;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: "+ exception.getMessage());
        }
    }

    public Boolean validateAddSubjectDto(AddSubjectDto addSubjectDto) throws Exception {
        try{

            if(addSubjectDto.getSubjectName() != null) {
                addSubjectDto.setSubjectName(addSubjectDto.getSubjectName().trim());
            }
            List<CustomSubject> subjects = getSubjectBySubjectName(addSubjectDto.getSubjectName());
            if(subjects != null && !subjects.isEmpty()) {
                throw new IllegalArgumentException("Duplicate Unarchived Subject Name");
            }
            if(addSubjectDto.getSubjectDescription() != null) {
                addSubjectDto.setSubjectDescription(addSubjectDto.getSubjectDescription().trim());
            }
            return true;
        } catch(IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException("ILLEGAL ARGUMENT EXCEPTION OCCURRED: "+ illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: "+ exception.getMessage());
        }

    }

    @Transactional
    public CustomSubject saveSubject(AddSubjectDto addSubjectDto, Long creatorId, Role creatorRole) throws Exception {
        try{
            CustomSubject subject = new CustomSubject();
            subject.setSubjectName(addSubjectDto.getSubjectName());
            subject.setSubjectDescription(addSubjectDto.getSubjectDescription());
            subject.setCreatedDate(new Date());
            subject.setCreatorUserId(creatorId);
            subject.setCreatorRole(creatorRole);
            return entityManager.merge(subject);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: "+ exception.getMessage());
        }
    }

    @Transactional
    public void removeSubjectById(CustomSubject subject) throws Exception {
        try {

            if(subject == null) {
                throw new IllegalArgumentException("No Subject Found");
            }
            subject.setArchived('Y');
            entityManager.merge(subject);

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage());
        }
    }

}
