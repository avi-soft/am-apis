package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.dto.UpdateQualificationDto;
import com.community.api.endpoint.avisoft.controller.Qualification.QualificationController;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.BoardUniversity;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomStream;
import com.community.api.entity.CustomSubject;
import com.community.api.entity.Institution;
import com.community.api.entity.OtherItem;
import com.community.api.entity.Qualification;
import com.community.api.entity.QualificationDetails;
import com.community.api.entity.ScoringCriteria;
import com.community.api.entity.SubjectDetail;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.exception.CustomerDoesNotExistsException;
import com.community.api.services.exception.EntityAlreadyExistsException;
import com.community.api.services.exception.EntityDoesNotExistsException;
import com.community.api.services.exception.ExaminationDoesNotExistsException;
import com.community.api.utils.Document;
import com.community.api.utils.DocumentType;
import com.community.api.utils.ServiceProviderDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QualificationDetailsService {
    EntityManager entityManager;
    QualificationController qualificationController;
    QualificationService qualificationService;
    SharedUtilityService sharedUtilityService;
    ServiceProviderServiceImpl serviceProviderService;
    BoardUniversityService boardUniversityService;
    InstitutionService institutionService;
    StreamService streamService ;
    SubjectService subjectService;

    public QualificationDetailsService(EntityManager entityManager, QualificationController qualificationController, QualificationService qualificationService, SharedUtilityService sharedUtilityService, ServiceProviderServiceImpl serviceProviderService,BoardUniversityService boardUniversityService,StreamService streamService,SubjectService subjectService,InstitutionService institutionService) {
        this.entityManager = entityManager;
        this.qualificationController = qualificationController;
        this.qualificationService = qualificationService;
        this.sharedUtilityService = sharedUtilityService;
        this.serviceProviderService=serviceProviderService;
        this.boardUniversityService=boardUniversityService;
        this.streamService=streamService;
        this.subjectService=subjectService;
        this.institutionService=institutionService;
    }

    @Transactional
    public QualificationDetails addQualificationDetails(Long userId, QualificationDetails qualificationDetails,String boardUniversityOthers,Integer roleId, String roleName )
            throws EntityAlreadyExistsException, ExaminationDoesNotExistsException, CustomerDoesNotExistsException {

        if (roleName.equals(Constant.SERVICE_PROVIDER)) {
            ServiceProviderEntity serviceProviderEntity = findServiceProviderById(userId);
            List<Qualification> qualifications = qualificationService.getAllQualifications();
            Integer qualificationToAdd = findQualificationId(qualificationDetails.getQualification_id(), qualifications);
            qualificationDetails.setQualification_id(qualificationToAdd);
            List<Institution> institutions = institutionService.getAllInstitutions();
            Long institutionToAdd = findInstitutionId(qualificationDetails.getInstitution_id(), institutions);
            qualificationDetails.setInstitution_id(institutionToAdd);
            List<BoardUniversity> boardUniversities = boardUniversityService.getAllBoardUniversities();
            Long boardUniversityToAdd = findBoardUniversityById(qualificationDetails.getBoard_university_id(), boardUniversities);
            OtherItem boardUniversityOtherItemToAdd=handleOtherCaseForBoardUniversity(boardUniversityToAdd,boardUniversityOthers,roleId,userId);
            qualificationDetails.setBoard_university_id(boardUniversityToAdd);
    /*      List<Long> subjects = validateAndGetSubjectIds(qualificationDetails.getSubject_ids());
            qualificationDetails.setSubject_ids(subjects);*/
            if (qualificationDetails.getSubject_name() == null) {
                throw new IllegalArgumentException("Subject_name cannot be null");
            }
            validateQualificationDetail(qualificationDetails);
            List<CustomStream> streams = streamService.getAllStream();
            Long streamToAdd = findStreamId(qualificationDetails.getStream_id(), streams);
            qualificationDetails.setStream_id(streamToAdd);
            qualificationDetails.setService_provider(serviceProviderEntity);
            if (serviceProviderEntity.getQualificationDetailsList().isEmpty()) {
                serviceProviderEntity.getQualificationDetailsList().add(qualificationDetails);
            } else if (!serviceProviderEntity.getQualificationDetailsList().isEmpty()) {
                serviceProviderEntity.getQualificationDetailsList().clear();
                serviceProviderEntity.getQualificationDetailsList().add(qualificationDetails);
                List<ServiceProviderDocument> serviceProviderDocuments = serviceProviderEntity.getDocuments();
                if (!serviceProviderDocuments.isEmpty()) {
                    for (ServiceProviderDocument serviceProviderDocument : serviceProviderDocuments) {
                        iterateQualificationsToDeleteDocumentsForServiceProvider(serviceProviderDocument, qualifications);
                    }
                }
            }
            entityManager.persist(qualificationDetails);
            if(boardUniversityOthers!=null)
            {
                qualificationDetails.setOtherItems(new ArrayList<>(List.of(boardUniversityOtherItemToAdd)));
                QualificationDetails addedQualificationDetails=entityManager.merge(qualificationDetails);
                entityManager.merge(boardUniversityOtherItemToAdd);
                giveQualificationScore(userId);
                return addedQualificationDetails;
            }
            giveQualificationScore(userId);
            return qualificationDetails;

        }
        CustomCustomer customCustomer = findCustomCustomerById(userId);
        checkIfQualificationAlreadyExists(userId, qualificationDetails.getQualification_id(), roleName);
        List<Qualification> qualifications = qualificationService.getAllQualifications();
        Integer qualificationToAdd = findQualificationId(qualificationDetails.getQualification_id(), qualifications);
        qualificationDetails.setQualification_id(qualificationToAdd);
        List<Institution> institutions = institutionService.getAllInstitutions();
        Long institutionToAdd = findInstitutionId(qualificationDetails.getInstitution_id(), institutions);
        qualificationDetails.setInstitution_id(institutionToAdd);
        List<BoardUniversity> boardUniversities = boardUniversityService.getAllBoardUniversities();
        Long boardUniversityToAdd = findBoardUniversityById(qualificationDetails.getBoard_university_id(), boardUniversities);
        OtherItem boardUniversityOtherItemToAdd=handleOtherCaseForBoardUniversity(boardUniversityToAdd,boardUniversityOthers,roleId,userId);
        qualificationDetails.setBoard_university_id(boardUniversityToAdd);
        Qualification qualificationToSearch= entityManager.find(Qualification.class,qualificationDetails.getQualification_id());
        Boolean subjectValidationCheck= null;
        if(qualificationToSearch!=null)
        {
            subjectValidationCheck=qualificationToSearch.getIs_subjects_required();
        }
        if(subjectValidationCheck.equals(true)) {
            if (qualificationDetails.getSubject_ids() == null || qualificationDetails.getSubject_ids().isEmpty()) {
                throw new IllegalArgumentException("Subjects list cannot be empty");
            }
        }
        if(!(qualificationDetails.getSubject_ids()==null|| qualificationDetails.getSubject_ids().isEmpty()))
        {
            List<Long> subjects = validateAndGetSubjectIds(qualificationDetails.getSubject_ids());
            qualificationDetails.setSubject_ids(subjects);
            createSubjectDetails(qualificationDetails);
            validateSubjectSizeForCustomer(qualificationDetails);
        }
        validateQualificationDetail(qualificationDetails);
        List<CustomStream> streams= streamService.getAllStream();
        Long streamToAdd= findStreamId(qualificationDetails.getStream_id(),streams);
        qualificationDetails.setStream_id(streamToAdd);
        qualificationDetails.setCustom_customer(customCustomer);
        customCustomer.getQualificationDetailsList().add(qualificationDetails);
        entityManager.persist(qualificationDetails);
        if(boardUniversityOthers!=null)
        {
            qualificationDetails.setOtherItems(new ArrayList<>(List.of(boardUniversityOtherItemToAdd)));
            QualificationDetails addedQualificationDetails=entityManager.merge(qualificationDetails);
            entityManager.merge(boardUniversityOtherItemToAdd);
            return addedQualificationDetails;
        }
       return qualificationDetails;
    }

    @Transactional
    public List<Map<String, Object>> getQualificationDetailsByCustomerId(Long userId, String roleName) throws CustomerDoesNotExistsException, RuntimeException {
        List<QualificationDetails> qualificationDetails;
        if (roleName.equals(Constant.SERVICE_PROVIDER)) {
            ServiceProviderEntity serviceProviderEntity = findServiceProviderById(userId);
            qualificationDetails = serviceProviderEntity.getQualificationDetailsList();
            return sharedUtilityService.mapQualificationsForServiceProvider(qualificationDetails);
        }
        CustomCustomer customCustomer = findCustomCustomerById(userId);
        qualificationDetails = customCustomer.getQualificationDetailsList();
        return sharedUtilityService.mapQualificationsForCustomer(qualificationDetails);
    }

    @Transactional
    public QualificationDetails deleteQualificationDetail(Long userId, Long qualificationId, String roleName) throws EntityDoesNotExistsException, CustomerDoesNotExistsException {
        List<QualificationDetails> qualificationDetails;
        ServiceProviderEntity serviceProviderEntity=null;
        CustomCustomer customCustomer=null;
        if (roleName.equals(Constant.SERVICE_PROVIDER)) {
            serviceProviderEntity= findServiceProviderById(userId);
            qualificationDetails = serviceProviderEntity.getQualificationDetailsList();
        } else {
            customCustomer  = findCustomCustomerById(userId);
            qualificationDetails = customCustomer.getQualificationDetailsList();
        }

        QualificationDetails qualificationDetailsToDelete = null;
        for (QualificationDetails qualificationDetails1 : qualificationDetails) {
            if (qualificationDetails1.getQualification_detail_id().equals(qualificationId)) {
                qualificationDetailsToDelete = qualificationDetails1;
                break;
            }
        }
        if (qualificationDetailsToDelete == null) {
            throw new EntityDoesNotExistsException("QualificationDetails with id " + qualificationId + " does not exists");
        }
        List<Qualification> qualifications= qualificationService.getAllQualifications();
        qualificationDetails.remove(qualificationDetailsToDelete);
        entityManager.remove(qualificationDetailsToDelete);
        if(roleName.equalsIgnoreCase("CUSTOMER"))
        {
            List<Document> customerDocuments= customCustomer.getDocuments();
            if(!customerDocuments.isEmpty())
            {
                for(Document customerDocument: customerDocuments)
                {
                    iterateQualificationsToDeleteDocumentsForCustomer(customerDocument,qualifications);
                }
            }
        }
       else if(roleName.equalsIgnoreCase(Constant.SERVICE_PROVIDER))
        {
            List<ServiceProviderDocument> serviceProviderDocuments= serviceProviderEntity.getDocuments();
            if(!serviceProviderDocuments.isEmpty())
            {
                for(ServiceProviderDocument serviceProviderDocument: serviceProviderDocuments)
                {
                    iterateQualificationsToDeleteDocumentsForServiceProvider(serviceProviderDocument,qualifications);
                }
            }
            giveQualificationScore(userId);
        }
        return qualificationDetailsToDelete;
    }

    @Transactional
    public QualificationDetails updateQualificationDetail(Long userId, Long qualificationId, UpdateQualificationDto qualification, String roleName) throws EntityDoesNotExistsException, EntityAlreadyExistsException, CustomerDoesNotExistsException, ExaminationDoesNotExistsException {
        String marksType=null;
        String marksObtained=null;
        String totalMarks=null;
        Integer qualificationIdToUpdate=null;
        String subjectMarksType=null;
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
            if (qualificationDetails1.getQualification_detail_id().equals(qualificationId)) {
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
        if("CUSTOMER".equalsIgnoreCase(roleName))
        {
            QualificationDetails existingQualificationDetails = query.getResultStream().findFirst().orElse(null);

            if (existingQualificationDetails != null && !qualificationId.equals(existingQualificationDetails.getQualification_detail_id())) {
                throw new EntityAlreadyExistsException("Qualification details with id " + qualification.getQualification_id() + " already exists");
            }
            if (Objects.nonNull(qualification.getSubject_ids())) {
                List<Long> subjects = validateAndGetSubjectIds(qualification.getSubject_ids());
                qualificationDetailsToUpdate.setSubject_ids(subjects);
            }
        }
        else if("SERVICE_PROVIDER".equalsIgnoreCase(roleName))
        {
            if (Objects.nonNull(qualification.getSubject_name())) {
                qualificationDetailsToUpdate.setSubject_name(qualification.getSubject_name());
            }
        }

        if (Objects.nonNull(qualification.getQualification_id())) {
            List<Qualification> qualificationDetailsList = qualificationService.getAllQualifications();
            Integer qualificationToAdd = findQualificationId(qualification.getQualification_id(), qualificationDetailsList);
            qualificationDetailsToUpdate.setQualification_id(qualificationToAdd);

            //Qualification scoring
            if(roleName.equalsIgnoreCase(Constant.SERVICE_PROVIDER))
            {
                giveQualificationScore(userId);
            }
            qualificationIdToUpdate=qualification.getQualification_id();
        }
        else {
            qualificationIdToUpdate=qualificationDetailsToUpdate.getQualification_id();
        }

        if(Objects.nonNull(qualification.getBoard_university_id()))
        {
            List<BoardUniversity> boardUniversities = boardUniversityService.getAllBoardUniversities();
            Long boardUniversityToAdd= findBoardUniversityById(qualification.getBoard_university_id(),boardUniversities);
            qualificationDetailsToUpdate.setBoard_university_id(boardUniversityToAdd);
        }
        if(Objects.nonNull(qualification.getInstitution_id()))
        {
            List<Institution> institutions = institutionService.getAllInstitutions();
            Long institutionToAdd= findInstitutionId(qualification.getInstitution_id(),institutions);
            qualificationDetailsToUpdate.setInstitution_id(institutionToAdd);
        }

        if (Objects.nonNull(qualification.getExamination_role_number())) {
            qualificationDetailsToUpdate.setExamination_role_number(qualification.getExamination_role_number());
        }
        if (Objects.nonNull(qualification.getExamination_registration_number())) {
            qualificationDetailsToUpdate.setExamination_registration_number(qualification.getExamination_registration_number());
        }

        if(Objects.nonNull(qualification.getTotal_marks_type()))
        {
            if(!qualification.getTotal_marks_type().equalsIgnoreCase("Percentage")&& !qualification.getTotal_marks_type().equalsIgnoreCase("CGPA") && !qualification.getTotal_marks_type().equalsIgnoreCase("Grade"))
            {
                throw new IllegalArgumentException("Total marks type must be either percentage or Grade or CGPA");
            }
            if(qualification.getTotal_marks_type().trim().isEmpty())
            {
                throw new IllegalArgumentException("Total marks type cannot be empty");
            }
            qualificationDetailsToUpdate.setTotal_marks_type(qualification.getTotal_marks_type());
            marksType=qualification.getTotal_marks_type();
        }
        else {
            marksType= qualificationDetailsToUpdate.getTotal_marks_type();
        }

        if (Objects.nonNull(qualification.getMarks_obtained())) {
            if(marksType.equalsIgnoreCase("Percentage") || marksType.equalsIgnoreCase("CGPA"))
            {
                if (!qualification.getMarks_obtained().matches("-?\\d+(\\.\\d+)?")) { // Regex to allow integers or decimals
                    throw new IllegalArgumentException("Overall Marks obtained must be a valid numeric value");
                }
            }
            else if(marksType.equalsIgnoreCase("Grade")) {
                String gradePattern = "^[A-Z]([+-]?)$";

                if (!qualification.getMarks_obtained().trim().matches(gradePattern)) {
                    throw new IllegalArgumentException("Overall marks obtained should be a valid grade (A, A+, B-, etc.)");
                }
            }
            marksObtained=qualification.getMarks_obtained();
        }
        else {
            if(marksType.equalsIgnoreCase("Percentage") || marksType.equalsIgnoreCase("CGPA"))
            {
                if (!qualificationDetailsToUpdate.getMarks_obtained().matches("-?\\d+(\\.\\d+)?")) { // Regex to allow integers or decimals
                    throw new IllegalArgumentException("Overall Marks obtained must be a valid numeric value");
                }
            }
            else if(marksType.equalsIgnoreCase("Grade")) {
                String gradePattern = "^[A-Z]([+-]?)$";

                if (!qualificationDetailsToUpdate.getMarks_obtained().trim().matches(gradePattern)) {
                    throw new IllegalArgumentException("Overall marks obtained should be a valid grade (A, A+, B-, etc.)");
                }
            }
            marksObtained=qualificationDetailsToUpdate.getMarks_obtained();
        }

        if(Objects.nonNull(qualification.getTotal_marks()))
        {
            if(marksType.equalsIgnoreCase("Percentage") || marksType.equalsIgnoreCase("CGPA"))
            {
                if (!qualification.getTotal_marks().matches("-?\\d+(\\.\\d+)?")) { // Regex to allow integers or decimals
                    throw new IllegalArgumentException("Overall Total marks must be a valid numeric value (no alphabet or special characters) ");
                }
            }
            else if (marksType.equalsIgnoreCase("Grade"))
            {
                String gradePattern = "^[A-Z]([+-]?)$";
                if (!qualification.getTotal_marks().trim().matches(gradePattern)) {
                    throw new IllegalArgumentException("Overall total marks should be a valid grade (A, A+, B-, etc.) ");
                }
            }
            totalMarks= qualification.getTotal_marks();
        }
        else
        {
            if(marksType.equalsIgnoreCase("Percentage") || marksType.equalsIgnoreCase("CGPA"))
            {
                if (!qualificationDetailsToUpdate.getTotal_marks().matches("-?\\d+(\\.\\d+)?")) { // Regex to allow integers or decimals
                    throw new IllegalArgumentException("Overall Total marks must be a valid numeric value (no alphabet or special characters) ");
                }
            }
            else if (marksType.equalsIgnoreCase("Grade"))
            {
                String gradePattern = "^[A-Z]([+-]?)$";
                if (!qualificationDetailsToUpdate.getTotal_marks().trim().matches(gradePattern)) {
                    throw new IllegalArgumentException("Overall total marks should be a valid grade (A, A+, B-, etc.) ");
                }
            }
            totalMarks=qualificationDetailsToUpdate.getTotal_marks();
        }

        if(marksType.equalsIgnoreCase("Percentage") || marksType.equalsIgnoreCase("CGPA"))
        {
            Double overallObtainedMarks = Double.parseDouble(marksObtained);
            Double overallTotalMarks = Double.parseDouble(totalMarks);

            if (overallObtainedMarks < 0) {
                throw new IllegalArgumentException("Overall Marks obtained cannot be negative ");
            }
            if (overallTotalMarks <= 0) {
                throw new IllegalArgumentException("Overall Total marks must be greater than zero ");
            }
            if(overallObtainedMarks>overallTotalMarks)
            {
                throw new IllegalArgumentException("Overall Marks obtained cannot be greater than the total marks ");
            }
        }

        qualificationDetailsToUpdate.setMarks_obtained(marksObtained);
        qualificationDetailsToUpdate.setTotal_marks(totalMarks);

        if(marksType.equalsIgnoreCase("Percentage"))
        {
            Double percentage= (Double.parseDouble(marksObtained)/Double.parseDouble(totalMarks))*100;
            qualificationDetailsToUpdate.setCumulative_percentage_value(percentage);
        }

        if (Objects.nonNull(qualification.getStream_id())) {
            List<CustomStream> streams = streamService.getAllStream();
            Long streamToAdd= findStreamId(qualification.getStream_id(),streams);
            qualificationDetailsToUpdate.setStream_id(streamToAdd);
        }

        if (Objects.nonNull(qualification.getCumulative_percentage_value())) {
            qualificationDetailsToUpdate.setCumulative_percentage_value(qualification.getCumulative_percentage_value());
        }

        if (Objects.nonNull(qualification.getDate_of_passing())) {
            qualificationDetailsToUpdate.setDate_of_passing(qualification.getDate_of_passing());
        }
        if("CUSTOMER".equalsIgnoreCase(roleName))
        {
            Qualification qualificationToSearch= entityManager.find(Qualification.class,qualificationIdToUpdate);
            Boolean subjectValidationCheck= null;
            if(qualificationToSearch!=null)
            {
                subjectValidationCheck=qualificationToSearch.getIs_subjects_required();
            }
            if(Objects.nonNull(qualification.getSubject_ids()))
            {
                createSubjectDetailsForUpdateQualification(qualification,qualificationDetailsToUpdate);
                if(subjectValidationCheck.equals(true))
                {
                    if(qualification.getSubject_details().size()<5)
                    {
                        throw new IllegalArgumentException("You have to add at least five subjects");
                    }
                }
            }
            else
            {
                if(subjectValidationCheck.equals(true))
                {
                    if(qualificationDetailsToUpdate.getSubject_ids().isEmpty() || qualificationDetailsToUpdate.getSubject_ids()==null)
                    {
                        throw new IllegalArgumentException("You have to add at least five subjects");
                    }
                }
            }
        }
        return entityManager.merge(qualificationDetailsToUpdate);
    }

    public void validateQualificationDetail(QualificationDetails qualificationDetails)
    {
        Qualification qualificationToSearch= entityManager.find(Qualification.class,qualificationDetails.getQualification_id());
        Boolean streamValidationCheck= null;
        if(qualificationToSearch!=null)
        {
            streamValidationCheck=qualificationToSearch.getIs_stream_required();
        }
        if(streamValidationCheck.equals(true))
        {
            if(qualificationDetails.getStream_id()==null)
            {
                throw new IllegalArgumentException("Stream id cannot be null");
            }
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

        if(qualificationDetails.getTotal_marks_type().equalsIgnoreCase("Percentage") || qualificationDetails.getTotal_marks_type().equalsIgnoreCase("CGPA"))
        {
            String marksObtainedStr = qualificationDetails.getMarks_obtained();
            String totalMarksStr = qualificationDetails.getTotal_marks();

            // Check if the marks are valid numeric values (no alphabet or special characters)
            if (!marksObtainedStr.matches("-?\\d+(\\.\\d+)?")) { // Regex to allow integers or decimals
                throw new IllegalArgumentException("Overall Marks obtained must be a valid numeric value");
            }
            if (!totalMarksStr.matches("-?\\d+(\\.\\d+)?")) { // Regex to allow integers or decimals
                throw new IllegalArgumentException("Overall Total marks must be a valid numeric value (no alphabet or special characters) ");
            }
            Double marksObtained = Double.parseDouble(marksObtainedStr);
            Double totalMarks = Double.parseDouble(totalMarksStr);

            if (marksObtained < 0) {
                throw new IllegalArgumentException("Overall Marks obtained cannot be negative ");
            }
            if (totalMarks <= 0) {
                throw new IllegalArgumentException("Overall Total marks must be greater than zero ");
            }
            if(marksObtained>totalMarks)
            {
                throw new IllegalArgumentException("Overall Marks obtained cannot be greater than the total marks ");
            }
        }
        else if (qualificationDetails.getTotal_marks_type().equalsIgnoreCase("Grade"))
        {
            String gradeObtained = qualificationDetails.getMarks_obtained();
            String gradeTotal = qualificationDetails.getTotal_marks();

            String gradePattern = "^[A-Z]([+-]?)$";

            if (!gradeObtained.trim().matches(gradePattern)) {
                throw new IllegalArgumentException("Overall marks obtained should be a valid grade (A, A+, B-, etc.)");
            }

            if (!gradeTotal.trim().matches(gradePattern)) {
                throw new IllegalArgumentException("Overall total marks should be a valid grade (A, A+, B-, etc.) ");
            }
        }

        if(qualificationDetails.getTotal_marks_type().equalsIgnoreCase("Percentage"))
        {
            Double percentage= (Double.parseDouble(qualificationDetails.getMarks_obtained())/Double.parseDouble(qualificationDetails.getTotal_marks()))*100;
            qualificationDetails.setCumulative_percentage_value(percentage);
        }
        else if(qualificationDetails.getTotal_marks_type().equalsIgnoreCase("CGPA") || qualificationDetails.getTotal_marks_type().equalsIgnoreCase("Grade"))
        {
            if(qualificationDetails.getCumulative_percentage_value()==null)
            {
                throw new IllegalArgumentException("Overall Cumulative Percentage value cannot be null");
            }
        }
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

    public Integer findQualificationId(Integer qualificationId, List<Qualification> qualifications) throws ExaminationDoesNotExistsException {
        for (Qualification qualification : qualifications) {
            if (qualification.getQualification_id().equals(qualificationId)) {
                return qualification.getQualification_id();
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
    public Long findInstitutionId(Long institutionId,List<Institution> institutions)
    {
        for(Institution institution : institutions)
        {
            if(institution.getInstitution_id().equals(institutionId))
            {
                return institution.getInstitution_id();
            }
        }
        throw new IllegalArgumentException("Institution with id "+ institutionId+ " does not exist");
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
        Set<Long> uniqueSubjectIds = new HashSet<>(subjectIds);
        if (uniqueSubjectIds.size() != subjectIds.size()) {
            throw new IllegalArgumentException("Duplicate subject IDs are not allowed.");
        }

        // Query to check which subject IDs exist in the database
        if(!(subjectIds==null|| subjectIds.isEmpty()))
        {
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
       return null;
    }

    @Transactional
    public void iterateQualificationsToDeleteDocumentsForServiceProvider(ServiceProviderDocument serviceProviderDocument, List<Qualification> qualifications)
    {
        for(Qualification qualification: qualifications)
        {
            if(qualification.getQualification_id().equals(serviceProviderDocument.getDocumentType().getDocument_type_id()))
            {
                entityManager.remove(serviceProviderDocument);
            }
        }
    }
    @Transactional
    public void iterateQualificationsToDeleteDocumentsForCustomer(Document custmerDocument, List<Qualification> qualifications)
    {
        for(Qualification qualification: qualifications)
        {
            if(qualification.getQualification_id().equals(custmerDocument.getDocumentType().getDocument_type_id()))
            {
                entityManager.remove(custmerDocument);
            }
        }
    }

    public void giveQualificationScore(Long userId) throws CustomerDoesNotExistsException {
        ServiceProviderEntity serviceProviderEntity = findServiceProviderById(userId);
        TypedQuery<ScoringCriteria> typedQuery=  entityManager.createQuery(Constant.GET_ALL_SCORING_CRITERIA, ScoringCriteria.class);
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

    public void validateSubjectSizeForCustomer(QualificationDetails qualificationDetails)
    {
        Qualification qualificationToSearch= entityManager.find(Qualification.class,qualificationDetails.getQualification_id());
        Boolean subjectValidationCheck= null;
        if(qualificationToSearch!=null)
        {
            subjectValidationCheck=qualificationToSearch.getIs_subjects_required();
        }
        if(subjectValidationCheck.equals(true))
        {
            if(qualificationDetails.getSubject_details().size()<5)
            {
                throw new IllegalArgumentException("You have to add at least five subjects");
            }
        }
    }

    @Transactional
    public void createSubjectDetails(QualificationDetails qualificationDetail) {
        List<Long> subjectIds = qualificationDetail.getSubject_ids();
        List<SubjectDetail> userProvidedDetails = qualificationDetail.getSubject_details();
        if (subjectIds == null || subjectIds.isEmpty()) {
            throw new IllegalArgumentException("Subject IDs list cannot be empty");
        }
        if (userProvidedDetails == null || userProvidedDetails.isEmpty() || userProvidedDetails.size() != subjectIds.size()) {
            throw new IllegalArgumentException("Subject details must be provided for all subject IDs");
        }

        List<SubjectDetail> subjectDetailsList = new ArrayList<>();

        // Iterate over subject IDs and corresponding user details
        for (int i = 0; i < subjectIds.size(); i++) {
            Long subjectId = subjectIds.get(i);
            SubjectDetail userDetail = userProvidedDetails.get(i);

            // Find the subject
            CustomSubject customSubject = entityManager.find(CustomSubject.class, subjectId);
            if (customSubject == null) {
                throw new IllegalArgumentException("Subject with ID " + subjectId + " not found");
            }

            // Create and populate SubjectDetail
            SubjectDetail subjectDetail = new SubjectDetail();
            validateSubjectDetails(userDetail,qualificationDetail,customSubject);
            subjectDetail.setCustomSubject(customSubject);
            subjectDetail.setQualificationDetails(qualificationDetail);
            subjectDetail.setSubject_marks_obtained(userDetail.getSubject_marks_obtained());
            subjectDetail.setSubject_total_marks(userDetail.getSubject_total_marks());
            subjectDetail.setSubject_marks_type(userDetail.getSubject_marks_type());
            if(subjectDetail.getSubject_marks_type().equalsIgnoreCase("Percentage"))
            {
                subjectDetail.setSubject_equivalent_percentage((Double.parseDouble(userDetail.getSubject_marks_obtained())/Double.parseDouble(userDetail.getSubject_total_marks()))*100);
            }
            else if(subjectDetail.getSubject_marks_type().equalsIgnoreCase("CGPA") || subjectDetail.getSubject_marks_type().equalsIgnoreCase("Grade"))
            {
                subjectDetail.setSubject_equivalent_percentage(userDetail.getSubject_equivalent_percentage());
            }
            subjectDetailsList.add(subjectDetail);
        }
        qualificationDetail.setSubject_details(subjectDetailsList);
    }

    @Transactional
    public void createSubjectDetailsForUpdateQualification(UpdateQualificationDto qualificationDetail, QualificationDetails qualificationDetailsToUpdate) {

        List<Long> subjectIds = qualificationDetail.getSubject_ids();
        List<SubjectDetail> userProvidedDetails = qualificationDetail.getSubject_details();

        if (subjectIds == null || subjectIds.isEmpty() || userProvidedDetails == null || userProvidedDetails.isEmpty() || userProvidedDetails.size() != subjectIds.size()) {
            throw new IllegalArgumentException("Subject details must be provided for all subject IDs");
        }

        List<SubjectDetail> subjectDetailsList = new ArrayList<>();
        qualificationDetailsToUpdate.getSubject_details().forEach(detail -> detail.setQualificationDetails(null));
        qualificationDetailsToUpdate.getSubject_details().clear();

        for (int i = 0; i < subjectIds.size(); i++) {
            Long subjectId = subjectIds.get(i);
            SubjectDetail userDetail = userProvidedDetails.get(i);

            CustomSubject customSubject = entityManager.find(CustomSubject.class, subjectId);
            if (customSubject == null) {
                throw new IllegalArgumentException("Subject with ID " + subjectId + " not found");
            }

            SubjectDetail subjectDetail = new SubjectDetail();
            validateSubjectDetailsForUpdateQualification(userDetail, customSubject);
            subjectDetail.setCustomSubject(customSubject);
            subjectDetail.setQualificationDetails(qualificationDetailsToUpdate);
            subjectDetail.setSubject_marks_obtained(userDetail.getSubject_marks_obtained());
            subjectDetail.setSubject_total_marks(userDetail.getSubject_total_marks());
            subjectDetail.setSubject_marks_type(userDetail.getSubject_marks_type());
            if(subjectDetail.getSubject_marks_type().equalsIgnoreCase("Percentage"))
            {
                subjectDetail.setSubject_equivalent_percentage((Double.parseDouble(userDetail.getSubject_marks_obtained())/Double.parseDouble(userDetail.getSubject_total_marks()))*100);
            }
            else if(subjectDetail.getSubject_marks_type().equalsIgnoreCase("CGPA") || subjectDetail.getSubject_marks_type().equalsIgnoreCase("Grade"))
            {
                subjectDetail.setSubject_equivalent_percentage(userDetail.getSubject_equivalent_percentage());
            }

            subjectDetailsList.add(subjectDetail);
        }

        qualificationDetailsToUpdate.getSubject_details().addAll(subjectDetailsList);
        entityManager.merge(qualificationDetailsToUpdate);
    }

    public void validateSubjectDetails(SubjectDetail subjectDetail,QualificationDetails qualificationDetails,CustomSubject customSubject)
    {
        if(subjectDetail.getSubject_marks_obtained() ==null|| subjectDetail.getSubject_total_marks()==null)
        {
            throw new IllegalArgumentException("Both subject marks obtained and subject total marks cannot be null for subject "+ customSubject.getSubjectName() + " with subject_id "+ customSubject.getSubjectId());
        }
        if(subjectDetail.getSubject_marks_obtained().trim().isEmpty() || subjectDetail.getSubject_total_marks().trim().isEmpty())
        {
            throw new IllegalArgumentException("Both obtained and total subject marks cannot be empty for subject "+ customSubject.getSubjectName() + " with subject_id "+ customSubject.getSubjectId());
        }

        if(subjectDetail.getSubject_marks_type()==null)
        {
            throw new IllegalArgumentException("You have to select whether the you want to add the total marks in normal marks, cgpa or grade for subject "+ customSubject.getSubjectName() + " with subject_id "+ customSubject.getSubjectId());
        }

        if(!subjectDetail.getSubject_marks_type().equalsIgnoreCase("Percentage")&& !subjectDetail.getSubject_marks_type().equalsIgnoreCase("CGPA") && !subjectDetail.getSubject_marks_type().equalsIgnoreCase("Grade"))
        {
            throw new IllegalArgumentException("Subject marks type must be either percentage or Grade or CGPA for subject "+ customSubject.getSubjectName() + " with subject_id "+ customSubject.getSubjectId());
        }
        if(subjectDetail.getSubject_marks_type().trim().isEmpty())
        {
            throw new IllegalArgumentException("Subject marks type cannot be empty for subject "+ customSubject.getSubjectName() + " with subject_id "+ customSubject.getSubjectId());
        }
        if(subjectDetail.getSubject_marks_type().equalsIgnoreCase("Grade")|| subjectDetail.getSubject_marks_type().equalsIgnoreCase("CGPA"))
        {
            if(subjectDetail.getSubject_equivalent_percentage()==null)
            {
                throw new IllegalArgumentException("Equivalent percentage cannot be null for subject"+ customSubject.getSubjectName() + " with subject_id "+ customSubject.getSubjectId());
            }
            if (subjectDetail.getSubject_equivalent_percentage() < 0 || subjectDetail.getSubject_equivalent_percentage() > 100) {
                throw new IllegalArgumentException("Equivalent percentage must be between 0 and 100 for subject "
                        + customSubject.getSubjectName() + " with subject_id " + customSubject.getSubjectId());
            }
        }

        if(subjectDetail.getSubject_marks_type().equalsIgnoreCase("Percentage") || subjectDetail.getSubject_marks_type().equalsIgnoreCase("CGPA"))
        {
            try {
                String marksObtainedStr = subjectDetail.getSubject_marks_obtained();
                String totalMarksStr = subjectDetail.getSubject_total_marks();

                // Check if the marks are valid numeric values (no alphabet or special characters)
                if (!marksObtainedStr.matches("-?\\d+(\\.\\d+)?")) { // Regex to allow integers or decimals
                    throw new IllegalArgumentException("Marks obtained must be a valid numeric value (no alphabet or special characters) for subject "
                            + customSubject.getSubjectName() + " with subject_id " + customSubject.getSubjectId());
                }
                if (!totalMarksStr.matches("-?\\d+(\\.\\d+)?")) { // Regex to allow integers or decimals
                    throw new IllegalArgumentException("Total marks must be a valid numeric value (no alphabet or special characters) for subject "
                            + customSubject.getSubjectName() + " with subject_id " + customSubject.getSubjectId());
                }
                Double marksObtained = Double.parseDouble(subjectDetail.getSubject_marks_obtained());
                Double totalMarks = Double.parseDouble(subjectDetail.getSubject_total_marks());

                if (marksObtained < 0) {
                    throw new IllegalArgumentException("Marks obtained cannot be negative for subject "+  customSubject.getSubjectName() + " with subject_id "+ customSubject.getSubjectId());
                }
                if (totalMarks <= 0) {
                    throw new IllegalArgumentException("Total marks must be greater than zero subject "+  customSubject.getSubjectName() + " with subject_id "+ customSubject.getSubjectId());
                }
                if(marksObtained>totalMarks)
                {
                    throw new IllegalArgumentException("Marks obtained cannot be greater than the total marks for subject  "+  customSubject.getSubjectName() + " with subject_id "+ customSubject.getSubjectId());
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Marks obtained and total marks must be numeric values for Percentage or CGPA for subject "+  customSubject.getSubjectName() + " with subject_id "+ customSubject.getSubjectId());
            }

        }
        else if(subjectDetail.getSubject_marks_type().equalsIgnoreCase("Grade"))
        {
            String gradeObtained = subjectDetail.getSubject_marks_obtained();
            String gradeTotal = subjectDetail.getSubject_total_marks();

            String gradePattern = "^[A-Z]([+-]?)$";

            // Validate that gradeObtained matches the grade pattern
            if (!gradeObtained.trim().matches(gradePattern)) {
                throw new IllegalArgumentException("Subject marks obtained should be a valid grade (A, A+, B-, etc.) for subject "
                        + customSubject.getSubjectName() + " with subject_id " + customSubject.getSubjectId());
            }

            // Validate that gradeTotal matches the grade pattern
            if (!gradeTotal.trim().matches(gradePattern)) {
                throw new IllegalArgumentException("Subject marks total should be a valid grade (A, A+, B-, etc.) for subject "
                        + customSubject.getSubjectName() + " with subject_id " + customSubject.getSubjectId());
            }
        }
    }

    public void validateSubjectDetailsForUpdateQualification(SubjectDetail subjectDetail,CustomSubject customSubject)
    {
        if(subjectDetail.getSubject_marks_obtained() ==null|| subjectDetail.getSubject_total_marks()==null)
        {
            throw new IllegalArgumentException("Both subject marks obtained and subject total marks cannot be null for subject "+ customSubject.getSubjectName() + " with subject_id "+ customSubject.getSubjectId());
        }
        if(subjectDetail.getSubject_marks_obtained().trim().isEmpty() || subjectDetail.getSubject_total_marks().trim().isEmpty())
        {
            throw new IllegalArgumentException("Both obtained and total subject marks cannot be empty for subject "+ customSubject.getSubjectName() + " with subject_id "+ customSubject.getSubjectId());
        }
        if(subjectDetail.getSubject_marks_type()==null)
        {
            throw new IllegalArgumentException("You have to select whether the you want to add the total marks in normal marks, cgpa or grade for subject "+ customSubject.getSubjectName() + " with subject_id "+ customSubject.getSubjectId());
        }

        if(!subjectDetail.getSubject_marks_type().equalsIgnoreCase("Percentage")&& !subjectDetail.getSubject_marks_type().equalsIgnoreCase("CGPA") && !subjectDetail.getSubject_marks_type().equalsIgnoreCase("Grade"))
        {
            throw new IllegalArgumentException("Subject marks type must be either percentage or Grade or CGPA for subject "+ customSubject.getSubjectName() + " with subject_id "+ customSubject.getSubjectId());
        }
        if(subjectDetail.getSubject_marks_type().trim().isEmpty())
        {
            throw new IllegalArgumentException("Subject marks type cannot be empty for subject "+ customSubject.getSubjectName() + " with subject_id "+ customSubject.getSubjectId());
        }
        if(subjectDetail.getSubject_marks_type().equalsIgnoreCase("Grade")|| subjectDetail.getSubject_marks_type().equalsIgnoreCase("CGPA"))
        {
            if(subjectDetail.getSubject_equivalent_percentage()==null)
            {
                throw new IllegalArgumentException("Equivalent percentage cannot be null for subject"+ customSubject.getSubjectName() + " with subject_id "+ customSubject.getSubjectId());
            }
            if (subjectDetail.getSubject_equivalent_percentage() < 0 || subjectDetail.getSubject_equivalent_percentage() > 100) {
                throw new IllegalArgumentException("Equivalent percentage must be between 0 and 100 for subject "
                        + customSubject.getSubjectName() + " with subject_id " + customSubject.getSubjectId());
            }
        }

        if(subjectDetail.getSubject_marks_type().equalsIgnoreCase("Percentage") || subjectDetail.getSubject_marks_type().equalsIgnoreCase("CGPA"))
        {
            try {
                String marksObtainedStr = subjectDetail.getSubject_marks_obtained();
                String totalMarksStr = subjectDetail.getSubject_total_marks();

                // Check if the marks are valid numeric values (no alphabet or special characters)
                if (!marksObtainedStr.matches("-?\\d+(\\.\\d+)?")) { // Regex to allow integers or decimals
                    throw new IllegalArgumentException("Marks obtained must be a valid numeric value (no alphabet or special characters) for subject "
                            + customSubject.getSubjectName() + " with subject_id " + customSubject.getSubjectId());
                }
                if (!totalMarksStr.matches("-?\\d+(\\.\\d+)?")) { // Regex to allow integers or decimals
                    throw new IllegalArgumentException("Total marks must be a valid numeric value (no alphabet or special characters) for subject "
                            + customSubject.getSubjectName() + " with subject_id " + customSubject.getSubjectId());
                }
                Double marksObtained = Double.parseDouble(subjectDetail.getSubject_marks_obtained());
                Double totalMarks = Double.parseDouble(subjectDetail.getSubject_total_marks());

                if (marksObtained < 0) {
                    throw new IllegalArgumentException("Marks obtained cannot be negative for subject "+  customSubject.getSubjectName() + " with subject_id "+ customSubject.getSubjectId());
                }
                if (totalMarks <= 0) {
                    throw new IllegalArgumentException("Total marks must be greater than zero subject "+  customSubject.getSubjectName() + " with subject_id "+ customSubject.getSubjectId());
                }
                if(marksObtained>totalMarks)
                {
                    throw new IllegalArgumentException("Marks obtained cannot be greater than the total marks for subject  "+  customSubject.getSubjectName() + " with subject_id "+ customSubject.getSubjectId());
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Marks obtained and total marks must be numeric values for Percentage or CGPA for subject "+  customSubject.getSubjectName() + " with subject_id "+ customSubject.getSubjectId());
            }

        }
        else if(subjectDetail.getSubject_marks_type().equalsIgnoreCase("Grade"))
        {
            String gradeObtained = subjectDetail.getSubject_marks_obtained();
            String gradeTotal = subjectDetail.getSubject_total_marks();

            String gradePattern = "^[A-Z]([+-]?)$";

            // Validate that gradeObtained matches the grade pattern
            if (!gradeObtained.trim().matches(gradePattern)) {
                throw new IllegalArgumentException("Subject marks obtained should be a valid grade (A, A+, B-, etc.) for subject "
                        + customSubject.getSubjectName() + " with subject_id " + customSubject.getSubjectId());
            }

            // Validate that gradeTotal matches the grade pattern
            if (!gradeTotal.trim().matches(gradePattern)) {
                throw new IllegalArgumentException("Subject marks total should be a valid grade (A, A+, B-, etc.) for subject "
                        + customSubject.getSubjectName() + " with subject_id " + customSubject.getSubjectId());
            }
        }
    }

    public OtherItem handleOtherCaseForBoardUniversity(Long foundedBoardUniversityId,String boardUniversityOthers,Integer roleId,Long userId)
    {
        if(foundedBoardUniversityId.equals(1L))
        {
            if(boardUniversityOthers==null) {
                throw new IllegalArgumentException("You have to enter a text for other board/university");
            }
            if(boardUniversityOthers.trim().isEmpty())
            {
                throw new IllegalArgumentException("The text field cannot be empty for adding other board/university");
            }
            OtherItem otherItem =new OtherItem();
            otherItem.setTyped_text(boardUniversityOthers);
            otherItem.setField_name("Board or University");
            otherItem.setSource_name("Qualification Details");
            otherItem.setRole_id(roleId);
            otherItem.setUser_id(userId);
            entityManager.persist(otherItem);
            return otherItem;
        }
        return null;
    }

}