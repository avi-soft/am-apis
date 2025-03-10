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
import com.community.api.services.exception.ExceptionHandlingService;
import com.community.api.utils.CustomDateDeserializer;
import com.community.api.utils.Document;
import com.community.api.utils.ServiceProviderDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.community.api.endpoint.avisoft.controller.Customer.CustomerEndpoint.convertStringToDate;
import static com.community.api.utils.CustomDateDeserializer.validationState;

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
    ExceptionHandlingService exceptionHandlingService;

    public QualificationDetailsService(EntityManager entityManager, QualificationController qualificationController, QualificationService qualificationService, SharedUtilityService sharedUtilityService, ServiceProviderServiceImpl serviceProviderService,BoardUniversityService boardUniversityService,StreamService streamService,SubjectService subjectService,InstitutionService institutionService,ExceptionHandlingService exceptionHandlingService) {
        this.entityManager = entityManager;
        this.qualificationController = qualificationController;
        this.qualificationService = qualificationService;
        this.sharedUtilityService = sharedUtilityService;
        this.serviceProviderService=serviceProviderService;
        this.boardUniversityService=boardUniversityService;
        this.streamService=streamService;
        this.subjectService=subjectService;
        this.institutionService=institutionService;
        this.exceptionHandlingService= exceptionHandlingService;
    }

    @Transactional
    public QualificationDetails addQualificationDetails(Long userId, QualificationDetails qualificationDetails,String boardUniversityOthers,String streamOthers,String qualificationOthers,String institutionOthers,Integer roleId, String roleName )
            throws Exception {
        List<OtherItem> allOtherItemsToSave=new ArrayList<>();
        String sourceName= "add_qualification";
        Qualification qualificationToSearch= entityManager.find(Qualification.class,qualificationDetails.getQualification_id());
        if (roleName.equals(Constant.SERVICE_PROVIDER)) {
            ServiceProviderEntity serviceProviderEntity = findServiceProviderById(userId);
            dateValidations();

            List<Qualification> qualifications = qualificationService.getAllQualifications();
            Integer qualificationToAdd= findQualificationId(qualificationDetails.getQualification_id(), qualifications);
            OtherItem qualificationOtherItemToAdd=handleOtherCaseForQualification(qualificationToAdd,qualificationOthers,roleId,userId,sourceName);
            allOtherItemsToSave.add(qualificationOtherItemToAdd);
            qualificationDetails.setQualification_id(qualificationToAdd);

            validateQualificationDetail(qualificationDetails);
            List<Institution> institutions = institutionService.getAllInstitutions();
            Institution institutionToAdd = findInstitutionId(qualificationDetails.getInstitution().getInstitution_id(), institutions);
            OtherItem institutionOtherItemToAdd=handleOtherCaseForInstitution(institutionToAdd.getInstitution_id(),institutionOthers,roleId,userId,sourceName);
            allOtherItemsToSave.add(institutionOtherItemToAdd);
            qualificationDetails.setInstitution(institutionToAdd);
            List<BoardUniversity> boardUniversities = boardUniversityService.getAllBoardUniversities();
            Long boardUniversityToAdd = findBoardUniversityById(qualificationDetails.getBoard_university_id(), boardUniversities);
            OtherItem boardUniversityOtherItemToAdd=handleOtherCaseForBoardUniversity(boardUniversityToAdd,boardUniversityOthers,roleId,userId,sourceName);
            allOtherItemsToSave.add(boardUniversityOtherItemToAdd);
            qualificationDetails.setBoard_university_id(boardUniversityToAdd);
    /*      List<Long> subjects = validateAndGetSubjectIds(qualificationDetails.getSubject_ids());
            qualificationDetails.setSubject_ids(subjects);*/
            if (qualificationDetails.getSubject_name() == null) {
                throw new IllegalArgumentException("Subject_name cannot be null");
            }
            Long streamToAdd=null;
            OtherItem streamOtherItemToAdd=null;
            if(qualificationDetails.getQualification_id().equals(1))
            {
                qualificationDetails.setStream_id(0L);
            }
            else {
                List<CustomStream> streams = streamService.getStreamByQualificationId(qualificationDetails.getQualification_id());
                streamToAdd= findStreamId(qualificationDetails.getStream_id(),streams);
                streamOtherItemToAdd=handleOtherCaseForStream(streamToAdd,streamOthers,roleId,userId,sourceName);
                allOtherItemsToSave.add(streamOtherItemToAdd);
                qualificationDetails.setStream_id(streamToAdd);
            }
            qualificationDetails.setService_provider(serviceProviderEntity);
            if (serviceProviderEntity.getQualificationDetailsList().isEmpty()) {
                serviceProviderEntity.getQualificationDetailsList().add(qualificationDetails);
            }

            else if (!serviceProviderEntity.getQualificationDetailsList().isEmpty()) {
                serviceProviderEntity.getQualificationDetailsList().forEach(detail -> {
                    if (detail.getServiceProviderDocument() != null) {
                        ServiceProviderDocument serviceProviderDocument=detail.getServiceProviderDocument();
                        if(serviceProviderDocument.getQualificationDetails().getQualification_id().equals(detail.getQualification_id()))
                        {
                            serviceProviderDocument.setQualificationDetails(null);
                            serviceProviderDocument.setIsArchived(true);
                            entityManager.merge(serviceProviderDocument);
                        }
                    }
                });
                serviceProviderEntity.getQualificationDetailsList().clear();
            }

            serviceProviderEntity.getQualificationDetailsList().add(qualificationDetails);
            qualificationDetails.setService_provider(serviceProviderEntity);

            entityManager.persist(qualificationDetails);
            CustomStream customStream=null;
            if(streamToAdd!=null)
            {
                 customStream=entityManager.find(CustomStream.class,streamToAdd);
                if(customStream==null)
                {
                    throw new IllegalArgumentException("No stream found with id"+ customStream);
                }
            }

            if((boardUniversityOthers!=null && boardUniversityToAdd.equals(1L))||(institutionOthers!=null && institutionToAdd.getInstitution_name().equalsIgnoreCase("Others")) || (!qualificationDetails.getQualification_id().equals(1) &&
                    streamOthers != null && customStream.getStreamName().equalsIgnoreCase("Others")) || qualificationOthers != null && qualificationToSearch.getQualification_name().equalsIgnoreCase("Others"))
            {
                qualificationDetails.setOtherItems(allOtherItemsToSave);
                qualificationDetails.setOtherItems(allOtherItemsToSave);

                if(boardUniversityOtherItemToAdd!=null)
                {
                    entityManager.merge(boardUniversityOtherItemToAdd);
                    qualificationDetails.setOther_board_university(boardUniversityOthers);
                }
                if(institutionOtherItemToAdd!=null)
                {
                    entityManager.merge(institutionOtherItemToAdd);
                    qualificationDetails.setOther_institution(institutionOthers);
                }
                if(streamOtherItemToAdd!=null)
                {
                    entityManager.merge(streamOtherItemToAdd);
                    qualificationDetails.setOther_stream(streamOthers);
                }
                if(qualificationOtherItemToAdd!=null)
                {
                    entityManager.merge(qualificationOtherItemToAdd);
                    qualificationDetails.setOther_qualification(qualificationOthers);
                }
                QualificationDetails addedQualificationDetails=entityManager.merge(qualificationDetails);
                return addedQualificationDetails;
            }
            giveQualificationScore(userId);
            return qualificationDetails;

        }
        CustomCustomer customCustomer = findCustomCustomerById(userId);
        dateValidations();
        checkIfQualificationAlreadyExists(userId, qualificationDetails.getQualification_id(), roleName);
        List<Qualification> qualifications = qualificationService.getAllQualifications();
        Integer qualificationToAdd= findQualificationId(qualificationDetails.getQualification_id(), qualifications);
        OtherItem qualificationOtherItemToAdd=handleOtherCaseForQualification(qualificationToAdd,qualificationOthers,roleId,userId,sourceName);
        allOtherItemsToSave.add(qualificationOtherItemToAdd);
        qualificationDetails.setQualification_id(qualificationToAdd);
        if(qualificationDetails.getQualification_id().equals(6) || qualificationDetails.getQualification_id().equals(7))
        {
            if(qualificationDetails.getCourse_duration_in_months()==null)
            {
                throw new IllegalArgumentException("Provide the duration of course");
            }
            if(qualificationDetails.getCourse_duration_in_months()<1)
            {
                throw new IllegalArgumentException("Duration of course cannot be a negative number or zero");
            }
        }
        else {
            if(qualificationDetails.getCourse_duration_in_months()!=null)
            {
                throw new IllegalArgumentException("Duration of course is required only for diploma and ITI");
            }
        }
        validateQualificationDetail(qualificationDetails);
        List<Institution> institutions = institutionService.getAllInstitutions();
        Institution institutionToAdd = findInstitutionId(qualificationDetails.getInstitution().getInstitution_id(),  institutions);
        OtherItem institutionOtherItemToAdd=handleOtherCaseForInstitution(institutionToAdd.getInstitution_id(),institutionOthers,roleId,userId,sourceName);
        allOtherItemsToSave.add(institutionOtherItemToAdd);
        qualificationDetails.setInstitution(institutionToAdd);
        List<BoardUniversity> boardUniversities = boardUniversityService.getAllBoardUniversities();
        Long boardUniversityToAdd = findBoardUniversityById(qualificationDetails.getBoard_university_id(), boardUniversities);
        OtherItem boardUniversityOtherItemToAdd=handleOtherCaseForBoardUniversity(boardUniversityToAdd,boardUniversityOthers,roleId,userId,sourceName);
        allOtherItemsToSave.add(boardUniversityOtherItemToAdd);
        qualificationDetails.setBoard_university_id(boardUniversityToAdd);
        Long streamToAdd=null;
        OtherItem streamOtherItemToAdd=null;
        if(qualificationDetails.getQualification_id().equals(1))
        {
            qualificationDetails.setStream_id(0L);
        }
        else {
            List<CustomStream> streams = streamService.getStreamByQualificationId(qualificationDetails.getQualification_id());
            streamToAdd= findStreamId(qualificationDetails.getStream_id(),streams);
            streamOtherItemToAdd=handleOtherCaseForStream(streamToAdd,streamOthers,roleId,userId,sourceName);
            allOtherItemsToSave.add(streamOtherItemToAdd);
            qualificationDetails.setStream_id(streamToAdd);
        }

        Boolean subjectValidationCheck= null;
        if(qualificationToSearch!=null)
        {
            subjectValidationCheck=qualificationToSearch.getIs_subjects_required();
        }
        if(subjectValidationCheck.equals(true)) {
            if (qualificationDetails.getSubject_ids() == null || qualificationDetails.getSubject_ids().isEmpty()) {
                throw new IllegalArgumentException("Subjects list cannot be empty");
            }
            if(!(qualificationDetails.getSubject_ids()==null|| qualificationDetails.getSubject_ids().isEmpty()))
            {
                if(qualificationDetails.getQualification_id().equals(1))
                {
                    validateAndGetSubjectIds(qualificationDetails.getSubject_ids(), 0L);
                    qualificationDetails.setSubject_ids(qualificationDetails.getSubject_ids());
                }
                else {
                    validateAndGetSubjectIds(qualificationDetails.getSubject_ids(), qualificationDetails.getStream_id());
                    qualificationDetails.setSubject_ids(qualificationDetails.getSubject_ids());

                }
                allOtherItemsToSave=createSubjectDetails(qualificationDetails,roleId,userId,allOtherItemsToSave);
                validateSubjectSizeForCustomer(qualificationDetails);
            }
        }
        else if(subjectValidationCheck.equals(false))
        {
            if(qualificationDetails.getSubject_ids()!=null && !qualificationDetails.getSubject_ids().isEmpty())
            {
                throw new IllegalArgumentException("Subject ids should be empty for qualification who do not require subjects details. In this case you can enter list of subjects manually using key highest_qualification_subject_names");
            }
            if(qualificationDetails.getSubject_details()!=null && !qualificationDetails.getSubject_details().isEmpty())
            {
                throw new IllegalArgumentException("Subject details should be empty for qualification who do not require subjects details. In this case you can enter list of subjects manually using key highest_qualification_subject_names");
            }
           List<String> highestQualificationSubjectList = qualificationDetails.getHighest_qualification_subject_names();
           if(highestQualificationSubjectList!=null && !highestQualificationSubjectList.isEmpty())
           {
               for(String subjectName: highestQualificationSubjectList)
               {
                   if (!isValidSubjectName(subjectName)) {
                       throw new IllegalArgumentException("Invalid subject name: " + subjectName);
                   }
               }
           }
        }

        qualificationDetails.setCustom_customer(customCustomer);
        customCustomer.getQualificationDetailsList().add(qualificationDetails);
        entityManager.persist(qualificationDetails);
        CustomStream customStream=null;
        if(streamToAdd!=null)
        {
            customStream=entityManager.find(CustomStream.class,streamToAdd);
            if(customStream==null)
            {
                throw new IllegalArgumentException("No stream found with id"+ customStream);
            }
        }

        boolean isOtherSubjects=false;
        if(qualificationDetails.getSubject_ids()!=null)
        {
            if(qualificationDetails.getSubject_ids().contains(54L))
            {
                isOtherSubjects=true;
            }
        }
        if((boardUniversityOthers!=null && boardUniversityToAdd.equals(1L))||(institutionOthers!=null && institutionToAdd.getInstitution_name().equalsIgnoreCase("Others")) ||(!qualificationDetails.getQualification_id().equals(1) &&
                streamOthers != null && customStream.getStreamName().equalsIgnoreCase("Others")) || qualificationOthers != null && qualificationToSearch.getQualification_name().equalsIgnoreCase("Others") || isOtherSubjects)
        {
            qualificationDetails.setOtherItems(allOtherItemsToSave);

            if(boardUniversityOtherItemToAdd!=null)
            {
                entityManager.merge(boardUniversityOtherItemToAdd);
                qualificationDetails.setOther_board_university(boardUniversityOthers);
            }
            if(institutionOtherItemToAdd!=null)
            {
                entityManager.merge(institutionOtherItemToAdd);
                qualificationDetails.setOther_institution(institutionOthers);
            }
            if(streamOtherItemToAdd!=null)
            {
                entityManager.merge(streamOtherItemToAdd);
                qualificationDetails.setOther_stream(streamOthers);
            }
            if(qualificationOtherItemToAdd!=null)
            {
                entityManager.merge(qualificationOtherItemToAdd);
                qualificationDetails.setOther_qualification(qualificationOthers);
            }
            if(qualificationDetails.getOtherSubjects()!=null && !qualificationDetails.getOtherSubjects().isEmpty())
            {

                for(OtherItem otherSubject : allOtherItemsToSave)
                {
                    if(otherSubject!=null)
                    {
                        if(otherSubject.getField_name().equalsIgnoreCase("subject"))
                        {
                            entityManager.merge(otherSubject);
                        }
                    }
                }
            }
            QualificationDetails addedQualificationDetails=entityManager.merge(qualificationDetails);
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
        if(roleName.equalsIgnoreCase("CUSTOMER"))
        {
            customCustomer.getQualificationDetailsList().forEach(detail -> {
                if (detail.getQualificationDocument() != null ) {
                    Document customerDocument=detail.getQualificationDocument();
                    if(customerDocument.getQualificationDetails().getQualification_detail_id().equals(qualificationId))
                    {
                        customerDocument.setQualificationDetails(null);
                        customerDocument.setIsArchived(true);
                        entityManager.merge(customerDocument);
                    }
                }
            });
        }
        else if(roleName.equalsIgnoreCase(Constant.SERVICE_PROVIDER))
        {
            serviceProviderEntity.getQualificationDetailsList().forEach(detail -> {
                if (detail.getServiceProviderDocument() != null) {
                    ServiceProviderDocument serviceProviderDocument=detail.getServiceProviderDocument();
                    if(serviceProviderDocument.getQualificationDetails().getQualification_detail_id().equals(qualificationId))
                    {
                        serviceProviderDocument.setQualificationDetails(null);
                        serviceProviderDocument.setIsArchived(true);
                        entityManager.merge(serviceProviderDocument);
                    }
                }
            });
        }
        qualificationDetails.remove(qualificationDetailsToDelete);
        entityManager.remove(qualificationDetailsToDelete);

        if (qualificationDetailsToDelete.getHighest_qualification_subject_names() != null) {
            qualificationDetailsToDelete.getHighest_qualification_subject_names().size();  // This forces Hibernate to initialize the collection
        }

        if (qualificationDetailsToDelete.getOtherSubjects() != null) {
            qualificationDetailsToDelete.getOtherSubjects().size();  // This forces Hibernate to initialize the collection
        }
        if(roleName.equalsIgnoreCase(Constant.SERVICE_PROVIDER))
        {
            giveQualificationScore(userId);
        }
        return qualificationDetailsToDelete;
    }

    @Transactional
    public QualificationDetails updateQualificationDetail(Long userId, Long qualificationId, UpdateQualificationDto qualification, String boardUniversityOthers,String streamOthers,String qualificationOthers,String institutionOthers,Integer roleId, String roleName) throws Exception {
        String sourceName= "update_qualification";
        String marksType=null;
        String marksObtained=null;
        String totalMarks=null;
        Integer qualificationIdToUpdate=null;
        Long streamId=null;
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
        if("CUSTOMER".equalsIgnoreCase(roleName)) {
            QualificationDetails existingQualificationDetails = query.getResultStream().findFirst().orElse(null);

            if (existingQualificationDetails != null && !qualificationId.equals(existingQualificationDetails.getQualification_detail_id())) {
                throw new EntityAlreadyExistsException("Qualification details with id " + qualification.getQualification_id() + " already exists");
            }
        }
        List<OtherItem> existingItems = qualificationDetailsToUpdate.getOtherItems();
        if (Objects.nonNull(qualification.getQualification_id())) {
            Boolean isOtherQualification = false;
            List<Qualification> qualificationDetailsList = qualificationService.getAllQualifications();
            Integer qualificationToAdd = findQualificationId(qualification.getQualification_id(), qualificationDetailsList);
            OtherItem qualificationOtherItemToAdd = null;
            Qualification qualificationToFind= entityManager.find(Qualification.class,qualificationToAdd);
            qualificationDetailsToUpdate.setQualification_id(qualificationToAdd);
            List<OtherItem> currentOtherItems = qualificationDetailsToUpdate.getOtherItems();
            Boolean userExists= false;
            if (!qualificationToAdd.equals(1) && !qualificationToAdd.equals(2)) {

                if (qualificationDetailsToUpdate.getSubject_details() != null && !qualificationDetailsToUpdate.getSubject_details().isEmpty()) {
                    for (SubjectDetail subjectDetail : qualificationDetailsToUpdate.getSubject_details()) {
                        entityManager.remove(entityManager.contains(subjectDetail) ? subjectDetail : entityManager.merge(subjectDetail));
                    }
                    qualificationDetailsToUpdate.getSubject_details().clear();
                }
                qualificationDetailsToUpdate.setSubject_ids(null);

                qualificationDetailsToUpdate.setOtherSubjects(null);
                if (!currentOtherItems.isEmpty()) {
                    Iterator<OtherItem> iterator = currentOtherItems.iterator();
                    while (iterator.hasNext()) {
                        OtherItem otherItem = iterator.next();
                        if(roleName.equalsIgnoreCase(Constant.SERVICE_PROVIDER))
                        {
                            if(qualificationDetailsToUpdate.getService_provider().getService_provider_id().equals(otherItem.getUser_id()))
                            {
                                userExists=true;
                            }
                        }
                        else if(roleName.equalsIgnoreCase(Constant.roleUser))
                        {
                            if(qualificationDetailsToUpdate.getCustom_customer().getId().equals(otherItem.getUser_id()))
                            {
                                userExists=true;
                            }
                        }
                        if ((otherItem.getSource_name().equalsIgnoreCase("add_qualification") ||
                                otherItem.getSource_name().equalsIgnoreCase("update_qualification")) &&
                                otherItem.getField_name().equalsIgnoreCase("subject") && userExists) {
                            iterator.remove();
                        }
                    }
                    qualificationDetailsToUpdate.setOtherSubjects(null);
                    qualificationDetailsToUpdate.setOtherItems(currentOtherItems);
                }
            }

            if(roleName.equalsIgnoreCase(Constant.SERVICE_PROVIDER))
            {
                giveQualificationScore(userId);
            }
            qualificationIdToUpdate=qualification.getQualification_id();

            if (qualificationToFind.getQualification_name().equalsIgnoreCase("Others")) {
                isOtherQualification = true;
            }

            if (isOtherQualification.equals(false)) {
                qualificationDetailsToUpdate.setOther_stream(null);
                 currentOtherItems = qualificationDetailsToUpdate.getOtherItems();
                if (!currentOtherItems.isEmpty()) {
                    Iterator<OtherItem> iterator = currentOtherItems.iterator();
                    while (iterator.hasNext()) {
                        OtherItem otherItem = iterator.next();
                        if(roleName.equalsIgnoreCase(Constant.SERVICE_PROVIDER))
                        {
                            if(qualificationDetailsToUpdate.getService_provider().getService_provider_id().equals(otherItem.getUser_id()))
                            {
                                userExists=true;
                            }
                        }
                        else if(roleName.equalsIgnoreCase(Constant.roleUser))
                        {
                            if(qualificationDetailsToUpdate.getCustom_customer().getId().equals(otherItem.getUser_id()))
                            {
                                userExists=true;
                            }
                        }
                        if ((otherItem.getSource_name().equalsIgnoreCase("add_qualification") ||
                                otherItem.getSource_name().equalsIgnoreCase("update_qualification")) &&
                                otherItem.getField_name().equalsIgnoreCase("qualification_name") && userExists) {
                            iterator.remove();
                        }
                    }
                    qualificationDetailsToUpdate.setOther_qualification(null);
                    qualificationDetailsToUpdate.setOtherItems(currentOtherItems);
                }
            } else if (isOtherQualification.equals(true)) {
                if (existingItems != null && !existingItems.isEmpty()) {
                    boolean itemUpdated = false;
                    Iterator<OtherItem> iterator = existingItems.iterator();

                    while (iterator.hasNext()) {
                        OtherItem otherItem = iterator.next();
                        if ((otherItem.getSource_name().equalsIgnoreCase("add_qualification") ||
                                otherItem.getSource_name().equalsIgnoreCase("update_qualification")) &&
                                otherItem.getField_name().equalsIgnoreCase("qualification_name")) {
                            if(qualificationOthers==null)
                            {
                                throw new IllegalArgumentException("You have to enter text for other qualification");
                            }
                            otherItem.setTyped_text(qualificationOthers);
                            otherItem.setSource_name(sourceName);
                            entityManager.merge(otherItem);
                            itemUpdated = true;
                        }
                    }

                    if (!itemUpdated) {
                        qualificationOtherItemToAdd = handleOtherCaseForQualification(
                                qualificationToAdd, qualificationOthers, roleId, userId, sourceName);
                        existingItems.add(qualificationOtherItemToAdd);
                    }
                } else {
                    if (existingItems == null) {
                        existingItems = new ArrayList<>();
                    }
                    qualificationOtherItemToAdd = handleOtherCaseForQualification(
                            qualificationToAdd, qualificationOthers, roleId, userId, sourceName);
                    existingItems.add(qualificationOtherItemToAdd);
                }

                qualificationDetailsToUpdate.setOtherItems(existingItems);
                qualificationDetailsToUpdate.setOther_qualification(qualificationOthers);
                entityManager.merge(qualificationDetailsToUpdate);
            }
        }
        else {
            qualificationIdToUpdate=qualificationDetailsToUpdate.getQualification_id();
        }

        if(qualificationIdToUpdate.equals(1))
        {
            qualificationDetailsToUpdate.setStream_id(0L);
        }
        else {
            if (Objects.nonNull(qualification.getStream_id())) {
                Boolean isOtherStream = false;
                List<CustomStream> streams = streamService.getStreamByQualificationId(qualificationIdToUpdate);
                Long streamToAdd = findStreamId(qualification.getStream_id(), streams);
                OtherItem streamOtherItemToAdd = null;
                qualificationDetailsToUpdate.setStream_id(streamToAdd);
                streamId = qualificationDetailsToUpdate.getStream_id();
                CustomStream customStream= entityManager.find(CustomStream.class,streamToAdd);

                if (customStream.getStreamName().equalsIgnoreCase("Others")) {
                    isOtherStream = true;
                }

                Boolean userExists= false;
                if (isOtherStream.equals(false)) {
                    qualificationDetailsToUpdate.setOther_stream(null);
                    List<OtherItem> currentOtherItems = qualificationDetailsToUpdate.getOtherItems();
                    if (!currentOtherItems.isEmpty()) {
                        Iterator<OtherItem> iterator = currentOtherItems.iterator();
                        while (iterator.hasNext()) {
                            OtherItem otherItem = iterator.next();
                            if(roleName.equalsIgnoreCase(Constant.SERVICE_PROVIDER))
                            {
                                if(qualificationDetailsToUpdate.getService_provider().getService_provider_id().equals(otherItem.getUser_id()))
                                {
                                    userExists=true;
                                }
                            }
                            else if(roleName.equalsIgnoreCase(Constant.roleUser))
                            {
                                if(qualificationDetailsToUpdate.getCustom_customer().getId().equals(otherItem.getUser_id()))
                                {
                                    userExists=true;
                                }
                            }
                            if ((otherItem.getSource_name().equalsIgnoreCase("add_qualification") ||
                                    otherItem.getSource_name().equalsIgnoreCase("update_qualification")) &&
                                    otherItem.getField_name().equalsIgnoreCase("stream") && userExists) {
                                iterator.remove();
                            }
                        }
                        qualificationDetailsToUpdate.setOtherItems(currentOtherItems);
                    }
                } else if (isOtherStream.equals(true)) {
                 existingItems = qualificationDetailsToUpdate.getOtherItems();
                    if (existingItems != null && !existingItems.isEmpty()) {
                        boolean itemUpdated = false;
                        Iterator<OtherItem> iterator = existingItems.iterator();

                        while (iterator.hasNext()) {
                            OtherItem otherItem = iterator.next();
                            if ((otherItem.getSource_name().equalsIgnoreCase("add_qualification") ||
                                    otherItem.getSource_name().equalsIgnoreCase("update_qualification")) &&
                                    otherItem.getField_name().equalsIgnoreCase("stream")) {
                                if(streamOthers==null)
                                {
                                    throw new IllegalArgumentException("You have to enter text for stream");
                                }
                                otherItem.setTyped_text(streamOthers);
                                otherItem.setSource_name(sourceName);
                                entityManager.merge(otherItem);
                                itemUpdated = true;
                            }
                        }

                        if (!itemUpdated) {
                            streamOtherItemToAdd = handleOtherCaseForStream(
                                    streamToAdd, streamOthers, roleId, userId, sourceName);
                            existingItems.add(streamOtherItemToAdd);
                        }
                    } else {
                        if (existingItems == null) {
                            existingItems = new ArrayList<>();
                        }
                        streamOtherItemToAdd = handleOtherCaseForStream(
                                streamToAdd, streamOthers, roleId, userId, sourceName);
                        existingItems.add(streamOtherItemToAdd);
                    }
                    qualificationDetailsToUpdate.setOtherItems(existingItems);
                    qualificationDetailsToUpdate.setOther_stream(streamOthers);
                    entityManager.merge(qualificationDetailsToUpdate);
                }

            } else {
                streamId = qualificationDetailsToUpdate.getStream_id();
                List<CustomStream> streams = streamService.getStreamByQualificationId(qualificationIdToUpdate);
                findStreamId(streamId, streams);
            }
        }

        if("CUSTOMER".equalsIgnoreCase(roleName)) {
            if(Objects.nonNull(qualification.getCourse_duration_in_months()))
            {
                if(qualificationIdToUpdate.equals(6) || qualificationIdToUpdate.equals(7))
                {
                    if(qualification.getCourse_duration_in_months()<1)
                    {
                        throw new IllegalArgumentException("Duration of course cannot be a negative number or zero");
                    }
                    qualificationDetailsToUpdate.setCourse_duration_in_months(qualification.getCourse_duration_in_months());
                }
                else
                {
                    qualificationDetailsToUpdate.setCourse_duration_in_months(null);
                }
            }
            else {
                if(qualificationIdToUpdate.equals(6) || qualificationIdToUpdate.equals(7))
                {
                    if(qualificationDetailsToUpdate.getCourse_duration_in_months()==null)
                    {
                        throw new IllegalArgumentException("Provide the duration of course");
                    }
                    if(qualificationDetailsToUpdate.getCourse_duration_in_months()<1)
                    {
                        throw new IllegalArgumentException("Duration of course cannot be a negative number or zero");
                    }
                }
            }
        }
        else if("SERVICE_PROVIDER".equalsIgnoreCase(roleName))
        {
            if (Objects.nonNull(qualification.getSubject_name())) {
                qualificationDetailsToUpdate.setSubject_name(qualification.getSubject_name());
            }
        }

        if(Objects.nonNull(qualification.getBoard_university_id())) {
            Boolean isOtherBoardUniversity = false;
            List<BoardUniversity> boardUniversities = boardUniversityService.getAllBoardUniversities();
            Long boardUniversityToAdd = findBoardUniversityById(qualification.getBoard_university_id(), boardUniversities);
            OtherItem boardUniversityOtherItemToAdd = null;
            qualificationDetailsToUpdate.setBoard_university_id(boardUniversityToAdd);

            if (boardUniversityToAdd.equals(1L)) {
                isOtherBoardUniversity = true;
            }

            Boolean userExists= false;
                if (isOtherBoardUniversity.equals(false)) {
                    qualificationDetailsToUpdate.setOther_board_university(null);
                    List<OtherItem> currentOtherItems = qualificationDetailsToUpdate.getOtherItems();
                    if (!currentOtherItems.isEmpty()) {
                        Iterator<OtherItem> iterator = currentOtherItems.iterator();
                        while (iterator.hasNext()) {
                            OtherItem otherItem = iterator.next();
                            if(roleName.equalsIgnoreCase(Constant.SERVICE_PROVIDER))
                            {
                                if(qualificationDetailsToUpdate.getService_provider().getService_provider_id().equals(otherItem.getUser_id()))
                                {
                                    userExists=true;
                                }
                            }
                            else if(roleName.equalsIgnoreCase(Constant.roleUser))
                            {
                                if(qualificationDetailsToUpdate.getCustom_customer().getId().equals(otherItem.getUser_id()))
                                {
                                    userExists=true;
                                }
                            }
                            if ((otherItem.getSource_name().equalsIgnoreCase("add_qualification") ||
                                    otherItem.getSource_name().equalsIgnoreCase("update_qualification")) &&
                                    otherItem.getField_name().equalsIgnoreCase("board_or_university") && userExists) {
                                iterator.remove();
                            }
                        }
                        qualificationDetailsToUpdate.setOther_board_university(null);
                        qualificationDetailsToUpdate.setOtherItems(currentOtherItems);
                    }
                } else if (isOtherBoardUniversity.equals(true)) {
                   existingItems = qualificationDetailsToUpdate.getOtherItems();
                    if (existingItems != null && !existingItems.isEmpty()) {
                        boolean itemUpdated = false;
                        Iterator<OtherItem> iterator = existingItems.iterator();

                        while (iterator.hasNext()) {
                            OtherItem otherItem = iterator.next();
                            if ((otherItem.getSource_name().equalsIgnoreCase("add_qualification") ||
                                    otherItem.getSource_name().equalsIgnoreCase("update_qualification")) &&
                                    otherItem.getField_name().equalsIgnoreCase("board_or_university")) {
                                if(boardUniversityOthers==null)
                                {
                                    throw new IllegalArgumentException("You have to enter text for other board or university");
                                }
                                otherItem.setTyped_text(boardUniversityOthers);
                                otherItem.setSource_name(sourceName);
                                entityManager.merge(otherItem);
                                itemUpdated = true;
                            }
                        }

                        if (!itemUpdated) {
                            boardUniversityOtherItemToAdd = handleOtherCaseForBoardUniversity(
                                    boardUniversityToAdd, boardUniversityOthers, roleId, userId, sourceName);
                            existingItems.add(boardUniversityOtherItemToAdd);
                        }
                    } else {
                        if (existingItems == null) {
                            existingItems = new ArrayList<>();
                        }
                        boardUniversityOtherItemToAdd = handleOtherCaseForBoardUniversity(
                                boardUniversityToAdd, boardUniversityOthers, roleId, userId, sourceName);
                        existingItems.add(boardUniversityOtherItemToAdd);
                    }

                    qualificationDetailsToUpdate.setOtherItems(existingItems);
                    qualificationDetailsToUpdate.setOther_board_university(boardUniversityOthers);
                    entityManager.merge(qualificationDetailsToUpdate);
                }
        }

        if(Objects.nonNull(qualification.getInstitution_id()))
        {
            Boolean isOtherInstitution = false;
            List<Institution> institutions = institutionService.getAllInstitutions();
            Institution institutionToAdd= findInstitutionId(qualification.getInstitution_id(),institutions);
            OtherItem institutionOtherItemToAdd = null;
            qualificationDetailsToUpdate.setInstitution(institutionToAdd);

            if (institutionToAdd.getInstitution_id().equals(1L)) {
                isOtherInstitution = true;
            }

            Boolean userExists= false;
            if (isOtherInstitution.equals(false)) {
                qualificationDetailsToUpdate.setOther_institution(null);
                List<OtherItem> currentOtherItems = qualificationDetailsToUpdate.getOtherItems();
                if (!currentOtherItems.isEmpty()) {
                    Iterator<OtherItem> iterator = currentOtherItems.iterator();
                    while (iterator.hasNext()) {
                        OtherItem otherItem = iterator.next();
                        if(roleName.equalsIgnoreCase(Constant.SERVICE_PROVIDER))
                        {
                            if(qualificationDetailsToUpdate.getService_provider().getService_provider_id().equals(otherItem.getUser_id()))
                            {
                                userExists=true;
                            }
                        }
                        else if(roleName.equalsIgnoreCase(Constant.roleUser))
                        {
                            if(qualificationDetailsToUpdate.getCustom_customer().getId().equals(otherItem.getUser_id()))
                            {
                                userExists=true;
                            }
                        }
                        if ((otherItem.getSource_name().equalsIgnoreCase("add_qualification") ||
                                otherItem.getSource_name().equalsIgnoreCase("update_qualification")) &&
                                otherItem.getField_name().equalsIgnoreCase("institution") && userExists) {
                            iterator.remove();
                        }
                    }
                    qualificationDetailsToUpdate.setOther_institution(null);
                    qualificationDetailsToUpdate.setOtherItems(currentOtherItems);
                }
            } else if (isOtherInstitution.equals(true)) {
                 existingItems = qualificationDetailsToUpdate.getOtherItems();
                if (existingItems != null && !existingItems.isEmpty()) {
                    boolean itemUpdated = false;
                    Iterator<OtherItem> iterator = existingItems.iterator();

                    while (iterator.hasNext()) {
                        OtherItem otherItem = iterator.next();
                        if ((otherItem.getSource_name().equalsIgnoreCase("add_qualification") ||
                                otherItem.getSource_name().equalsIgnoreCase("update_qualification")) &&
                                otherItem.getField_name().equalsIgnoreCase("institution")) {
                            if(institutionOthers==null)
                            {
                                throw new IllegalArgumentException("You have to enter text for other institution");
                            }
                            otherItem.setTyped_text(institutionOthers);
                            otherItem.setSource_name(sourceName);
                            entityManager.merge(otherItem);
                            itemUpdated = true;
                        }
                    }

                    if (!itemUpdated) {
                        institutionOtherItemToAdd = handleOtherCaseForInstitution(
                                institutionToAdd.getInstitution_id(), institutionOthers, roleId, userId, sourceName);
                        existingItems.add(institutionOtherItemToAdd);
                    }
                } else {
                    if (existingItems == null) {
                        existingItems = new ArrayList<>();
                    }
                    institutionOtherItemToAdd = handleOtherCaseForInstitution(
                            institutionToAdd.getInstitution_id(), institutionOthers, roleId, userId, sourceName);
                    existingItems.add(institutionOtherItemToAdd);
                }

                qualificationDetailsToUpdate.setOtherItems(existingItems);
                qualificationDetailsToUpdate.setOther_institution(institutionOthers);
                entityManager.merge(qualificationDetailsToUpdate);
            }
        }

        if (Objects.nonNull(qualification.getExamination_role_number())) {
            qualificationDetailsToUpdate.setExamination_role_number(qualification.getExamination_role_number());
        }
        if (Objects.nonNull(qualification.getExamination_registration_number())) {
            qualificationDetailsToUpdate.setExamination_registration_number(qualification.getExamination_registration_number());
        }

        if(Objects.nonNull(qualification.getTotal_marks_type()))
        {
            if(!qualification.getTotal_marks_type().equalsIgnoreCase("Percentage")&& !qualification.getTotal_marks_type().equalsIgnoreCase("CGPA") )
            {
                throw new IllegalArgumentException("Total marks type must be either percentage or CGPA");
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
            if (!qualification.getMarks_obtained().matches("-?\\d+(\\.\\d+)?")) { // Regex to allow integers or decimals
                throw new IllegalArgumentException("Overall Marks obtained must be a valid numeric value");
            }
            marksObtained=qualification.getMarks_obtained();
        }
        else {
            if (!qualificationDetailsToUpdate.getMarks_obtained().matches("-?\\d+(\\.\\d+)?")) { // Regex to allow integers or decimals
                throw new IllegalArgumentException("Overall Marks obtained must be a valid numeric value");
            }
            marksObtained=qualificationDetailsToUpdate.getMarks_obtained();
        }

        if(Objects.nonNull(qualification.getTotal_marks()))
        {
            if (!qualification.getTotal_marks().matches("-?\\d+(\\.\\d+)?")) { // Regex to allow integers or decimals
                throw new IllegalArgumentException("Overall Total marks must be a valid numeric value (no alphabet or special characters) ");
            }
            totalMarks= qualification.getTotal_marks();
        }
        else
        {
            if (!qualificationDetailsToUpdate.getTotal_marks().matches("-?\\d+(\\.\\d+)?")) { // Regex to allow integers or decimals
                throw new IllegalArgumentException("Overall Total marks must be a valid numeric value (no alphabet or special characters) ");
            }
            totalMarks=qualificationDetailsToUpdate.getTotal_marks();
        }

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

        qualificationDetailsToUpdate.setMarks_obtained(marksObtained);
        qualificationDetailsToUpdate.setTotal_marks(totalMarks);

        if(marksType.equalsIgnoreCase("Percentage"))
        {
            Double percentage= (Double.parseDouble(marksObtained)/Double.parseDouble(totalMarks))*100;
            qualificationDetailsToUpdate.setCumulative_percentage_value(percentage);
        }

        if (Objects.nonNull(qualification.getCumulative_percentage_value())) {
            qualificationDetailsToUpdate.setCumulative_percentage_value(qualification.getCumulative_percentage_value());
        }

        if (Objects.nonNull(qualification.getDate_of_passing())) {
            validateDate(qualification.getDate_of_passing(),"date of passing");
            qualificationDetailsToUpdate.setDate_of_passing(convertStringToDate(qualification.getDate_of_passing(),"yyyy-MM-dd"));
        }

        if(Objects.nonNull(qualification.getGrade_value()))
        {
            if(Objects.nonNull(qualification.getIs_grade()) && qualification.getIs_grade().equals(true) || !Objects.nonNull(qualification.getIs_grade()) && qualification.getIs_grade().equals(true))
            {
                String gradePattern = "^[A-Za-z]([+-]?)$";

                if (!qualification.getGrade_value().trim().matches(gradePattern)) {
                    throw new IllegalArgumentException("Overall grade obtained should be a valid grade (A, A+, B-, etc.)");
                }
                qualificationDetailsToUpdate.setGrade_value(qualification.getGrade_value());
            }
            else if(!Objects.nonNull(qualification.getIs_grade()) && qualificationDetailsToUpdate.getIs_grade().equals(false))
            {
                throw new IllegalArgumentException("You have to check the grade option to fill the grade value");
            }
            qualificationDetailsToUpdate.setGrade_value(qualification.getGrade_value());

        }

        if(Objects.nonNull(qualification.getIs_grade()))
        {
            if(qualification.getIs_grade().equals(true))
            {
                if(Objects.nonNull(qualification.getGrade_value()))
                {
                    String gradePattern = "^[A-Za-z]([+-]?)$";

                    if (!qualification.getGrade_value().trim().matches(gradePattern)) {
                        throw new IllegalArgumentException("Overall grade obtained should be a valid grade (A, A+, B-, etc.)");
                    }
                    qualificationDetailsToUpdate.setGrade_value(qualification.getGrade_value());
                }
                else if(!Objects.nonNull(qualification.getGrade_value()) && qualificationDetailsToUpdate.getGrade_value()==null)
                {
                    throw new IllegalArgumentException("You have to enter the grade value");
                }
            }
            else if(qualification.getIs_grade().equals(false))
            {
                qualificationDetailsToUpdate.setIs_grade(false);
                qualificationDetailsToUpdate.setGrade_value(null);
            }

            qualificationDetailsToUpdate.setIs_grade(qualification.getIs_grade());
        }

        if(Objects.nonNull(qualification.getDivision_value()))
        {
            if(Objects.nonNull(qualification.getIs_division()) && qualification.getIs_division().equals(true) || !Objects.nonNull(qualification.getIs_division()) && qualification.getIs_division().equals(true))
            {
                if(qualification.getDivision_value().trim().isEmpty())
                {
                    throw new IllegalArgumentException("Overall division value cannot be empty");
                }

                String divisionValue = qualification.getDivision_value().trim();
                if (!divisionValue.matches("[a-zA-Z0-9+-]+")) {
                    throw new IllegalArgumentException("Division value must not contain leading spaces or special characters except + or -");
                }
                qualificationDetailsToUpdate.setDivision_value(qualification.getDivision_value());
            }
            else if(!Objects.nonNull(qualification.getIs_division()) && qualificationDetailsToUpdate.getIs_division().equals(false))
            {
                throw new IllegalArgumentException("You have to check the division option to fill the division value");
            }
            qualificationDetailsToUpdate.setDivision_value(qualification.getDivision_value());

        }

        if(Objects.nonNull(qualification.getIs_division()))
        {
            if(qualification.getIs_division().equals(true))
            {
                if(Objects.nonNull(qualification.getDivision_value()))
                {
                    if(qualification.getDivision_value().trim().isEmpty())
                    {
                        throw new IllegalArgumentException("Overall division value cannot be empty");
                    }

                    String divisionValue = qualification.getDivision_value().trim();
                    if (!divisionValue.matches("[a-zA-Z0-9+-]+")) {
                        throw new IllegalArgumentException("Division value must not contain leading spaces or special characters except + or -");
                    }
                    qualificationDetailsToUpdate.setDivision_value(qualification.getDivision_value());
                }
                else if(!Objects.nonNull(qualification.getDivision_value()) && qualificationDetailsToUpdate.getDivision_value()==null)
                {
                    throw new IllegalArgumentException("You have to enter the division value");
                }
            }
            else if(qualification.getIs_division().equals(false))
            {
                qualificationDetailsToUpdate.setIs_division(false);
                qualificationDetailsToUpdate.setDivision_value(null);
            }

            qualificationDetailsToUpdate.setIs_division(qualification.getIs_division());
        }

        if("CUSTOMER".equalsIgnoreCase(roleName))
        {
            Qualification qualificationToSearch= entityManager.find(Qualification.class,qualificationIdToUpdate);
            Boolean subjectValidationCheck= null;
            if(qualificationToSearch!=null)
            {
                subjectValidationCheck=qualificationToSearch.getIs_subjects_required();
            }
            if (Objects.nonNull(qualification.getSubject_ids())) {
                if(subjectValidationCheck.equals(true))
                {
                    if(qualification.getSubject_ids().size()<5)
                    {
                        throw new IllegalArgumentException("You have to add at least five subjects");
                    }
                    if(qualificationIdToUpdate.equals(1))
                    {
                        validateAndGetSubjectIds(qualification.getSubject_ids(),0L);
                        qualificationDetailsToUpdate.setSubject_ids(qualification.getSubject_ids());
                        createSubjectDetailsForUpdateQualification(qualification,qualificationDetailsToUpdate,roleName,roleId,userId);
                    }
                    else {
                        validateAndGetSubjectIds(qualification.getSubject_ids(),streamId);
                        qualificationDetailsToUpdate.setSubject_ids(qualification.getSubject_ids());
                        createSubjectDetailsForUpdateQualification(qualification,qualificationDetailsToUpdate,roleName,roleId,userId);
                    }
                }
                else if(subjectValidationCheck.equals(false)) {
                    if (qualification.getSubject_ids() != null && !qualification.getSubject_ids().isEmpty()) {
                        throw new IllegalArgumentException("Subject ids should be empty for qualification who do not require subjects details. In this case you can enter list of subjects manually using key highest_qualification_subject_names");
                    }
                    if(qualification.getSubject_details()!=null && !qualification.getSubject_details().isEmpty())
                    {
                        throw new IllegalArgumentException("Subject details should be empty for qualification who do not require subjects details. In this case you can enter list of subjects manually using key highest_qualification_subject_names");
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
                else if(subjectValidationCheck.equals(false)) {
                    if (qualificationDetailsToUpdate.getSubject_ids() != null && !qualificationDetailsToUpdate.getSubject_ids().isEmpty()) {
                        throw new IllegalArgumentException("Subject ids should be empty for qualification who do not require subjects details. In this case you can enter list of subjects manually using key highest_qualification_subject_names");
                    }
                    if(qualificationDetailsToUpdate.getSubject_details()!=null && !qualificationDetailsToUpdate.getSubject_details().isEmpty())
                    {
                        throw new IllegalArgumentException("Subject details should be empty for qualification who do not require subjects details. In this case you can enter list of subjects manually using key highest_qualification_subject_names");
                    }
                }
            }

            if(Objects.nonNull(qualification.getHighest_qualification_subject_names()))
            {
                if(subjectValidationCheck.equals(false))
                {
                    if(qualification.getSubject_ids()!=null && !qualification.getSubject_ids().isEmpty())
                    {
                        throw new IllegalArgumentException("Subject ids should be empty for qualification who do not require subjects details. In this case you can enter list of subjects manually using key highest_qualification_subject_names");
                    }
                    if(qualification.getSubject_details()!=null && !qualification.getSubject_details().isEmpty())
                    {
                        throw new IllegalArgumentException("Subject details should be empty for qualification who do not require subjects details. In this case you can enter list of subjects manually using key highest_qualification_subject_names");
                    }
                    if(qualification.getHighest_qualification_subject_names()!=null && !qualification.getHighest_qualification_subject_names().isEmpty())
                    {
                        List<String> highestQualificationSubjectList = qualification.getHighest_qualification_subject_names();
                        for(String subjectName: highestQualificationSubjectList)
                        {
                            if (!isValidSubjectName(subjectName)) {
                                throw new IllegalArgumentException("Invalid subject name: " + subjectName);
                            }
                        }
                    }
                    qualificationDetailsToUpdate.setHighest_qualification_subject_names(qualification.getHighest_qualification_subject_names());
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
            throw new IllegalArgumentException("You have to select whether the you want to add the total marks in percentage or cgpa ");
        }
        if(!qualificationDetails.getTotal_marks_type().equalsIgnoreCase("Percentage")&& !qualificationDetails.getTotal_marks_type().equalsIgnoreCase("CGPA") )
        {
            throw new IllegalArgumentException("Total marks type must be either percentage or CGPA");
        }
        if(qualificationDetails.getTotal_marks_type().trim().isEmpty())
        {
            throw new IllegalArgumentException("Total marks type cannot be empty");
        }

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
            if(qualificationDetails.getIs_grade()!=null)
            {
                if(qualificationDetails.getIs_grade().equals(true))
                {
                    if(qualificationDetails.getGrade_value()==null)
                    {
                        throw new IllegalArgumentException("You have to enter a overall grade value ");
                    }
                    String gradeObtained = qualificationDetails.getGrade_value();
                    String gradePattern = "^[A-Za-z]([+-]?)$";

                    if (!gradeObtained.trim().matches(gradePattern)) {
                        throw new IllegalArgumentException("Overall marks obtained should be a valid grade (A, A+, B-, etc.)");
                    }
                }
            }

            if(qualificationDetails.getIs_division()!=null)
            {
                if(qualificationDetails.getIs_division().equals(true))
                {
                    if(qualificationDetails.getDivision_value()==null)
                    {
                        throw new IllegalArgumentException("You have to enter a overall division value");
                    }
                    if(qualificationDetails.getDivision_value().trim().isEmpty())
                    {
                        throw new IllegalArgumentException("Overall division value cannot be empty");
                    }

                    String divisionValue = qualificationDetails.getDivision_value().trim();
                    if (!divisionValue.matches("[a-zA-Z0-9+-]+")) {
                        throw new IllegalArgumentException("Division value must not contain leading spaces or special characters except + or -");
                    }
                }
            }

        if(qualificationDetails.getTotal_marks_type().equalsIgnoreCase("Percentage"))
        {
            Double percentage= (Double.parseDouble(qualificationDetails.getMarks_obtained())/Double.parseDouble(qualificationDetails.getTotal_marks()))*100;
            qualificationDetails.setCumulative_percentage_value(percentage);
        }
        else if(qualificationDetails.getTotal_marks_type().equalsIgnoreCase("CGPA"))
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
    public Institution findInstitutionId(Long institutionId,List<Institution> institutions)
    {
        for(Institution institution : institutions)
        {
            if(institution.getInstitution_id().equals(institutionId))
            {
                return institution;
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
        throw new IllegalArgumentException("Stream with id "+ streamId+ " does not exist for specified Qualification");
    }
    public List<Long> validateAndGetSubjectIds(List<Long> subjectIds, Long streamId) {
        if (subjectIds == null || subjectIds.isEmpty()) {
            return Collections.emptyList();
        }
        // Ensure there are no duplicate IDs
        Set<Long> uniqueSubjectIds = new HashSet<>();
        boolean hasDuplicate = false;

        for (Long id : subjectIds) {
            if (id != 54 && !uniqueSubjectIds.add(id)) {
                hasDuplicate = true;
                break;
            }
        }

        if (hasDuplicate) {
            throw new IllegalArgumentException("Duplicate subject IDs are not allowed, except for ID 54 (For others).");
        }

        // Query to get valid subject IDs associated with the given stream
        List<Long> validSubjectIds = entityManager.createQuery(
                        "SELECT s.subjectId FROM CustomStream cs " +
                                "JOIN cs.subjects s " +
                                "WHERE cs.streamId = :streamId AND s.subjectId IN :subjectIds", Long.class)
                .setParameter("streamId", streamId)
                .setParameter("subjectIds", subjectIds)
                .getResultList();

        // Identify missing subject IDs
        List<Long> missingSubjectIds = subjectIds.stream()
                .filter(id -> !validSubjectIds.contains(id))
                .collect(Collectors.toList());

        if (!missingSubjectIds.isEmpty()) {
            if(streamId.equals(0L))
            {
                throw new IllegalArgumentException("The following subject IDs do not exist for 10th/Matriculation qualification: " + missingSubjectIds);
            }
            throw new IllegalArgumentException("The following subject IDs do not exist for the specified stream: " + missingSubjectIds);
        }
        return subjectIds;

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

    public void giveQualificationScore(Long userId) throws CustomerDoesNotExistsException {
        ServiceProviderEntity serviceProviderEntity = findServiceProviderById(userId);
        TypedQuery<ScoringCriteria> typedQuery=  entityManager.createQuery(Constant.GET_ALL_SCORING_CRITERIA, ScoringCriteria.class);
        List<ScoringCriteria> scoringCriteriaList = typedQuery.getResultList();
        Integer totalScore=0;
        ScoringCriteria scoringCriteriaToMap =null;
        if(!serviceProviderEntity.getQualificationDetailsList().isEmpty())
        {
            QualificationDetails qualificationDetail= serviceProviderEntity.getQualificationDetailsList().get(serviceProviderEntity.getQualificationDetailsList().size()-1);
            Qualification qualification1 = entityManager.find(Qualification.class, qualificationDetail.getQualification_id());
            if (qualification1 != null) {
                if (!qualification1.getQualification_id().equals(1)&& !qualification1.getQualification_id().equals(2)) {
                    scoringCriteriaToMap=serviceProviderService.traverseListOfScoringCriteria(6L,scoringCriteriaList,serviceProviderEntity);
                    if(scoringCriteriaToMap==null)
                    {
                        throw new IllegalArgumentException("Scoring Criteria is not found for scoring Qualification Score");
                    }
                    else {
                        serviceProviderEntity.setQualificationScore(scoringCriteriaToMap.getScore());
                    }
                }
                else if(qualification1.getQualification_id().equals(2)) {
                    scoringCriteriaToMap=serviceProviderService.traverseListOfScoringCriteria(7L,scoringCriteriaList,serviceProviderEntity);
                    if(scoringCriteriaToMap==null)
                    {
                        throw new IllegalArgumentException("Scoring Criteria is not found for scoring Qualification Score");
                    }
                    else {
                        serviceProviderEntity.setQualificationScore(scoringCriteriaToMap.getScore());
                    }
                }
                else if(qualification1.getQualification_id().equals(1)) {
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
            if(qualificationDetails.getSubject_ids().size()<5)
            {
                throw new IllegalArgumentException("You have to add at least five subjects");
            }
        }
    }

    @Transactional
    public List<OtherItem> createSubjectDetails(QualificationDetails qualificationDetail,Integer roleId,Long userId,List<OtherItem> allOtherItemsToSave) {

        List<Long> subjectIds = qualificationDetail.getSubject_ids();
        List<SubjectDetail> userProvidedDetails = qualificationDetail.getSubject_details();
        if (subjectIds == null || subjectIds.isEmpty()) {
            throw new IllegalArgumentException("Subject IDs list cannot be empty");
        }
        if (userProvidedDetails == null || userProvidedDetails.isEmpty() || userProvidedDetails.size() != subjectIds.size()) {
            throw new IllegalArgumentException("Subject details must be provided for all subject IDs");
        }

// Validate that "Other Subjects" count matches the number of ID 54 occurrences
        int count54=0;
        for(Long subjectId: subjectIds)
        {
            if(subjectId.equals(54L))
            {
                count54++;
            }
        }
        if (count54 != qualificationDetail.getOtherSubjects().size()) {
            throw new IllegalArgumentException("Provide the subject name for all subjects selected as 'Others' (ID 54).");
        }

        List<SubjectDetail> subjectDetailsList = new ArrayList<>();
        int indexForOtherSubjects =0;
        // Iterate over subject IDs and corresponding user details
        for (int i = 0; i < subjectIds.size(); i++) {
            Long subjectId = subjectIds.get(i);

            // Find the subject
            CustomSubject customSubject = entityManager.find(CustomSubject.class, subjectId);
            if (customSubject == null) {
                throw new IllegalArgumentException("Subject with ID " + subjectId + " not found");
            }

            if(customSubject.getSubjectName().equalsIgnoreCase("Others"))
            {
                OtherItem subjectOtherItemToAdd=handleOtherCaseForSubjects(subjectId,qualificationDetail.getOtherSubjects().get(indexForOtherSubjects),roleId,userId,"add_qualification");
                allOtherItemsToSave.add(subjectOtherItemToAdd);
                indexForOtherSubjects++;
            }
            SubjectDetail userDetail = userProvidedDetails.get(i);
            // Create and populate SubjectDetail
            SubjectDetail subjectDetail = new SubjectDetail();
            validateSubjectDetails(userDetail,qualificationDetail,customSubject);
            subjectDetail.setCustomSubject(customSubject);
            subjectDetail.setQualificationDetails(qualificationDetail);
            if(userDetail.getSubject_marks_type().equalsIgnoreCase("Percentage") || userDetail.getSubject_marks_type().equalsIgnoreCase("CGPA"))
            {
                subjectDetail.setSubject_marks_obtained(userDetail.getSubject_marks_obtained());
                subjectDetail.setSubject_total_marks(userDetail.getSubject_total_marks());
            }
            else if(userDetail.getSubject_marks_type().equalsIgnoreCase("Grade"))
            {
                subjectDetail.setSubject_grade(userDetail.getSubject_grade());
            }
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
            qualificationDetail.setSubject_details(subjectDetailsList);
            }
        return allOtherItemsToSave;
    }

    @Transactional
    public void createSubjectDetailsForUpdateQualification(UpdateQualificationDto qualificationDetail, QualificationDetails qualificationDetailsToUpdate,String roleName,Integer roleId, Long userId) {

        List<Long> subjectIds = qualificationDetail.getSubject_ids();
        List<SubjectDetail> userProvidedDetails = qualificationDetail.getSubject_details();

        if (! subjectIds.isEmpty()) {
            if (userProvidedDetails == null || userProvidedDetails.isEmpty() || userProvidedDetails.size() != subjectIds.size()) {
                throw new IllegalArgumentException("Subject details must be provided for all subject IDs");
            }
        }
        int count54=0;
        for(Long subjectId: subjectIds)
        {
            if(subjectId.equals(54L))
            {
                count54++;
            }
        }
        if (count54 != qualificationDetail.getOtherSubjects().size()) {
            throw new IllegalArgumentException("Provide the subject name for all subjects selected as 'Others' (ID 54).");
        }

        List<SubjectDetail> subjectDetailsList = new ArrayList<>();
        qualificationDetailsToUpdate.getSubject_details().forEach(detail -> detail.setQualificationDetails(null));
        qualificationDetailsToUpdate.getSubject_details().clear();
        qualificationDetailsToUpdate.getOtherSubjects().clear();
        List<OtherItem> currentOtherItems = qualificationDetailsToUpdate.getOtherItems();
        if (!currentOtherItems.isEmpty()) {
            List<OtherItem> itemsToKeep = new ArrayList<>(currentOtherItems);
            itemsToKeep.removeIf(otherItem ->
                    roleName.equalsIgnoreCase(Constant.roleUser) &&
                            qualificationDetailsToUpdate.getCustom_customer().getId().equals(otherItem.getUser_id()) &&
                            (otherItem.getSource_name().equalsIgnoreCase("add_qualification") ||
                                    otherItem.getSource_name().equalsIgnoreCase("update_qualification")) &&
                            otherItem.getField_name().equalsIgnoreCase("subject")
            );

            qualificationDetailsToUpdate.getOtherItems().clear();
            qualificationDetailsToUpdate.getOtherItems().addAll(itemsToKeep);
        }
        int indexForOtherSubjects =0;
        for (int i = 0; i < subjectIds.size(); i++) {
            Long subjectId = subjectIds.get(i);

            // Find the subject
            CustomSubject customSubject = entityManager.find(CustomSubject.class, subjectId);
            if (customSubject == null) {
                throw new IllegalArgumentException("Subject with ID " + subjectId + " not found");
            }

            if(customSubject.getSubjectName().equalsIgnoreCase("Others")) {
                OtherItem subjectOtherItemToAdd = handleOtherCaseForSubjects(subjectId,
                        qualificationDetail.getOtherSubjects().get(indexForOtherSubjects),
                        roleId, userId, "update_qualification");
                qualificationDetailsToUpdate.getOtherItems().add(subjectOtherItemToAdd);
                indexForOtherSubjects++;
            }
            SubjectDetail userDetail = userProvidedDetails.get(i);
            SubjectDetail subjectDetail = new SubjectDetail();
            validateSubjectDetailsForUpdateQualification(userDetail, customSubject);
            subjectDetail.setCustomSubject(customSubject);
            subjectDetail.setQualificationDetails(qualificationDetailsToUpdate);
            if(userDetail.getSubject_marks_type().equalsIgnoreCase("Percentage") || userDetail.getSubject_marks_type().equalsIgnoreCase("CGPA"))
            {
                subjectDetail.setSubject_marks_obtained(userDetail.getSubject_marks_obtained());
                subjectDetail.setSubject_total_marks(userDetail.getSubject_total_marks());
            }
            else if(userDetail.getSubject_marks_type().equalsIgnoreCase("Grade"))
            {
                subjectDetail.setSubject_grade(userDetail.getSubject_grade());
            }
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
        qualificationDetailsToUpdate.setOtherSubjects(qualificationDetail.getOtherSubjects());
        entityManager.merge(qualificationDetailsToUpdate);
    }

    public void validateSubjectDetails(SubjectDetail subjectDetail,QualificationDetails qualificationDetails,CustomSubject customSubject)
    {
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

        if(subjectDetail.getSubject_marks_type().equalsIgnoreCase("Percentage") || subjectDetail.getSubject_marks_type().equalsIgnoreCase("CGPA"))
        {
            if(subjectDetail.getSubject_marks_obtained() ==null|| subjectDetail.getSubject_total_marks()==null)
            {
                throw new IllegalArgumentException("Both subject marks obtained and subject total marks cannot be null for subject "+ customSubject.getSubjectName() + " with subject_id "+ customSubject.getSubjectId());
            }
            if(subjectDetail.getSubject_marks_obtained().trim().isEmpty() || subjectDetail.getSubject_total_marks().trim().isEmpty())
            {
                throw new IllegalArgumentException("Both obtained and total subject marks cannot be empty for subject "+ customSubject.getSubjectName() + " with subject_id "+ customSubject.getSubjectId());
            }
        }

        if(subjectDetail.getSubject_marks_type().equalsIgnoreCase("Grade")|| subjectDetail.getSubject_marks_type().equalsIgnoreCase("CGPA"))
        {
            if(subjectDetail.getSubject_equivalent_percentage()!=null)
            {
                if (subjectDetail.getSubject_equivalent_percentage() < 0 || subjectDetail.getSubject_equivalent_percentage() > 100) {
                    throw new IllegalArgumentException("Equivalent percentage must be between 0 and 100 for subject "
                            + customSubject.getSubjectName() + " with subject_id " + customSubject.getSubjectId());
                }
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
            if(subjectDetail.getSubject_grade()==null)
            {
                throw new IllegalArgumentException("You have to enter the obtained grade in subject with id "+ customSubject.getSubjectId());
            }
            String gradeObtained = subjectDetail.getSubject_grade();

            String gradePattern = "^[A-Za-z]([+-]?)$";

            // Validate that gradeObtained matches the grade pattern
            if (!gradeObtained.trim().matches(gradePattern)) {
                throw new IllegalArgumentException("Subject grade obtained should be a valid grade (A, A+, B-, etc.) for subject "
                        + customSubject.getSubjectName() + " with subject_id " + customSubject.getSubjectId());
            }
        }
    }

    public void validateSubjectDetailsForUpdateQualification(SubjectDetail subjectDetail,CustomSubject customSubject)
    {
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
        if(subjectDetail.getSubject_marks_type().equalsIgnoreCase("Percentage") || subjectDetail.getSubject_marks_type().equalsIgnoreCase("CGPA"))
        {
            if(subjectDetail.getSubject_marks_obtained() ==null|| subjectDetail.getSubject_total_marks()==null)
            {
                throw new IllegalArgumentException("Both subject marks obtained and subject total marks cannot be null for subject "+ customSubject.getSubjectName() + " with subject_id "+ customSubject.getSubjectId());
            }
            if(subjectDetail.getSubject_marks_obtained().trim().isEmpty() || subjectDetail.getSubject_total_marks().trim().isEmpty())
            {
                throw new IllegalArgumentException("Both obtained and total subject marks cannot be empty for subject "+ customSubject.getSubjectName() + " with subject_id "+ customSubject.getSubjectId());
            }
        }
        if(subjectDetail.getSubject_marks_type().equalsIgnoreCase("Grade")|| subjectDetail.getSubject_marks_type().equalsIgnoreCase("CGPA"))
        {
            if(subjectDetail.getSubject_equivalent_percentage()!=null)
            {
                if (subjectDetail.getSubject_equivalent_percentage() < 0 || subjectDetail.getSubject_equivalent_percentage() > 100) {
                    throw new IllegalArgumentException("Equivalent percentage must be between 0 and 100 for subject "
                            + customSubject.getSubjectName() + " with subject_id " + customSubject.getSubjectId());
                }
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

            if(subjectDetail.getSubject_grade()==null)
            {
                throw new IllegalArgumentException("You have to enter the obtained grade in subject with id "+ customSubject.getSubjectId());
            }
            String gradeObtained = subjectDetail.getSubject_grade();

            String gradePattern = "^[A-Za-z]([+-]?)$";

            // Validate that gradeObtained matches the grade pattern
            if (!gradeObtained.trim().matches(gradePattern)) {
                throw new IllegalArgumentException("Subject grade should be a valid grade (A, A+, B-, etc.) for subject "
                        + customSubject.getSubjectName() + " with subject_id " + customSubject.getSubjectId());
            }
        }
    }

    public OtherItem handleOtherCaseForBoardUniversity(Long foundedBoardUniversityId,String boardUniversityOthers,Integer roleId,Long userId,String sourceName)
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
            otherItem.setField_name("board_or_university");
            otherItem.setSource_name(sourceName);
            otherItem.setRole_id(roleId);
            otherItem.setUser_id(userId);
            entityManager.persist(otherItem);
            return otherItem;
        }
        return null;
    }
    public OtherItem handleOtherCaseForStream(Long foundedStreamId,String streamOthers,Integer roleId,Long userId,String sourceName)
    {
        CustomStream customStream= entityManager.find(CustomStream.class,foundedStreamId);
        if(customStream==null)
        {
            throw new IllegalArgumentException("No stream found with id"+ customStream);
        }
        if(customStream.getStreamName().equalsIgnoreCase("Others"))
        {
            if(streamOthers==null) {
                throw new IllegalArgumentException("You have to enter a text for other stream");
            }
            if(streamOthers.trim().isEmpty())
            {
                throw new IllegalArgumentException("The text field cannot be empty for adding other stream");
            }
            OtherItem otherItem =new OtherItem();
            otherItem.setTyped_text(streamOthers);
            otherItem.setField_name("stream");
            otherItem.setSource_name(sourceName);
            otherItem.setRole_id(roleId);
            otherItem.setUser_id(userId);
            entityManager.persist(otherItem);
            return otherItem;
        }
        return null;
    }
    public OtherItem handleOtherCaseForQualification(Integer foundedQualificationId,String qualificationOthers,Integer roleId,Long userId,String sourceName)
    {
        Qualification qualification= entityManager.find(Qualification.class,foundedQualificationId);
        if(qualification==null)
        {
            throw new IllegalArgumentException("No Qualification found with id"+ qualification);
        }
        if(qualification.getQualification_name().equalsIgnoreCase("Others"))
        {
            if(qualificationOthers==null) {
                throw new IllegalArgumentException("You have to enter a text for other qualification name");
            }
            if(qualificationOthers.trim().isEmpty())
            {
                throw new IllegalArgumentException("The text field cannot be empty for adding other qualification");
            }
            OtherItem otherItem =new OtherItem();
            otherItem.setTyped_text(qualificationOthers);
            otherItem.setField_name("qualification_name");
            otherItem.setSource_name(sourceName);
            otherItem.setRole_id(roleId);
            otherItem.setUser_id(userId);
            entityManager.persist(otherItem);
            return otherItem;
        }
        return null;
    }
    public OtherItem handleOtherCaseForInstitution(Long foundedInstitutionId,String institutionOthers,Integer roleId,Long userId,String sourceName)
    {
        Institution institution= entityManager.find(Institution.class,foundedInstitutionId);
        if(institution==null)
        {
            throw new IllegalArgumentException("No institution found with id"+ institution);
        }
        if(institution.getInstitution_name().equalsIgnoreCase("Others"))
        {
            if(institutionOthers==null) {
                throw new IllegalArgumentException("You have to enter a text for other institution name");
            }
            if(institutionOthers.trim().isEmpty())
            {
                throw new IllegalArgumentException("The text field cannot be empty for adding other institution");
            }
            OtherItem otherItem =new OtherItem();
            otherItem.setTyped_text(institutionOthers);
            otherItem.setField_name("institution");
            otherItem.setSource_name(sourceName);
            otherItem.setRole_id(roleId);
            otherItem.setUser_id(userId);
            entityManager.persist(otherItem);
            return otherItem;
        }
        return null;
    }
    public OtherItem handleOtherCaseForSubjects(Long foundedSubjectId,String subjectOthers,Integer roleId,Long userId,String sourceName)
    {
        CustomSubject subject= entityManager.find(CustomSubject.class,foundedSubjectId);
        if(subject==null)
        {
            throw new IllegalArgumentException("No Subject found with id"+ subject);
        }
        if(subject.getSubjectName().equalsIgnoreCase("Others"))
        {
            if(subjectOthers==null) {
                throw new IllegalArgumentException("You have to enter a text for other subject name");
            }
            if(subjectOthers.trim().isEmpty())
            {
                throw new IllegalArgumentException("The text field cannot be empty for adding other subject");
            }
            OtherItem otherItem =new OtherItem();
            otherItem.setTyped_text(subjectOthers);
            otherItem.setField_name("subject");
            otherItem.setSource_name(sourceName);
            otherItem.setRole_id(roleId);
            otherItem.setUser_id(userId);
            entityManager.persist(otherItem);
            return otherItem;
        }
        return null;
    }
    public Boolean validateDate(String dateOfPassing,String dateType) throws Exception {
        String dateToBevalidate=dateOfPassing.substring(0,10);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);

        try {
            // Validate format
            if (!dateToBevalidate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                throw new IllegalArgumentException(dateType+" must be in yyyy-MM-dd format");
            }

            String[] dateParts = dateToBevalidate.split("-");
            int month = Integer.parseInt(dateParts[1]);
            if (month < 1 || month > 12) {
                throw new IllegalArgumentException("Invalid "+dateType+": Month should be between 1 and 12");
            }
            Date parsedDate = dateFormat.parse(dateToBevalidate);
            return true;
        }
        catch (ParseException e) {// Invalid date (correct format but invalid value)
            throw new IllegalArgumentException("Invalid "+dateType+": Day is not valid");
        }catch (IllegalArgumentException ex) {
            exceptionHandlingService.handleException(ex);
            throw new IllegalArgumentException(ex.getMessage()); // Rethrow with meaningful context
        }
    }

    private boolean isValidDateFormat(String dateStr, SimpleDateFormat dateFormat) {
        try {
            dateFormat.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public void dateValidations()
    {
        if(validationState==0)
        {
            throw new IllegalArgumentException("Date must be in yyyy-MM-dd format");
        }
        else if(validationState==-1)
        {
            throw new IllegalArgumentException("Invalid date: Day is not valid");
        }
        else if(validationState==-2)
        {
            throw new IllegalArgumentException("Invalid date: Month should be between 1 and 12");
        }
    }
    private boolean isValidSubjectName(String subjectName) {
        if (subjectName == null || subjectName.trim().isEmpty()) {
            return true; // Allow empty values
        }

        // Check if the string contains any forbidden special characters
        if (subjectName.matches(".*[?!@#*^>].*")) {
            return false; // Reject if it contains forbidden characters
        }

        // Ensure it contains at least one letter
        return subjectName.matches(".*[a-zA-Z].*");
    }
}