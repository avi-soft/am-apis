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
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    SharedUtilityService sharedUtilityService;

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

    public List<CustomStream> getAllStream(Boolean archived) {
        try {
            Query query=entityManager.createQuery(Constant.GET_ALL_STREAM, CustomStream.class);
            if(archived)
                query.setParameter("archived",'Y');
            else
                query.setParameter("archived",'N');
            List<CustomStream> streamList = query.getResultList();
            return streamList;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return Collections.emptyList();
        }
    }

    public List<CustomStream> getAllStreamArchiveNonArchive( ){
        try {
            Query query=entityManager.createQuery(Constant.GET_ALL_STREAM_ARCHIVE_NONARCHIVE, CustomStream.class);
            List<CustomStream> streamList = query.getResultList();
            return streamList;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return Collections.emptyList();
        }
    }

    public List<CustomStream> getStreamByQualificationId(Integer qualificationId) {
        try {
            List<CustomStream> streamList = entityManager.createQuery(
                            "SELECT s FROM Qualification q JOIN q.streams s WHERE q.qualification_id = :qualificationId AND s.archived = 'N' ORDER BY s.sortOrder ASC",
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
    public CustomStream saveStream(AddStreamDto addStreamDto, Long creatorId, Role creatorRole)
            throws IllegalArgumentException, Exception {

        try {
            // 1. Validate input
            if (addStreamDto == null) {
                throw new IllegalArgumentException("Stream data cannot be null");
            }

            // 2. Check for duplicate stream name (case-insensitive)
            TypedQuery<Long> duplicateCheck = entityManager.createQuery(
                    "SELECT COUNT(s) FROM CustomStream s WHERE LOWER(s.streamName) = LOWER(:name)",
                    Long.class);
            duplicateCheck.setParameter("name", addStreamDto.getStreamName());

            if (duplicateCheck.getSingleResult() > 0) {
                throw new IllegalArgumentException("Stream with this name already exists");
            }

            // 3. Validate and collect qualifications
            List<Qualification> qualifications = new ArrayList<>();
            if (addStreamDto.getQualificationIds() == null || addStreamDto.getQualificationIds().isEmpty()) {
                throw new IllegalArgumentException("At least one qualification is required");
            }

            for (Integer id : addStreamDto.getQualificationIds()) {
                Qualification qualification = entityManager.find(Qualification.class, id);
                if (qualification == null) {
                    throw new IllegalArgumentException("Qualification not found with ID: " + id);
                }
                qualifications.add(qualification);
            }

            // 4. Determine sort order
            TypedQuery<Long> maxSortQuery = entityManager.createQuery(
                    "SELECT COALESCE(MAX(c.sortOrder), 0) FROM CustomStream c WHERE c.sortOrder < 10000000",
                    Long.class);
            Long maxSortOrder = maxSortQuery.getSingleResult();

            // 5. Create and persist new stream
            CustomStream stream = new CustomStream();
            stream.setStreamId(getNextStreamId());
            stream.setStreamName(addStreamDto.getStreamName());
            stream.setStreamDescription(addStreamDto.getStreamDescription());
            stream.setQualifications(qualifications);
            stream.setSortOrder(maxSortOrder + 1);
            stream.setCreatedDate(new Date());
            stream.setCreatorUserId(creatorId);
            stream.setCreatorRole(creatorRole);
            stream.setArchived('N');

            entityManager.persist(stream);
            return stream;

        } catch (IllegalArgumentException e) {
            // Re-throw validation exceptions directly
            throw e;
        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            throw new Exception("Failed to create stream: " + e.getMessage());
        }
    }
    public Long getNextStreamId() {
        String sql = "SELECT COALESCE(MAX(stream_id), 0) + 1 FROM custom_stream";
        Object result = entityManager.createNativeQuery(sql).getSingleResult();
        return ((Number) result).longValue();  // Cast result safely
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
    @Transactional
    public CustomStream manageStream(Long streamId, Boolean archive) throws IllegalArgumentException, Exception {
        try {
            CustomStream stream = entityManager.find(CustomStream.class, streamId);
            if (stream == null) {
                throw new IllegalArgumentException("Stream not found");
            }

            if (archive) {
                if (stream.getArchived() == 'Y') {
                    throw new IllegalArgumentException("Stream already archived");
                } else {
                    stream.setArchived('Y');
                    entityManager.merge(stream);
                }
            } else {
                if (stream.getArchived() == 'N') {
                    throw new IllegalArgumentException("Stream already unarchived");
                } else {
                    stream.setArchived('N');
                    entityManager.merge(stream);
                }
            }
            return stream;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public List<Qualification> getQualificationsForStream(Long streamId) {
        CustomStream stream = entityManager.find(CustomStream.class, streamId);
        if (stream == null) {
            throw new IllegalArgumentException("Stream not found");
        }
        return entityManager.createQuery(
                        "SELECT q FROM Qualification q JOIN q.streams s WHERE s.streamId = :streamId",
                        Qualification.class)
                .setParameter("streamId", streamId)
                .getResultList();
    }
    @Transactional
    public CustomStream editStream(Long streamId, List<Integer> qualificationIds, CustomStream stream)
            throws IllegalArgumentException, Exception {

        try {
            // 1. Validate existing stream
            CustomStream streamToEdit = entityManager.find(CustomStream.class, streamId);
            if (streamToEdit == null) {
                throw new IllegalArgumentException("Stream not found with ID: " + streamId);
            }

            // 2. Validate stream ID consistency
            if (stream.getStreamId() != null && !stream.getStreamId().equals(streamId)) {
                throw new IllegalArgumentException("Cannot change stream ID during update");
            }

            // 3. Validate and process stream name
            if (stream.getStreamName() != null) {
                // Case-insensitive duplicate check
                List<CustomStream> existingStreams = getAllStreamArchiveNonArchive();
                for (CustomStream existingStream: existingStreams) {
                    if (existingStream.getStreamName().equalsIgnoreCase(stream.getStreamName()) && !existingStream.getStreamId().equals(streamId)) {
                        throw new IllegalArgumentException("Stream with name '"+stream.getStreamName()+"' already exists");
                    }
                }
               /* // Validate name format
                if (!sharedUtilityService.isAlphabeticWithHyphen(stream.getStreamName())) {
                    throw new IllegalArgumentException(
                            "Stream name should contain only alphabets and hyphens");
                }*/
                streamToEdit.setStreamName(stream.getStreamName());
            }

            // 4. Process description update if provided
            if (stream.getStreamDescription() != null) {
                streamToEdit.setStreamDescription(stream.getStreamDescription());
            }

            // 5. Process qualifications update if provided
            if (qualificationIds != null && !qualificationIds.isEmpty()) {
                List<Qualification> newQualifications = new ArrayList<>();
                Set<Integer> processedIds = new HashSet<>(); // To prevent duplicates

                for (Integer id : qualificationIds) {
                    if (id == null) {
                        continue; // Skip null IDs
                    }

                    if (!processedIds.add(id)) {
                        continue; // Skip duplicate IDs
                    }

                    Qualification qualification = entityManager.find(Qualification.class, id);
                    if (qualification == null) {
                        throw new IllegalArgumentException(
                                "Qualification not found with ID: " + id);
                    }
                    newQualifications.add(qualification);
                }

                if (newQualifications.isEmpty()) {
                    throw new IllegalArgumentException(
                            "At least one valid qualification must be provided");
                }

                streamToEdit.setQualifications(newQualifications);
            }
            else
            {
                streamToEdit.setQualifications(new ArrayList<>());
            }

            // 6. Save changes
            entityManager.merge(streamToEdit);
            return streamToEdit;

        } catch (IllegalArgumentException e) {
            throw e; // Re-throw validation exceptions directly
        } catch (Exception e) {
            throw new Exception("Failed to update stream: " + e.getMessage());
        }
    }
}
