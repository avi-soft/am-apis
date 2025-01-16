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
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.community.api.endpoint.avisoft.controller.Customer.CustomerEndpoint.convertStringToDate;

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
    public QualificationDetails addQualificationDetails(Long userId, QualificationDetails qualificationDetails,String boardUniversityOthers,Integer roleId, String roleName )
            throws Exception {
        String sourceName= "add_qualification";
        if (roleName.equals(Constant.SERVICE_PROVIDER)) {
            ServiceProviderEntity serviceProviderEntity = findServiceProviderById(userId);
            if(!CustomDateDeserializer.isValidDate)
            {
                throw new IllegalArgumentException("Date must be in yyyy-MM-dd format");
            }
            List<Qualification> qualifications = qualificationService.getAllQualifications();
            Integer qualificationToAdd = findQualificationId(qualificationDetails.getQualification_id(), qualifications);
            qualificationDetails.setQualification_id(qualificationToAdd);
            List<Institution> institutions = institutionService.getAllInstitutions();
            Institution institutionToAdd = findInstitutionId(qualificationDetails.getInstitution().getInstitution_id(), institutions);
            qualificationDetails.setInstitution(institutionToAdd);
            List<BoardUniversity> boardUniversities = boardUniversityService.getAllBoardUniversities();
            Long boardUniversityToAdd = findBoardUniversityById(qualificationDetails.getBoard_university_id(), boardUniversities);
            OtherItem boardUniversityOtherItemToAdd=handleOtherCaseForBoardUniversity(boardUniversityToAdd,boardUniversityOthers,roleId,userId,sourceName);
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
            }

            else if (!serviceProviderEntity.getQualificationDetailsList().isEmpty()) {
                serviceProviderEntity.getQualificationDetailsList().forEach(detail -> {
                    if (detail.getServiceProviderDocument() != null) {
                        ServiceProviderDocument serviceProviderDocument=detail.getServiceProviderDocument();
                        serviceProviderDocument.setQualificationDetails(null);
                        serviceProviderDocument.setIsArchived(true);
                        entityManager.merge(serviceProviderDocument);
                    }
                });
                serviceProviderEntity.getQualificationDetailsList().clear();
            }

            serviceProviderEntity.getQualificationDetailsList().add(qualificationDetails);
            qualificationDetails.setService_provider(serviceProviderEntity);

            entityManager.persist(qualificationDetails);
            if(boardUniversityOthers!=null && boardUniversityToAdd.equals(1L))
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
        if(!CustomDateDeserializer.isValidDate)
        {
            throw new IllegalArgumentException("Date must be in yyyy-MM-dd format");
        }
        checkIfQualificationAlreadyExists(userId, qualificationDetails.getQualification_id(), roleName);
        List<Qualification> qualifications = qualificationService.getAllQualifications();
        Integer qualificationToAdd = findQualificationId(qualificationDetails.getQualification_id(), qualifications);
        qualificationDetails.setQualification_id(qualificationToAdd);
        List<Institution> institutions = institutionService.getAllInstitutions();
        Institution institutionToAdd = findInstitutionId(qualificationDetails.getInstitution().getInstitution_id(), institutions);
        qualificationDetails.setInstitution(institutionToAdd);
        List<BoardUniversity> boardUniversities = boardUniversityService.getAllBoardUniversities();
        Long boardUniversityToAdd = findBoardUniversityById(qualificationDetails.getBoard_university_id(), boardUniversities);
        OtherItem boardUniversityOtherItemToAdd=handleOtherCaseForBoardUniversity(boardUniversityToAdd,boardUniversityOthers,roleId,userId,sourceName);
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
        if(boardUniversityOthers!=null && boardUniversityToAdd.equals(1L))
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
        if(roleName.equalsIgnoreCase("CUSTOMER"))
        {
            customCustomer.getQualificationDetailsList().forEach(detail -> {
                if (detail.getQualificationDocument() != null) {
                    Document customerDocument=detail.getQualificationDocument();
                    customerDocument.setQualificationDetails(null);
                    customerDocument.setIsArchived(true);
                    entityManager.merge(customerDocument);
                }
            });
        }
        else if(roleName.equalsIgnoreCase(Constant.SERVICE_PROVIDER))
        {
            serviceProviderEntity.getQualificationDetailsList().forEach(detail -> {
                if (detail.getServiceProviderDocument() != null) {
                    ServiceProviderDocument serviceProviderDocument = detail.getServiceProviderDocument();
                    serviceProviderDocument.setQualificationDetails(null); // Detach reference
                    serviceProviderDocument.setIsArchived(true);           // Update field
                    entityManager.merge(serviceProviderDocument);          // Merge immediately
                }
            });
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
    public QualificationDetails updateQualificationDetail(Long userId, Long qualificationId, UpdateQualificationDto qualification, String boardUniversityOthers,Integer roleId, String roleName) throws Exception {
        String sourceName= "update_qualification";
        String marksType=null;
        String marksObtained=null;
        String totalMarks=null;
        Integer qualificationIdToUpdate=null;
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
                            if (otherItem.getSource_name().equalsIgnoreCase("add_qualification") ||
                                    otherItem.getSource_name().equalsIgnoreCase("update_qualification") &&
                                            otherItem.getField_name().equalsIgnoreCase("board_or_university") && userExists.equals(true)) {
                                iterator.remove();
                            }
                        }
                        qualificationDetailsToUpdate.setOtherItems(currentOtherItems);
                    }
                } else if (isOtherBoardUniversity.equals(true)) {
                    List<OtherItem> existingItems = qualificationDetailsToUpdate.getOtherItems();
                    if (existingItems != null && !existingItems.isEmpty()) {
                        boolean itemUpdated = false;
                        Iterator<OtherItem> iterator = existingItems.iterator();

                        while (iterator.hasNext()) {
                            OtherItem otherItem = iterator.next();
                            if (otherItem.getSource_name().equalsIgnoreCase("add_qualification") ||
                                    (otherItem.getSource_name().equalsIgnoreCase("update_qualification") &&
                                            otherItem.getField_name().equalsIgnoreCase("board_or_university"))) {
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
                    entityManager.merge(qualificationDetailsToUpdate);
                }
        }

        if(Objects.nonNull(qualification.getInstitution_id()))
        {
            List<Institution> institutions = institutionService.getAllInstitutions();
            Institution institutionToAdd= findInstitutionId(qualification.getInstitution_id(),institutions);
            qualificationDetailsToUpdate.setInstitution(institutionToAdd);
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

        if (Objects.nonNull(qualification.getStream_id())) {
            List<CustomStream> streams = streamService.getAllStream();
            Long streamToAdd= findStreamId(qualification.getStream_id(),streams);
            qualificationDetailsToUpdate.setStream_id(streamToAdd);
        }

        if (Objects.nonNull(qualification.getCumulative_percentage_value())) {
            qualificationDetailsToUpdate.setCumulative_percentage_value(qualification.getCumulative_percentage_value());
        }

        if (Objects.nonNull(qualification.getDate_of_passing())) {
            validateDate(qualification.getDate_of_passing());
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
    public Boolean validateDate(String dateOfPassing) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);

        try {
            // Validate format
            if (!isValidDateFormat(dateOfPassing, dateFormat)) {
                throw new IllegalArgumentException("Date of Passing must be in yyyy-MM-dd format");
            }

            Date dateOfIssue = dateFormat.parse(dateOfPassing);
            return true;
        } catch (IllegalArgumentException ex) {
            exceptionHandlingService.handleException(ex);
            throw ex; // Rethrow with meaningful context
        } catch (ParseException ex) {
            exceptionHandlingService.handleException(ex);
            throw new IllegalArgumentException("Invalid date format", ex);
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

}