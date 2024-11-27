package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.dto.UpdateQualificationDto;
import com.community.api.endpoint.avisoft.controller.Qualification.QualificationController;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.*;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.exception.*;
import com.community.api.utils.DocumentType;
import com.community.api.utils.ServiceProviderDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QualificationDetailsService {
    EntityManager entityManager;
    QualificationController qualificationController;
    QualificationService qualificationService;
    SharedUtilityService sharedUtilityService;
    ServiceProviderServiceImpl serviceProviderService;
    BoardUniversityService boardUniversityService;
    StreamService streamService ;
    SubjectService subjectService;

    public QualificationDetailsService(EntityManager entityManager, QualificationController qualificationController, QualificationService qualificationService, SharedUtilityService sharedUtilityService, ServiceProviderServiceImpl serviceProviderService,BoardUniversityService boardUniversityService,StreamService streamService,SubjectService subjectService) {
        this.entityManager = entityManager;
        this.qualificationController = qualificationController;
        this.qualificationService = qualificationService;
        this.sharedUtilityService = sharedUtilityService;
        this.serviceProviderService=serviceProviderService;
        this.boardUniversityService=boardUniversityService;
        this.streamService=streamService;
        this.subjectService=subjectService;
    }

    @Transactional
    public QualificationDetails addQualificationDetails(Long userId, QualificationDetails qualificationDetails, String roleName)
            throws EntityAlreadyExistsException, ExaminationDoesNotExistsException, CustomerDoesNotExistsException {

        if (roleName.equals(Constant.SERVICE_PROVIDER)) {
            ServiceProviderEntity serviceProviderEntity = findServiceProviderById(userId);
//            checkIfQualificationAlreadyExists(userId, qualificationDetails.getQualification_id(), roleName);
            List<DocumentType> qualifications = qualificationService.getAllQualifications();
            Integer qualificationToAdd = findQualificationId(qualificationDetails.getQualification_id(), qualifications);
            qualificationDetails.setQualification_id(qualificationToAdd);
            List<BoardUniversity> boardUniversities= boardUniversityService.getAllBoardUniversities();
            Long boardUniversityToAdd= findBoardUniversityById(qualificationDetails.getBoard_university_id(),boardUniversities);
            qualificationDetails.setBoard_university_id(boardUniversityToAdd);
            List<CustomStream> streams= streamService.getAllStream();
            Long streamToAdd= findStreamId(qualificationDetails.getStream_id(),streams);
            qualificationDetails.setStream_id(streamToAdd);
            List<Long> subjects = validateAndGetSubjectIds(qualificationDetails.getSubject_ids());
            qualificationDetails.setSubject_ids(subjects);
            qualificationDetails.setService_provider(serviceProviderEntity);
            if(serviceProviderEntity.getQualificationDetailsList().isEmpty())
            {
                serviceProviderEntity.getQualificationDetailsList().add(qualificationDetails);
            }
            else if(!serviceProviderEntity.getQualificationDetailsList().isEmpty())
            {
                serviceProviderEntity.getQualificationDetailsList().clear();
                serviceProviderEntity.getQualificationDetailsList().add(qualificationDetails);
                List<ServiceProviderDocument> serviceProviderDocuments= serviceProviderEntity.getDocuments();
                if(!serviceProviderDocuments.isEmpty())
                {
                    for(ServiceProviderDocument serviceProviderDocument: serviceProviderDocuments)
                    {
                        iterateQualificationsToDeleteDocumentsForServiceProvider(serviceProviderDocument,qualifications);
                    }
                }
            }
            entityManager.persist(qualificationDetails);
            giveQualificationScore(userId);
            return qualificationDetails;
        }
        CustomCustomer customCustomer = findCustomCustomerById(userId);
        checkIfQualificationAlreadyExists(userId, qualificationDetails.getQualification_id(), roleName);
        List<DocumentType> qualifications = qualificationService.getAllQualifications();
        Integer qualificationToAdd = null;
        qualificationToAdd = findQualificationId(qualificationDetails.getQualification_id(), qualifications);
        qualificationDetails.setQualification_id(qualificationToAdd);
        List<BoardUniversity> boardUniversities= boardUniversityService.getAllBoardUniversities();
        Long boardUniversityToAdd= findBoardUniversityById(qualificationDetails.getBoard_university_id(),boardUniversities);
        qualificationDetails.setBoard_university_id(boardUniversityToAdd);
        List<CustomStream> streams= streamService.getAllStream();
        Long streamToAdd= findStreamId(qualificationDetails.getStream_id(),streams);
        qualificationDetails.setStream_id(streamToAdd);
        List<Long> subjects = validateAndGetSubjectIds(qualificationDetails.getSubject_ids());
        qualificationDetails.setSubject_ids(subjects);
        createSubjectDetails(qualificationDetails);
        validateQualificationDetail(qualificationDetails);
        qualificationDetails.setCustom_customer(customCustomer);
        customCustomer.getQualificationDetailsList().add(qualificationDetails);
        entityManager.persist(qualificationDetails);
        return qualificationDetails;
    }

    @Transactional
    public List<Map<String, Object>> getQualificationDetailsByCustomerId(Long userId, String roleName) throws CustomerDoesNotExistsException, RuntimeException {
        List<QualificationDetails> qualificationDetails;
        if (roleName.equals(Constant.SERVICE_PROVIDER)) {
            ServiceProviderEntity serviceProviderEntity = findServiceProviderById(userId);
            qualificationDetails = serviceProviderEntity.getQualificationDetailsList();
            return sharedUtilityService.mapQualifications(qualificationDetails);
        }
        CustomCustomer customCustomer = findCustomCustomerById(userId);
        qualificationDetails = customCustomer.getQualificationDetailsList();
        return sharedUtilityService.mapQualifications(qualificationDetails);
    }

    @Transactional
    public QualificationDetails deleteQualificationDetail(Long userId, Long qualificationId, String roleName) throws EntityDoesNotExistsException, CustomerDoesNotExistsException {
        List<QualificationDetails> qualificationDetails;
        if (roleName.equals(Constant.SERVICE_PROVIDER)) {
            ServiceProviderEntity serviceProviderEntity = findServiceProviderById(userId);
            qualificationDetails = serviceProviderEntity.getQualificationDetailsList();
        } else {
            CustomCustomer customCustomer = findCustomCustomerById(userId);
            qualificationDetails = customCustomer.getQualificationDetailsList();
        }

        QualificationDetails qualificationDetailsToDelete = null;
        for (QualificationDetails qualificationDetails1 : qualificationDetails) {
            if (qualificationDetails1.getId().equals(qualificationId)) {
                qualificationDetailsToDelete = qualificationDetails1;
                break;
            }
        }
        if (qualificationDetailsToDelete == null) {
            throw new EntityDoesNotExistsException("QualificationDetails with id " + qualificationId + " does not exists");
        }
        qualificationDetails.remove(qualificationDetailsToDelete);
        entityManager.remove(qualificationDetailsToDelete);
        if(roleName.equalsIgnoreCase(Constant.SERVICE_PROVIDER))
        {
            giveQualificationScore(userId);
        }
        return qualificationDetailsToDelete;
    }

    @Transactional
    public QualificationDetails updateQualificationDetail(Long userId, Long qualificationId, UpdateQualificationDto qualification, String roleName) throws EntityDoesNotExistsException, EntityAlreadyExistsException, CustomerDoesNotExistsException, ExaminationDoesNotExistsException {
        List<QualificationDetails> qualificationDetails;
        if (roleName.equals(Constant.SERVICE_PROVIDER)) {
            ServiceProviderEntity serviceProviderEntity = findServiceProviderById(userId);
            qualificationDetails = serviceProviderEntity.getQualificationDetailsList();
        } else {
            CustomCustomer customCustomer = findCustomCustomerById(userId);
            qualificationDetails = customCustomer.getQualificationDetailsList();
        }

        QualificationDetails qualificationDetailsToUpdate = null;
        for (QualificationDetails qualificationDetails1 : qualificationDetails) {
            if (qualificationDetails1.getId().equals(qualificationId)) {
                qualificationDetailsToUpdate = qualificationDetails1;
                break;
            }
        }
        if (qualificationDetailsToUpdate == null) {
            throw new EntityDoesNotExistsException("Qualification details with id " + qualificationId + " does not exists");
        }
        String queryStr;

        // Build the query string based on the entity type
        if ("SERVICE_PROVIDER".equalsIgnoreCase(roleName)) {
            queryStr = "SELECT q FROM QualificationDetails q WHERE q.service_provider.service_provider_id = :entityId AND q.qualification_id = :qualification_id";
        } else if ("CUSTOMER".equalsIgnoreCase(roleName)) {
            queryStr = "SELECT q FROM QualificationDetails q WHERE q.custom_customer.id = :entityId AND q.qualification_id = :qualification_id";
        } else {
            throw new IllegalArgumentException("Invalid entity type specified.");
        }

        // Create the dynamic query based on the entity type
        TypedQuery<QualificationDetails> query = entityManager.createQuery(queryStr, QualificationDetails.class);
        query.setParameter("entityId", userId);
        query.setParameter("qualification_id", qualification.getQualification_id());

        // Execute the query and check if qualification already exists
        QualificationDetails existingQualificationDetails = query.getResultStream().findFirst().orElse(null);

        if (existingQualificationDetails != null && !qualificationId.equals(existingQualificationDetails.getId())) {
            throw new EntityAlreadyExistsException("Qualification details with id " + qualification.getQualification_id() + " already exists");
        }

        if (Objects.nonNull(qualification.getQualification_id())) {
            List<DocumentType> qualificationDetailsList = qualificationService.getAllQualifications();
            Integer qualificationToAdd = findQualificationId(qualification.getQualification_id(), qualificationDetailsList);
            qualificationDetailsToUpdate.setQualification_id(qualificationToAdd);

            //Qualification scoring
            if(roleName.equalsIgnoreCase(Constant.SERVICE_PROVIDER))
            {
                giveQualificationScore(userId);
            }
        }

        if(Objects.nonNull(qualification.getBoard_university_id()))
        {
            List<BoardUniversity> boardUniversities = boardUniversityService.getAllBoardUniversities();
            Long boardUniversityToAdd= findBoardUniversityById(qualification.getBoard_university_id(),boardUniversities);
            qualificationDetailsToUpdate.setBoard_university_id(boardUniversityToAdd);
        }

        if (Objects.nonNull(qualification.getInstitution_name())) {
            qualificationDetailsToUpdate.setInstitution_name(qualification.getInstitution_name());
        }

        if (Objects.nonNull(qualification.getMarks_obtained())) {
            qualificationDetailsToUpdate.setMarks_obtained(qualification.getMarks_obtained());
        }
        if (Objects.nonNull(qualification.getTotal_marks())) {
            qualificationDetailsToUpdate.setTotal_marks(qualification.getTotal_marks());
        }
        if (Objects.nonNull(qualification.getSubject_ids())) {
            List<Long> subjects = validateAndGetSubjectIds(qualification.getSubject_ids());
            qualificationDetailsToUpdate.setSubject_ids(subjects);
        }
        if (Objects.nonNull(qualification.getStream_id())) {
            List<CustomStream> streams = streamService.getAllStream();
            Long streamToAdd= findStreamId(qualification.getStream_id(),streams);
            qualificationDetailsToUpdate.setBoard_university_id(streamToAdd);
        }
        if (Objects.nonNull(qualification.getGrade_or_percentage_value())) {
            qualificationDetailsToUpdate.setGrade_or_percentage_value(qualification.getGrade_or_percentage_value());
        }
        if (Objects.nonNull(qualification.getDate_of_passing())) {
            qualificationDetailsToUpdate.setDate_of_passing(qualification.getDate_of_passing());
        }
        if (Objects.nonNull(qualification.getExamination_role_number())) {
            qualificationDetailsToUpdate.setExamination_role_number(qualification.getExamination_role_number());
        }
        if (Objects.nonNull(qualification.getExamination_registration_number())) {
            qualificationDetailsToUpdate.setExamination_registration_number(qualification.getExamination_registration_number());
        }
        return entityManager.merge(qualificationDetailsToUpdate);
    }

    private CustomCustomer findCustomCustomerById(Long customCustomerId) throws CustomerDoesNotExistsException {
        CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customCustomerId);
        if (customCustomer == null) {
            throw new CustomerDoesNotExistsException("Customer does not exist with id " + customCustomerId);
        }
        return customCustomer;
    }

    private ServiceProviderEntity findServiceProviderById(Long serviceProviderId) throws CustomerDoesNotExistsException {
        ServiceProviderEntity serviceProviderEntity = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
        if (serviceProviderEntity == null) {
            throw new CustomerDoesNotExistsException("ServiceProvider does not exist with id " + serviceProviderId);
        }
        return serviceProviderEntity;
    }

    private void checkIfQualificationAlreadyExists(Long entityId, Integer qualificationId, String entityType) throws EntityAlreadyExistsException {
        String queryStr;

        // Build the query string based on the entity type
        if ("SERVICE_PROVIDER".equalsIgnoreCase(entityType)) {
            queryStr = "SELECT q FROM QualificationDetails q WHERE q.service_provider.service_provider_id = :entityId AND q.qualification_id = :qualification_id";
        } else if ("CUSTOMER".equalsIgnoreCase(entityType)) {
            queryStr = "SELECT q FROM QualificationDetails q WHERE q.custom_customer.id = :entityId AND q.qualification_id = :qualification_id";
        } else {
            throw new IllegalArgumentException("Invalid entity type specified.");
        }

        // Create the dynamic query based on the entity type
        TypedQuery<QualificationDetails> query = entityManager.createQuery(queryStr, QualificationDetails.class);
        query.setParameter("entityId", entityId);
        query.setParameter("qualification_id", qualificationId);

        // Execute the query and check if qualification already exists
        QualificationDetails existingQualification = query.getResultStream().findFirst().orElse(null);

        if (existingQualification != null) {
            throw new EntityAlreadyExistsException("Qualification with id " + qualificationId + " already exists for " + entityType.toLowerCase());
        }
    }

    public Integer findQualificationId(Integer qualificationId, List<DocumentType> qualifications) throws ExaminationDoesNotExistsException {
        for (DocumentType qualification : qualifications) {
            if (qualification.getDocument_type_id().equals(qualificationId)) {
                return qualification.getDocument_type_id();
            }
        }
        throw new ExaminationDoesNotExistsException("Qualification with id " + qualificationId + " does not exist");
    }

    public Long findBoardUniversityById(Long boardUniversityId,List<BoardUniversity> boardUniversities)
    {
        for(BoardUniversity boardUniversity : boardUniversities)
        {
            if(boardUniversity.getBoard_university_id().equals(boardUniversityId))
            {
                return boardUniversity.getBoard_university_id();
            }
        }
        throw new IllegalArgumentException("Board or University with id "+ boardUniversityId+ " does not exist");
    }
    public Long findStreamId(Long streamId,List<CustomStream> streams)
    {
        for(CustomStream customStream : streams)
        {
            if(customStream.getStreamId().equals(streamId))
            {
                return customStream.getStreamId();
            }
        }
        throw new IllegalArgumentException("Stream with id "+ streamId+ " does not exist");
    }

    public List<Long> validateAndGetSubjectIds(List<Long> subjectIds) {
        if (subjectIds == null || subjectIds.isEmpty()) {
            throw new IllegalArgumentException("Subjects list cannot be empty");
        }

        // Query to check which subject IDs exist in the database
        List<Long> existingSubjectIds = entityManager.createQuery(
                        "SELECT s.subjectId FROM CustomSubject s WHERE s.subjectId IN :subjectIds",
                        Long.class)
                .setParameter("subjectIds", subjectIds)
                .getResultList();

        // Check if any IDs from the request do not exist
        List<Long> missingSubjectIds = subjectIds.stream()
                .filter(id -> !existingSubjectIds.contains(id))
                .collect(Collectors.toList());

        if (!missingSubjectIds.isEmpty()) {
            throw new IllegalArgumentException("The following subject IDs do not exist: " + missingSubjectIds);
        }

        // Return the validated list of IDs
        return subjectIds;
    }

    @Transactional
    public void iterateQualificationsToDeleteDocumentsForServiceProvider(ServiceProviderDocument serviceProviderDocument, List<DocumentType> qualifications)
    {
        for(DocumentType qualification: qualifications)
        {
            if(qualification.getDocument_type_id().equals(serviceProviderDocument.getDocumentType().getDocument_type_id()))
            {
                entityManager.remove(serviceProviderDocument);
            }
        }
    }

    public void giveQualificationScore(Long userId) throws CustomerDoesNotExistsException {
        ServiceProviderEntity serviceProviderEntity = findServiceProviderById(userId);
        TypedQuery<ScoringCriteria> typedQuery=  entityManager.createQuery(Constant.GET_ALL_SCORING_CRITERIA,ScoringCriteria.class);
        List<ScoringCriteria> scoringCriteriaList = typedQuery.getResultList();

        Integer totalScore=0;
        ScoringCriteria scoringCriteriaToMap =null;
        if(!serviceProviderEntity.getQualificationDetailsList().isEmpty())
        {
            QualificationDetails qualificationDetail= serviceProviderEntity.getQualificationDetailsList().get(serviceProviderEntity.getQualificationDetailsList().size()-1);
            DocumentType qualification1 = entityManager.find(DocumentType.class, qualificationDetail.getQualification_id());
            if (qualification1 != null) {
                if (qualification1.getDocument_type_name().equalsIgnoreCase("BACHELORS") || qualification1.getDocument_type_name().equalsIgnoreCase("MASTERS") || qualification1.getDocument_type_name().equalsIgnoreCase("DOCTORATE")) {
                    scoringCriteriaToMap=serviceProviderService.traverseListOfScoringCriteria(6L,scoringCriteriaList,serviceProviderEntity);
                    if(scoringCriteriaToMap==null)
                    {
                        throw new IllegalArgumentException("Scoring Criteria is not found for scoring Qualification Score");
                    }
                    else {
                        serviceProviderEntity.setQualificationScore(scoringCriteriaToMap.getScore());
                    }
                }
                else if(qualification1.getDocument_type_name().equalsIgnoreCase("INTERMEDIATE")) {
                    scoringCriteriaToMap=serviceProviderService.traverseListOfScoringCriteria(7L,scoringCriteriaList,serviceProviderEntity);
                    if(scoringCriteriaToMap==null)
                    {
                        throw new IllegalArgumentException("Scoring Criteria is not found for scoring Qualification Score");
                    }
                    else {
                        serviceProviderEntity.setQualificationScore(scoringCriteriaToMap.getScore());
                    }
                }
                else if(qualification1.getDocument_type_name().equalsIgnoreCase("MATRICULATION")) {
                    serviceProviderEntity.setQualificationScore(0);
                }
            }
            else {
                throw new IllegalArgumentException("Unknown Qualification is found");
            }
        }
        else if(serviceProviderEntity.getQualificationDetailsList().isEmpty()) {
            serviceProviderEntity.setQualificationScore(0);
        }

        if(serviceProviderEntity.getType().equalsIgnoreCase("PROFESSIONAL"))
        {
            totalScore=serviceProviderEntity.getBusinessUnitInfraScore()+serviceProviderEntity.getWorkExperienceScore()+serviceProviderEntity.getTechnicalExpertiseScore()+ serviceProviderEntity.getQualificationScore()+ serviceProviderEntity.getStaffScore();
        }
        else {
            totalScore=serviceProviderEntity.getInfraScore()+serviceProviderEntity.getWorkExperienceScore()+serviceProviderEntity.getTechnicalExpertiseScore()+serviceProviderEntity.getQualificationScore()+serviceProviderEntity.getPartTimeOrFullTimeScore();
        }
        if(serviceProviderEntity.getWrittenTestScore()!=null)
        {
            totalScore=totalScore+serviceProviderEntity.getWrittenTestScore();
        }
        if(serviceProviderEntity.getImageUploadScore()!=null)
        {
            totalScore=totalScore+serviceProviderEntity.getImageUploadScore();
        }
        serviceProviderEntity.setTotalScore(0);
        serviceProviderEntity.setTotalScore(totalScore);
        serviceProviderService.assignRank(serviceProviderEntity,totalScore);
        entityManager.merge(serviceProviderEntity);
    }

    public void validateQualificationDetail(QualificationDetails qualificationDetails)
    {
        if(qualificationDetails.getQualification_id().equals(14) || qualificationDetails.getQualification_id().equals(15))
        {
            if(qualificationDetails.getSubjectDetails().size()<5)
            {
                throw new IllegalArgumentException("You have to add at least five subjects");
            }
        }

        if(!qualificationDetails.getQualification_id().equals(14))
        {
            throw new IllegalArgumentException("Stream id cannot be null");
        }

        if(qualificationDetails.getTotal_marks_type()==null)
        {
            throw new IllegalArgumentException("You have to select whether the you want to add the total marks in normal marks, cgpa or grade");
        }
        if(!qualificationDetails.getTotal_marks_type().equalsIgnoreCase("Percentage")&& !qualificationDetails.getTotal_marks_type().equalsIgnoreCase("CGPA") && !qualificationDetails.getTotal_marks_type().equalsIgnoreCase("Grade"))
        {
            throw new IllegalArgumentException("Total marks type must be either percentage or Grade or CGPA");
        }
        if(qualificationDetails.getTotal_marks_type().trim().isEmpty())
        {
            throw new IllegalArgumentException("Total marks type cannot be empty");
        }

        if(qualificationDetails.getTotal_marks_type().equalsIgnoreCase("Percentage"))
        {
           Double percentage= (qualificationDetails.getMarks_obtained()/qualificationDetails.getTotal_marks())*100;
           qualificationDetails.setTotal_percentage(percentage);
        }
        else if(qualificationDetails.getTotal_marks_type().equalsIgnoreCase("CGPA") || qualificationDetails.getTotal_marks_type().equalsIgnoreCase("Grade"))
        {
            if(qualificationDetails.getTotal_percentage()==null)
            {
                throw new IllegalArgumentException("Percentage of total marks cannot be null");
            }
        }
    }

    @Transactional
    public void createSubjectDetails(QualificationDetails qualificationDetail) {
        if(qualificationDetail.getSubject_marks_type()==null)
        {
            throw new IllegalArgumentException("You have to select whether the you want to add the subject marks in normal marks, cgpa or grade");
        }
        if(!qualificationDetail.getSubject_marks_type().equalsIgnoreCase("Percentage")&& !qualificationDetail.getSubject_marks_type().equalsIgnoreCase("CGPA") && !qualificationDetail.getSubject_marks_type().equalsIgnoreCase("Grade"))
        {
            throw new IllegalArgumentException("Subject marks type must be either percentage or Grade or CGPA");
        }
        if(qualificationDetail.getSubject_marks_type().trim().isEmpty())
        {
            throw new IllegalArgumentException("Subject marks type cannot be empty");
        }

        List<Long> subjectIds = qualificationDetail.getSubject_ids(); // Selected subject IDs
        List<SubjectDetail> userProvidedDetails = qualificationDetail.getSubjectDetails(); // User-provided details

        List<SubjectDetail> subjectDetailsList = new ArrayList<>();

        for (Long subjectId : subjectIds) {
            // Find the subject
            CustomSubject customSubject = entityManager.find(CustomSubject.class, subjectId);
            if (customSubject == null) {
                throw new IllegalArgumentException("Subject with ID " + subjectId + " not found");
            }

            // Match user input with the subject ID
            SubjectDetail userDetail = userProvidedDetails.stream()
                    .filter(detail -> detail.getSubject_detail_id().equals(subjectId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Details for subject ID " + subjectId + " are missing"));

            // Create and populate SubjectDetail
            SubjectDetail subjectDetail = new SubjectDetail();
            subjectDetail.setCustomSubject(customSubject);
            subjectDetail.setQualificationDetails(qualificationDetail);
            subjectDetail.setSubject_marks_obtained(userDetail.getSubject_marks_obtained());
            subjectDetail.setSubject_total_marks(userDetail.getSubject_total_marks());
            // Automatically set default values for other fields if needed
            subjectDetail.setSubject_equivalent_percentage(userDetail.getSubject_equivalent_percentage());

            subjectDetailsList.add(subjectDetail);
        }

        qualificationDetail.setSubjectDetails(subjectDetailsList);
        entityManager.merge(qualificationDetail);
    }

}