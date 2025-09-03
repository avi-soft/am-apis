package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.AddSubjectDto;
import com.community.api.entity.CustomStream;
import com.community.api.entity.CustomSubject;
import com.community.api.entity.Role;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    SharedUtilityService sharedUtilityService;

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
        try {
            CustomStream stream = entityManager.find(CustomStream.class, streamId);
            CustomSubject subject = entityManager.find(CustomSubject.class, subjectId);

            if (stream != null && subject != null) {
                stream.getSubjects().add(subject);
                entityManager.merge(stream);
            }
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw exception;
        }
    }

    public List<CustomSubject> getAllSubject(Boolean archived) {
        try {
            Query query = entityManager.createQuery(Constant.GET_ALL_SUBJECT, CustomSubject.class);
            if (archived)
                query.setParameter("archived", 'Y');
            else
                query.setParameter("archived", 'N');
            List<CustomSubject> subjectList = query.getResultList();
            return subjectList;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return Collections.emptyList();
        }
    }

    public List<CustomSubject> getAllArchivedNonArchivedSubject() {
        try {
            Query query = entityManager.createQuery(Constant.GET_ALL_SUBJECT_ARCHIVE_UNARCHIVE, CustomSubject.class);
            List<CustomSubject> subjectList = query.getResultList();
            return subjectList;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return Collections.emptyList();
        }
    }

    public List<CustomSubject> getSubjectsByStreamIds(Long streamId) {
        try {
            String jpql = "SELECT s FROM CustomStream cs JOIN cs.subjects s " +
                    "WHERE cs.streamId = :streamId " +
                    "AND s.archived = 'N' " +
                    "ORDER BY s.sortOrder ASC";

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
                if (subject.get(0).getArchived() == 'Y') {
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
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }
    }

    public Boolean validateAddSubjectDto(AddSubjectDto addSubjectDto) throws Exception {
        try {

            if (addSubjectDto.getSubjectName() != null) {
                addSubjectDto.setSubjectName(addSubjectDto.getSubjectName().trim());
            }
            List<CustomSubject> subjects = getSubjectBySubjectName(addSubjectDto.getSubjectName());
            if (subjects != null && !subjects.isEmpty()) {
                throw new IllegalArgumentException("Duplicate Unarchived Subject Name");
            }
            if (addSubjectDto.getSubjectDescription() != null) {
                addSubjectDto.setSubjectDescription(addSubjectDto.getSubjectDescription().trim());
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException("ILLEGAL ARGUMENT EXCEPTION OCCURRED: " + illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }

    }

    @Transactional
    public CustomSubject saveSubject(AddSubjectDto addSubjectDto, Long creatorId, Role creatorRole)
            throws IllegalArgumentException, Exception {

        try {
            // 1. Validate input
            if (addSubjectDto == null) {
                throw new IllegalArgumentException("Subject data cannot be null");
            }

            // 2. Validate subject name
            if (addSubjectDto.getSubjectName() == null || addSubjectDto.getSubjectName().trim().isEmpty()) {
                throw new IllegalArgumentException("Subject name is required");
            }
            /*  if(!sharedUtilityService.isAlphabeticWithHyphen(addSubjectDto.getSubjectName()))
                throw new IllegalArgumentException("Subject names can contain only alphabets and hyphens");*/

            // 3. Check for duplicate subject name (case-insensitive)
            TypedQuery<Long> duplicateCheck = entityManager.createQuery(
                    "SELECT COUNT(s) FROM CustomSubject s WHERE LOWER(s.subjectName) = LOWER(:name)",
                    Long.class);
            duplicateCheck.setParameter("name", addSubjectDto.getSubjectName());

            if (duplicateCheck.getSingleResult() > 0) {
                throw new IllegalArgumentException("Subject with this name already exists");
            }


            // 4. Validate at least one stream is provided
            if (addSubjectDto.getStreamIds() == null || addSubjectDto.getStreamIds().isEmpty()) {
                throw new IllegalArgumentException("At least one stream must be provided");
            }

            // 5. Create new subject
            CustomSubject subject = new CustomSubject();
            subject.setSubjectName(addSubjectDto.getSubjectName());
            subject.setSubjectDescription(addSubjectDto.getSubjectDescription());
            subject.setCreatedDate(new Date());
            subject.setCreatorUserId(creatorId);
            subject.setCreatorRole(creatorRole);

            // 6. Set sort order
            TypedQuery<Long> maxSortQuery = entityManager.createQuery(
                    "SELECT COALESCE(MAX(c.sortOrder), 0) FROM CustomSubject c WHERE c.sortOrder < 1000000",
                    Long.class);
            subject.setSortOrder(maxSortQuery.getSingleResult() + 1);

            // 7. Validate and add streams
            List<CustomStream> streams = new ArrayList<>();
            Set<Long> processedStreamIds = new HashSet<>(); // To prevent duplicates

            for (Long streamId : addSubjectDto.getStreamIds()) {
                if (streamId == null) {
                    continue; // Skip null IDs
                }

                if (!processedStreamIds.add(streamId)) {
                    continue; // Skip duplicate IDs
                }

                CustomStream stream = entityManager.find(CustomStream.class, streamId);
                if (stream == null) {
                    throw new IllegalArgumentException("Stream not found with ID: " + streamId);
                }
                streams.add(stream);
            }

            if (streams.isEmpty()) {
                throw new IllegalArgumentException("No valid streams provided");
            }

            subject.setStreams(streams);

            // 8. Persist and return
            entityManager.persist(subject);
            return subject;

        } catch (IllegalArgumentException e) {
            exceptionHandlingService.handleException(e);
            throw e; // Re-throw validation exceptions directly
        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            throw new Exception("Failed to create subject: " + e.getMessage());
        }
    }

    @Transactional
    public void removeSubjectById(CustomSubject subject) throws Exception {
        try {

            if (subject == null) {
                throw new IllegalArgumentException("No Subject Found");
            }
            subject.setArchived('Y');
            entityManager.merge(subject);

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage());
        }
    }

    @Transactional
    public CustomSubject editSubject(Long subjectId, List<Long> streamIds, CustomSubject subject)
            throws IllegalArgumentException, Exception {

        try {
            // 1. Validate existing subject
            CustomSubject subjectToEdit = entityManager.find(CustomSubject.class, subjectId);
            if (subjectToEdit == null) {
                throw new IllegalArgumentException("Subject not found with ID: " + subjectId);
            }

            // 2. Validate ID consistency
            if (subject.getSubjectId() != null && !subject.getSubjectId().equals(subjectId)) {
                throw new IllegalArgumentException("Cannot change subject ID during update");
            }

            // 3. Process subject name update if provided
            if (subject.getSubjectName() != null) {
                // Validate name format
               /* if (!sharedUtilityService.isAlphabetic(subject.getSubjectName())) {
                    throw new IllegalArgumentException(
                            "Subject name should contain only alphabetic characters");
                }*/

                if (subject.getSubjectName().isEmpty()) {
                    throw new IllegalArgumentException("Subject name cannot be empty");
                }

                List<CustomSubject> existingSubjects = getAllArchivedNonArchivedSubject();
                for (CustomSubject existingSubject : existingSubjects) {
                    if (existingSubject.getSubjectName().equalsIgnoreCase(subject.getSubjectName()) && !existingSubject.getSubjectId().equals(subjectId)) {
                        throw new IllegalArgumentException("Subject with name '" + subject.getSubjectName() + "' already exists");
                    }
                }

                subjectToEdit.setSubjectName(subject.getSubjectName());
            }

            // 4. Process description update if provided
            if (subject.getSubjectDescription() != null) {
                if (subject.getSubjectDescription().isEmpty()) {
                    throw new IllegalArgumentException("Subject description cannot be empty");
                }
                subjectToEdit.setSubjectDescription(subject.getSubjectDescription());
            }

            // 5. Process sort order update if provided
            if (subject.getSortOrder() != null) {
                subjectToEdit.setSortOrder(subject.getSortOrder());
            }

            // 6. Process streams update if provided
            if (streamIds != null) {
                Set<Long> uniqueStreamIds = new HashSet<>(streamIds); // Remove duplicates
                List<CustomStream> validStreams = new ArrayList<>();

                for (Long streamId : uniqueStreamIds) {
                    if (streamId == null) continue;

                    CustomStream stream = entityManager.find(CustomStream.class, streamId);
                    if (stream == null) {
                        throw new IllegalArgumentException(
                                "Stream not found with ID: " + streamId);
                    }
                    validStreams.add(stream);
                }

                if (!validStreams.isEmpty()) {
                    subjectToEdit.setStreams(validStreams);
                }
            } else {
                subjectToEdit.setStreams(new ArrayList<>());
            }

            // 7. Save changes
            entityManager.merge(subjectToEdit);
            return subjectToEdit;

        } catch (IllegalArgumentException e) {
            exceptionHandlingService.handleException(e);
            throw e; // Re-throw validation exceptions directly
        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            throw new Exception("Failed to update subject: " + e.getMessage());
        }
    }

    @Transactional
    public CustomSubject manageSubject(Long subjectId, Boolean archive) throws Exception {
        try {
            CustomSubject subject = entityManager.find(CustomSubject.class, subjectId);
            if (subject == null) {
                throw new IllegalArgumentException("Subject not found");
            }

            if (archive) {
                if (subject.getArchived() == 'Y') {
                    throw new IllegalArgumentException("Subject already archived");
                }
                subject.setArchived('Y');
            } else {
                if (subject.getArchived() == 'N') {
                    throw new IllegalArgumentException("Subject already unarchived");
                }
                subject.setArchived('N');
            }

            entityManager.merge(subject);
            return subject;
        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            throw e;
        }
    }

    public List<CustomStream> getStreamsForSubject(Long subjectId) {
        try {
            CustomSubject subject = entityManager.find(CustomSubject.class, subjectId);
            if (subject == null) {
                throw new IllegalArgumentException("Subject not found");
            }
            return entityManager.createQuery(
                            "SELECT s FROM CustomStream s JOIN s.subjects sub WHERE sub.subjectId = :subjectId",
                            CustomStream.class)
                    .setParameter("subjectId", subjectId)
                    .getResultList();
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw exception;
        }
    }

}
