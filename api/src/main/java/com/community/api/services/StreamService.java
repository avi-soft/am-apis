package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.AddStreamDto;
import com.community.api.entity.CustomStream;
import com.community.api.entity.CustomSubject;
import com.community.api.entity.Qualification;
import com.community.api.entity.Role;
import com.community.api.services.exception.ExceptionHandlingService;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.community.api.component.Constant.FIND_ALL_QUALIFICATIONS_QUERY;

@Service
public class StreamService {

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

    public void addStreamToQualification(Integer qualificationId, Long streamId) {
        Qualification qualification = entityManager.find(Qualification.class, qualificationId);
        CustomStream stream = entityManager.find(CustomStream.class, streamId);

        if (qualification != null && stream != null) {
            qualification.getStreams().add(stream);
            entityManager.merge(qualification);
        }
    }

    public List<CustomStream> getAllStream() {
        try {

            List<CustomStream> streamList = entityManager.createQuery(Constant.GET_ALL_STREAM, CustomStream.class).getResultList();
            return streamList;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return Collections.emptyList();
        }
    }

    public List<CustomStream> getStreamByQualificationId(Integer qualificationId) {
        try {
            List<CustomStream> streamList = entityManager.createQuery(
                            "SELECT s FROM Qualification q JOIN q.streams s WHERE q.qualification_id = :qualificationId",
                            CustomStream.class)
                    .setParameter("qualificationId", qualificationId)
                    .getResultList();
            return streamList;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return Collections.emptyList();
        }
    }

    public CustomStream getStreamByStreamId(Long streamId) {
        try {

            Query query = entityManager.createQuery(Constant.GET_STREAM_BY_STREAM_ID, CustomStream.class);
            query.setParameter("streamId", streamId);
            List<CustomStream> stream = query.getResultList();

            if (!stream.isEmpty()) {
                if(stream.get(0).getArchived() == 'Y'){
                    throw new IllegalArgumentException("Subject is already Archived");
                }
                return stream.get(0);
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

    public List<CustomStream> getStreamByStreamName(String streamName) throws Exception {
        try {

            Query query = entityManager.createQuery(Constant.GET_STREAM_BY_STREAM_NAME, CustomStream.class);
            query.setParameter("streamName", streamName);
            List<CustomStream> stream = query.getResultList();

            return stream;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: "+ exception.getMessage());
        }
    }

    public Boolean validateAddStreamDto(AddStreamDto addStreamDto) throws Exception {
        try{

            if(addStreamDto.getStreamName() != null) {
                addStreamDto.setStreamName(addStreamDto.getStreamName().trim());
            }
            List<CustomStream> streams = getStreamByStreamName(addStreamDto.getStreamName());
            if(streams != null && !streams.isEmpty()) {
                throw new IllegalArgumentException("Duplicate Unarchived Stream Name");
            }
            if(addStreamDto.getStreamDescription() != null) {
                addStreamDto.setStreamDescription(addStreamDto.getStreamDescription().trim());
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
    public CustomStream saveStream(AddStreamDto addStreamDto, Long creatorId, Role creatorRole) throws Exception {
        try{
            CustomStream stream = new CustomStream();
            stream.setStreamName(addStreamDto.getStreamName());
            stream.setStreamDescription(addStreamDto.getStreamDescription());
            stream.setCreatedDate(new Date());
            stream.setCreatorUserId(creatorId);
            stream.setCreatorRole(creatorRole);
            return entityManager.merge(stream);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: "+ exception.getMessage());
        }
    }

    @Transactional
    public void removeStreamById(CustomStream stream) throws Exception {
        try {

            if(stream == null) {
                throw new IllegalArgumentException("No Stream Found");
            }
            stream.setArchived('Y');
            entityManager.merge(stream);

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage());
        }
    }
}
