package com.community.api.services;

import com.community.api.dto.PostDto;
import com.community.api.dto.QualificationEligibilityDto;
import com.community.api.dto.QualificationGroupDto;
import com.community.api.dto.QualificationInputDto;
import com.community.api.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

@Service
public class QualificationGroupService {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ProductService productService;
    /**
     * Converts linear qualification input with operators into grouped structure
     * Logic: AND creates new group, OR stays in same group
     */
    public List<QualificationGroupDto> buildQualificationGroups(QualificationInputDto inputDto) {
        List<QualificationGroupDto> groups = new ArrayList<>();

        if (inputDto.getQualificationEligibilities() == null || inputDto.getQualificationEligibilities().isEmpty()) {
            return groups;
        }

        List<QualificationEligibilityDto> qualifications = inputDto.getQualificationEligibilities();
        List<String> operators = inputDto.getOperators();

        // Start with first group
        QualificationGroupDto currentGroup = new QualificationGroupDto();
        currentGroup.setGroupOrder(1);
        currentGroup.setGroupName("Group 1");
        currentGroup.getQualificationGroups().add(qualifications.get(0));

        // Process remaining qualifications
        for (int i = 1; i < qualifications.size(); i++) {
            String operator = (operators != null && i - 1 < operators.size()) ?
                    operators.get(i - 1) : "OR";

            if ("AND".equalsIgnoreCase(operator)) {
                groups.add(currentGroup);

                currentGroup = new QualificationGroupDto();
                currentGroup.setGroupOrder(groups.size() + 1);
                currentGroup.setGroupName("Group " + (groups.size() + 1));
                currentGroup.getQualificationGroups().add(qualifications.get(i));
            } else {
                currentGroup.getQualificationGroups().add(qualifications.get(i));
            }
        }

        groups.add(currentGroup);

        return groups;
    }

    public boolean validateQualificationGroups(PostDto postDto) throws Exception {
        try {
            for (QualificationGroupDto group : postDto.getQualificationEligibility()) {
                for (QualificationEligibilityDto qualification : group.getQualificationGroups()) {
                    productService.validateQualificationRequirement(postDto);
                }
            }
            return true;
        } catch (Exception e) {
            throw e;
        }
    }

    public void saveQualificationGroups(List<QualificationGroupDto> groupDtos, Post post) {
        for (QualificationGroupDto groupDto : groupDtos) {
            QualificationGroup group = new QualificationGroup();
            group.setPost(post);
            group.setGroupOrder(groupDto.getGroupOrder());
            group.setAdditionalComments(groupDto.getAdditionalComments());

            entityManager.persist(group);
            entityManager.flush();

            for (QualificationEligibilityDto qualificationDto : groupDto.getQualificationGroups()) {
                saveQualificationEligibility(qualificationDto, group);
            }
        }
    }

    private void saveQualificationEligibility(QualificationEligibilityDto dto, QualificationGroup group) {
        QualificationEligibility qualification = new QualificationEligibility();
        qualification.setQualificationGroup(group);

        // Set qualifications
        if (dto.getQualificationIds() != null && !dto.getQualificationIds().isEmpty()) {
            List<Qualification> qualificationsToAdd = new ArrayList<>();
            for (Integer qualificationId : dto.getQualificationIds()) {
                Qualification qual = entityManager.find(Qualification.class, qualificationId);
                if (qual != null) {
                    qualificationsToAdd.add(qual);
                }
            }
            qualification.setQualifications(qualificationsToAdd);
        }

        // Set subjects
        if (dto.getCustomSubjectIds() != null && !dto.getCustomSubjectIds().isEmpty()) {
            List<CustomSubject> subjectsToAdd = new ArrayList<>();
            for (Long subjectId : dto.getCustomSubjectIds()) {
                CustomSubject subject = entityManager.find(CustomSubject.class, subjectId);
                if (subject != null) {
                    subjectsToAdd.add(subject);
                }
            }
            qualification.setCustomSubjects(subjectsToAdd);
        }

        // Set streams
        if (dto.getCustomStreamIds() != null && !dto.getCustomStreamIds().isEmpty()) {
            List<CustomStream> streamsToAdd = new ArrayList<>();
            for (Long streamId : dto.getCustomStreamIds()) {
                CustomStream stream = entityManager.find(CustomStream.class, streamId);
                if (stream != null) {
                    streamsToAdd.add(stream);
                }
            }
            qualification.setCustomStreams(streamsToAdd);
        }

        // Set reserve category
        if (dto.getCustomReserveCategoryId() != null) {
            CustomReserveCategory category = entityManager.find(CustomReserveCategory.class, dto.getCustomReserveCategoryId());
            qualification.setCustomReserveCategory(category);
        }

        // Set logical operators
        if (dto.getStreamsRelationId() != null) {
            LogicalOperator streamsRelation = entityManager.find(LogicalOperator.class, dto.getStreamsRelationId());
            qualification.setStreamsRelation(streamsRelation);
        }

        if (dto.getSubjectsRelationId() != null) {
            LogicalOperator subjectsRelation = entityManager.find(LogicalOperator.class, dto.getSubjectsRelationId());
            qualification.setSubjectsRelation(subjectsRelation);
        }

        // Set other properties
        qualification.setPercentage(dto.getPercentage());
        qualification.setIsPercentage(dto.getIsPercentage());
        qualification.setCgpa(dto.getCgpa());
        qualification.setQualificationIdRunningField(dto.getQualificationIdRunningField());
        qualification.setSubjectIdRunningField(dto.getSubjectIdRunningField());
        qualification.setStreamIdRunningField(dto.getStreamIdRunningField());
        qualification.setReserveCatIdRunningField(dto.getReserveCatIdRunningField());
        qualification.setAdditionalComments(dto.getAdditionalComments());
        qualification.setIsAppearing(dto.getIsAppearing());
        qualification.setHighestQualificationSubjectNames(dto.getHighestQualificationSubjectNames());
        qualification.setStreamsMandatory(dto.getStreamsMandatory());
        qualification.setSubjectsMandatory(dto.getSubjectsMandatory());
        qualification.setIsReserveCategoryMandatory(dto.getReserveCategoryMandatory());
        qualification.setIsCertificationRequired(dto.getIsCertificationRequired());
        if ( dto.getQualificationOperatorId() != null) {
            QualificationRelation qualificationRelation = new QualificationRelation();
            LogicalOperator operator = entityManager.find(LogicalOperator.class, dto.getQualificationOperatorId());
            qualificationRelation.setLogicalOperator(operator);
            entityManager.persist(qualificationRelation);
            entityManager.flush();
            qualification.setQualificationRelation(qualificationRelation);
        }

        entityManager.persist(qualification);
        entityManager.flush();
    }

}
