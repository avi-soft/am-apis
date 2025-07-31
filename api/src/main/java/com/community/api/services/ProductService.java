package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.AddProductDto;
import com.community.api.dto.CustomProductWrapper;
import com.community.api.dto.DivisionProjectionDTO;
import com.community.api.dto.PostProjectionDTO;
import com.community.api.dto.QualificationEligibilityDto;
import com.community.api.dto.CategoryDistributionDto;
import com.community.api.dto.DistrictDistributionDto;
import com.community.api.dto.PostDto;
import com.community.api.dto.DistrictCategoryDistributionDto;
import com.community.api.dto.QualificationGroupDto;
import com.community.api.dto.ZoneDistributionDto;
import com.community.api.dto.StateDistributionDto;
import com.community.api.dto.GenderDistributionDto;
import com.community.api.dto.DivisionDistributionDto;
import com.community.api.dto.DivisionCategoryDistributionDto;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.*;
import com.community.api.services.exception.ExceptionHandlingService;
import javassist.NotFoundException;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.community.api.component.Constant.*;
import static com.community.api.component.Constant.PRODUCTNOTFOUND;
import static com.community.api.endpoint.avisoft.controller.product.ProductController.*;

@Service
public class ProductService {

    protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Resource(name = "blCatalogService")
    protected CatalogService catalogService;

    @Autowired
    QualificationGroupService qualificationGroupService;
     @Autowired
     QualificationDetailsService qualificationDetailsService;
    @Autowired
    ReserveCategoryDtoService reserveCategoryDtoService;
    @Autowired
    ProductStateService productStateService;
    @Autowired
    private ReserveCategoryAgeService reserveCategoryAgeService;
    @Autowired
    ProductReserveCategoryBornBeforeAfterRefService productReserveCategoryBornBeforeAfterRefService;
    @Autowired
    ProductReserveCategoryFeePostRefService productReserveCategoryFeePostRefService;
    @Autowired
    ReserveCategoryService reserveCategoryService;
    @Autowired
    RoleService roleService;
    @Autowired
    PrivilegeService privilegeService;
    @Autowired
    ApplicationScopeService applicationScopeService;
    @Autowired
    JwtUtil jwtTokenUtil;
    @Autowired
    ExceptionHandlingService exceptionHandlingService;
    @Autowired
    JobGroupService jobGroupService;
    @Autowired
    ProductRejectionStatusService productRejectionStatusService;
    @Autowired
    DistrictService districtService;
    @Autowired
    GenderService genderService;
    @Autowired
    SectorService sectorService;
    @Autowired
    QualificationService qualificationService;
    @Autowired
    StreamService streamService;
    @Autowired
    SubjectService subjectService;
    @Autowired
    ProductGenderPhysicalRequirementService productGenderPhysicalRequirementService;
    @Autowired
    AdvertisementService advertisementService;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private ResponseService responseService;

    @Autowired
    ZoneDivisionService zoneDivisionService;

    /*public void saveCustomProduct(Product product, AddProductDto addProductDto, CustomProductState productState, Role role, Long creatorUserId, Date modifiedDate, Date currentDate) {

        try {

            // Start building the SQL query
            StringBuilder sql = new StringBuilder("INSERT INTO custom_product (product_id, creator_user_id, creator_role_id, last_modified, product_state_id, created_date, advertisement_id");
            StringBuilder values = new StringBuilder("VALUES (:productId, :creatorUserId, :role, :lastModified, :productState, :currentDate, :advertisement");

            // Dynamically add columns and values based on non-null fields

            if (addProductDto.getApplicationScope() != null) {
                sql.append(", application_scope_id");
                values.append(", :applicationScope");
            }
            if(addProductDto.getSectorRunningField()!=null)
            {
                sql.append(", sector_running_field");
                values.append(", :sectorRunningField");
            }
            if(addProductDto.getFeeAdditionalComments()!=null)
            {
                sql.append(", fee_additional_comments");
                values.append(", :feeComments");
            }
            if (addProductDto.getAdditionalComments() != null) {
                sql.append(", additional_comments");
                values.append(", :additionalComments");
            }
            if (addProductDto.getExamCenterAvailableDate() != null) {
                sql.append(", exam_center_available_date");
                values.append(", :examCenterAvailableDate");
            }

            if (addProductDto.getExamDateFrom() != null) {
                sql.append(", exam_date_from");
                values.append(", :examDateFrom");
            }
            if (addProductDto.getAnswerKeyAvailableDate() != null) {
                sql.append(", answer_key_available_date");
                values.append(", :answerKeyAvailableDate");
            }
            if (addProductDto.getResultDeclarationDate() != null) {
                sql.append(", result_declaration_date");
                values.append(", :resultDeclarationDate");
            }
            if (addProductDto.getTentativeVerificationFrom() != null) {
                sql.append(", tentative_document_verification_from");
                values.append(", :tentativeVerificationFrom");
            }
            if (addProductDto.getTentativeVerificationTo() != null) {
                sql.append(", tentative_document_verification_to");
                values.append(", :tentativeVerificationTo");
            }
            if (addProductDto.getCounsellingDate() != null) {
                sql.append(", counselling_date");
                values.append(", :counsellingDate");
            }

            if (addProductDto.getExamDateTo() != null) {
                sql.append(", exam_date_to");
                values.append(", :examDateTo");
            }

            if (addProductDto.getGoLiveDate() != null) {
                sql.append(", go_live_date");
                values.append(", :goLiveDate");
            }

            if (addProductDto.getPlatformFee() != null) {
                sql.append(", platform_fee");
                values.append(", :platformFee");
            }

            if (addProductDto.getPriorityLevel() != null) {
                sql.append(", priority_level");
                values.append(", :priorityLevel");
            }

            if (addProductDto.getAdmitCardDateFrom() != null) {
                sql.append(", admit_card_date_from");
                values.append(", :admitCardDateFrom");
            }

            if (addProductDto.getAdmitCardDateTo() != null) {
                sql.append(", admit_card_date_to");
                values.append(", :admitCardDateTo");
            }

            if (addProductDto.getModificationDateFrom() != null) {
                sql.append(", modification_date_from");
                values.append(", :modificationDateFrom");
            }

            if (addProductDto.getModificationDateTo() != null) {
                sql.append(", modification_date_to");
                values.append(", :modificationDateTo");
            }

            if (addProductDto.getState() != null) {
                sql.append(", state_id");
                values.append(", :state");
            }

            if (addProductDto.getLastDateToPayFee() != null) {
                sql.append(", last_date_to_pay_fee");
                values.append(", :lastDateToPayFee");
            }

            if (addProductDto.getDownloadNotificationLink() != null) {
                sql.append(", download_notification_link");
                values.append(", :downloadNotificationLink");
            }

            if (addProductDto.getDownloadSyllabusLink() != null) {
                sql.append(", download_syllabus_link");
                values.append(", :downloadSyllabusLink");
            }

            if (addProductDto.getFormComplexity() != null) {
                sql.append(", form_complexity");
                values.append(", :formComplexity");
            }

            if (addProductDto.getSector() != null) {
                sql.append(", sector_id");
                values.append(", :sectorId");
            }

            if (addProductDto.getSelectionCriteria() != null) {
                sql.append(", selection_criteria");
                values.append(", :selectionCriteria");
            }

            if(addProductDto.getIsReviewRequired()!=null)
            {
                sql.append(", is_review_required");
                values.append(", :isReviewRequired");
            }
            if(addProductDto.getOtherInfo()!=null)
            {
                sql.append(", other_info");
                values.append(", :otherInfo");
            }
            if(addProductDto.getIsMultiplePostSameFee()!=null)
            {
                sql.append(", is_multiple_post_same_fee");
                values.append(", :isMultiplePostSameFee");
            }
            if (addProductDto.getIsExamDateFromNa() != null) {
                sql.append(", is_exam_date_from_na");
                values.append(", :isExamDateFromNa");
            }
            if (addProductDto.getIsAnswerKeyAvailableDateNa() != null) {
                sql.append(", is_answer_key_available_date_na");
                values.append(", :isAnswerKeyAvailableDateNa");
            }
            if (addProductDto.getIsResultDeclarationDateNa() != null) {
                sql.append(", is_result_declaration_date_na");
                values.append(", :isResultDeclarationDateNa");
            }
            if (addProductDto.getIsCounsellingDateNa() != null) {
                sql.append(", is_counselling_date_na");
                values.append(", :isCounsellingDateNa");
            }
            if (addProductDto.getIsTentativeVerificationFromNa() != null) {
                sql.append(", is_tentative_document_verification_from_na");
                values.append(", :isTentativeVerificationFromNa");
            }
            if (addProductDto.getIsTentativeVerificationToNa() != null) {
                sql.append(", is_tentative_document_verification_to_na");
                values.append(", :isTentativeVerificationToNa");
            }
            if (addProductDto.getIsExamDateToNa() != null) {
                sql.append(", is_exam_date_to_na");
                values.append(", :isExamDateToNa");
            }
            if (addProductDto.getIsExamCenterAvailableDateNa() != null) {
                sql.append(", is_exam_center_available_date_na");
                values.append(", :isExamCenterAvailableDateNa");
            }
            if (addProductDto.getIsLateDateToPayFeeNa() != null) {
                sql.append(", is_last_date_to_pay_fee_na");
                values.append(", :isLateDateToPayFeeNa");
            }
            if (addProductDto.getIsAdmitCardDateFromNa() != null) {
                sql.append(", is_admit_card_date_from_na");
                values.append(", :isAdmitCardDateFromNa");
            }
            if (addProductDto.getIsAdmitCardDateToNa() != null) {
                sql.append(", is_admit_card_date_to_na");
                values.append(", :isAdmitCardDateToNa");
            }
            if (addProductDto.getIsModificationDateFromNa() != null) {
                sql.append(", is_modification_date_from_na");
                values.append(", :isModificationDateFromNa");
            }
            if (addProductDto.getIsModificationDateToNa() != null) {
                sql.append(", is_modification_date_to_na");
                values.append(", :isModificationDateToNa");
            }

            // Complete the SQL statement
            sql.append(") ").append(values).append(")");

            // Create the query
            var query = entityManager.createNativeQuery(sql.toString())
                    .setParameter("productId", product)
                    .setParameter("creatorUserId", creatorUserId)
                    .setParameter("role", role)
                    .setParameter("lastModified", modifiedDate)
                    .setParameter("currentDate", currentDate)
                    .setParameter("advertisement", addProductDto.getAdvertisement());

            // Set parameters conditionally

            if (addProductDto.getApplicationScope() != null) {
                query.setParameter("applicationScope", addProductDto.getApplicationScope());
            }
            if (addProductDto.getAdditionalComments() != null) {
                query.setParameter("additionalComments", addProductDto.getAdditionalComments());
            }
            if(addProductDto.getFeeAdditionalComments()!=null)
            {
                query.setParameter("feeComments",addProductDto.getFeeAdditionalComments());
            }
            if (addProductDto.getExamDateFrom() != null) {
                query.setParameter("examDateFrom", new Timestamp(addProductDto.getExamDateFrom().getTime()));
            }
             if(addProductDto.getSectorRunningField()!=null)
             {
                 query.setParameter("sectorRunningField",addProductDto.getSectorRunningField());
             }
            query.setParameter("productState", productState);

            if (addProductDto.getState() != null) {
                query.setParameter("state", addProductDto.getState());
            }
            if(addProductDto.getAnswerKeyAvailableDate()!=null)
            {
                query.setParameter("answerKeyAvailableDate", new Timestamp(addProductDto.getAnswerKeyAvailableDate().getTime()));
            }
            if(addProductDto.getCounsellingDate()!=null)
            {
                query.setParameter("counsellingDate", new Timestamp(addProductDto.getCounsellingDate().getTime()));
            }
            if(addProductDto.getResultDeclarationDate()!=null)
            {
                query.setParameter("resultDeclarationDate", new Timestamp(addProductDto.getResultDeclarationDate().getTime()));
            }
            if(addProductDto.getTentativeVerificationFrom()!=null)
            {
                query.setParameter("tentativeVerificationFrom", new Timestamp(addProductDto.getTentativeVerificationFrom().getTime()));
            }
            if(addProductDto.getTentativeVerificationTo()!=null)
            {
                query.setParameter("tentativeVerificationTo", new Timestamp(addProductDto.getTentativeVerificationTo().getTime()));
            }
            if(addProductDto.getExamCenterAvailableDate()!=null)
            {
                query.setParameter("examCenterAvailableDate",(addProductDto.getExamCenterAvailableDate()));
            }
            if (addProductDto.getExamDateTo() != null) {
                query.setParameter("examDateTo", new Timestamp(addProductDto.getExamDateTo().getTime()));
            }

            if (addProductDto.getGoLiveDate() != null) {
                query.setParameter("goLiveDate", new Timestamp(addProductDto.getGoLiveDate().getTime()));
            }

            if (addProductDto.getPlatformFee() != null) {
                query.setParameter("platformFee", addProductDto.getPlatformFee());
            }

            if (addProductDto.getPriorityLevel() != null) {
                query.setParameter("priorityLevel", addProductDto.getPriorityLevel());
            }

            if (addProductDto.getAdmitCardDateFrom() != null) {
                query.setParameter("admitCardDateFrom", new Timestamp(addProductDto.getAdmitCardDateFrom().getTime()));
            }

            if (addProductDto.getAdmitCardDateTo() != null) {
                query.setParameter("admitCardDateTo", new Timestamp(addProductDto.getAdmitCardDateTo().getTime()));
            }

            if (addProductDto.getModificationDateFrom() != null) {
                query.setParameter("modificationDateFrom", new Timestamp(addProductDto.getModificationDateFrom().getTime()));
            }

            if (addProductDto.getModificationDateTo() != null) {
                query.setParameter("modificationDateTo", new Timestamp(addProductDto.getModificationDateTo().getTime()));
            }

            if (addProductDto.getLastDateToPayFee() != null) {
                query.setParameter("lastDateToPayFee", new Timestamp(addProductDto.getLastDateToPayFee().getTime()));
            }

            if (addProductDto.getDownloadNotificationLink() != null) {
                query.setParameter("downloadNotificationLink", addProductDto.getDownloadNotificationLink());
            }

            if (addProductDto.getDownloadSyllabusLink() != null) {
                query.setParameter("downloadSyllabusLink", addProductDto.getDownloadSyllabusLink());
            }

            if (addProductDto.getFormComplexity() != null) {
                query.setParameter("formComplexity", addProductDto.getFormComplexity());
            }

            if (addProductDto.getSector() != null) {
                query.setParameter("sectorId", addProductDto.getSector());
            }

            if (addProductDto.getSelectionCriteria() != null) {
                query.setParameter("selectionCriteria", addProductDto.getSelectionCriteria());
            }

            if(addProductDto.getIsReviewRequired()!=null)
            {
                query.setParameter("isReviewRequired",addProductDto.getIsReviewRequired());
            } if(addProductDto.getOtherInfo()!=null)
            {
                query.setParameter("otherInfo",addProductDto.getOtherInfo());
            }
            if(addProductDto.getIsMultiplePostSameFee()!=null)
            {
                query.setParameter("isMultiplePostSameFee",addProductDto.getIsMultiplePostSameFee());
            }

            if (addProductDto.getIsExamDateFromNa() != null) {
                query.setParameter("isExamDateFromNa", addProductDto.getIsExamDateFromNa());
            }
            if (addProductDto.getIsAnswerKeyAvailableDateNa() != null) {
                query.setParameter("isAnswerKeyAvailableDateNa", addProductDto.getIsAnswerKeyAvailableDateNa());
            }
            if (addProductDto.getIsResultDeclarationDateNa() != null) {
                query.setParameter("isResultDeclarationDateNa", addProductDto.getIsResultDeclarationDateNa());
            }
            if (addProductDto.getIsCounsellingDateNa() != null) {
                query.setParameter("isCounsellingDateNa", addProductDto.getIsCounsellingDateNa());
            }
            if (addProductDto.getIsTentativeVerificationFromNa() != null) {
                query.setParameter("isTentativeVerificationFromNa", addProductDto.getIsTentativeVerificationFromNa());
            }
            if (addProductDto.getIsTentativeVerificationToNa() != null) {
                query.setParameter("isTentativeVerificationToNa", addProductDto.getIsTentativeVerificationToNa());
            }
            if (addProductDto.getIsExamDateToNa() != null) {
                query.setParameter("isExamDateToNa", addProductDto.getIsExamDateToNa());
            }
            if (addProductDto.getIsExamCenterAvailableDateNa() != null) {
                query.setParameter("isExamCenterAvailableDateNa", addProductDto.getIsExamCenterAvailableDateNa());
            }
            if (addProductDto.getIsLateDateToPayFeeNa() != null) {
                query.setParameter("isLateDateToPayFeeNa", addProductDto.getIsLateDateToPayFeeNa());
            }
            if (addProductDto.getIsAdmitCardDateFromNa() != null) {
                query.setParameter("isAdmitCardDateFromNa", addProductDto.getIsAdmitCardDateFromNa());
            }
            if (addProductDto.getIsAdmitCardDateToNa() != null) {
                query.setParameter("isAdmitCardDateToNa", addProductDto.getIsAdmitCardDateToNa());
            }
            if (addProductDto.getIsModificationDateFromNa() != null) {
                query.setParameter("isModificationDateFromNa", addProductDto.getIsModificationDateFromNa());
            }
            if (addProductDto.getIsModificationDateToNa() != null) {
                query.setParameter("isModificationDateToNa", addProductDto.getIsModificationDateToNa());
            }

            // Execute the update
            query.executeUpdate();

        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            throw new RuntimeException("Failed to save Custom Product: " + e.getMessage(), e);
        }
    }*/
    public void saveCustomProduct(Product product, AddProductDto addProductDto, CustomProductState productState,
                                  Role role, Long creatorUserId, Date modifiedDate, Date currentDate) {
        try {
            StringBuilder sql = new StringBuilder("INSERT INTO custom_product (product_id, creator_user_id, creator_role_id, " +
                    "last_modified, product_state_id, created_date");
            StringBuilder values = new StringBuilder("VALUES (:productId, :creatorUserId, :role, :lastModified, " +
                    ":productState, :currentDate");

// Create parameter map for named parameters
            Map<String, Object> params = new HashMap<>();
            params.put("productId", product);
            params.put("creatorUserId", creatorUserId);
            params.put("role", role);
            params.put("lastModified", modifiedDate);
            params.put("productState", productState);
            params.put("currentDate", currentDate);

// Only include advertisement if it's present
            if (addProductDto.getAdvertisement() != null) {
                sql.append(", advertisement_id");
                values.append(", :advertisement");
                params.put("advertisement", addProductDto.getAdvertisement());
            }

            // Helper method to add column and parameter if value is not null
            BiConsumer<String, Object> addColumnIfNotNull = (columnName, value) -> {
                if (value != null) {
                    sql.append(", ").append(columnName);
                    values.append(", :").append(columnName);
                    params.put(columnName, value);
                }
            };

            // Add all possible columns
            addColumnIfNotNull.accept("application_scope_id", addProductDto.getApplicationScope());
            addColumnIfNotNull.accept("sector_running_field", addProductDto.getSectorRunningField());
            addColumnIfNotNull.accept("fee_additional_comments", addProductDto.getFeeAdditionalComments());
            addColumnIfNotNull.accept("additional_comments", addProductDto.getAdditionalComments());
            addColumnIfNotNull.accept("exam_center_available_date", addProductDto.getExamCenterAvailableDate());
            addColumnIfNotNull.accept("exam_date_from", addProductDto.getExamDateFrom() != null ?
                    new Timestamp(addProductDto.getExamDateFrom().getTime()) : null);
            addColumnIfNotNull.accept("answer_key_available_date", addProductDto.getAnswerKeyAvailableDate() != null ?
                    new Timestamp(addProductDto.getAnswerKeyAvailableDate().getTime()) : null);
            addColumnIfNotNull.accept("result_declaration_date", addProductDto.getResultDeclarationDate() != null ?
                    new Timestamp(addProductDto.getResultDeclarationDate().getTime()) : null);
            addColumnIfNotNull.accept("tentative_document_verification_from", addProductDto.getTentativeVerificationFrom() != null ?
                    new Timestamp(addProductDto.getTentativeVerificationFrom().getTime()) : null);
            addColumnIfNotNull.accept("tentative_document_verification_to", addProductDto.getTentativeVerificationTo() != null ?
                    new Timestamp(addProductDto.getTentativeVerificationTo().getTime()) : null);
            addColumnIfNotNull.accept("counselling_date", addProductDto.getCounsellingDate() != null ?
                    new Timestamp(addProductDto.getCounsellingDate().getTime()) : null);
            addColumnIfNotNull.accept("exam_date_to", addProductDto.getExamDateTo() != null ?
                    new Timestamp(addProductDto.getExamDateTo().getTime()) : null);
            addColumnIfNotNull.accept("go_live_date", addProductDto.getGoLiveDate() != null ?
                    new Timestamp(addProductDto.getGoLiveDate().getTime()) : null);
            addColumnIfNotNull.accept("platform_fee", addProductDto.getPlatformFee());
            addColumnIfNotNull.accept("priority_level", addProductDto.getPriorityLevel());
            addColumnIfNotNull.accept("admit_card_date_from", addProductDto.getAdmitCardDateFrom() != null ?
                    new Timestamp(addProductDto.getAdmitCardDateFrom().getTime()) : null);
            addColumnIfNotNull.accept("admit_card_date_to", addProductDto.getAdmitCardDateTo() != null ?
                    new Timestamp(addProductDto.getAdmitCardDateTo().getTime()) : null);
            addColumnIfNotNull.accept("modification_date_from", addProductDto.getModificationDateFrom() != null ?
                    new Timestamp(addProductDto.getModificationDateFrom().getTime()) : null);
            addColumnIfNotNull.accept("modification_date_to", addProductDto.getModificationDateTo() != null ?
                    new Timestamp(addProductDto.getModificationDateTo().getTime()) : null);
            addColumnIfNotNull.accept("state_id", addProductDto.getState());
            addColumnIfNotNull.accept("last_date_to_pay_fee", addProductDto.getLastDateToPayFee() != null ?
                    new Timestamp(addProductDto.getLastDateToPayFee().getTime()) : null);
            addColumnIfNotNull.accept("download_notification_link", addProductDto.getDownloadNotificationLink());
            addColumnIfNotNull.accept("download_syllabus_link", addProductDto.getDownloadSyllabusLink());
            addColumnIfNotNull.accept("form_complexity", addProductDto.getFormComplexity());
            addColumnIfNotNull.accept("sector_id", addProductDto.getSector());
            addColumnIfNotNull.accept("selection_criteria", addProductDto.getSelectionCriteria());
            addColumnIfNotNull.accept("is_review_required", addProductDto.getIsReviewRequired());
            addColumnIfNotNull.accept("other_info", addProductDto.getOtherInfo());
            addColumnIfNotNull.accept("is_multiple_post_same_fee", addProductDto.getIsMultiplePostSameFee());
            addColumnIfNotNull.accept("is_exam_date_from_na", addProductDto.getIsExamDateFromNa());
            addColumnIfNotNull.accept("is_answer_key_available_date_na", addProductDto.getIsAnswerKeyAvailableDateNa());
            addColumnIfNotNull.accept("is_result_declaration_date_na", addProductDto.getIsResultDeclarationDateNa());
            addColumnIfNotNull.accept("is_counselling_date_na", addProductDto.getIsCounsellingDateNa());
            addColumnIfNotNull.accept("is_tentative_document_verification_from_na", addProductDto.getIsTentativeVerificationFromNa());
            addColumnIfNotNull.accept("is_tentative_document_verification_to_na", addProductDto.getIsTentativeVerificationToNa());
            addColumnIfNotNull.accept("is_exam_date_to_na", addProductDto.getIsExamDateToNa());
            addColumnIfNotNull.accept("is_exam_center_available_date_na", addProductDto.getIsExamCenterAvailableDateNa());
            addColumnIfNotNull.accept("is_last_date_to_pay_fee_na", addProductDto.getIsLateDateToPayFeeNa());
            addColumnIfNotNull.accept("is_admit_card_date_from_na", addProductDto.getIsAdmitCardDateFromNa());
            addColumnIfNotNull.accept("is_admit_card_date_to_na", addProductDto.getIsAdmitCardDateToNa());
            addColumnIfNotNull.accept("is_modification_date_from_na", addProductDto.getIsModificationDateFromNa());
            addColumnIfNotNull.accept("is_modification_date_to_na", addProductDto.getIsModificationDateToNa());

            // Complete the SQL statement
            sql.append(") ").append(values).append(")");

            // Create and execute the query
            var query = entityManager.createNativeQuery(sql.toString());
            params.forEach(query::setParameter);

            query.executeUpdate();

        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            throw new RuntimeException("Failed to save Custom Product: " + e.getMessage(), e);
        }
    }


    public List<CustomProduct> getCustomProducts() throws Exception {
        try {
            String sql = "SELECT * FROM custom_product";
            return entityManager.createNativeQuery(sql, CustomProduct.class).getResultList();

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Failed to retrieve CustomProducts: " + exception.getMessage(), exception);
        }
    }

    public CustomProduct getCustomProductByCustomProductId(Long productId) {
        String sql = "SELECT c FROM CustomProduct c WHERE c.id = :productId";
        return entityManager.createQuery(sql, CustomProduct.class).setParameter("productId", productId).getResultList().get(0);
    }

    @Transactional
    public void removeCategoryProductFromCategoryProductRefTable(Long categoryId, Long productId) {
        String sql = "DELETE FROM blc_category_product_xref WHERE product_id = :productId AND category_id = :categoryId";
        try {
            entityManager.createNativeQuery(sql)
                    .setParameter("productId", productId)
                    .setParameter("categoryId", categoryId)
                    .executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to Delete Category_Product: " + e.getMessage(), e);
        }
    }

    public Map<String, String> getRequestParamBasedOnQueryString(String queryString) throws UnsupportedEncodingException {
        if (queryString != null) {

            String[] params = queryString.split("&"); // Split the query string by '&' to get each parameter

            // Create a map to hold parameters
            Map<String, String> paramMap = new HashMap<>();

            // Process each parameter
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = keyValue[1];

                    // Encode the value to UTF-8
                    value = URLEncoder.encode(value, "UTF-8"); // may throw exception.

                    paramMap.put(key, value);
                }
            }
            return paramMap;
        } else {
            return null;
        }
    }

    public Map<String,Object> filterProducts(List<Long> states, List<Long> statuses, List<Long> categories,
                                             List<Long> reserveCategories, String title, String displayTemplate,Double fee,
                                             Integer post, Date startRange, Date endRange,
                                             Boolean isExpired, Boolean isArchived,Integer offset,Integer limit,Boolean all,Long createdById, List<Long> productIds) throws Exception {
        try {
            StringBuilder count = new StringBuilder("SELECT COUNT(DISTINCT p) FROM CustomProduct p ");
            StringBuilder result = new StringBuilder("SELECT DISTINCT p FROM CustomProduct p ");
            StringBuilder jpql = new StringBuilder("JOIN SkuImpl s WITH s.defaultProduct.id = p.id ");
            if(fee != null || (reserveCategories != null && !reserveCategories.isEmpty())) {
                jpql.append("JOIN CustomProductReserveCategoryFeePostRef r WITH r.customProduct.id = p.id ");
            }

            // Base condition to allow easy AND appending
            Map<String ,Object>response=new HashMap<>();

           /* jpql.append("AND s.activeEndDate IS NOT NULL AND s.activeEndDate >= CURRENT_TIMESTAMP ");*/
            // List to hold query parameters
            List<CustomProductState> customProductStates = new ArrayList<>();
            List<CustomProductRejectionStatus> productRejectionStatuses = new ArrayList<>();
            List<Category> categoryList = new ArrayList<>();
            List<CustomReserveCategory> customReserveCategoryList = new ArrayList<>();

            if (isArchived != null) {
                if (Boolean.TRUE.equals(isArchived)) {
                    // Access the embedded property correctly
                    jpql.append("AND p.archiveStatus.archived = 'Y' ");
                }
                else {
                    jpql.append("AND p.archiveStatus.archived = 'N' ");
                }
            }
            if (states != null && !states.isEmpty()) {
                boolean containsStateLive = false;
                for (Long id : states) {
                    CustomProductState productState = productStateService.getProductStateById(id);
                    if (productState == null) {
                        throw new IllegalArgumentException("NO PRODUCT STATE FOUND WITH THIS ID: " + id);
                    }
                    customProductStates.add(productState);
                    if (id == 5L) {
                        containsStateLive = true;
                    }
                }
                jpql.append("AND p.productState IN :states ");
                if(states.contains(2L))
                    jpql.append("OR p.isEdited = true ");
                if (containsStateLive) {
                    jpql.append("AND (p.productState.id != 5 OR (p.productState.id = 5 AND FUNCTION('DATE', p.goLiveDate) <= FUNCTION('DATE', CURRENT_TIMESTAMP))) ");
                }
            }

            if (statuses != null && !statuses.isEmpty()) {
                for (Long id : statuses) {
                    CustomProductRejectionStatus productRejectionStatus = productRejectionStatusService.getAllRejectionStatusByRejectionStatusId(id);
                    if (productRejectionStatus == null) {
                        throw new IllegalArgumentException("NO PRODUCT STATUS FOUND WITH THIS ID: " + id);
                    }
                    productRejectionStatuses.add(productRejectionStatus);
                }

                // Explicitly filter for non-null rejection status that matches the specified values
                jpql.append("AND p.rejectionStatus IS NOT NULL AND p.rejectionStatus IN :statuses ");
            }
            List<Long> customProductIds = new ArrayList<>();
            if(productIds!=null && !productIds.isEmpty())
            {
                for (Long id : productIds) {
                    CustomProduct customProduct = entityManager.find(CustomProduct.class, id);
                    if (customProduct == null) {
                        throw new IllegalArgumentException("NO PRODUCT FOUND WITH PRODUCT ID: " + id);
                    }
                    customProductIds.add(id);
                }
                jpql.append("AND p.id IN :productIds ");
            }

            if (createdById != null) {
                jpql.append(" AND p.userId = :creatorUserId ");
            }
            if (categories != null && !categories.isEmpty()) {
                boolean anyValidCategory = false;
                for (Long id : categories) {
                    Category category = catalogService.findCategoryById(id);
                    if (category == null) {
                        throw new IllegalArgumentException("NO CATEGORY FOUND WITH THIS ID: " + id);
                    }

                    // Check if category is active and not archived
                    if ((((Status) category).getArchived() != 'Y' && category.getActiveEndDate() == null) ||
                            (((Status) category).getArchived() != 'Y' && category.getActiveEndDate().after(new Date()))) {
                        categoryList.add(category);
                        anyValidCategory = true;
                    }
                }

                if (anyValidCategory) {
                    jpql.append(" AND p.defaultCategory IN :categories ");
                } else {
                    // If all requested categories are archived or inactive, return no results
                    throw new IllegalArgumentException("All requested categories are archived or inactive");
                }
            }

            if (reserveCategories != null && !reserveCategories.isEmpty()) {
                for (Long id : reserveCategories) {
                    customReserveCategoryList.add(reserveCategoryService.getReserveCategoryById(id));
                }
                jpql.append("AND r IS NOT NULL AND r.customReserveCategory IN :reserveCategories ");
            }

            if (title != null && !title.isEmpty()) {
                String trimmedTitle = title.trim();

                jpql.append("AND LOCATE(LOWER(:titlePhrase), LOWER(p.metaTitle)) > 0 ");
            }
            if (displayTemplate != null && !displayTemplate.isEmpty()) {
                String trimmedDisplayTemplate = displayTemplate.trim();
                jpql.append("AND LOCATE(LOWER(:displayTemplatePhrase), LOWER(p.displayTemplate)) > 0 ");
            }


            if (fee != null) {
                jpql.append("AND r.fee = :fee ");
            }

            if (post != null) {
                jpql.append("AND SIZE(p.posts) = :post ");
            }

            // Filter for exact date match, ignoring time portion
            if (startRange != null) {
                jpql.append("AND p.defaultSku.activeStartDate IS NOT NULL ");
                jpql.append("AND FUNCTION('DATE', p.defaultSku.activeStartDate) = FUNCTION('DATE', :startRange) ");
            }

            if (endRange != null) {
                jpql.append("AND p.defaultSku.activeEndDate IS NOT NULL ");
                jpql.append("AND FUNCTION('DATE', p.defaultSku.activeEndDate) = FUNCTION('DATE', :endRange) ");
            }

            if (Boolean.TRUE.equals(isExpired)) {
                // Only expired products
                jpql.append("AND s.activeEndDate IS NOT NULL AND s.activeEndDate <= CURRENT_TIMESTAMP ");
            } else if(Boolean.FALSE.equals(isExpired)) {
                // Only non-expired products
                jpql.append("AND (s.activeEndDate IS NOT NULL AND s.activeEndDate > CURRENT_TIMESTAMP) ");
            }
            jpql.append("AND p.del = 'N' ");
            jpql.append("ORDER BY p.createdDate DESC ");

            TypedQuery<Long> queryToCount = entityManager.createQuery(count.append(jpql.toString().replace("ORDER BY p.createdDate DESC ", "")).toString(),Long.class);
            jpql=result.append(jpql);
            // Create the query with the final JPQL string
            TypedQuery<CustomProduct> query = entityManager.createQuery(jpql.toString(), CustomProduct.class);
            query.setFirstResult(offset*limit);     // e.g., offset = 20
            query.setMaxResults(limit);// e.g., limit = 10
            // Set parameters
            if (!customProductStates.isEmpty()) {
                query.setParameter("states", customProductStates);
                queryToCount.setParameter("states", customProductStates);
            }
            if (!productRejectionStatuses.isEmpty()) {
                query.setParameter("statuses", productRejectionStatuses);
                queryToCount.setParameter("statuses", productRejectionStatuses);
            }
            if (!categoryList.isEmpty()) {
                query.setParameter("categories", categoryList);
                queryToCount.setParameter("categories", categoryList);
            }
            if (!customReserveCategoryList.isEmpty()) {
                query.setParameter("reserveCategories", customReserveCategoryList);
                queryToCount.setParameter("reserveCategories", customReserveCategoryList);
            }
            if(!customProductIds.isEmpty())
            {
                query.setParameter("productIds", customProductIds);
                queryToCount.setParameter("productIds", customProductIds);
            }
            if (title != null && !title.isEmpty()) {
                String trimmedTitle = title.trim();

                // Set the parameter for LOCATE function
                query.setParameter("titlePhrase", trimmedTitle);
                queryToCount.setParameter("titlePhrase", trimmedTitle);
            }
            if (displayTemplate != null && !displayTemplate.isEmpty()) {
                String trimmedDisplayTemplate = displayTemplate.trim();
                query.setParameter("displayTemplatePhrase", trimmedDisplayTemplate);
                queryToCount.setParameter("displayTemplatePhrase", trimmedDisplayTemplate);
            }

            if (fee != null) {
                query.setParameter("fee", fee);
                queryToCount.setParameter("fee", fee);
            }
            if (createdById != null) {
                query.setParameter("creatorUserId", createdById);
                queryToCount.setParameter("creatorUserId", createdById);
            }
            if (post != null) {
                query.setParameter("post", post);
                queryToCount.setParameter("post", post);
            }
            if (startRange != null) {
                query.setParameter("startRange", startRange);
                queryToCount.setParameter("startRange", startRange);
            }
            if (endRange != null) {
                query.setParameter("endRange", endRange);
                queryToCount.setParameter("endRange", endRange);
            }
            int res=queryToCount.getSingleResult().intValue();
             response.put("count",res);
             response.put("products",query.getResultList());
            // Execute and return the result
            return response;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION CAUGHT: " + exception.getMessage());
        }
    }

    public ResponseEntity<?> filterProductsByRoleAndUserId(Integer roleId, Long userId, int page, int limit, boolean showDraftProducts, List<Long> states, List<Long> statuses, List<Long> categories, List<Long> reserveCategories, String title,String displayTemplate, Double fee, Integer post, Date dateFrom, Date dateTo, List<Long> productIds,Boolean isArchived) {
        try {
            Role role = null;
            if (roleId != null) {
                role = entityManager.find(Role.class, roleId);
                if (role == null) {
                    throw new IllegalArgumentException("No role exists with id " + roleId);
                }
            }
            Long createdById = null;
            if (role != null && (ADMIN.equals(role.getRole_name()) || SUPER_ADMIN.equals(role.getRole_name()))) {
                createdById = null;
            } else {
                createdById = userId;
            }

            if (states == null) {
                states = new ArrayList<>();
            }

            if (showDraftProducts) {
                CustomProductState draftState = productStateService.getProductStateByName("DRAFT");
                if (draftState == null) {
                    throw new IllegalStateException("Draft product state not found in the system");
                }
                states = Collections.singletonList(draftState.getProductStateId());
            } else if (states.isEmpty()) {
                // Include all states
                List<CustomProductState> allStates = productStateService.getAllProductState();
                for (CustomProductState state : allStates) {
                    if(!state.getProductState().equals("DRAFT"))
                    {
                        states.add(state.getProductStateId());
                    }
                }
            }
            Boolean isExpired = null;
            Map<String, Object> allProductsResponse = filterProducts(
                    states, statuses, categories, reserveCategories,
                    title,displayTemplate, fee, post, dateFrom, dateTo,
                    isExpired,isArchived, 0, Integer.MAX_VALUE, false, createdById, productIds
            );

            @SuppressWarnings("unchecked")
            List<CustomProduct> allProducts = (List<CustomProduct>) allProductsResponse.get("products");
            List<CustomProduct> nonArchivedProducts = allProducts.stream()
                    .filter(p -> p != null && ((Status) p).getArchived() != 'Y')
                    .collect(Collectors.toList());
            int totalItems = nonArchivedProducts.size();
            int totalPages = totalItems > 0 ? (int) Math.ceil((double) totalItems / limit) : 0;
            if (page >= totalPages && page > 0 && totalItems > 0) {
                throw new IllegalArgumentException("No more products available");
            }
            int startIndex = page * limit;
            int endIndex = Math.min(startIndex + limit, totalItems);
            List<CustomProduct> pagedProducts;
            if (startIndex < totalItems) {
                pagedProducts = nonArchivedProducts.subList(startIndex, endIndex);
            } else {
                pagedProducts = new ArrayList<>();
            }
            if (pagedProducts.isEmpty() && page == 0) {
                String message = showDraftProducts ?
                        "No draft products found with the given criteria" :
                        "No products found with the given criteria";
                return ResponseService.generateSuccessResponse(message, new ArrayList<>(), HttpStatus.OK);
            }

            List<CustomProductWrapper> responses = new ArrayList<>();
            for (CustomProduct customProduct : pagedProducts) {
                CustomProductWrapper wrapper = new CustomProductWrapper();
                List<Post> postList = customProduct.getPosts();
                List<PostProjectionDTO> postProjectionDTOs = getPosts(customProduct.getPosts());
                wrapper.wrapDetails(customProduct, postList, postProjectionDTOs, productReserveCategoryFeePostRefService);
                responses.add(wrapper);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("products", responses);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", page);

            String successMessage = showDraftProducts ?
                    "Draft products retrieved successfully" :
                    "PRODUCTS RETRIEVED SUCCESSFULLY";

            return ResponseService.generateSuccessResponse(successMessage, response, HttpStatus.OK);

        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse("EXCEPTION OCCURRED: " + exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public boolean addProductAccessAuthorisation(String authHeader) throws Exception {
        try {
            String jwtToken = authHeader.substring(7);

            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            String role = roleService.getRoleByRoleId(roleId).getRole_name();

            Long userId = null;
            if (role.equals(Constant.SUPER_ADMIN) || role.equals(Constant.ADMIN)) {
                return true;

                // -> NEED TO ADD THE USER_ID OF ADMIN OR SUPER ADMIN.

            } else if (role.equals(Constant.SERVICE_PROVIDER)) {
                userId = jwtTokenUtil.extractId(jwtToken);
                List<Privileges> privileges = privilegeService.getServiceProviderPrivilege(userId);

                for (Privileges privilege : privileges) {
                    if (privilege.getPrivilege_name().equals(Constant.PRIVILEGE_ADD_PRODUCT)) {
                        return true;
                    }
                }

                ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, userId);
                if(serviceProvider.getApproved()!=null && serviceProvider.getApproved()) {
                    return true;
                }
            }
            return false;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("ERRORS WHILE VALIDATING AUTHORIZATION: " + exception.getMessage() + "\n");
        }
    }

    public boolean deleteProductAccessAuthorisation(String authHeader) throws Exception {
        try {
            String jwtToken = authHeader.substring(7);

            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            String role = roleService.getRoleByRoleId(roleId).getRole_name();

            Long userId = null;
            if (role.equals(Constant.SUPER_ADMIN) || role.equals(Constant.ADMIN)) {
                return true;

                // -> NEED TO ADD THE USER_ID OF ADMIN OR SUPER ADMIN.

            } else if (role.equals(Constant.SERVICE_PROVIDER)) {
                userId = jwtTokenUtil.extractId(jwtToken);
                List<Privileges> privileges = privilegeService.getServiceProviderPrivilege(userId);

                for (Privileges privilege : privileges) {
                    if (privilege.getPrivilege_name().equals(PRIVILEGE_DELETE_PRODUCT)) {
                        return true;
                    }
                }

                ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, userId);
                if(serviceProvider.getApproved()!=null && serviceProvider.getApproved()) {
                    return true;
                }
            }
            return false;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("ERRORS WHILE VALIDATING AUTHORIZATION: " + exception.getMessage() + "\n");
        }
    }

    public Category validateCategory(Long categoryId) throws Exception {
        try {
            if (categoryId <= 0) throw new IllegalArgumentException("Category id cannot be <= 0.");
            Category category = catalogService.findCategoryById(categoryId);
            if (category == null || ((Status) category).getArchived() == 'Y') {
                throw new IllegalArgumentException("Category not found with this Id.");
            }
            return category;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught while validating category: " + exception.getMessage() + "\n");
        }
    }

    public boolean addProductDtoValidation(AddProductDto addProductDto) throws Exception {
        try {
            if (addProductDto.getQuantity() != null) {
                if (addProductDto.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Quantity cannot be <= 0.");
                }
            } else {
                addProductDto.setQuantity(Constant.DEFAULT_QUANTITY);
            }

            if (addProductDto.getPlatformFee() != null) {
                if (addProductDto.getPlatformFee() <= 0) {
                    throw new IllegalArgumentException("Platform fee cannot be <= 0.");
                }
            } else {
                addProductDto.setPlatformFee(DEFAULT_PLATFORM_FEE);
            }

            if (addProductDto.getPriorityLevel() != null) {
                if (addProductDto.getPriorityLevel() <= 0 || addProductDto.getPriorityLevel() > 5) {
                    throw new IllegalArgumentException("Priority level must lie between 1-5.");
                }
            } else {
                addProductDto.setPriorityLevel(DEFAULT_PRIORITY_LEVEL);
            }

            if (addProductDto.getMetaTitle() == null || addProductDto.getMetaTitle().trim().isEmpty()) {
                throw new IllegalArgumentException(PRODUCTTITLENOTGIVEN);
            } else {
                addProductDto.setMetaTitle(addProductDto.getMetaTitle().trim());
            }

            if (addProductDto.getDisplayTemplate() == null || addProductDto.getDisplayTemplate().trim().isEmpty()) {
                addProductDto.setDisplayTemplate(addProductDto.getMetaTitle());
            } else {
                addProductDto.setDisplayTemplate(addProductDto.getDisplayTemplate().trim());
            }

/*            if (addProductDto.getMetaDescription() == null || addProductDto.getMetaDescription().trim().isEmpty()) {
                throw new IllegalArgumentException("Description cannot be null or empty.");
            } else {
                addProductDto.setMetaDescription(addProductDto.getMetaDescription().trim());
            }
             */

            if(addProductDto.getIsExamDateFromNa()==null)
            {
                throw new IllegalArgumentException("You have to select whether exam date from is NA or not");
            }

            if(addProductDto.getIsAnswerKeyAvailableDateNa()==null)
            {
                throw new IllegalArgumentException("You have to select whether answer key available date is NA or not");
            }
            if(addProductDto.getIsResultDeclarationDateNa()==null)
            {
                throw new IllegalArgumentException("You have to select whether result declaration date is NA or not");
            }
            if(addProductDto.getIsCounsellingDateNa()==null)
            {
                throw new IllegalArgumentException("You have to select whether counselling date is NA or not");
            }
            if(addProductDto.getIsTentativeVerificationFromNa()==null)
            {
                throw new IllegalArgumentException("You have to select whether tentative verification from  date is NA or not");
            }
            if(addProductDto.getIsTentativeVerificationToNa()==null)
            {
                throw new IllegalArgumentException("You have to select whether tentative verification to date is NA or not");
            }
            if(addProductDto.getIsExamDateToNa()==null)
            {
                throw new IllegalArgumentException("You have to select whether exam date to is NA or not");
            }
            if(addProductDto.getIsExamCenterAvailableDateNa()==null)
            {
                throw new IllegalArgumentException("You have to select whether exam center available date is NA or not");
            }
            if(addProductDto.getIsLateDateToPayFeeNa()==null)
            {
                throw new IllegalArgumentException("You have to select whether last date to pay fee date is NA or not");
            }
            if(addProductDto.getIsAdmitCardDateFromNa()==null)
            {
                throw new IllegalArgumentException("You have to select whether admit card from date is NA or not");
            }
            if(addProductDto.getIsAdmitCardDateToNa()==null)
            {
                throw new IllegalArgumentException("You have to select whether admit card to is NA or not");
            }
            if(addProductDto.getIsModificationDateFromNa()==null)
            {
                throw new IllegalArgumentException("You have to select whether modification date from is NA or not");
            }
            if(addProductDto.getIsModificationDateToNa()==null)
            {
                throw new IllegalArgumentException("You have to select whether modification date to is NA or not");
            }
            if(addProductDto.getIsExamDateFromNa().equals(true))
            {
                addProductDto.setExamDateFrom(null);
            }
            if(addProductDto.getIsAnswerKeyAvailableDateNa().equals(true))
            {
                addProductDto.setAnswerKeyAvailableDate(null);
            }
            if(addProductDto.getIsResultDeclarationDateNa().equals(true))
            {
                addProductDto.setResultDeclarationDate(null);
            }
            if(addProductDto.getIsCounsellingDateNa().equals(true))
            {
                addProductDto.setCounsellingDate(null);
            }
            if(addProductDto.getIsTentativeVerificationFromNa().equals(true))
            {
                addProductDto.setTentativeVerificationFrom(null);
            }
            if(addProductDto.getIsTentativeVerificationToNa().equals(true))
            {
                addProductDto.setTentativeVerificationTo(null);
            }
            if(addProductDto.getIsExamDateToNa().equals(true))
            {
                addProductDto.setExamDateTo(null);
            }
            if(addProductDto.getIsExamCenterAvailableDateNa().equals(true))
            {
                addProductDto.setExamCenterAvailableDate(null);
            }
            if(addProductDto.getIsLateDateToPayFeeNa().equals(true))
            {
                addProductDto.setLastDateToPayFee(null);
            }
            if(addProductDto.getIsAdmitCardDateFromNa().equals(true))
            {
                addProductDto.setAdmitCardDateFrom(null);
            }
            if(addProductDto.getIsAdmitCardDateToNa().equals(true))
            {
                addProductDto.setAdmitCardDateTo(null);
            }
            if(addProductDto.getIsModificationDateFromNa().equals(true))
            {
                addProductDto.setModificationDateFrom(null);
            }
            if(addProductDto.getIsModificationDateToNa().equals(true))
            {
                addProductDto.setModificationDateTo(null);
            }
            String formattedDate = dateFormat.format(new Date());
            Date activeStartDate = dateFormat.parse(formattedDate); // Convert formatted date string back to Date

            if (addProductDto.getActiveEndDate() == null || addProductDto.getGoLiveDate() == null || addProductDto.getActiveStartDate() == null) {
                throw new IllegalArgumentException("Active start date, active end date, and go live date cannot be empty.");
            }
            dateFormat.parse(dateFormat.format(addProductDto.getActiveStartDate()));
            dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));
            dateFormat.parse(dateFormat.format(addProductDto.getGoLiveDate()));
            Date activeDateStart = stripTime(addProductDto.getActiveStartDate());
            Date activeDateEnd = stripTime(addProductDto.getActiveEndDate());
            if (!addProductDto.getActiveEndDate().after(activeStartDate)) {
                throw new IllegalArgumentException("Expiration date cannot be before or equal of current date.");
            } else if (!addProductDto.getGoLiveDate().before(addProductDto.getActiveEndDate())) {
                throw new IllegalArgumentException("Go-live date must be before active end date, or if on the same day, its time must be earlier.");
            } else if (activeDateStart.after(activeDateEnd)) {
                throw new IllegalArgumentException("Active start date cannot be after active end date.");
            } /*else if (!isSameOrFutureDate(addProductDto.getGoLiveDate())) {
                throw new IllegalArgumentException("Go live date cannot be past of current date.");
            }*/

            if(addProductDto.getExamDateFrom()!=null)
            {
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                Date examDateFrom = stripTime(addProductDto.getExamDateFrom());
                Date activeEndDate = stripTime(addProductDto.getActiveEndDate());

                if (!examDateFrom.after(activeEndDate)) {
                    throw new IllegalArgumentException("Tentative examination date from must be after active end date.");
                }
            }
            if(addProductDto.getExamDateTo()!=null)
            {
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));
                Date examDateTo = stripTime(addProductDto.getExamDateTo());
                Date activeEndDate = stripTime(addProductDto.getActiveEndDate());

                if (!examDateTo.after(activeEndDate))
                {
                    throw new IllegalArgumentException("tentative examination date to must be after active end date");
                }
            }
            if(addProductDto.getExamDateFrom()!=null && addProductDto.getExamDateTo()!=null )
            {
                Date examDateTo = stripTime(addProductDto.getExamDateTo());
                Date examDateFrom = stripTime(addProductDto.getExamDateFrom());
                if (examDateTo.before(examDateFrom)) {
                    throw new IllegalArgumentException(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM);
                }
            }

            if(addProductDto.getAdvertisement() == null || addProductDto.getAdvertisement() <= 0) {
                throw new IllegalArgumentException("Advertisement cannot be null or <= 0.");
            }

            Advertisement advertisement = advertisementService.getAdvertisementById(addProductDto.getAdvertisement());
            if (advertisement == null) {
                throw new NoSuchElementException("Advertisement not found.");
            }

//            if (addProductDto.getApplicationScope() == null || addProductDto.getApplicationScope() <= 0) {
//                throw new IllegalArgumentException("Application scope cannot be null or <= 0.");
//            }
           if(addProductDto.getApplicationScope()!=null)
           {
               CustomApplicationScope applicationScope = applicationScopeService.getApplicationScopeById(addProductDto.getApplicationScope());
               if (applicationScope == null) {
                   throw new NoSuchElementException("application scope not found.");
               }

               if (applicationScope.getApplicationScope().equals(Constant.APPLICATION_SCOPE_CENTER)) {

                   if (addProductDto.getState() != null) {
                       throw new IllegalArgumentException("State cannot be given if application scope " + applicationScope.getApplicationScope());
                   }
                   if (addProductDto.getDomicileRequired() != null && addProductDto.getDomicileRequired()) {
                       throw new IllegalArgumentException("Domicile required cannot be true if application scope " + applicationScope.getApplicationScope());
                   }
                   addProductDto.setDomicileRequired(false);

               } else if (applicationScope.getApplicationScope().equals(APPLICATION_SCOPE_STATE)) {
                   if (addProductDto.getDomicileRequired() == null || addProductDto.getState() == null) {
                       throw new IllegalArgumentException("For application scope: " + applicationScope.getApplicationScope() + " domicile and state cannot be null.");
                   }

                   if (addProductDto.getState() <= 0) {
                       throw new IllegalArgumentException("State cannot be <= 0.");
                   }

                   StateCode state = districtService.getStateByStateId(addProductDto.getState());
                   if (state == null) {
                       throw new NoSuchElementException("State not found.");
                   }
               }
           }


      /*      if (addProductDto.getReservedCategory() == null || addProductDto.getReservedCategory().isEmpty()) {
                throw new IllegalArgumentException("Reserve category must not be null or empty.");
            }
*/
            /*if(addProductDto.getIsReviewRequired()==null)
            {
                addProductDto.setIsReviewRequired(true);
            }*/

            if(addProductDto.getIsMultiplePostSameFee()==null)
            {
                throw new IllegalArgumentException("You have to select whether multiple post have same fees");
            }


            /*if(addProductDto.getPosts()==null || addProductDto.getPosts().isEmpty())
            {
                throw new IllegalArgumentException("Post cannot be null or empty");
            }*/

            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (NoSuchElementException noSuchElementException) {
            exceptionHandlingService.handleException(noSuchElementException);
            throw new IllegalArgumentException(noSuchElementException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new ParseException(parseException.getMessage() + "\n", parseException.getErrorOffset());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage() + "\n");
        }
    }

    /*public boolean addProductDtoWithoutValidation(AddProductDto addProductDto) throws Exception {
        try {
            if (addProductDto.getQuantity() != null) {
                if (addProductDto.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Quantity cannot be <= 0.");
                }
            } else {
                addProductDto.setQuantity(Constant.DEFAULT_QUANTITY);
            }

            if (addProductDto.getPlatformFee() != null) {
                if (addProductDto.getPlatformFee() <= 0) {
                    throw new IllegalArgumentException("Platform fee cannot be <= 0.");
                }
            } else {
                addProductDto.setPlatformFee(DEFAULT_PLATFORM_FEE);
            }

            if (addProductDto.getPriorityLevel() != null) {
                if (addProductDto.getPriorityLevel() <= 0 || addProductDto.getPriorityLevel() > 5) {
                    throw new IllegalArgumentException("Priority level must lie between 1-5.");
                }
            } else {
                addProductDto.setPriorityLevel(DEFAULT_PRIORITY_LEVEL);
            }

            if (addProductDto.getMetaTitle() == null || addProductDto.getMetaTitle().trim().isEmpty()) {
                throw new IllegalArgumentException(PRODUCTTITLENOTGIVEN);
            } else {
                addProductDto.setMetaTitle(addProductDto.getMetaTitle().trim());
            }

            if(addProductDto.getDisplayTemplate()!=null)
            {
                addProductDto.setDisplayTemplate(addProductDto.getDisplayTemplate().trim());
            }

           *//* if (addProductDto.getMetaDescription() == null || addProductDto.getMetaDescription().trim().isEmpty()) {
                throw new IllegalArgumentException("Description cannot be null or empty.");
            } else {
                addProductDto.setMetaDescription(addProductDto.getMetaDescription().trim());
            }
            *//*
            String formattedDate = dateFormat.format(new Date());
            Date activeStartDate = dateFormat.parse(formattedDate); // Convert formatted date string back to Date

            *//*if (addProductDto.getActiveEndDate() == null || addProductDto.getGoLiveDate() == null || addProductDto.getActiveStartDate() == null) {
                throw new IllegalArgumentException("Active start date, active end date, and go live date cannot be empty.");
            }*//*
            if(addProductDto.getActiveStartDate()!=null)
                dateFormat.parse(dateFormat.format(addProductDto.getActiveStartDate()));
            if(addProductDto.getActiveEndDate()!=null)
                dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));
            if(addProductDto.getGoLiveDate()!=null)
                dateFormat.parse(dateFormat.format(addProductDto.getGoLiveDate()));
            Date activeDateStart=null;
            Date activeDateEnd=null;
            if(addProductDto.getActiveStartDate()!=null)
                 activeDateStart = stripTime(addProductDto.getActiveStartDate());
            if(addProductDto.getActiveStartDate()!=null)
                 activeDateEnd = stripTime(addProductDto.getActiveEndDate());

            if (!addProductDto.getActiveEndDate().after(activeStartDate)) {
                throw new IllegalArgumentException("Expiration date cannot be before or equal of current date.");
            } else if (!addProductDto.getGoLiveDate().before(addProductDto.getActiveEndDate())) {
                throw new IllegalArgumentException("Go live date cannot be after or equal of active end date.");
            }else if (activeDateStart.after(activeDateEnd)) {
                throw new IllegalArgumentException("Active start date cannot be after active end date.");
            } *//*else if (!isSameOrFutureDate(addProductDto.getGoLiveDate())) {
                throw new IllegalArgumentException("Go live date cannot be past of current date.");
            }*//*
            if(addProductDto.getExamDateFrom()!=null)
            {
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
            }
            if(addProductDto.getExamDateTo()!=null)
            {
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));
            }

            if(addProductDto.getExamDateFrom()!=null)
            {
                Date examDateFrom = stripTime(addProductDto.getExamDateFrom());
                Date activeEndDate = stripTime(addProductDto.getActiveEndDate());

                if (!examDateFrom.after(activeEndDate)) {
                    throw new IllegalArgumentException("Tentative examination date from must be after active end date.");
                }
            }
            if(addProductDto.getExamDateTo()!=null)
            {
                Date examDateTo = stripTime(addProductDto.getExamDateTo());
                Date activeEndDate = stripTime(addProductDto.getActiveEndDate());

                if (!examDateTo.after(activeEndDate))
                {
                    throw new IllegalArgumentException("tentative examination date to must be after active end date");
                }
            }
            if(addProductDto.getExamDateFrom()!=null && addProductDto.getExamDateTo()!=null )
            {
                Date examDateTo = stripTime(addProductDto.getExamDateTo());
                Date examDateFrom = stripTime(addProductDto.getExamDateFrom());
                if (examDateTo.before(examDateFrom)) {
                    throw new IllegalArgumentException(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM);
                }
            }

            if (addProductDto.getApplicationScope() !=null) {
                CustomApplicationScope applicationScope = applicationScopeService.getApplicationScopeById(addProductDto.getApplicationScope());
                if (applicationScope == null) {
                    throw new NoSuchElementException("application scope not found.");
                }

                if (applicationScope.getApplicationScope().equals(Constant.APPLICATION_SCOPE_CENTER)) {

                    if (addProductDto.getState() != null) {
                        throw new IllegalArgumentException("State cannot be given if application scope " + applicationScope.getApplicationScope());
                    }
                    if (addProductDto.getDomicileRequired() != null && addProductDto.getDomicileRequired()) {
                        throw new IllegalArgumentException("Domicile required cannot be true if application scope " + applicationScope.getApplicationScope());
                    }
                    addProductDto.setDomicileRequired(false);

                } else if (applicationScope.getApplicationScope().equals(APPLICATION_SCOPE_STATE)) {
                    if (addProductDto.getDomicileRequired() == null || addProductDto.getState() == null) {
                        throw new IllegalArgumentException("For application scope: " + applicationScope.getApplicationScope() + " domicile and state cannot be null.");
                    }

                    if (addProductDto.getState() <= 0) {
                        throw new IllegalArgumentException("State cannot be <= 0.");
                    }

                    StateCode state = districtService.getStateByStateId(addProductDto.getState());
                    if (state == null) {
                        throw new NoSuchElementException("State not found.");
                    }
                }
            }
           *//* if(addProductDto.getIsReviewRequired()==null)
            {
                addProductDto.setIsReviewRequired(true);
            }*//*
           *//* if (addProductDto.getReservedCategory() == null || addProductDto.getReservedCategory().isEmpty()) {
                throw new IllegalArgumentException("Reserve category must not be null or empty.");
            }*//*


           *//* if (addProductDto.getIsMultiplePostSameFee() != null) {
                if(addProductDto.getPosts()==null || addProductDto.getPosts().isEmpty())
                {
                    throw new IllegalArgumentException("Post cannot be null or empty");
                }
            }*//*


            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (NoSuchElementException noSuchElementException) {
            exceptionHandlingService.handleException(noSuchElementException);
            throw new IllegalArgumentException(noSuchElementException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new ParseException(parseException.getMessage() + "\n", parseException.getErrorOffset());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage() + "\n");
        }
    }*/

    public boolean addProductDtoWithoutValidation(AddProductDto addProductDto) throws Exception {
        try {
            // Validate quantity
            if (addProductDto.getQuantity() != null) {
                if (addProductDto.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Quantity cannot be <= 0.");
                }
            } else {
                addProductDto.setQuantity(Constant.DEFAULT_QUANTITY);
            }

            // Validate platform fee
            if (addProductDto.getPlatformFee() != null) {
                if (addProductDto.getPlatformFee() <= 0) {
                    throw new IllegalArgumentException("Platform fee cannot be <= 0.");
                }
            } else {
                addProductDto.setPlatformFee(DEFAULT_PLATFORM_FEE);
            }

            // Validate priority level
            if (addProductDto.getPriorityLevel() != null) {
                if (addProductDto.getPriorityLevel() <= 0 || addProductDto.getPriorityLevel() > 5) {
                    throw new IllegalArgumentException("Priority level must lie between 1-5.");
                }
            } else {
                addProductDto.setPriorityLevel(DEFAULT_PRIORITY_LEVEL);
            }

            // Validate meta title
            if (addProductDto.getMetaTitle() == null || addProductDto.getMetaTitle().trim().isEmpty()) {
                throw new IllegalArgumentException(PRODUCTTITLENOTGIVEN);
            } else {
                addProductDto.setMetaTitle(addProductDto.getMetaTitle().trim());
            }

            // Trim display template if present
            if (addProductDto.getDisplayTemplate() != null) {
                addProductDto.setDisplayTemplate(addProductDto.getDisplayTemplate().trim());
            }

            // Date validations
            String formattedDate = dateFormat.format(new Date());
            Date currentDate = dateFormat.parse(formattedDate); // Current date without time

            // Parse and validate dates if they are not null
            if (addProductDto.getActiveStartDate() != null) {
                Date activeStartDate = stripTime(dateFormat.parse(dateFormat.format(addProductDto.getActiveStartDate())));

                if (addProductDto.getActiveEndDate() != null) {
                    Date activeEndDate = stripTime(dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate())));

                    if (activeStartDate.after(activeEndDate)) {
                        throw new IllegalArgumentException("Active start date cannot be after active end date.");
                    }

                    if (!activeEndDate.after(currentDate)) {
                        throw new IllegalArgumentException("Expiration date cannot be before or equal of current date.");
                    }
                }

                if (addProductDto.getGoLiveDate() != null && addProductDto.getActiveEndDate() != null) {
                    Date goLiveDate = stripTime(dateFormat.parse(dateFormat.format(addProductDto.getGoLiveDate())));
                    Date activeEndDate = stripTime(dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate())));

                    if (!goLiveDate.before(activeEndDate)) {
                        throw new IllegalArgumentException("Go live date cannot be after or equal of active end date.");
                    }
                }
            }

            // Validate exam dates if present
            if (addProductDto.getExamDateFrom() != null) {
                Date examDateFrom = stripTime(dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom())));

                if (addProductDto.getActiveEndDate() != null) {
                    Date activeEndDate = stripTime(dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate())));

                    if (!examDateFrom.after(activeEndDate)) {
                        throw new IllegalArgumentException("Tentative examination date from must be after active end date.");
                    }
                }
            }

            if (addProductDto.getExamDateTo() != null) {
                Date examDateTo = stripTime(dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo())));

                if (addProductDto.getActiveEndDate() != null) {
                    Date activeEndDate = stripTime(dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate())));

                    if (!examDateTo.after(activeEndDate)) {
                        throw new IllegalArgumentException("tentative examination date to must be after active end date");
                    }
                }
            }

            if (addProductDto.getExamDateFrom() != null && addProductDto.getExamDateTo() != null) {
                Date examDateTo = stripTime(dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo())));
                Date examDateFrom = stripTime(dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom())));

                if (examDateTo.before(examDateFrom)) {
                    throw new IllegalArgumentException(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM);
                }
            }

            // Validate application scope
            if (addProductDto.getApplicationScope() != null) {
                CustomApplicationScope applicationScope = applicationScopeService.getApplicationScopeById(addProductDto.getApplicationScope());
                if (applicationScope == null) {
                    throw new NoSuchElementException("application scope not found.");
                }

                if (applicationScope.getApplicationScope().equals(Constant.APPLICATION_SCOPE_CENTER)) {
                    if (addProductDto.getState() != null) {
                        throw new IllegalArgumentException("State cannot be given if application scope " + applicationScope.getApplicationScope());
                    }
                    if (addProductDto.getDomicileRequired() != null && addProductDto.getDomicileRequired()) {
                        throw new IllegalArgumentException("Domicile required cannot be true if application scope " + applicationScope.getApplicationScope());
                    }
                    addProductDto.setDomicileRequired(false);
                } else if (applicationScope.getApplicationScope().equals(APPLICATION_SCOPE_STATE)) {
                    if (addProductDto.getDomicileRequired() == null || addProductDto.getState() == null) {
                        throw new IllegalArgumentException("For application scope: " + applicationScope.getApplicationScope() + " domicile and state cannot be null.");
                    }

                    if (addProductDto.getState() <= 0) {
                        throw new IllegalArgumentException("State cannot be <= 0.");
                    }

                    StateCode state = districtService.getStateByStateId(addProductDto.getState());
                    if (state == null) {
                        throw new NoSuchElementException("State not found.");
                    }
                }
            }

            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (NoSuchElementException noSuchElementException) {
            exceptionHandlingService.handleException(noSuchElementException);
            throw new IllegalArgumentException(noSuchElementException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new ParseException(parseException.getMessage() + "\n", parseException.getErrorOffset());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage() + "\n");
        }
    }

    public void validateUpdateFields(CustomProduct customProduct) throws Exception {
        try
        {
//            if (customProduct.getDisplayTemplate() == null || customProduct.getDisplayTemplate().trim().isEmpty()) {
//                throw new IllegalArgumentException("Display Template cannot be null to move Product from Draft to NEW state ");
//            }

//            if (customProduct.getExamDateFrom() == null || customProduct.getExamDateTo() == null) {
//                throw new IllegalArgumentException("Exam Date-From and Exam Date-To cannot be null to move Product from Draft to NEW state ");
//            }

//            if (customProduct.getCustomApplicationScope() == null) {
//                throw new IllegalArgumentException("Application scope cannot be null to move Product from Draft to NEW state ");
//            }
//            if(customProduct.getPosts()==null || customProduct.getPosts().isEmpty())
//            {
//                throw new IllegalArgumentException("Posts cannot be empty or null to move Product from Draft to NEW state");
//            }
        }
        catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        }
        catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage() + "\n");
        }
    }

    public ResponseEntity<?> changeStateProductFromDraftToNew(CustomProduct customProduct, CustomProductWrapper wrapper) throws Exception {
        try{
            validateUpdateFields(customProduct);
            CustomProductState customProductState=null;
            customProductState= productStateService.getProductStateByName(PRODUCT_STATE_NEW);
            if (customProductState == null) {
                return ResponseService.generateErrorResponse("Custom product state not found.", HttpStatus.NOT_FOUND);
            }
            customProduct.setProductState(customProductState);
            List<Post>postList= customProduct.getPosts();
            wrapper.wrapDetails(customProduct,postList,null,productReserveCategoryFeePostRefService);
            return ResponseService.generateSuccessResponse("Product is saved as NEW Product",wrapper,HttpStatus.OK);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        }
        catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage() + "\n");
        }
    }

    public CustomJobGroup validateCustomJobGroup(Long customJobGroupId) throws Exception {
        try {
            CustomJobGroup jobGroup = jobGroupService.getJobGroupById(customJobGroupId);
            return jobGroup;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION WHILE VALIDATING ADD PRODUCT DTO: " + exception.getMessage() + "\n");
        }
    }

    public Role getRoleByToken(String authHeader) throws Exception {
        try {
            String jwtToken = authHeader.substring(7);

            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Role role = roleService.getRoleByRoleId(roleId);
            return role;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION WHILE VALIDATING AUTHORIZATION: " + exception.getMessage() + "\n");
        }
    }

    public Long getUserIdByToken(String authHeader) throws Exception {
        try {
            String jwtToken = authHeader.substring(7);
            Long userId = jwtTokenUtil.extractId(jwtToken);

            return userId;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION WHILE VALIDATING AUTHORIZATION: " + exception.getMessage() + "\n");
        }
    }

    public boolean validateReserveCategory(AddProductDto addProductDto) throws Exception {
        try {


          /*  if (addProductDto.getReservedCategory().isEmpty()) {
                throw new IllegalArgumentException("Reserve category cannot be empty.");
            }*/
            Set<Long> reserveCategoryId = new HashSet<>();
            Set<Integer>genderCategoryComboSet=new HashSet<>();

            Date currentDate = new Date(); // Current date for comparison
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);

            calendar.add(Calendar.YEAR, -105);
            Date minBornAfterDate = calendar.getTime();
            calendar.add(Calendar.YEAR, 100);
            Date maxBornBeforeDate = calendar.getTime();

            for (int reserveCategoryIndex = 0; reserveCategoryIndex < addProductDto.getReservedCategory().size(); reserveCategoryIndex++) {
                if(!addProductDto.getReservedCategory().get(reserveCategoryIndex).getIsOtherOrStateCategory()){
                if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory() == null || addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory() <= 0) {
                    throw new IllegalArgumentException("Reserve category id cannot be null or <= 0.");
                }
                }if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getGender() == null || addProductDto.getReservedCategory().get(reserveCategoryIndex).getGender() <= 0) {
                    throw new IllegalArgumentException("Gender id cannot be null or <= 0.");
                }
                CustomGender gender=genderService.getGenderByGenderId(addProductDto.getReservedCategory().get(reserveCategoryIndex).getGender());
                if(gender==null)
                    throw new NotFoundException("Invalid gender id");

                CustomReserveCategory category = reserveCategoryService.getReserveCategoryById(addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory());

                if(!addProductDto.getReservedCategory().get(reserveCategoryIndex).getIsOtherOrStateCategory()) {
//                    CustomReserveCategory category = reserveCategoryService.getReserveCategoryById(addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory());
                    if (category == null)
                        throw new NotFoundException("Invalid category id");
                } if(category!=null){
                int genderAndCategoryCombo=(addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory().intValue())*10+(addProductDto.getReservedCategory().get(reserveCategoryIndex).getGender().intValue());
                if (gender.getGenderName().equals(Constant.NO_GENDER) && category.getReserveCategoryName().equals(Constant.NO_CATEGORY) && addProductDto.getReservedCategory().size() > 1) {
                     throw new IllegalArgumentException("This product is set to be category and gender independent, so no additional category/gender fees can be applied.");
                 }

                if(addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory().intValue()!=6) {
                    if (!genderCategoryComboSet.add(genderAndCategoryCombo)) {
                        throw new IllegalArgumentException("Duplicate combination of gender and reserve category not allowed.");
                    }
                }
                    if(addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory()!=6&&(addProductDto.getReservedCategory().get(reserveCategoryIndex).getRunningField()!=null&&!addProductDto.getReservedCategory().get(reserveCategoryIndex).getRunningField().isEmpty()))
                    {
                        throw new IllegalArgumentException("Cannot add running field for any other category except OTHERS");
                    }
                    else if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory() == 6 &&
                            (addProductDto.getReservedCategory().get(reserveCategoryIndex).getRunningField() == null ||
                                    addProductDto.getReservedCategory().get(reserveCategoryIndex).getRunningField().trim().isEmpty())) {
                        throw new IllegalArgumentException("Running field is required when selecting 'Others' for reserved category");
                    }
                    if(addProductDto.getReservedCategory().get(reserveCategoryIndex).getGender()!=3&&(addProductDto.getReservedCategory().get(reserveCategoryIndex).getGenderRunningField()!=null&&!addProductDto.getReservedCategory().get(reserveCategoryIndex).getGenderRunningField().isEmpty()))
                    {
                        throw new IllegalArgumentException("Cannot add running field for any other gender except OTHERS");
                    }
                    else if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getGender() == 3 &&
                            (addProductDto.getReservedCategory().get(reserveCategoryIndex).getGenderRunningField() == null ||
                                    addProductDto.getReservedCategory().get(reserveCategoryIndex).getGenderRunningField().trim().isEmpty())) {
                        throw new IllegalArgumentException("Running field is required when selecting 'Others' for gender in reserved category");
                    }
             }
                /*if(gender.getGenderName().equals(Constant.NO_GENDER))
                {
                    Boolean result=checkForOpenGender(gender,addProductDto);
                    if(result)
                        throw new IllegalArgumentException("This product is set to be gender independent, so no additional gender fees can be applied.");
                }

                if(category.getReserveCategoryName().equals(Constant.NO_CATEGORY))
                {
                    Boolean result=checkForOpenCategory(category,addProductDto);
                    if(result)
                        throw new IllegalArgumentException("This product is set to be category independent, so no additional category fees can be applied.");
                }*/
                reserveCategoryId.add(addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory());

                CustomReserveCategory reserveCategory = reserveCategoryService.getReserveCategoryById(addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory());
                if(!addProductDto.getReservedCategory().get(reserveCategoryIndex).getIsOtherOrStateCategory()) {
                    if (reserveCategory == null) {
                        throw new IllegalArgumentException("Reserve category not found with id: " + addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory());
                    }
                }

                if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getFee() == null || addProductDto.getReservedCategory().get(reserveCategoryIndex).getFee() < 0) {
                    throw new IllegalArgumentException("Fee cannot be null or <= 0.");
                }

                if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getPost() == null) {
                    addProductDto.getReservedCategory().get(reserveCategoryIndex).setPost(0);
                }/* else if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getPost() <= 0) {
                    throw new IllegalArgumentException(POSTLESSTHANORZERO);
                }*/

              /*  if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornBefore() == null || addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornAfter() == null) {
                    throw new IllegalArgumentException("Born before date and born after date cannot be empty.");
                }

                dateFormat.parse(dateFormat.format(addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornAfter()));
                dateFormat.parse(dateFormat.format(addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornBefore()));

                if (!addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornBefore().before(new Date()) || !addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornAfter().before(new Date())) {
                    throw new IllegalArgumentException("Born before date and born after date must be of past.");
                } else if (!addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornAfter().before(addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornBefore())) {
                    throw new IllegalArgumentException("Born after date must be past of born before date.");
                }

                if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornAfter().before(minBornAfterDate)) {
                    throw new IllegalArgumentException("Born after date cannot be more than 105 years in the past.");
                }
                if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornBefore().after(maxBornBeforeDate)) {
                    throw new IllegalArgumentException("Born before date must be at least 5 years in the past.");
                }*/
            }

            /*if (reserveCategoryId.size() != addProductDto.getReservedCategory().size()) {
                throw new IllegalArgumentException("Duplicate reserve categories not allowed.");
            }*/

            return true;
        } catch (NotFoundException | IllegalArgumentException notFoundException) {
            exceptionHandlingService.handleException(notFoundException);
            throw new IllegalArgumentException(notFoundException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception while validating reserve category: " + exception.getMessage());
        }
    }
    public static Map<String, Date> calculateDateRange(Date asOfDate, int minAge, int maxAge) {
        LocalDate asOfLocalDate = asOfDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate bornBeforeLocalDate = asOfLocalDate.minusYears(minAge).plusDays(1);
        LocalDate bornAfterLocalDate = asOfLocalDate.minusYears(maxAge).minusDays(1);

        ZonedDateTime bornBeforeDateTime = bornBeforeLocalDate.atStartOfDay(ZoneId.of("Z"));
        ZonedDateTime bornAfterDateTime = bornAfterLocalDate.atStartOfDay(ZoneId.of("Z"));

        Date bornBeforeDate = Date.from(bornBeforeDateTime.toInstant());
        Date bornAfterDate = Date.from(bornAfterDateTime.toInstant());

        Map<String, Date> dateMap = new HashMap<>();
        dateMap.put("bornBeforeDate", bornBeforeDate);
        dateMap.put("bornAfterDate", bornAfterDate);

        return dateMap;
    }
    //****************************************
    //FOR FUTURE USE IF NEEDED
    /*public Boolean checkForOpenCategory(CustomReserveCategory openCategory,AddProductDto addProductDto)
    {
        Boolean flag=false;
        Boolean contains=false;
        for(AddReserveCategoryDto reserveCategory:addProductDto.getReservedCategory())
        {
            CustomReserveCategory reserveCategoryEntity=reserveCategoryService.getReserveCategoryById(reserveCategory.getReserveCategory());
            if(reserveCategoryEntity.getReserveCategoryName().equals(openCategory.getReserveCategoryName())&&contains.equals(false)) {
                contains = true;
                continue;
            }
            if(!reserveCategoryEntity.getReserveCategoryName().equals(openCategory.getReserveCategoryName())&&contains.equals(true))
                return true;
        }
        return flag;
    }
    public Boolean checkForOpenGender(CustomGender openGender,AddProductDto addProductDto)
    {
        Boolean flag=false;
        Boolean contains=false;
        for(AddReserveCategoryDto reserveCategory:addProductDto.getReservedCategory())
        {
            CustomGender genderEntity=genderService.getGenderByGenderId(reserveCategory.getGender());
            if(genderEntity.getGenderName().equals(openGender.getGenderName())&&contains.equals(false)) {
                contains = true;
                continue;
            }
            if(!genderEntity.getGenderName().equals(openGender.getGenderName())&&contains.equals(true))
                return true;
        }
        return flag;
    }*/
    //****************************************

    public boolean updateProductAccessAuthorisation(String authHeader, Long productId) throws Exception {
        try {
            String jwtToken = authHeader.substring(7);

            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            String role = roleService.getRoleByRoleId(roleId).getRole_name();

            if (productId <= 0) {
                throw new IllegalArgumentException("PRODUCT ID CANNOT BE <= 0");
            }
            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId);
            if (customProduct == null || ((Status) customProduct).getArchived() == 'Y') {
                throw new IllegalArgumentException(PRODUCTNOTFOUND);
            }
            // if (!customProduct.getProductState().getProductState().equals(PRODUCT_STATE_MODIFIED) && !customProduct.getProductState().getProductState().equals(PRODUCT_STATE_NEW)) {
            //     throw new IllegalArgumentException("PRODUCT CAN ONLY BE MODIFIED IF IT IS IN NEW AND MODIFIED STATE");
            // }
            Long userId = null;
            if (role.equals(Constant.SUPER_ADMIN) || role.equals(Constant.ADMIN)) {
                return true;

                // -> NEED TO ADD THE USER_ID OF ADMIN OR SUPER ADMIN.

            } else if (role.equals(Constant.SERVICE_PROVIDER)) {

                userId = jwtTokenUtil.extractId(jwtToken);
                if (customProduct.getCreatoRole().getRole_name().equals(role) && customProduct.getUserId().equals(userId)) {
                    return true;
                }

                List<Privileges> privileges = privilegeService.getServiceProviderPrivilege(userId);
                for (Privileges privilege : privileges) {
                    if (privilege.getPrivilege_name().equals(Constant.PRIVILEGE_UPDATE_PRODUCT)) {
                        return true;
                    }
                }
            }

            return false;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("ERRORS WHILE VALIDATING AUTHORIZATION: " + exception.getMessage() + "\n");
        }
    }

    public boolean updateProductValidation(AddProductDto addProductDto, CustomProduct customProduct) throws Exception {
        try {
            if (addProductDto.getQuantity() != null) {
                if (addProductDto.getQuantity() <= 0) {
                    throw new IllegalArgumentException("QUANTITY CANNOT BE EMPTY <= 0");
                }
                customProduct.getDefaultSku().setQuantityAvailable(addProductDto.getQuantity());
            }

            if (addProductDto.getPriorityLevel() != null) {
                if (addProductDto.getPriorityLevel() <= 0 || addProductDto.getPriorityLevel() > 5) {
                    throw new IllegalArgumentException("PRIORITY LEVEL MUST BE BETWEEN 1-5");
                }
                customProduct.setPriorityLevel(addProductDto.getPriorityLevel());
            }

            if (addProductDto.getMetaTitle() != null && !addProductDto.getMetaTitle().trim().isEmpty()) {
                addProductDto.setMetaTitle(addProductDto.getMetaTitle().trim());
                customProduct.setMetaTitle(addProductDto.getMetaTitle());
                customProduct.getDefaultSku().setName(addProductDto.getMetaTitle());
            }

            if (addProductDto.getDisplayTemplate() != null && !addProductDto.getDisplayTemplate().trim().isEmpty()) {
                customProduct.setDisplayTemplate(addProductDto.getDisplayTemplate().trim());
            }

            if ((addProductDto.getPriorityLevel() != null) && (addProductDto.getPriorityLevel() <= 0 || addProductDto.getPriorityLevel() > 5)) {
                throw new IllegalArgumentException("PRIORITY LEVEL MUST LIE BETWEEN 1-5");
            }
            if (addProductDto.getMetaDescription() != null && !addProductDto.getMetaDescription().trim().isEmpty()) {
                addProductDto.setMetaDescription(addProductDto.getMetaDescription().trim());
                customProduct.setMetaDescription(addProductDto.getMetaDescription());
                customProduct.getDefaultSku().setDescription(addProductDto.getMetaDescription());
            }

            if (addProductDto.getPlatformFee() != null) {
                if (addProductDto.getPlatformFee() <= 0) {
                    throw new IllegalArgumentException("PLATFORM FEE CANNOT BE LESS THAN OR EQUAL TO ZERO");
                }
                customProduct.setPlatformFee(addProductDto.getPlatformFee());
            }

            if (addProductDto.getApplicationScope() != null) {
                CustomApplicationScope applicationScope = applicationScopeService.getApplicationScopeById(addProductDto.getApplicationScope());
                if (applicationScope == null) {
                    throw new IllegalArgumentException("NO APPLICATION SCOPE EXISTS WITH THIS ID");
                }
                if (customProduct.getCustomApplicationScope() != null) {
                    if (applicationScope.getApplicationScope().equals(Constant.APPLICATION_SCOPE_STATE) && customProduct.getCustomApplicationScope().getApplicationScope().equals(Constant.APPLICATION_SCOPE_STATE)) {
                        if (addProductDto.getState() != null && districtService.getStateByStateId(addProductDto.getState()) != null) {
                            customProduct.setState(districtService.getStateByStateId(addProductDto.getState()));
                            customProduct.setCustomApplicationScope(applicationScope);
                        } else {
                            throw new IllegalArgumentException("STATE NOT FOUND");
                        }

                        if (addProductDto.getDomicileRequired() != null) {
                            customProduct.setDomicileRequired(addProductDto.getDomicileRequired());
                            customProduct.setCustomApplicationScope(applicationScope);
                        }
                    } else if (applicationScope.getApplicationScope().equals(Constant.APPLICATION_SCOPE_STATE) && customProduct.getCustomApplicationScope().getApplicationScope().equals(Constant.APPLICATION_SCOPE_CENTER)) {
                        if (addProductDto.getState() == null || addProductDto.getDomicileRequired() == null) {
                            throw new IllegalArgumentException("DOMICILE AND STATE ARE REQUIRED FIELDS FOR STATE APPLICATION SCOPE");
                        }

                        if (districtService.getStateByStateId(addProductDto.getState()) != null) {
                            customProduct.setState(districtService.getStateByStateId(addProductDto.getState()));
                        } else {
                            throw new IllegalArgumentException("STATE IS NOT FOUND");
                        }
                        customProduct.setDomicileRequired(addProductDto.getDomicileRequired());
                        customProduct.setCustomApplicationScope(applicationScope);
                    } else if (applicationScope.getApplicationScope().equals(APPLICATION_SCOPE_CENTER)) {
                        if (addProductDto.getState() != null) {
                            throw new IllegalArgumentException("STATE NOT REQUIRED IN CASE OF CENTER LEVEL APPLICATION SCOPE");
                        }
                        if (addProductDto.getDomicileRequired() != null && addProductDto.getDomicileRequired()) {
                            throw new IllegalArgumentException("DOMICILE IS NOT REQUIRED IN CASE OF CENTER APPLICATION SCOPE");
                        }
                        addProductDto.setDomicileRequired(false);
                        addProductDto.setState(null);
                        customProduct.setState(null);
                        customProduct.setDomicileRequired(addProductDto.getDomicileRequired());
                        customProduct.setCustomApplicationScope(applicationScope);
                    }
                }
                else if(customProduct.getCustomApplicationScope()==null)
                {
                    if (applicationScope.getApplicationScope().equals(Constant.APPLICATION_SCOPE_CENTER)) {
                        if (addProductDto.getState() != null) {
                            throw new IllegalArgumentException("State cannot be given if application scope " + applicationScope.getApplicationScope());
                        }
                        if (addProductDto.getDomicileRequired() != null && addProductDto.getDomicileRequired()) {
                            throw new IllegalArgumentException("Domicile required cannot be true if application scope " + applicationScope.getApplicationScope());
                        }
                        addProductDto.setDomicileRequired(false);
                        customProduct.setDomicileRequired(false);
                        customProduct.setState(null);
                        customProduct.setCustomApplicationScope(applicationScope);

                    } else if (applicationScope.getApplicationScope().equals(APPLICATION_SCOPE_STATE)) {
                        if (addProductDto.getDomicileRequired() == null || addProductDto.getState() == null) {
                            throw new IllegalArgumentException("For application scope: " + applicationScope.getApplicationScope() + " domicile and state cannot be null.");
                        }

                        if (addProductDto.getState() <= 0) {
                            throw new IllegalArgumentException("State cannot be <= 0.");
                        }

                        StateCode state = districtService.getStateByStateId(addProductDto.getState());
                        if (state == null) {
                            throw new NoSuchElementException("State not found.");
                        }
                        customProduct.setDomicileRequired(addProductDto.getDomicileRequired());
                        customProduct.setState(state);
                        customProduct.setCustomApplicationScope(applicationScope);

                    }
                }
            }

//            else if(customProduct.getCustomApplicationScope().getApplicationScope()!=null) {
//                if (customProduct.getCustomApplicationScope().getApplicationScope().equals(APPLICATION_SCOPE_STATE)) {
//                    if (addProductDto.getState() != null) {
//                        StateCode stateCode = districtService.getStateByStateId(addProductDto.getState());
//                        customProduct.setState(stateCode);
//                    }
//                    if (addProductDto.getDomicileRequired() != null) {
//                        customProduct.setDomicileRequired(addProductDto.getDomicileRequired());
//                    }
//                }
//            }

            if (addProductDto.getState() != null) {
                CustomSector customSector = sectorService.getSectorBySectorId(addProductDto.getSector());
                customProduct.setSector(customSector);
            }

            if (addProductDto.getFormComplexity() != null) {
                if (addProductDto.getFormComplexity() < 0 || addProductDto.getFormComplexity() > 5) {
                    throw new IllegalArgumentException("Form complexity must lie between 1 and 5");
                }
                customProduct.setFormComplexity(addProductDto.getFormComplexity());
            }

            if (addProductDto.getSelectionCriteria() != null) {
                customProduct.setSelectionCriteria(addProductDto.getSelectionCriteria());
            }

            if (addProductDto.getSector() != null) {
                CustomSector customSector = sectorService.getSectorBySectorId(addProductDto.getSector());
                customProduct.setSector(customSector);
            }

            if (addProductDto.getDownloadNotificationLink() != null) {
                addProductDto.setDownloadNotificationLink(addProductDto.getDownloadNotificationLink().trim());
                customProduct.setDownloadNotificationLink(addProductDto.getDownloadNotificationLink());
            }

            if (addProductDto.getDownloadSyllabusLink() != null) {
                addProductDto.setDownloadSyllabusLink(addProductDto.getDownloadSyllabusLink().trim());
                customProduct.setDownloadSyllabusLink(addProductDto.getDownloadSyllabusLink());
            }
            if(addProductDto.getIsExamDateFromNa()!=null)
            {
                if(addProductDto.getIsExamDateFromNa().equals(true))
                {
                    addProductDto.setExamDateFrom(null);
                }
                customProduct.setIsExamDateFromNa(addProductDto.getIsExamDateFromNa());
            }
            if(addProductDto.getIsAnswerKeyAvailableDateNa()!=null)
            {
                if(addProductDto.getIsAnswerKeyAvailableDateNa().equals(true))
                {
                    addProductDto.setAnswerKeyAvailableDate(null);
                }
                customProduct.setIsAnswerKeyAvailableDateNa(addProductDto.getIsAnswerKeyAvailableDateNa());
            }
            if(addProductDto.getIsResultDeclarationDateNa()!=null)
            {
                if(addProductDto.getIsResultDeclarationDateNa().equals(true))
                {
                    addProductDto.setResultDeclarationDate(null);
                }
                customProduct.setIsResultDeclarationDateNa(addProductDto.getIsResultDeclarationDateNa());
            }
            if(addProductDto.getIsCounsellingDateNa()!=null)
            {
                if(addProductDto.getIsCounsellingDateNa().equals(true))
                {
                    addProductDto.setCounsellingDate(null);
                }
                customProduct.setIsCounsellingDateNa(addProductDto.getIsCounsellingDateNa());
            }
            if(addProductDto.getIsTentativeVerificationToNa()!=null)
            {
                if(addProductDto.getIsTentativeVerificationToNa().equals(true))
                {
                    addProductDto.setTentativeVerificationTo(null);
                }
                customProduct.setIsTentativeVerificationToNa(addProductDto.getIsTentativeVerificationToNa());
            }
            if(addProductDto.getIsTentativeVerificationFromNa()!=null)
            {
                if(addProductDto.getIsTentativeVerificationFromNa().equals(true))
                {
                    addProductDto.setTentativeVerificationFrom(null);
                }
                customProduct.setIsTentativeVerificationFromNa(addProductDto.getIsTentativeVerificationFromNa());
            }
            if(addProductDto.getIsExamDateToNa()!=null)
            {
                if(addProductDto.getIsExamDateToNa().equals(true))
                {
                    addProductDto.setExamDateTo(null);
                }
                customProduct.setIsExamDateToNa(addProductDto.getIsExamDateToNa());
            }
            if(addProductDto.getIsExamCenterAvailableDateNa()!=null)
            {
                if(addProductDto.getIsExamCenterAvailableDateNa().equals(true))
                {
                    addProductDto.setExamCenterAvailableDate(null);
                }
                customProduct.setIsExamCenterAvailableDateNa(addProductDto.getIsExamCenterAvailableDateNa());
            }
            if(addProductDto.getIsLateDateToPayFeeNa()!=null)
            {
                if(addProductDto.getIsLateDateToPayFeeNa().equals(true))
                {
                    addProductDto.setLastDateToPayFee(null);
                }
                customProduct.setIsLateDateToPayFeeNa(addProductDto.getIsLateDateToPayFeeNa());
            }
            if(addProductDto.getIsAdmitCardDateToNa()!=null)
            {
                if(addProductDto.getIsAdmitCardDateToNa().equals(true))
                {
                    addProductDto.setAdmitCardDateTo(null);
                }
                customProduct.setIsAdmitCardDateToNa(addProductDto.getIsAdmitCardDateToNa());
            }
            if(addProductDto.getIsAdmitCardDateFromNa()!=null)
            {
                if(addProductDto.getIsAdmitCardDateFromNa().equals(true))
                {
                    addProductDto.setAdmitCardDateFrom(null);
                }
                customProduct.setIsAdmitCardDateFromNa(addProductDto.getIsAdmitCardDateFromNa());
            }
            if(addProductDto.getIsModificationDateFromNa()!=null)
            {
                if(addProductDto.getIsModificationDateFromNa().equals(true))
                {
                    addProductDto.setModificationDateFrom(null);
                }
                customProduct.setIsModificationDateFromNa(addProductDto.getIsModificationDateFromNa());
            }
            if(addProductDto.getIsModificationDateToNa()!=null)
            {
                if(addProductDto.getIsModificationDateToNa().equals(true))
                {
                    addProductDto.setModificationDateTo(null);
                }
                customProduct.setIsModificationDateToNa(addProductDto.getIsModificationDateToNa());
            }

            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new Exception(illegalArgumentException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("ERRORS WHILE VALIDATION: " + exception.getMessage() + "\n");
        }
    }

    public Boolean  validateAndSetActiveStartDate(AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            if (addProductDto.getActiveStartDate() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getActiveStartDate()));

                if (addProductDto.getActiveEndDate() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));
                    Date activeDateEnd= stripTime(addProductDto.getActiveEndDate());
                    Date activeDateStart=stripTime(addProductDto.getActiveStartDate());
                    if (activeDateStart.after(activeDateEnd)) {
                        throw new IllegalArgumentException("Active start date cannot be after active end date.");
                    }
                } else {
                    Date activeDateEnd= stripTime(customProduct.getActiveEndDate());
                    Date activeDateStart=stripTime(addProductDto.getActiveStartDate());
                    if (activeDateStart.after(activeDateEnd)) {
                        throw new IllegalArgumentException("Active start date cannot be after active end date.");
                    }
                }
                customProduct.setActiveStartDate(addProductDto.getActiveStartDate());
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating active start date: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    public Boolean validateAndSetGoLiveDate(AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            if (addProductDto.getGoLiveDate() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getGoLiveDate()));

                if(createdDate!=null)
                {
                    /*if(!isSameOrFutureDate(addProductDto.getGoLiveDate()))
                    {
                        throw new IllegalArgumentException("Go live date cannot be past of current date.");
                    }*/
                }

                if (addProductDto.getActiveEndDate() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));
                    if (!addProductDto.getGoLiveDate().before(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException("Go live date must be before active end date.");
                    }
                } else {
                    if (!addProductDto.getGoLiveDate().before(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException("Go live date must be before active end date.");
                    }
                }
                customProduct.setGoLiveDate(addProductDto.getGoLiveDate());
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating go live date: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    public Boolean validateAndSetActiveEndDate(AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            if (addProductDto.getActiveEndDate() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));

                if(addProductDto.getGoLiveDate() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getGoLiveDate()));
                    if(!addProductDto.getGoLiveDate().before(addProductDto.getActiveEndDate())){
                        throw new IllegalArgumentException("Active end date has be future of go Live Date");
                    }
                }else {
                    if(!customProduct.getGoLiveDate().before(addProductDto.getActiveEndDate())){
                        throw new IllegalArgumentException("Active end date has be future of go Live Date");
                    }
                }
                if(addProductDto.getActiveStartDate() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getActiveStartDate()));
                    Date activeDateEnd= stripTime(addProductDto.getActiveEndDate());
                    Date activeDateStart=stripTime(addProductDto.getActiveStartDate());
                    if (activeDateStart.after(activeDateEnd)) {
                        throw new IllegalArgumentException("Active start date cannot be after active end date.");
                    }
                } else {
                    Date activeDateEnd= stripTime(addProductDto.getActiveEndDate());
                    Date activeDateStart=stripTime(customProduct.getActiveStartDate());
                    if (activeDateStart.after(activeDateEnd)) {
                        throw new IllegalArgumentException("Active start date cannot be after active end date.");
                    }
                }

                if (addProductDto.getLastDateToPayFee() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getLastDateToPayFee()));
                    if (!addProductDto.getActiveEndDate().before(addProductDto.getLastDateToPayFee()) &&
                            !addProductDto.getActiveEndDate().equals(addProductDto.getLastDateToPayFee())) {
                        throw new IllegalArgumentException("active end date must be before or equal to the last date to pay fee.");
                    }
                } /*else if (addProductDto.getModificationDateFrom() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getModificationDateFrom()));
                    if (!addProductDto.getActiveEndDate().before(addProductDto.getModificationDateFrom())) {
                        throw new IllegalArgumentException("active end date have to be before of modification date from.");
                    }
                }*/ else if (addProductDto.getAdmitCardDateFrom() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getAdmitCardDateFrom()));
                    if (!addProductDto.getActiveEndDate().before(addProductDto.getAdmitCardDateFrom())) {
                        throw new IllegalArgumentException("active end date have to be before of admit card from.");
                    }
                } else if (addProductDto.getExamDateFrom() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                    if (!addProductDto.getActiveEndDate().before(addProductDto.getExamDateFrom())) {
                        throw new IllegalArgumentException("active end date have to be before of exam date from.");
                    }
                } else if (addProductDto.getLastDateToPayFee() != null) {
                    if (!addProductDto.getActiveEndDate().before(addProductDto.getLastDateToPayFee()) && !addProductDto.getActiveEndDate().equals(addProductDto.getLastDateToPayFee())) {
                        throw new IllegalArgumentException("active end date must be before or equal to the last date to pay fee.");
                    }
                } /*else if (customProduct.getModificationDateFrom() != null) {
                    if (!addProductDto.getActiveEndDate().before(customProduct.getModificationDateFrom())) {
                        throw new IllegalArgumentException("active end date have to be before of modification date from.");
                    }
                }*/ else if (customProduct.getAdmitCardDateFrom() != null) {
                    if (!addProductDto.getActiveEndDate().before(customProduct.getAdmitCardDateFrom())) {
                        throw new IllegalArgumentException("active end date have to be before of admit card from.");
                    }
                } else if (customProduct.getExamDateFrom() != null) {
                    if (!addProductDto.getActiveEndDate().before(customProduct.getExamDateFrom())) {
                        throw new IllegalArgumentException("active end date have to be before of exam date from.");
                    }
                }
                customProduct.setActiveEndDate(addProductDto.getActiveEndDate());
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating active start date: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    public Boolean validateAndSetLastDateToPayFeeDate(AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            // Check if lastDateToPayFee is null or empty (for when an empty value is passed)
            if (addProductDto.getLastDateToPayFee() == null) {
                // If last date to pay fee is null or empty, set it to null in the custom product
                return true;
            }
            validateLastDateToPayFromForNonNullDates(customProduct);
            // Proceed with validation only if the date is not null
            dateFormat.parse(dateFormat.format(addProductDto.getLastDateToPayFee()));

            // Your existing validation checks
            if (addProductDto.getActiveEndDate() != null) {
                if (addProductDto.getLastDateToPayFee().before(addProductDto.getActiveEndDate())) {
                    throw new IllegalArgumentException("Last day to pay fee cannot be before of active end date.");
                }
            } else if (customProduct.getActiveEndDate() != null) {
                if (addProductDto.getLastDateToPayFee().before(customProduct.getActiveEndDate())) {
                    throw new IllegalArgumentException("Last day to pay fee cannot be before of active end date.");
                }
            }

            // Additional validation checks remain the same
            /*if (addProductDto.getModificationDateFrom() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getLastDateToPayFee()));
                if (!addProductDto.getLastDateToPayFee().before(addProductDto.getModificationDateFrom())) {
                    throw new IllegalArgumentException("last date to pay fee have to be before of modified date from.");
                }
            } */else if (customProduct.getModificationDateFrom() != null) {
                /*if (!addProductDto.getLastDateToPayFee().before(customProduct.getModificationDateFrom())) {
                    throw new IllegalArgumentException("last date to pay fee have to be before of modified date from.");
                }*/
            }

            if (addProductDto.getAdmitCardDateFrom() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getLastDateToPayFee()));
                if (!addProductDto.getLastDateToPayFee().before(addProductDto.getAdmitCardDateFrom())) {
                    throw new IllegalArgumentException("last date to pay fee have to be before of admit card from.");
                }
            } else if (customProduct.getAdmitCardDateFrom() != null) {
                if (!addProductDto.getLastDateToPayFee().before(customProduct.getAdmitCardDateFrom())) {
                    throw new IllegalArgumentException("last date to pay fee have to be before of admit card from.");
                }
            }

            if (addProductDto.getExamDateFrom() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getLastDateToPayFee()));
                if (!addProductDto.getLastDateToPayFee().before(addProductDto.getExamDateFrom())) {
                    throw new IllegalArgumentException("last date to pay fee have to be before of exam date from.");
                }
            } else if (customProduct.getExamDateFrom() != null) {
                if (!addProductDto.getLastDateToPayFee().before(customProduct.getExamDateFrom())) {
                    throw new IllegalArgumentException("last date to pay fee have to be before of exam date from.");
                }
            }

            // Set the validated date
            customProduct.setLateDateToPayFee(addProductDto.getLastDateToPayFee());
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating active start date: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    public Boolean validateAndSetModifiedDates(AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            // Case 1: Both dates are null - set both as null in customProduct and return
            if (addProductDto.getModificationDateFrom() == null && addProductDto.getModificationDateTo() == null) {
                customProduct.setModificationDateFrom(null);
                customProduct.setModificationDateTo(null);
                return true;
            }

            // Case 2: Only ModificationDateFrom is provided
            if (addProductDto.getModificationDateFrom() != null && addProductDto.getModificationDateTo() == null) {
                validateModificationDateFromForNonNullDates(customProduct);
                dateFormat.parse(dateFormat.format(addProductDto.getModificationDateFrom()));

                // Validate ModificationDateFrom against other dates
                validateModificationDateFrom(addProductDto, customProduct);

                // Set values in customProduct
                customProduct.setModificationDateFrom(addProductDto.getModificationDateFrom());
                customProduct.setModificationDateTo(null);
                return true;
            }

            // Case 3: Only ModificationDateTo is provided
            if (addProductDto.getModificationDateFrom() == null && addProductDto.getModificationDateTo() != null) {
                validateModificationDateToForNonNullDates(customProduct);
                dateFormat.parse(dateFormat.format(addProductDto.getModificationDateTo()));

                // Validate ModificationDateTo against other dates
                validateModificationDateTo(addProductDto, customProduct);

                // Set values in customProduct
                customProduct.setModificationDateFrom(null);
                customProduct.setModificationDateTo(addProductDto.getModificationDateTo());
                return true;
            }

            // Case 4: Both dates are provided - full validation
            validateModificationDateFromForNonNullDates(customProduct);
            validateModificationDateToForNonNullDates(customProduct);
            dateFormat.parse(dateFormat.format(addProductDto.getModificationDateFrom()));
            dateFormat.parse(dateFormat.format(addProductDto.getModificationDateTo()));

            // Check if ModificationDateFrom is after ModificationDateTo
            if (addProductDto.getModificationDateFrom().after(addProductDto.getModificationDateTo())) {
                throw new IllegalArgumentException("Modified date from must be before or equal of modified date to.");
            }

            // Perform all validations
            validateModificationDateFrom(addProductDto, customProduct);
            validateModificationDateTo(addProductDto, customProduct);

            // Set values in customProduct
            customProduct.setModificationDateFrom(addProductDto.getModificationDateFrom());
            customProduct.setModificationDateTo(addProductDto.getModificationDateTo());
            return true;

        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating modification dates: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    // Helper method to validate ModificationDateFrom
    private void validateModificationDateFrom(AddProductDto addProductDto, CustomProduct customProduct) {
        // Check against LastDateToPayFee
        if (addProductDto.getLastDateToPayFee() != null) {
            /*if (!addProductDto.getModificationDateFrom().after(addProductDto.getLastDateToPayFee())) {
                throw new IllegalArgumentException("Modified date from must be after last date to pay fee.");
            }*/
        } else if (customProduct.getLateDateToPayFee() != null) {
            /*if (!addProductDto.getModificationDateFrom().after(customProduct.getLateDateToPayFee())) {
                throw new IllegalArgumentException("Modified date from must be after last date to pay fee.");
            }*/
        }

        // Check against ActiveEndDate
        if (addProductDto.getActiveEndDate() != null) {
           /* if (!addProductDto.getModificationDateFrom().after(addProductDto.getActiveEndDate())) {
                throw new IllegalArgumentException("Modified date from must be after active end date.");
            }*/
        } else if (customProduct.getActiveEndDate() != null) {
           /* if (!addProductDto.getModificationDateFrom().after(customProduct.getActiveEndDate())) {
                throw new IllegalArgumentException("Modified date from must be after active end date.");
            }*/
        }
    }

    // Helper method to validate ModificationDateTo
    private void validateModificationDateTo(AddProductDto addProductDto, CustomProduct customProduct) {
        // Check against AdmitCardDateFrom
        if (addProductDto.getAdmitCardDateFrom() != null) {
            if (!addProductDto.getModificationDateTo().before(addProductDto.getAdmitCardDateFrom())) {
                throw new IllegalArgumentException("Modified date to must be before or equal of admit card date from.");
            }
        } else if (customProduct.getAdmitCardDateFrom() != null) {
            if (!addProductDto.getModificationDateTo().before(customProduct.getAdmitCardDateFrom())) {
                throw new IllegalArgumentException("Modified date to must be before or equal of admit card date from.");
            }
        }

        // Check against ExamDateFrom
        if (addProductDto.getExamDateFrom() != null) {
            if (!addProductDto.getModificationDateTo().before(addProductDto.getExamDateFrom())) {
                throw new IllegalArgumentException("Modified date to must be before or equal of exam date from.");
            }
        } else if (customProduct.getExamDateFrom() != null) {
            if (!addProductDto.getModificationDateTo().before(customProduct.getExamDateFrom())) {
                throw new IllegalArgumentException("Modified date to must be before or equal of exam date from.");
            }
        }
    }

    public Boolean validateAndSetExamDates(AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            // Case 1: If both dates are provided, validate them
            if (addProductDto.getExamDateFrom() != null && addProductDto.getExamDateTo() != null) {
                validateExamDateFromForNonNullDates(customProduct);
                validateExamDateToForNonNullDates(customProduct);
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));

                // Check if from date is before to date
                if (addProductDto.getExamDateFrom().after(addProductDto.getExamDateTo())) {
                    throw new IllegalArgumentException("Exam date from must be before or equal of exam date to.");
                }

                // Perform all other validation checks
                validateExamDatesAgainstOtherDates(addProductDto, customProduct);

                // Set both dates
                customProduct.setExamDateFrom(addProductDto.getExamDateFrom());
                customProduct.setExamDateTo(addProductDto.getExamDateTo());
            }
            // Case 2: If only from date is provided
            else if (addProductDto.getExamDateFrom() != null) {
                validateExamDateFromForNonNullDates(customProduct);
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));

                // Perform validation checks
                validateExamDatesAgainstOtherDates(addProductDto, customProduct);

                // Set both dates
                customProduct.setExamDateFrom(addProductDto.getExamDateFrom());
                customProduct.setExamDateTo(null);
            }
            // Case 3: If only to date is provided
            else if (addProductDto.getExamDateTo() != null) {
                validateExamDateToForNonNullDates(customProduct);
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));

                if (customProduct.getExamDateFrom() != null) {
                    addProductDto.setExamDateFrom(customProduct.getExamDateFrom());
                }

                // Perform validation checks
                validateExamDatesAgainstOtherDates(addProductDto, customProduct);

                // Set both dates
                customProduct.setExamDateFrom(null);
                customProduct.setExamDateTo(addProductDto.getExamDateTo());
            }
            // Case 4: If both dates are null
            else {
                // Set both dates to null
                customProduct.setExamDateFrom(null);
                customProduct.setExamDateTo(null);
            }

            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating exam dates: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    // Helper method to centralize validation logic
    private void validateExamDatesAgainstOtherDates(AddProductDto addProductDto, CustomProduct customProduct) {
        // Validation against admit card dates
        if (addProductDto.getAdmitCardDateTo() != null&&addProductDto.getExamDateFrom()!=null) {
            if (!addProductDto.getExamDateFrom().after(addProductDto.getAdmitCardDateTo())) {
                throw new IllegalArgumentException("Exam date from must be after of admit card date to.");
            }
        } else if (customProduct.getAdmitCardDateTo() != null&&addProductDto.getExamDateFrom()!=null) {
            if (!addProductDto.getExamDateFrom().after(customProduct.getAdmitCardDateTo())) {
                throw new IllegalArgumentException("Exam date from must be after of admit card to.");
            }
        }

        // Validation against modification dates
        if (addProductDto.getModificationDateTo() != null&&addProductDto.getExamDateFrom()!=null) {
            if (!addProductDto.getExamDateFrom().after(addProductDto.getModificationDateTo())) {
                throw new IllegalArgumentException("Exam date from must be after of modified date to.");
            }
        } else if (customProduct.getModificationDateTo() != null&&addProductDto.getExamDateFrom()!=null) {
            if (!addProductDto.getExamDateFrom().after(customProduct.getModificationDateTo())) {
                throw new IllegalArgumentException("Exam date from must be after of modified date to.");
            }
        }

        // Validation against fee payment dates
        if (addProductDto.getLastDateToPayFee() != null&&addProductDto.getExamDateFrom()!=null) {
            if (!addProductDto.getExamDateFrom().after(addProductDto.getLastDateToPayFee())) {
                throw new IllegalArgumentException("Exam date from must be after of last date to pay fee.");
            }
        } else if (customProduct.getLateDateToPayFee() != null&&addProductDto.getExamDateFrom()!=null) {
            if (!addProductDto.getExamDateFrom().after(customProduct.getLateDateToPayFee())) {
                throw new IllegalArgumentException("Exam date from must be after of last date to pay fee.");
            }
        }

        // Validation against active end dates
        if (addProductDto.getActiveEndDate() != null&&addProductDto.getExamDateFrom()!=null) {
            if (!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate())) {
                throw new IllegalArgumentException("Exam date from must be after of active end date.");
            }
        } else if (customProduct.getActiveEndDate() != null&&addProductDto.getExamDateFrom()!=null) {
            if (!addProductDto.getExamDateFrom().after(customProduct.getActiveEndDate())) {
                throw new IllegalArgumentException("Exam date from must be after of active end date.");
            }
        }
        if(addProductDto.getExamDateFrom()!=null&&addProductDto.getExamDateTo()!=null&&addProductDto.getExamDateFrom().equals(addProductDto.getExamDateTo()))
            return;
    }

    public Boolean validateAndSetAdmitCardDates(AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            // Case 1: If both dates are provided, validate them
            if (addProductDto.getAdmitCardDateFrom() != null && addProductDto.getAdmitCardDateTo() != null) {
                validateAdmitCardDateToForNonNullDates(customProduct);
                validateAdmitDateFromForNonNullDates(customProduct);
                dateFormat.parse(dateFormat.format(addProductDto.getAdmitCardDateFrom()));
                dateFormat.parse(dateFormat.format(addProductDto.getAdmitCardDateTo()));

                // Check if from date is before to date
                if (addProductDto.getAdmitCardDateFrom().after(addProductDto.getAdmitCardDateTo())) {
                    throw new IllegalArgumentException("Admit card date from must be before or equal of admit card date to.");
                }

                // Perform all other validation checks
                validateAdmitCardDatesAgainstOtherDates(addProductDto, customProduct);

                // Set both dates
                customProduct.setAdmitCardDateFrom(addProductDto.getAdmitCardDateFrom());
                customProduct.setAdmitCardDateTo(addProductDto.getAdmitCardDateTo());
            }
            // Case 2: If only from date is provided
            else if (addProductDto.getAdmitCardDateFrom() != null) {
                validateAdmitDateFromForNonNullDates(customProduct);
                dateFormat.parse(dateFormat.format(addProductDto.getAdmitCardDateFrom()));
//                addProductDto.setAdmitCardDateTo(addProductDto.getAdmitCardDateFrom());

                // Perform validation checks
                validateAdmitCardDatesAgainstOtherDates(addProductDto, customProduct);

                // Set both dates
                customProduct.setAdmitCardDateFrom(addProductDto.getAdmitCardDateFrom());
                customProduct.setAdmitCardDateTo(null);
            }
            // Case 3: If only to date is provided
            else if (addProductDto.getAdmitCardDateTo() != null) {
                validateAdmitCardDateToForNonNullDates(customProduct);
                dateFormat.parse(dateFormat.format(addProductDto.getAdmitCardDateTo()));

                if (customProduct.getAdmitCardDateFrom() != null) {
                    addProductDto.setAdmitCardDateFrom(customProduct.getAdmitCardDateFrom());
                }

                // Perform validation checks
                validateAdmitCardDatesAgainstOtherDates(addProductDto, customProduct);

                // Set both dates
                customProduct.setAdmitCardDateFrom(null);
                customProduct.setAdmitCardDateTo(addProductDto.getAdmitCardDateTo());
            }
            // Case 4: If both dates are null
            else {
                // Set both dates to null
                customProduct.setAdmitCardDateFrom(null);
                customProduct.setAdmitCardDateTo(null);
            }

            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating admit card dates: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    // Helper method to centralize validation logic for admit card dates
    private void validateAdmitCardDatesAgainstOtherDates(AddProductDto addProductDto, CustomProduct customProduct) {

        // Validation against modification dates
        if (addProductDto.getModificationDateTo() != null) {
            if (!addProductDto.getAdmitCardDateFrom().after(addProductDto.getModificationDateTo())) {
                throw new IllegalArgumentException("Admit card date from must be after modification date to.");
            }
        } else if (customProduct.getModificationDateTo() != null) {
            if (!addProductDto.getAdmitCardDateFrom().after(customProduct.getModificationDateTo())) {
                throw new IllegalArgumentException("Admit card date from must be after modification date.");
            }
        }

        // Validation against fee payment dates - Fix bug in original code that was checking ModificationDateTo
        if (addProductDto.getLastDateToPayFee() != null) {
            if (!addProductDto.getAdmitCardDateFrom().after(addProductDto.getLastDateToPayFee())) {
                throw new IllegalArgumentException("Admit card date from must be after last date to pay fee.");
            }
        } else if (customProduct.getLateDateToPayFee() != null) {
            if (!addProductDto.getAdmitCardDateFrom().after(customProduct.getLateDateToPayFee())) {
                throw new IllegalArgumentException("Admit card date from must be after last date to pay fee.");
            }
        }

        // Validation against active end dates
        if (addProductDto.getActiveEndDate() != null) {
            if (!addProductDto.getAdmitCardDateFrom().after(addProductDto.getActiveEndDate())) {
                throw new IllegalArgumentException("Admit card date from must be after active end date.");
            }
        } else if (customProduct.getActiveEndDate() != null) {
            if (!addProductDto.getAdmitCardDateFrom().after(customProduct.getActiveEndDate())) {
                throw new IllegalArgumentException("Admit card date from must be after active end date.");
            }
        }

        // Validation against exam dates
        if (addProductDto.getExamDateFrom() != null) {
            if(addProductDto.getAdmitCardDateTo()!=null)
            {
                if (!addProductDto.getAdmitCardDateTo().before(addProductDto.getExamDateFrom())) {
                    throw new IllegalArgumentException("Admit card date to must be before or equal of exam date from.");
                }
            }

        } else if (customProduct.getExamDateFrom() != null) {
            if(addProductDto.getAdmitCardDateTo()!=null) {
                if (!addProductDto.getAdmitCardDateTo().before(customProduct.getExamDateFrom())) {
                    throw new IllegalArgumentException("Admit card date to must be before or equal of exam date from.");
                }
            }
        }
        if(addProductDto.getAdmitCardDateFrom().equals(addProductDto.getAdmitCardDateTo()))
            return;
    }

    public Boolean validateAndSetActiveStartDateActiveEndDateAndGoLiveDateFields(AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            if (addProductDto.getActiveEndDate() != null && addProductDto.getGoLiveDate() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));
                dateFormat.parse(dateFormat.format(addProductDto.getGoLiveDate()));

                if (addProductDto.getGoLiveDate().before(createdDate)) {
                    throw new IllegalArgumentException("GO LIVE DATE HAS TO OF FUTURE OF CURRENT DATE");
                } else if (!addProductDto.getActiveEndDate().after(customProduct.getActiveStartDate())) {
                    throw new IllegalArgumentException("ACTIVE END DATE CANNOT BE BEFORE OR EQUAL OF ACTIVE START DATE");
                } else if (!addProductDto.getActiveEndDate().after(addProductDto.getGoLiveDate()) || !addProductDto.getGoLiveDate().after(customProduct.getActiveStartDate())) {
                    throw new IllegalArgumentException("GO LIVE DATE CANNOT BE BEFORE OR EQUAL OF GO LIVE DATE AND BEFORE OR EQUAL OF ACTIVE START DATE");
                } else if (addProductDto.getExamDateFrom() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));

                    if (!addProductDto.getActiveEndDate().before(addProductDto.getExamDateFrom())) {
                        throw new IllegalArgumentException("ACTIVE END DATE CANNOT BE AFTER OR EQUAL OF EXAM DATE FROM DATE");
                    }
                } else {
                    if (!addProductDto.getActiveEndDate().before(customProduct.getExamDateFrom())) {
                        throw new IllegalArgumentException("ACTIVE END DATE CANNOT BE AFTER OR EQUAL OF EXAM DATE FROM DATE");
                    }
                }
                customProduct.getDefaultSku().setActiveEndDate(addProductDto.getActiveEndDate());
                customProduct.setGoLiveDate(addProductDto.getGoLiveDate());

            } else if (addProductDto.getActiveEndDate() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));

                if (!addProductDto.getActiveEndDate().after(customProduct.getActiveStartDate())) {
                    throw new IllegalArgumentException("ACTIVE END DATE CANNOT BE BEFORE OR EQUAL OF ACTIVE START DATE");
                } else if (!addProductDto.getActiveEndDate().after(customProduct.getGoLiveDate())) {
                    throw new IllegalArgumentException("ACTIVE END DATE CANNOT BE BEFORE OR EQUAL OF GO LIVE DATE");
                } else if (addProductDto.getExamDateFrom() != null) {

                    dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                    if (!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException("EXAM DATE FROM MUST BE AFTER ACTIVE END DATE");
                    }
                } else {
                    if (!customProduct.getExamDateFrom().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException("EXAM DATE FROM MUST BE AFTER ACTIVE END DATE");
                    }
                }

                customProduct.getDefaultSku().setActiveEndDate(addProductDto.getActiveEndDate());
            } else if (addProductDto.getGoLiveDate() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getGoLiveDate()));

                if (!addProductDto.getGoLiveDate().after(customProduct.getActiveStartDate())) {
                    throw new IllegalArgumentException("GO LIVE DATE CANNOT BE BEFORE OR EQUAL OF ACTIVE START DATE");
                } else if (!customProduct.getActiveEndDate().after(addProductDto.getGoLiveDate())) {
                    throw new IllegalArgumentException("GO LIVE DATE CANNOT BE AFTER AND EQUAL OF EXPIRY DATE");
                }
                customProduct.setGoLiveDate(addProductDto.getGoLiveDate());
            }

            return true;
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("PARSE EXCEPTION CAUGHT WHILE VALIDATING ADD PRODUCT DTO: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }
    }

    public Boolean validateAndSetExamDateFromAndExamDateToFields(AddProductDto addProductDto, CustomProduct customProduct) throws Exception {
        try {
            if (addProductDto.getExamDateFrom() != null && addProductDto.getExamDateTo() != null) {

                // Validation on date for being wrong types. -> these needs to be changed or we have to add exception.
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));

                if (addProductDto.getExamDateTo().before(addProductDto.getExamDateFrom())) {
                    throw new IllegalArgumentException(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM);
                }

                if (addProductDto.getActiveEndDate() != null) {
                    if (!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate()) || !addProductDto.getExamDateTo().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEDATEAFTERACTIVEENDDATE);
                    }

                } else {
                    if (!addProductDto.getExamDateFrom().after(customProduct.getActiveEndDate()) || !addProductDto.getExamDateTo().after(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEDATEAFTERACTIVEENDDATE);
                    }
                }
                customProduct.setExamDateFrom(addProductDto.getExamDateFrom());
                customProduct.setExamDateTo(addProductDto.getExamDateTo());

            } else if (addProductDto.getExamDateFrom() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                if (customProduct.getExamDateTo().before(addProductDto.getExamDateFrom())) {
                    throw new IllegalArgumentException(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM);
                }

                if (addProductDto.getActiveEndDate() == null) {
                    if (!addProductDto.getExamDateFrom().after(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEEXAMDATEAFTERACTIVEENDDATE);
                    }
                } else {
                    if (!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEEXAMDATEAFTERACTIVEENDDATE);
                    }
                }
                customProduct.setExamDateFrom(addProductDto.getExamDateFrom());

            } else if (addProductDto.getExamDateTo() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));
                if (addProductDto.getExamDateTo().before(customProduct.getExamDateFrom())) {
                    throw new IllegalArgumentException(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM);
                }
                if (addProductDto.getActiveEndDate() == null) {
                    if (!addProductDto.getExamDateTo().after(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEEXAMDATEAFTERACTIVEENDDATE);
                    }
                } else {
                    if (!addProductDto.getExamDateTo().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException("TENTATIVE EXAMINATION DATA MUST BE AFTER ACTIVE END DATE");
                    }
                }
                customProduct.setExamDateTo(addProductDto.getExamDateTo());
            }
            return true;

        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("PARSE EXCEPTION CAUGHT WHILE VALIDATING ADD PRODUCT DTO: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }
    }

    public boolean validateExamDateFromAndExamDateTo(AddProductDto addProductDto, CustomProduct customProduct) throws Exception {
        try {
            if (addProductDto.getExamDateFrom() != null && addProductDto.getExamDateTo() != null) {

                // Validation on date for being wrong types. -> these needs to be changed or we have to add exception.
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));

                if (addProductDto.getExamDateTo().before(addProductDto.getExamDateFrom())) {
                    throw new IllegalArgumentException(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM);
                }

                if (addProductDto.getActiveEndDate() != null) {
                    if (!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate()) || !addProductDto.getExamDateTo().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEDATEAFTERACTIVEENDDATE);
                    }

                } else {
                    if (!addProductDto.getExamDateFrom().after(customProduct.getActiveEndDate()) || !addProductDto.getExamDateTo().after(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEDATEAFTERACTIVEENDDATE);
                    }
                }
                customProduct.setExamDateFrom(addProductDto.getExamDateFrom());
                customProduct.setExamDateTo(addProductDto.getExamDateTo());

            } else if (addProductDto.getExamDateFrom() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                if (customProduct.getExamDateTo().before(addProductDto.getExamDateFrom())) {
                    throw new IllegalArgumentException(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM);
                }

                if (addProductDto.getActiveEndDate() == null) {
                    if (!addProductDto.getExamDateFrom().after(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEEXAMDATEAFTERACTIVEENDDATE);
                    }
                } else {
                    if (!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEEXAMDATEAFTERACTIVEENDDATE);
                    }
                }
                customProduct.setExamDateFrom(addProductDto.getExamDateFrom());

            } else if (addProductDto.getExamDateTo() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));
                if (addProductDto.getExamDateTo().before(customProduct.getExamDateFrom())) {
                    throw new IllegalArgumentException(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM);
                }
                if (addProductDto.getActiveEndDate() == null) {
                    if (!addProductDto.getExamDateTo().after(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEEXAMDATEAFTERACTIVEENDDATE);
                    }
                } else {
                    if (!addProductDto.getExamDateTo().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException("TENTATIVE EXAMINATION DATA MUST BE AFTER ACTIVE END DATE");
                    }
                }
                customProduct.setExamDateTo(addProductDto.getExamDateTo());

            }
            return true;
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("PARSE EXCEPTION CAUGHT WHILE VALIDATING ADD PRODUCT DTO: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }
    }

    public Boolean validateAndSetExamCenterAvailableDate (AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            if (addProductDto.getExamCenterAvailableDate() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getExamCenterAvailableDate()));

                if(createdDate!=null)
                {
                    if(!isSameOrFutureDate(addProductDto.getExamCenterAvailableDate()))
                    {
                        throw new IllegalArgumentException("Exam Center Available cannot be past of current date.");
                    }
                }

                if (addProductDto.getActiveEndDate() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getExamCenterAvailableDate()));
                    if (!addProductDto.getExamCenterAvailableDate().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException("Exam Center Available date must be after active end date.");
                    }
                } else {
                    if (!addProductDto.getExamCenterAvailableDate().after(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException("Exam Center Available date must be after active end date.");
                    }
                }
                validateExamCenterAvailableDateAgainstOtherDates(addProductDto, customProduct);
                customProduct.setExamCenterAvailableDate(addProductDto.getExamCenterAvailableDate());
            } else {
                customProduct.setExamCenterAvailableDate(null);
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating go live date: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    // Helper method to centralize validation logic for Exam Center Available date
    private void validateExamCenterAvailableDateAgainstOtherDates (AddProductDto addProductDto, CustomProduct customProduct) {
        // Validation against Admit card dates (BEFORE)
        if (addProductDto.getAdmitCardDateTo() != null) {
            if (!addProductDto.getExamCenterAvailableDate().after(addProductDto.getAdmitCardDateTo())) {
                throw new IllegalArgumentException("Exam Center Available Date must be after Admit card date to.");
            }
        } else if (customProduct.getAdmitCardDateTo() != null) {
            if (!addProductDto.getExamCenterAvailableDate().after(customProduct.getAdmitCardDateTo())) {
                throw new IllegalArgumentException("Exam Center Available Date must be after Admit card date to.");
            }
        }

        // Validation against modification dates
        if (addProductDto.getModificationDateTo() != null) {
            if (!addProductDto.getExamCenterAvailableDate().after(addProductDto.getModificationDateTo())) {
                throw new IllegalArgumentException("Exam Center Available Date must be after modification date to.");
            }
        } else if (customProduct.getModificationDateTo() != null) {
            if (!addProductDto.getExamCenterAvailableDate().after(customProduct.getModificationDateTo())) {
                throw new IllegalArgumentException("Exam Center Available Date must be after modification date.");
            }
        }

        // Validation against fee payment dates
        if (addProductDto.getLastDateToPayFee() != null) {
            if (!addProductDto.getExamCenterAvailableDate().after(addProductDto.getLastDateToPayFee())) {
                throw new IllegalArgumentException("Exam Center Available Date must be after last date to pay fee.");
            }
        } else if (customProduct.getLateDateToPayFee() != null) {
            if (!addProductDto.getExamCenterAvailableDate().after(customProduct.getLateDateToPayFee())) {
                throw new IllegalArgumentException("Exam Center Available Date must be after last date to pay fee.");
            }
        }

        // Validation against active end dates
        if (addProductDto.getActiveEndDate() != null) {
            if (!addProductDto.getExamCenterAvailableDate().after(addProductDto.getActiveEndDate())) {
                throw new IllegalArgumentException("Exam Center Available Date must be after active end date.");
            }
        } else if (customProduct.getActiveEndDate() != null) {
            if (!addProductDto.getExamCenterAvailableDate().after(customProduct.getActiveEndDate())) {
                throw new IllegalArgumentException("Exam Center Available Date must be after active end date.");
            }
        }

        // Validation against exam dates (AFTER)
        if (addProductDto.getExamDateFrom() != null) {
            if(addProductDto.getExamCenterAvailableDate()!=null)
            {
                if (!addProductDto.getExamCenterAvailableDate().before(addProductDto.getExamDateFrom())) {
                    throw new IllegalArgumentException("Exam Center Available Date must be before or equal of exam date from.");
                }
            }
        }
    }

    public Boolean validateAndSetAnswerKeyAvailableDate (AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            if (addProductDto.getAnswerKeyAvailableDate() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getAnswerKeyAvailableDate()));

                if(createdDate!=null)
                {
                    if(!isSameOrFutureDate(addProductDto.getAnswerKeyAvailableDate()))
                    {
                        throw new IllegalArgumentException("Answer Key Available cannot be past of current date.");
                    }
                }

                if (addProductDto.getActiveEndDate() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));
                    if (!addProductDto.getAnswerKeyAvailableDate().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException("Answer Key Available date must be after active end date.");
                    }
                } else {
                    if (!addProductDto.getAnswerKeyAvailableDate().after(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException("Answer Key Available date must be after active end date.");
                    }
                }
                validateAnswerKeyAvailableDateAgainstOtherDates(addProductDto, customProduct);
                customProduct.setAnswerKeyAvailableDate(addProductDto.getAnswerKeyAvailableDate());
            } else {
                customProduct.setAnswerKeyAvailableDate(null);
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating go live date: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    // Helper method to centralize validation logic for Exam Center Available date
    private void validateAnswerKeyAvailableDateAgainstOtherDates(AddProductDto addProductDto, CustomProduct customProduct) {

        // Validation against Exam dates (BEFORE)
        if (addProductDto.getExamDateTo() != null) {
                if (!addProductDto.getAnswerKeyAvailableDate().after(addProductDto.getExamDateTo())) {
                    throw new IllegalArgumentException("Answer Key Available date must be before or equal of exam date to.");
                }
        } else if (customProduct.getExamDateTo() != null) {
                if (!addProductDto.getAnswerKeyAvailableDate().after(customProduct.getExamDateTo())) {
                    throw new IllegalArgumentException("Answer Key Available date must be before or equal of exam date to.");
                }
        }

        // Validation against Admit card dates
        if (addProductDto.getAdmitCardDateTo() != null) {
            if (!addProductDto.getAnswerKeyAvailableDate().after(addProductDto.getAdmitCardDateTo())) {
                throw new IllegalArgumentException("Answer Key Available date must be after Admit Card date to.");
            }
        } else if (customProduct.getAdmitCardDateTo() != null) {
            if (!addProductDto.getAnswerKeyAvailableDate().after(customProduct.getAdmitCardDateTo())) {
                throw new IllegalArgumentException("Answer Key Available date must be after Admit Card date to.");
            }
        }

        // Validation against Exam Center Available date
        if (addProductDto.getExamCenterAvailableDate() != null) {
            if (!addProductDto.getAnswerKeyAvailableDate().after(addProductDto.getExamCenterAvailableDate())) {
                throw new IllegalArgumentException("Answer Key Available date must be after Exam Center Available date.");
            }
        } else if (customProduct.getExamCenterAvailableDate() != null) {
            if (!addProductDto.getAnswerKeyAvailableDate().after(customProduct.getExamCenterAvailableDate())) {
                throw new IllegalArgumentException("Answer Key Available date must be after Exam Center Available date.");
            }
        }

        // Validation against modification dates
        if (addProductDto.getModificationDateTo() != null) {
            if (!addProductDto.getAnswerKeyAvailableDate().after(addProductDto.getModificationDateTo())) {
                throw new IllegalArgumentException("Answer Key Available Date must be after modification date to.");
            }
        } else if (customProduct.getModificationDateTo() != null) {
            if (!addProductDto.getAnswerKeyAvailableDate().after(customProduct.getModificationDateTo())) {
                throw new IllegalArgumentException("Answer Key Available Date must be after modification date.");
            }
        }

        // Validation against fee payment dates
        if (addProductDto.getLastDateToPayFee() != null) {
            if (!addProductDto.getAnswerKeyAvailableDate().after(addProductDto.getLastDateToPayFee())) {
                throw new IllegalArgumentException("Answer Key Available Date must be after last date to pay fee.");
            }
        } else if (customProduct.getLateDateToPayFee() != null) {
            if (!addProductDto.getAnswerKeyAvailableDate().after(customProduct.getLateDateToPayFee())) {
                throw new IllegalArgumentException("Answer Key Available Date must be after last date to pay fee.");
            }
        }

        // Validation against active end dates
        if (addProductDto.getActiveEndDate() != null) {
            if (!addProductDto.getAnswerKeyAvailableDate().after(addProductDto.getActiveEndDate())) {
                throw new IllegalArgumentException("Answer Key Available Date must be after active end date.");
            }
        } else if (customProduct.getActiveEndDate() != null) {
            if (!addProductDto.getAnswerKeyAvailableDate().after(customProduct.getActiveEndDate())) {
                throw new IllegalArgumentException("Answer Key Available Date must be after active end date.");
            }
        }
    }

    public Boolean validateAndSetResultDeclarationDate (AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            if (addProductDto.getResultDeclarationDate() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getResultDeclarationDate()));

                if(createdDate!=null)
                {
                    if(!isSameOrFutureDate(addProductDto.getResultDeclarationDate()))
                    {
                        throw new IllegalArgumentException("Result Declaration date cannot be past of current date.");
                    }
                }

                if (addProductDto.getActiveEndDate() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));
                    if (!addProductDto.getResultDeclarationDate().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException("Result Declaration date must be after active end date.");
                    }
                } else {
                    if (!addProductDto.getResultDeclarationDate().after(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException("Answer Key Available date must be after active end date.");
                    }
                }
                validateResultDeclarationDateAgainstOtherDates(addProductDto, customProduct);
                customProduct.setResultDeclarationDate(addProductDto.getResultDeclarationDate());
            } else {
                customProduct.setResultDeclarationDate(null);
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating go live date: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    // Helper method to centralize validation logic for Exam Center Available date
    private void validateResultDeclarationDateAgainstOtherDates (AddProductDto addProductDto, CustomProduct customProduct) {

        // Validation against Answer Key Available (BEFORE)
        if (addProductDto.getAnswerKeyAvailableDate() != null) {
            if (!addProductDto.getResultDeclarationDate().after(addProductDto.getAnswerKeyAvailableDate())) {
                throw new IllegalArgumentException("Result Declaration date must be after or equal of Answer Key Available date.");
            }
        } else if (customProduct.getAnswerKeyAvailableDate() != null) {
            if (!addProductDto.getResultDeclarationDate().after(customProduct.getAnswerKeyAvailableDate())) {
                throw new IllegalArgumentException("Result Declaration date must be after or equal of Answer Key Available date.");
            }
        }

        // Validation against Exam dates (BEFORE)
        if (addProductDto.getExamDateTo() != null) {
            if (!addProductDto.getResultDeclarationDate().after(addProductDto.getExamDateTo())) {
                throw new IllegalArgumentException("Result Declaration date must be after or equal of exam date to.");
            }
        } else if (customProduct.getExamDateTo() != null) {
            if (!addProductDto.getResultDeclarationDate().after(customProduct.getExamDateTo())) {
                throw new IllegalArgumentException("Result Declaration date must be after or equal of exam date to.");
            }
        }

        // Validation against Admit card dates
        if (addProductDto.getAdmitCardDateTo() != null) {
            if (!addProductDto.getResultDeclarationDate().after(addProductDto.getAdmitCardDateTo())) {
                throw new IllegalArgumentException("Result Declaration date must be after Admit Card date to.");
            }
        } else if (customProduct.getAdmitCardDateTo() != null) {
            if (!addProductDto.getResultDeclarationDate().after(customProduct.getAdmitCardDateTo())) {
                throw new IllegalArgumentException("Result Declaration date must be after Admit Card date to.");
            }
        }

        // Validation against Exam Center Available date
        if (addProductDto.getExamCenterAvailableDate() != null) {
            if (!addProductDto.getResultDeclarationDate().after(addProductDto.getExamCenterAvailableDate())) {
                throw new IllegalArgumentException("Result Declaration date must be after Exam Center Available date.");
            }
        } else if (customProduct.getExamCenterAvailableDate() != null) {
            if (!addProductDto.getResultDeclarationDate().after(customProduct.getExamCenterAvailableDate())) {
                throw new IllegalArgumentException("Result Declaration date must be after Exam Center Available date.");
            }
        }

        // Validation against modification dates
        if (addProductDto.getModificationDateTo() != null) {
            if (!addProductDto.getResultDeclarationDate().after(addProductDto.getModificationDateTo())) {
                throw new IllegalArgumentException("Result Declaration Date must be after modification date to.");
            }
        } else if (customProduct.getModificationDateTo() != null) {
            if (!addProductDto.getResultDeclarationDate().after(customProduct.getModificationDateTo())) {
                throw new IllegalArgumentException("Result Declaration Date must be after modification date.");
            }
        }

        // Validation against fee payment dates
        if (addProductDto.getLastDateToPayFee() != null) {
            if (!addProductDto.getResultDeclarationDate().after(addProductDto.getLastDateToPayFee())) {
                throw new IllegalArgumentException("Result Declaration Date must be after last date to pay fee.");
            }
        } else if (customProduct.getLateDateToPayFee() != null) {
            if (!addProductDto.getResultDeclarationDate().after(customProduct.getLateDateToPayFee())) {
                throw new IllegalArgumentException("Result Declaration Date must be after last date to pay fee.");
            }
        }

        // Validation against active end dates
        if (addProductDto.getActiveEndDate() != null) {
            if (!addProductDto.getResultDeclarationDate().after(addProductDto.getActiveEndDate())) {
                throw new IllegalArgumentException("Result Declaration Date must be after active end date.");
            }
        } else if (customProduct.getActiveEndDate() != null) {
            if (!addProductDto.getResultDeclarationDate().after(customProduct.getActiveEndDate())) {
                throw new IllegalArgumentException("Result Declaration Date must be after active end date.");
            }
        }
    }

    public Boolean validateAndSetCounsellingDate (AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            if (addProductDto.getCounsellingDate() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getCounsellingDate()));

                if(createdDate!=null)
                {
                    if(!isSameOrFutureDate(addProductDto.getCounsellingDate()))
                    {
                        throw new IllegalArgumentException("Counselling date cannot be past of current date.");
                    }
                }

                if (addProductDto.getActiveEndDate() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));
                    if (!addProductDto.getCounsellingDate().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException("Counselling date must be after active end date.");
                    }
                } else {
                    if (!addProductDto.getCounsellingDate().after(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException("Counselling date must be after active end date.");
                    }
                }
                validateCounsellingDateAgainstOtherDates(addProductDto, customProduct);
                customProduct.setCounsellingDate(addProductDto.getCounsellingDate());
            } else {
                customProduct.setCounsellingDate(null);
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating go live date: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    // Helper method to centralize validation logic for Exam Center Available date
    private void validateCounsellingDateAgainstOtherDates (AddProductDto addProductDto, CustomProduct customProduct) {

        // Validation against Result Declaration (BEFORE)
        if (addProductDto.getResultDeclarationDate() != null) {
            if (!addProductDto.getCounsellingDate().after(addProductDto.getResultDeclarationDate())) {
                throw new IllegalArgumentException("Counselling date must be after Result Declaration date.");
            }
        } else if (customProduct.getResultDeclarationDate() != null) {
            if (!addProductDto.getCounsellingDate().after(customProduct.getResultDeclarationDate())) {
                throw new IllegalArgumentException("Counselling date must be after Result Declaration date.");
            }
        }

        // Validation against Answer Key Available (BEFORE)
        if (addProductDto.getAnswerKeyAvailableDate() != null) {
            if (!addProductDto.getCounsellingDate().after(addProductDto.getAnswerKeyAvailableDate())) {
                throw new IllegalArgumentException("Counselling date must be after of Answer Key Available date.");
            }
        } else if (customProduct.getAnswerKeyAvailableDate() != null) {
            if (!addProductDto.getCounsellingDate().after(customProduct.getAnswerKeyAvailableDate())) {
                throw new IllegalArgumentException("Counselling date must be after of Answer Key Available date.");
            }
        }

        // Validation against Exam dates (BEFORE)
        if (addProductDto.getExamDateTo() != null) {
            if (!addProductDto.getCounsellingDate().after(addProductDto.getExamDateTo())) {
                throw new IllegalArgumentException("Counselling date must be after of exam date to.");
            }
        } else if (customProduct.getExamDateTo() != null) {
            if (!addProductDto.getCounsellingDate().after(customProduct.getExamDateTo())) {
                throw new IllegalArgumentException("Counselling date must be after of exam date to.");
            }
        }

        // Validation against Admit card dates
        if (addProductDto.getAdmitCardDateTo() != null) {
            if (!addProductDto.getCounsellingDate().after(addProductDto.getAdmitCardDateTo())) {
                throw new IllegalArgumentException("Counselling date must be after Admit Card date to.");
            }
        } else if (customProduct.getAdmitCardDateTo() != null) {
            if (!addProductDto.getCounsellingDate().after(customProduct.getAdmitCardDateTo())) {
                throw new IllegalArgumentException("Counselling date must be after Admit Card date to.");
            }
        }

        // Validation against Exam Center Available date
        if (addProductDto.getExamCenterAvailableDate() != null) {
            if (!addProductDto.getCounsellingDate().after(addProductDto.getExamCenterAvailableDate())) {
                throw new IllegalArgumentException("Counselling date must be after Exam Center Available date.");
            }
        } else if (customProduct.getExamCenterAvailableDate() != null) {
            if (!addProductDto.getCounsellingDate().after(customProduct.getExamCenterAvailableDate())) {
                throw new IllegalArgumentException("Counselling date must be after Exam Center Available date.");
            }
        }

        // Validation against modification dates
        if (addProductDto.getModificationDateTo() != null) {
            if (!addProductDto.getCounsellingDate().after(addProductDto.getModificationDateTo())) {
                throw new IllegalArgumentException("Counselling date must be after modification date to.");
            }
        } else if (customProduct.getModificationDateTo() != null) {
            if (!addProductDto.getCounsellingDate().after(customProduct.getModificationDateTo())) {
                throw new IllegalArgumentException("Counselling date must be after modification date.");
            }
        }

        // Validation against fee payment dates
        if (addProductDto.getLastDateToPayFee() != null) {
            if (!addProductDto.getCounsellingDate().after(addProductDto.getLastDateToPayFee())) {
                throw new IllegalArgumentException("Counselling date must be after last date to pay fee.");
            }
        } else if (customProduct.getLateDateToPayFee() != null) {
            if (!addProductDto.getCounsellingDate().after(customProduct.getLateDateToPayFee())) {
                throw new IllegalArgumentException("Counselling date must be after last date to pay fee.");
            }
        }

        // Validation against active end dates
        if (addProductDto.getActiveEndDate() != null) {
            if (!addProductDto.getCounsellingDate().after(addProductDto.getActiveEndDate())) {
                throw new IllegalArgumentException("Counselling date must be after active end date.");
            }
        } else if (customProduct.getActiveEndDate() != null) {
            if (!addProductDto.getCounsellingDate().after(customProduct.getActiveEndDate())) {
                throw new IllegalArgumentException("Counselling date must be after active end date.");
            }
        }
    }

    public boolean validateProductState(AddProductDto addProductDto, CustomProduct customProduct, String authHeader) throws Exception {
        try {
            if (addProductDto.getProductState() != null) {

                String jwtToken = authHeader.substring(7);
                Long userId = jwtTokenUtil.extractId(jwtToken);

                Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
                String role = roleService.findRoleName(roleId);

                if (customProduct.getUserId().equals(userId) && roleId.equals(4)) {
                    throw new IllegalArgumentException("SERVICE PROVIDER WHO CREATED THE PRODUCT CANNOT CHANGE ITS STATE");
                }

                CustomProductState customProductState = productStateService.getProductStateById(addProductDto.getProductState());
                if (customProductState == null) {
                    throw new IllegalArgumentException("NO PRODUCT STATE EXIST WITH THIS ID");
                }

                if(customProductState.getProductState().equals("DRAFT") && !customProduct.getProductState().getProductState().equals("DRAFT")) {
                    throw new IllegalArgumentException("PRODUCT STATE CANNOT BE CHANGED FROM ACTUAL PRODUCT STATES TO DRAFT STATE.");
                }

                if (role.equals(Constant.SERVICE_PROVIDER)) {
                    if ((!customProduct.getProductState().getProductState().equals(Constant.PRODUCT_STATE_NEW) && !customProduct.getProductState().getProductState().equals(Constant.PRODUCT_STATE_MODIFIED)) || (!customProductState.getProductState().equals(PRODUCT_STATE_APPROVED) && !customProductState.getProductState().equals(PRODUCT_STATE_REJECTED))) {
                        throw new IllegalArgumentException("PRODUCT STATE ONLY CHANGE FROM NEW/MODIFIABLE TO APPROVED OR REJECTED STATE");
                    }
                    List<Privileges> privileges = privilegeService.getServiceProviderPrivilege(userId);
                    for (Privileges privilege : privileges) {
                        if ((privilege.getPrivilege_name().equals(Constant.PRIVILEGE_APPROVE_PRODUCT) && customProductState.getProductState().equals(Constant.PRODUCT_STATE_APPROVED))) {
                            if (addProductDto.getIsReviewRequired() == null) {
                                throw new IllegalArgumentException("Is review Required cannot be null");
                            }
                            customProduct.setIsReviewRequired(addProductDto.getIsReviewRequired());
                            customProduct.setProductState(customProductState);
                            customProduct.setIsApproved(true);
                            return true;
                        } else if ((privilege.getPrivilege_name().equals(Constant.PRIVILEGE_REJECT_PRODUCT) && customProductState.getProductState().equals(Constant.PRODUCT_STATE_REJECTED))) {
                            if (addProductDto.getRejectionStatus() == null) {
                                throw new IllegalArgumentException("REJECTION STATUS CANNOT BE NULL IF PRODUCT IS REJECTED");
                            }
                            CustomProductRejectionStatus productRejectionStatus = productRejectionStatusService.getAllRejectionStatusByRejectionStatusId(addProductDto.getRejectionStatus());
                            if (productRejectionStatus == null) {
                                throw new IllegalArgumentException("NO PRODUCT REJECTION STATUS IS FOUND");
                            }
                            customProduct.setProductState(customProductState);
                            customProduct.setRejectionStatus(productRejectionStatus);
                            return true;
                        }
                    }
                    throw new IllegalArgumentException("Not have privilege to perform action.");
                } else if (role.equals(Constant.ADMIN) || role.equals(Constant.SUPER_ADMIN)) {
                    if (customProductState.getProductState().equals("REJECTED")) {
                        if (addProductDto.getRejectionStatus() == null) {
                            throw new IllegalArgumentException("REJECTION STATE CANNOT BE NULL IF PRODUCT IS REJECTED");
                        } else {
                            CustomProductRejectionStatus productRejectionStatus = productRejectionStatusService.getAllRejectionStatusByRejectionStatusId(addProductDto.getRejectionStatus());
                            if (productRejectionStatus == null) {
                                throw new IllegalArgumentException("NO PRODUCT REJECTION STATUS IS FOUND");
                            }
                            customProduct.setRejectionStatus(productRejectionStatus);
                        }
                    }
                    else if (customProductState.getProductState().equals(Constant.PRODUCT_STATE_APPROVED)) {
                        if (addProductDto.getIsReviewRequired() == null) {
                            throw new IllegalArgumentException("Is review Required cannot be null");
                        }
                        customProduct.setIsReviewRequired(addProductDto.getIsReviewRequired());
                        customProduct.setProductState(customProductState);
                        customProduct.setIsApproved(true);
                    }
                    else if (customProductState.getProductState().equals(PRODUCT_STATE_RESUBMIT))
                    {
                        if(addProductDto.getResubmitComment()==null)
                        {
                            throw new IllegalArgumentException("Comment reason why the product is needed to be resubmit");
                        }
                        customProduct.setResubmitComment(addProductDto.getResubmitComment());
                        customProduct.setProductState(customProductState);
                    }
                    else if (!customProduct.getProductState().getProductState().equals(PRODUCT_STATE_MODIFIED) && customProductState.getProductState().equals(PRODUCT_STATE_MODIFIED)) {
                        throw new IllegalArgumentException("PRODUCT STATE CANNOT MOVE FROM ANY OTHER STATE TO MODIFIED STATE");
                    } else if (!customProduct.getProductState().getProductState().equals(PRODUCT_STATE_DRAFT) && customProductState.getProductState().equals(PRODUCT_STATE_DRAFT)) {
                        throw new IllegalArgumentException("PRODUCT STATE CANNOT MOVE FROM ANY OTHER STATE TO DRAFT STATE");
                    }
                    customProduct.setProductState(customProductState);

                    return true;
                } else {
                    throw new IllegalArgumentException("Role not Service provider and admin or super admin");
                }
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }
    }

    public boolean deleteOldReserveCategoryMapping(CustomProduct customProduct) throws Exception {
        try {
            productReserveCategoryFeePostRefService.removeProductReserveCategoryFeeAndPostByProductId(customProduct);
            productReserveCategoryBornBeforeAfterRefService.removeProductReserveCategoryBornBeforeAfterByProductId(customProduct);
            return true;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }
    }

    public boolean deleteOldPhysicalRequirement(CustomProduct customProduct) throws Exception {
        try {
            productGenderPhysicalRequirementService.removeProductGenderPhysicalRequirementByProductId(customProduct);
            return true;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }
    }

    public boolean validateAdmitCardDates(AddProductDto addProductDto) throws Exception {
        try {
           if (addProductDto.getAdmitCardDateFrom() != null && addProductDto.getAdmitCardDateTo() != null) {
               if (addProductDto.getAdmitCardDateFrom() != null) {
                   dateFormat.parse(dateFormat.format(addProductDto.getAdmitCardDateFrom()));
               }
               if (addProductDto.getAdmitCardDateTo() != null) {
                   dateFormat.parse(dateFormat.format(addProductDto.getAdmitCardDateTo()));
               }

               if (addProductDto.getAdmitCardDateFrom() != null && addProductDto.getAdmitCardDateTo() != null) {
                   if (addProductDto.getAdmitCardDateFrom().after(addProductDto.getAdmitCardDateTo())) {
                       throw new IllegalArgumentException("Admit card date from cannot be of future of admit card date to.");
                   }
               } else if (addProductDto.getAdmitCardDateFrom() != null) {
                   addProductDto.setAdmitCardDateTo(addProductDto.getAdmitCardDateFrom());
               } else if (addProductDto.getAdmitCardDateTo() != null) {
                   addProductDto.setAdmitCardDateFrom(addProductDto.getAdmitCardDateTo());
               }

               if (addProductDto.getExamDateFrom() != null && !addProductDto.getExamDateFrom().after(addProductDto.getAdmitCardDateTo())) {
                   throw new IllegalArgumentException("Admit card to cannot be future of exam date from.");
               }

               if (addProductDto.getModificationDateTo() != null) {
                   dateFormat.parse(dateFormat.format(addProductDto.getModificationDateTo()));
                   if (!addProductDto.getAdmitCardDateFrom().after(addProductDto.getModificationDateTo())) {
                       throw new IllegalArgumentException("Admit card date from must be of future of modification date to.");
                   }
               } else if (addProductDto.getLastDateToPayFee() != null) {
                   dateFormat.parse(dateFormat.format(addProductDto.getLastDateToPayFee()));
                   if (!addProductDto.getAdmitCardDateFrom().after(addProductDto.getLastDateToPayFee())) {
                       throw new IllegalArgumentException("Admit card date from must be of future of last date to pay application fee.");
                   }
               } else {
                   assert addProductDto.getAdmitCardDateFrom() != null;
                   if (!addProductDto.getAdmitCardDateFrom().after(addProductDto.getActiveEndDate())) {
                       throw new IllegalArgumentException("Admit card date from must be of future of active end date.");
                   }
               }
           }
            if (addProductDto.getAnswerKeyAvailableDate()!=null&&addProductDto.getExamDateTo()!=null)
            {
                if(addProductDto.getAnswerKeyAvailableDate().before(addProductDto.getExamDateTo()))
                    throw new IllegalArgumentException("Answer key cannot be available before exam concludes");
            }
            if (addProductDto.getAnswerKeyAvailableDate()!=null&&addProductDto.getAdmitCardDateTo()!=null)
            {
                if(addProductDto.getAnswerKeyAvailableDate().before(addProductDto.getAdmitCardDateTo()))
                    throw new IllegalArgumentException("Answer key cannot be before admit cards release date");
            }
            if (addProductDto.getAnswerKeyAvailableDate()!=null&&addProductDto.getModificationDateTo()!=null)
            {
                if(addProductDto.getAnswerKeyAvailableDate().before(addProductDto.getModificationDateTo()))
                    throw new IllegalArgumentException("Answer key cannot be before Tentative correction last date");
            }
            if (addProductDto.getAnswerKeyAvailableDate()!=null&&addProductDto.getLastDateToPayFee()!=null)
            {
                if(addProductDto.getAnswerKeyAvailableDate().before(addProductDto.getLastDateToPayFee()))
                    throw new IllegalArgumentException("Answer key cannot be before last day to pay fee");
            }
            if (addProductDto.getAnswerKeyAvailableDate()!=null&&addProductDto.getActiveEndDate()!=null)
            {
                if(addProductDto.getAnswerKeyAvailableDate().before(addProductDto.getActiveEndDate()))
                    throw new IllegalArgumentException("Answer key cannot be past active end date");
            }
            //****************
            if (addProductDto.getCounsellingDate()!=null&&addProductDto.getExamDateTo()!=null)
            {
                if(addProductDto.getCounsellingDate().before(addProductDto.getExamDateTo()))
                    throw new IllegalArgumentException("Counselling date cannot be available before exam concludes");
            }
            if (addProductDto.getCounsellingDate()!=null&&addProductDto.getAdmitCardDateTo()!=null)
            {
                if(addProductDto.getCounsellingDate().before(addProductDto.getAdmitCardDateTo()))
                    throw new IllegalArgumentException("Counselling date cannot be before admit cards release date");
            }
            if (addProductDto.getCounsellingDate()!=null&&addProductDto.getModificationDateTo()!=null)
            {
                if(addProductDto.getCounsellingDate().before(addProductDto.getModificationDateTo()))
                    throw new IllegalArgumentException("Counselling cannot be before Tentative correction last date");
            }
            if (addProductDto.getCounsellingDate()!=null&&addProductDto.getLastDateToPayFee()!=null)
            {
                if(addProductDto.getCounsellingDate().before(addProductDto.getLastDateToPayFee()))
                    throw new IllegalArgumentException("Counselling date cannot be before last day to pay fee");
            }
            if (addProductDto.getCounsellingDate()!=null&&addProductDto.getActiveEndDate()!=null)
            {
                if(addProductDto.getCounsellingDate().before(addProductDto.getActiveEndDate()))
                    throw new IllegalArgumentException("Counselling date cannot be before active end date");
            }
            if (addProductDto.getCounsellingDate()!=null&&addProductDto.getResultDeclarationDate()!=null)
            {
                if(addProductDto.getCounsellingDate().before(addProductDto.getResultDeclarationDate()))
                    throw new IllegalArgumentException("Counselling date cannot be before Result declaration date");
            }
            if (addProductDto.getCounsellingDate()!=null&&addProductDto.getAnswerKeyAvailableDate()!=null)
            {
                if(addProductDto.getCounsellingDate().before(addProductDto.getAnswerKeyAvailableDate()))
                    throw new IllegalArgumentException("Counselling date cannot be before answer key available date");
            }
            //*************
            if (addProductDto.getResultDeclarationDate() != null && addProductDto.getExamDateTo() != null) {
                if (addProductDto.getResultDeclarationDate().before(addProductDto.getExamDateTo())) {
                    throw new IllegalArgumentException("Result declaration date cannot be before exam concludes");
                }
            }
            if (addProductDto.getResultDeclarationDate() != null && addProductDto.getAdmitCardDateTo() != null) {
                if (addProductDto.getResultDeclarationDate().before(addProductDto.getAdmitCardDateTo())) {
                    throw new IllegalArgumentException("Result declaration date cannot be before admit cards release date");
                }
            }
            if (addProductDto.getResultDeclarationDate() != null && addProductDto.getModificationDateTo() != null) {
                if (addProductDto.getResultDeclarationDate().before(addProductDto.getModificationDateTo())) {
                    throw new IllegalArgumentException("Result declaration date cannot be before Tentative correction last date");
                }
            }
            if (addProductDto.getResultDeclarationDate() != null && addProductDto.getLastDateToPayFee() != null) {
                if (addProductDto.getResultDeclarationDate().before(addProductDto.getLastDateToPayFee())) {
                    throw new IllegalArgumentException("Result declaration date cannot be before last day to pay fee");
                }
            }
            if (addProductDto.getResultDeclarationDate() != null && addProductDto.getActiveEndDate() != null) {
                if (addProductDto.getResultDeclarationDate().before(addProductDto.getActiveEndDate())) {
                    throw new IllegalArgumentException("Result declaration date cannot be before active end date");
                }
            }
            if (addProductDto.getResultDeclarationDate() != null && addProductDto.getAnswerKeyAvailableDate() != null) {
                if (addProductDto.getResultDeclarationDate().before(addProductDto.getAnswerKeyAvailableDate())) {
                    throw new IllegalArgumentException("Result declaration date cannot be before answer key available date");
                }
            }
            //*********
            if (addProductDto.getTentativeVerificationFrom()!=null&&addProductDto.getExamDateTo()!=null)
            {
                if(addProductDto.getTentativeVerificationFrom().before(addProductDto.getExamDateTo()))
                    throw new IllegalArgumentException("Tentative document verification from date cannot be available before exam concludes");
            }
            if (addProductDto.getTentativeVerificationFrom()!=null&&addProductDto.getAdmitCardDateTo()!=null)
            {
                if(addProductDto.getTentativeVerificationFrom().before(addProductDto.getAdmitCardDateTo()))
                    throw new IllegalArgumentException("Tentative document verification from date cannot be before admit cards release date");
            }
            if (addProductDto.getTentativeVerificationFrom()!=null&&addProductDto.getModificationDateTo()!=null)
            {
                if(addProductDto.getTentativeVerificationFrom().before(addProductDto.getModificationDateTo()))
                    throw new IllegalArgumentException("Tentative document verification from date cannot be before Tentative correction last date");
            }
            if (addProductDto.getTentativeVerificationFrom()!=null&&addProductDto.getLastDateToPayFee()!=null)
            {
                if(addProductDto.getTentativeVerificationFrom().before(addProductDto.getLastDateToPayFee()))
                    throw new IllegalArgumentException("Tentative document verification from date cannot be before last day to pay fee");
            }
            if (addProductDto.getTentativeVerificationFrom()!=null&&addProductDto.getActiveEndDate()!=null)
            {
                if(addProductDto.getTentativeVerificationFrom().before(addProductDto.getActiveEndDate()))
                    throw new IllegalArgumentException("Tentative document verification from date cannot be before active end date");
            }
            if (addProductDto.getTentativeVerificationFrom()!=null&&addProductDto.getResultDeclarationDate()!=null)
            {
                if(addProductDto.getTentativeVerificationFrom().before(addProductDto.getResultDeclarationDate()))
                    throw new IllegalArgumentException("Tentative document verification from date cannot be before Result declaration date");
            }
            if (addProductDto.getTentativeVerificationFrom()!=null&&addProductDto.getAnswerKeyAvailableDate()!=null)
            {
                if(addProductDto.getTentativeVerificationFrom().before(addProductDto.getAnswerKeyAvailableDate()))
                    throw new IllegalArgumentException("Tentative document verification from date cannot be before answer key available date");
            }
            if (addProductDto.getTentativeVerificationFrom()!=null&&addProductDto.getCounsellingDate()!=null)
            {
                if(addProductDto.getTentativeVerificationFrom().before(addProductDto.getCounsellingDate()))
                    throw new IllegalArgumentException("Tentative document verification from date cannot be before Counselling");
            }
            //****
            if (addProductDto.getTentativeVerificationTo()!=null&&addProductDto.getExamDateTo()!=null)
            {
                if(addProductDto.getTentativeVerificationTo().before(addProductDto.getExamDateTo()))
                    throw new IllegalArgumentException("Tentative document verification to date cannot be available before exam concludes");
            }
            if (addProductDto.getTentativeVerificationTo()!=null&&addProductDto.getAdmitCardDateTo()!=null)
            {
                if(addProductDto.getTentativeVerificationTo().before(addProductDto.getAdmitCardDateTo()))
                    throw new IllegalArgumentException("Tentative document verification to date cannot be before admit cards release date");
            }
            if (addProductDto.getTentativeVerificationTo()!=null&&addProductDto.getModificationDateTo()!=null)
            {
                if(addProductDto.getTentativeVerificationTo().before(addProductDto.getModificationDateTo()))
                    throw new IllegalArgumentException("Tentative document verification to date cannot be before Tentative correction last date");
            }
            if (addProductDto.getTentativeVerificationTo()!=null&&addProductDto.getLastDateToPayFee()!=null)
            {
                if(addProductDto.getTentativeVerificationTo().before(addProductDto.getLastDateToPayFee()))
                    throw new IllegalArgumentException("Tentative document verification to date cannot be before last day to pay fee");
            }
            if (addProductDto.getTentativeVerificationTo()!=null&&addProductDto.getActiveEndDate()!=null)
            {
                if(addProductDto.getTentativeVerificationTo().before(addProductDto.getActiveEndDate()))
                    throw new IllegalArgumentException("Tentative document verification to date cannot be before active end date");
            }
            if (addProductDto.getTentativeVerificationTo()!=null&&addProductDto.getResultDeclarationDate()!=null)
            {
                if(addProductDto.getTentativeVerificationTo().before(addProductDto.getResultDeclarationDate()))
                    throw new IllegalArgumentException("Tentative document verification to date cannot be before Result declaration date");
            }
            if (addProductDto.getTentativeVerificationTo()!=null&&addProductDto.getAnswerKeyAvailableDate()!=null)
            {
                if(addProductDto.getTentativeVerificationTo().before(addProductDto.getAnswerKeyAvailableDate()))
                    throw new IllegalArgumentException("Tentative document verification to date cannot be before answer key available date");
            }
            if (addProductDto.getTentativeVerificationTo()!=null&&addProductDto.getCounsellingDate()!=null)
            {
                if(addProductDto.getTentativeVerificationTo().before(addProductDto.getCounsellingDate()))
                    throw new IllegalArgumentException("Tentative document verification to date cannot be before Counselling");
            }
            if (addProductDto.getTentativeVerificationTo()!=null&&addProductDto.getTentativeVerificationFrom()!=null)
            {
                if(addProductDto.getTentativeVerificationTo().before(addProductDto.getTentativeVerificationFrom()))
                    throw new IllegalArgumentException("Tentative document verification to date cannot be before tentative verification date from");
            }
            //***********
            if (addProductDto.getExamCenterAvailableDate() != null && addProductDto.getExamDateTo() != null) {
                if (addProductDto.getExamCenterAvailableDate().after(addProductDto.getExamDateTo())) {
                    throw new IllegalArgumentException("Exam dates cannot be after exam center available date");
                }
            }
           /* if (addProductDto.getResultDeclarationDate() != null && addProductDto.getAdmitCardDateTo() != null) {
                if (addProductDto.getResultDeclarationDate().before(addProductDto.getAdmitCardDateTo())) {
                    throw new IllegalArgumentException("Result declaration date cannot be before admit cards release date");
                }
            }*/
            if (addProductDto.getExamCenterAvailableDate() != null && addProductDto.getModificationDateTo() != null) {
                if (addProductDto.getExamCenterAvailableDate().before(addProductDto.getModificationDateTo())) {
                    throw new IllegalArgumentException("exam center available date cannot be before Tentative correction last date");
                }
            }
            if (addProductDto.getExamCenterAvailableDate() != null && addProductDto.getLastDateToPayFee() != null) {
                if (addProductDto.getExamCenterAvailableDate().before(addProductDto.getLastDateToPayFee())) {
                    throw new IllegalArgumentException("Exam center available date cannot be before last day to pay fee");
                }
            }
            if (addProductDto.getExamCenterAvailableDate() != null && addProductDto.getActiveEndDate() != null) {
                if (addProductDto.getExamCenterAvailableDate().before(addProductDto.getActiveEndDate())) {
                    throw new IllegalArgumentException("Exam center available date cannot be before active end date");
                }
            }
            if (addProductDto.getExamCenterAvailableDate() != null && addProductDto.getAnswerKeyAvailableDate() != null) {
                if (addProductDto.getExamCenterAvailableDate().after(addProductDto.getAnswerKeyAvailableDate())) {
                    throw new IllegalArgumentException("answer key available date cannot be after exam center available date");
                }
            }
            if (addProductDto.getExamCenterAvailableDate()!=null&&addProductDto.getResultDeclarationDate()!=null)
            {
                if(addProductDto.getExamCenterAvailableDate().after(addProductDto.getResultDeclarationDate()))
                    throw new IllegalArgumentException("Exam center available date cannot be after Result declaration date");
            }
            if (addProductDto.getExamCenterAvailableDate()!=null&&addProductDto.getCounsellingDate()!=null)
            {
                if(addProductDto.getExamCenterAvailableDate().after(addProductDto.getCounsellingDate()))
                    throw new IllegalArgumentException("Counselling date cannot be after exam center available date");
            }
            if (addProductDto.getExamCenterAvailableDate()!=null&&addProductDto.getTentativeVerificationFrom()!=null)
            {
                if(addProductDto.getExamCenterAvailableDate().after(addProductDto.getTentativeVerificationFrom()))
                    throw new IllegalArgumentException("Tentative document verification from date cannot be before Exam center available date");
            }
            /*if (addProductDto.getResultDeclarationDate() != null && addProductDto.getCounsellingDate() != null) {
                if (addProductDto.getResultDeclarationDate().before(addProductDto.getCounsellingDate())) {
                    throw new IllegalArgumentException("Result declaration date cannot be before counselling date");
                }
            }*/

            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("PARSE EXCEPTION CAUGHT WHILE VALIDATING ADMIT CARD DATES: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }
    }

    public boolean validateModificationDates(AddProductDto addProductDto) throws Exception {
        try {

            if (addProductDto.getModificationDateFrom() == null && addProductDto.getModificationDateTo() == null) {
                return true;
            }

            if (addProductDto.getModificationDateFrom() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getModificationDateFrom()));
            }
            if (addProductDto.getModificationDateTo() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getModificationDateTo()));
            }

            if (addProductDto.getModificationDateFrom() != null && addProductDto.getModificationDateTo() != null) {
                if (addProductDto.getModificationDateFrom().after(addProductDto.getModificationDateTo())) {
                    throw new IllegalArgumentException("Modification date from cannot be of future of modification date to.");
                }

            } else if (addProductDto.getAdmitCardDateFrom() != null) {
                addProductDto.setModificationDateTo(addProductDto.getModificationDateFrom());
            } else if (addProductDto.getAdmitCardDateTo() != null) {
                addProductDto.setModificationDateFrom(addProductDto.getModificationDateTo());
            }

            if (addProductDto.getAdmitCardDateFrom() != null) {
                if (addProductDto.getModificationDateTo().after(addProductDto.getAdmitCardDateFrom())) {
                    throw new IllegalArgumentException("Modification date to cannot be of future of admit card date from.");
                }
            } else {
                if(addProductDto.getExamDateFrom()!=null)
                {
                    if (addProductDto.getModificationDateTo().after(addProductDto.getExamDateFrom())) {
                        throw new IllegalArgumentException("Modification date to cannot be of future of exam date from");
                    }
                }
            }
            if(addProductDto.getModificationDateFrom()!=null&&addProductDto.getModificationDateTo()!=null)
            {
                if(addProductDto.getModificationDateFrom().equals(addProductDto.getModificationDateTo()))
                    return true;
            }
            if (addProductDto.getLastDateToPayFee() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getLastDateToPayFee()));

                if(addProductDto.getModificationDateFrom()!=null)
                {
                    /*if (!addProductDto.getModificationDateFrom().after(addProductDto.getLastDateToPayFee())) {
                        throw new IllegalArgumentException("Modification date from has to be future of last date to pay application fee.");
                    }*/

                }

            } /*else {
                if (!addProductDto.getModificationDateFrom().after(addProductDto.getActiveEndDate())) {
                    throw new IllegalArgumentException("Modification date from has to be future of active end date.");
                }
            }*/

            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating modification dates: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception occurred while validating modification dates: " + exception.getMessage());
        }
    }

    public boolean validateLastDateToPayFee(AddProductDto addProductDto) throws Exception {
        try {

            if (addProductDto.getLastDateToPayFee() == null) {
                return true;
            }

            if (addProductDto.getLastDateToPayFee() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getLastDateToPayFee()));
            }

            if (addProductDto.getModificationDateFrom() != null) {
                /*if (addProductDto.getLastDateToPayFee().after(addProductDto.getModificationDateFrom())) {
                    throw new IllegalArgumentException("Last date to pay fee cannot be after or equal to modifying date from.");
                }*/
            } else if (addProductDto.getAdmitCardDateFrom() != null) {
                if (addProductDto.getLastDateToPayFee().after(addProductDto.getAdmitCardDateFrom())) {
                    throw new IllegalArgumentException("Last date to pay fee cannot be after or equal to admit card date from.");
                }
            } else {
                if(addProductDto.getExamDateFrom()!=null)
                {
                    if (addProductDto.getLastDateToPayFee().after(addProductDto.getExamDateFrom())) {
                        throw new IllegalArgumentException("Last date to pay fee cannot be after or equal to exam date from.");
                    }
                }
            }
            if (addProductDto.getLastDateToPayFee() != null) {
                LocalDate lastPayDate = addProductDto.getLastDateToPayFee().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate activeEndDate = addProductDto.getActiveEndDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate();

                if (lastPayDate.isBefore(activeEndDate)) {
                    throw new IllegalArgumentException(
                            "Last date to pay fee must be on or after the active end date (time ignored)."
                    );
                }
            }

            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating last date to pay application fee: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception occurred while validating last date to pay application fee: " + exception.getMessage());
        }
    }

    public boolean validateLinks(AddProductDto addProductDto) throws Exception {
        try {
            if (addProductDto.getDownloadNotificationLink() != null) {
                addProductDto.setDownloadNotificationLink(addProductDto.getDownloadNotificationLink().trim());
            }

            if (addProductDto.getDownloadSyllabusLink() != null) {
                addProductDto.setDownloadSyllabusLink(addProductDto.getDownloadSyllabusLink().trim());
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception occurred while validating links: " + exception.getMessage());
        }
    }

    public boolean validateFormComplexity(AddProductDto addProductDto) throws Exception {
        try {
            if (addProductDto.getFormComplexity() == null) {
                addProductDto.setFormComplexity(1L);
            } else if (addProductDto.getFormComplexity() <= 0 || addProductDto.getFormComplexity() > 5) {
                throw new IllegalArgumentException("Form complexity must lie in range 1-5.");
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception occurred while validating form complexity: " + exception.getMessage());
        }
    }

    public boolean validatePhysicalRequirement(PostDto postDto, CustomProduct customProduct) throws Exception {
        try {
            if (postDto.getPhysicalRequirements() == null) {
                return true;
            }
            if (!postDto.getPhysicalRequirements().isEmpty()) {
                Set<Long> genderId = new HashSet<>();
                int nullGenderIdCount = 0;

                for (int physicalAttributeIndex = 0; physicalAttributeIndex < postDto.getPhysicalRequirements().size(); physicalAttributeIndex++) {
                    Long genderIdValue = postDto.getPhysicalRequirements().get(physicalAttributeIndex).getGenderId();
                    if(postDto.getPhysicalRequirements().get(physicalAttributeIndex).getGenderId()!=3&&postDto.getPhysicalRequirements().get(physicalAttributeIndex).getGenderRunningField()!=null)
                    {
                        throw new IllegalArgumentException("Cannot add running field for gender except OTHERS");
                    }
                    if(postDto.getPhysicalRequirements().get(physicalAttributeIndex).getGenderId()==3&&(postDto.getPhysicalRequirements().get(physicalAttributeIndex).getGenderRunningField()==null||postDto.getPhysicalRequirements().get(physicalAttributeIndex).getGenderRunningField().isEmpty()))
                    {
                        throw new IllegalArgumentException("running field is required when adding others for gender");
                    }
                    if (genderIdValue == null) {
                        nullGenderIdCount++;
                        continue;
                    } else
                        if (!genderId.add(genderIdValue)&&genderIdValue!=3){
                        throw new IllegalArgumentException("DUPLICATE GENDER ID FOUND: " + genderIdValue);
                }

                CustomGender customGender = genderService.getGenderByGenderId(genderIdValue);
                if (customGender == null) {
                    throw new IllegalArgumentException("GENDER NOT FOUND WITH ID: " + genderIdValue);
                }

                }


//                    if (postDto.getPhysicalRequirements().get(physicalAttributeIndex).getHeight() == null || postDto.getPhysicalRequirements().get(physicalAttributeIndex).getHeight() > MAX_HEIGHT || postDto.getPhysicalRequirements().get(physicalAttributeIndex).getHeight() < MIN_HEIGHT) {
//                        throw new IllegalArgumentException("HEIGHT IS MANDATORY FIELD AND MUST BE LESS THAN " + MAX_HEIGHT + " AND GREATER THAN " + MIN_HEIGHT);
//                    }
//                    if (postDto.getPhysicalRequirements().get(physicalAttributeIndex).getWeight() == null || postDto.getPhysicalRequirements().get(physicalAttributeIndex).getWeight() > MAX_WEIGHT || postDto.getPhysicalRequirements().get(physicalAttributeIndex).getWeight() < MIN_WEIGHT) {
//                        throw new IllegalArgumentException("WEIGHT IS MANDATORY FIELD AND MUST BE LESS THAN " + MAX_WEIGHT + " AND GREATER THAN " + MIN_WEIGHT);
//                    }
//
//                    if (postDto.getPhysicalRequirements().get(physicalAttributeIndex).getShoeSize() != null && (postDto.getPhysicalRequirements().get(physicalAttributeIndex).getShoeSize() > MAX_SHOE_SIZE || postDto.getPhysicalRequirements().get(physicalAttributeIndex).getShoeSize() < MIN_SHOE_SIZE)) {
//                        throw new IllegalArgumentException("SHOE SIZE MUST BE LESS THAN " + MAX_SHOE_SIZE + " AND GREATER THAN " + MIN_SHOE_SIZE);
//                    }
//                    if (postDto.getPhysicalRequirements().get(physicalAttributeIndex).getWaistSize() != null && (postDto.getPhysicalRequirements().get(physicalAttributeIndex).getWaistSize() > MAX_WAIST_SIZE || postDto.getPhysicalRequirements().get(physicalAttributeIndex).getWaistSize() < MIN_WAIST_SIZE)) {
//                        throw new IllegalArgumentException("WAIST SIZE MUST BE LESS THAN " + MAX_WAIST_SIZE + " AND GREATER THAN " + MIN_WAIST_SIZE);
//                    }
//
//                    if (postDto.getPhysicalRequirements().get(physicalAttributeIndex).getChestSize() != null && (postDto.getPhysicalRequirements().get(physicalAttributeIndex).getChestSize() > MAX_CHEST_SIZE || postDto.getPhysicalRequirements().get(physicalAttributeIndex).getChestSize() < MIN_CHEST_SIZE)) {
//                        throw new IllegalArgumentException("CHEST SIZE MUST BE LESS THAN " + MAX_CHEST_SIZE + " AND GREATER THAN " + MIN_CHEST_SIZE);
//                    }


                if (nullGenderIdCount > 1) {
                    throw new IllegalArgumentException("DUPLICATE NULL GENDER ID ENTRIES FOUND");
                }
            }



            return true;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION WHILE VALIDATING PHYSICAL REQUIREMENTS: " + exception.getMessage() + "\n");
        }
    }

    public boolean validateQualificationRequirement(PostDto postDto) throws Exception {
        try {
            if (postDto.getQualificationEligibility() == null || postDto.getQualificationEligibility().isEmpty()) {
                return true;
            }

            // Validate each qualification group
            for (QualificationGroupDto groupDto : postDto.getQualificationEligibility()) {
                if (groupDto.getQualificationGroups() == null || groupDto.getQualificationGroups().isEmpty()) {
                    throw new IllegalArgumentException("Qualification group cannot be empty: " + groupDto.getGroupName());
                }

                Set<QualificationEligibilityDto> seenSet = new HashSet<>();

                for (QualificationEligibilityDto dto : groupDto.getQualificationGroups()) {
                    // Get qualification details
                    if (dto.getQualificationIds() == null || dto.getQualificationIds().isEmpty()) {
                        throw new IllegalArgumentException("Qualification ID is required");
                    }

                    Qualification qualificationDetails = entityManager.find(Qualification.class, dto.getQualificationIds().get(0));
                    if (qualificationDetails == null) {
                        throw new IllegalArgumentException("Qualification not found");
                    }

                    // Check for duplicates within the group
                    if (!seenSet.add(dto)) {
                        throw new IllegalArgumentException("Duplicate Qualification Eligibility found in group: " + groupDto.getGroupName());
                    }

                    // Basic validations
                    if (dto.getIsAppearing() == null) {
                        throw new IllegalArgumentException("Need to specify whether appearing or pass for qualification " + qualificationDetails.getQualification_name());
                    }

                    // Validate logical operators for streams and subjects
                    validateLogicalOperatorId(dto.getStreamsRelationId(), "streams");
                    validateLogicalOperatorId(dto.getSubjectsRelationId(), "subjects");

                    validateRunningFields(dto, qualificationDetails);

                    validatePercentageAndCgpa(dto, qualificationDetails);

                    validateQualificationIds(dto);

                    validateStreamsWithMappingAndLogic(dto, qualificationDetails);

                    validateSubjectsWithMappingAndLogic(dto, qualificationDetails);

                    validateQualificationOperator(dto.getQualificationOperatorId(),qualificationDetails);

                    validateReserveCategoryWithMandatory(dto);

                    validatePercentageRange(dto);
                }
            }

            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage() + "\n");
        }
    }

    private void validateQualificationOperator(Long operatorId, Qualification currentQualification) {
        if (operatorId == null) {
            throw new IllegalArgumentException("Operator ID is required for qualification " + currentQualification.getQualification_name());
        }

        LogicalOperator logicalOperator = entityManager.find(LogicalOperator.class, operatorId);
        if (logicalOperator == null) {
            throw new IllegalArgumentException("Invalid logical operator ID for qualification " + currentQualification.getQualification_name());
        }
    }

    private void validateLogicalOperatorId(Long operatorId, String context) {
        if (operatorId != null) {
            LogicalOperator logicalOperator = entityManager.find(LogicalOperator.class, operatorId);
            if (logicalOperator == null) {
                throw new IllegalArgumentException("Invalid logical operator ID for " + context + ". Logical operator not found.");
            }
        }
    }

    private void validateStreamsWithMappingAndLogic(QualificationEligibilityDto dto, Qualification qualification) {
        if (dto.getCustomStreamIds() != null && !dto.getCustomStreamIds().isEmpty()) {
            // Check if qualification requires streams
            if (!qualification.getIs_stream_required()) {
                if (qualification.getQualification_id().equals(MATRICULATION_QUALIFICATION)) {
                    dto.setCustomStreamIds(List.of(MATRICULATION_IMPLICIT_STREAM_ID));
                } else if (dto.getStreamsMandatory()) {
                    throw new IllegalArgumentException("Stream is not required for qualification: " + qualification.getQualification_name());
                }
            }

            // Get valid streams for this qualification
            List<CustomStream> validStreams = streamService.getStreamByQualificationId(qualification.getQualification_id());
            Set<Long> validStreamIds = validStreams.stream()
                    .map(CustomStream::getStreamId)
                    .collect(Collectors.toSet());

            Set<Long> streamIdSet = new HashSet<>();
            List<Long> streamIds = dto.getCustomStreamIds();

            for (Long streamId : streamIds) {
                // Check if stream exists
                CustomStream customStream = entityManager.find(CustomStream.class, streamId);
                if (customStream == null) {
                    throw new IllegalArgumentException("Stream with id " + streamId + " does not exist");
                }

                // Check if stream is valid for this qualification (except for "Others" stream ID 215)
                if (streamId != 215 && !validStreamIds.contains(streamId)) {
                    throw new IllegalArgumentException("Stream '" + customStream.getStreamName() +
                            "' is not valid for qualification '" + qualification.getQualification_name() + "'");
                }

                streamIdSet.add(streamId);
            }

            if (streamIdSet.size() != streamIds.size()) {
                throw new IllegalArgumentException("DUPLICATE STREAMS NOT ALLOWED");
            }
        }
    }

    // Enhanced subject validation with logical operations
    private void validateSubjectsWithMappingAndLogic(QualificationEligibilityDto dto, Qualification qualification) {
        Integer qualificationId = qualification.getQualification_id();

        // For qualifications 1 and 2, use predefined subjects
        if (qualificationId == 1 || qualificationId == 2) {
            if (dto.getCustomSubjectIds() != null && !dto.getCustomSubjectIds().isEmpty()) {

                // Get valid subjects based on selected streams
                Set<Long> validSubjectIds = new HashSet<>();
                if (dto.getCustomStreamIds() != null && !dto.getCustomStreamIds().isEmpty()) {
                    for (Long streamId : dto.getCustomStreamIds()) {
                        List<CustomSubject> streamSubjects = subjectService.getSubjectsByStreamIds(streamId);
                        validSubjectIds.addAll(streamSubjects.stream()
                                .map(CustomSubject::getSubjectId)
                                .collect(Collectors.toSet()));
                    }
                }

                Set<Long> subjectIdsSet = new HashSet<>();
                List<Long> subjectIds = dto.getCustomSubjectIds();

                for (Long subjectId : subjectIds) {
                    CustomSubject customSubject = entityManager.find(CustomSubject.class, subjectId);
                    if (customSubject == null) {
                        throw new IllegalArgumentException("Subject with id " + subjectId + " does not exist");
                    }

                    // Check if subject is valid for selected streams (except for "Others" subject ID 54)
                    if (subjectId != 54 && !validSubjectIds.isEmpty() && !validSubjectIds.contains(subjectId)) {
                        throw new IllegalArgumentException("Subject '" + customSubject.getSubjectName() +
                                "' is not valid for the selected stream(s)");
                    }

                    subjectIdsSet.add(subjectId);
                }

                if (subjectIdsSet.size() != subjectIds.size()) {
                    throw new IllegalArgumentException("DUPLICATE SUBJECTS NOT ALLOWED");
                }

            }

            // For qualifications 1 and 2, manual subject names should not be used
            if (dto.getHighestQualificationSubjectNames() != null && !dto.getHighestQualificationSubjectNames().isEmpty()) {
                throw new IllegalArgumentException("Manual subject names are not allowed for qualification: " + qualification.getQualification_name() +
                        ". Please select from predefined subjects.");
            }
        } else {
            if (dto.getCustomSubjectIds() != null && !dto.getCustomSubjectIds().isEmpty()) {
                throw new IllegalArgumentException("Predefined subjects are not allowed for qualification: " + qualification.getQualification_name() +
                        ". Please use manual subject names.");
            }
            if (dto.getHighestQualificationSubjectNames() != null && !dto.getHighestQualificationSubjectNames().isEmpty()) {
                Set<String> subjectNameSet = new HashSet<>();
                for (String subjectName : dto.getHighestQualificationSubjectNames()) {
                    if (subjectName == null || subjectName.trim().isEmpty()) {
                        throw new IllegalArgumentException("Subject name cannot be empty");
                    }

                    if (!subjectName.matches("^[a-zA-Z0-9 ,.!?';:()&-]*$")) {
                        throw new IllegalArgumentException("Invalid subject name format: " + subjectName);
                    }

                    if (!subjectNameSet.add(subjectName.trim().toLowerCase())) {
                        throw new IllegalArgumentException("Duplicate subject name found: " + subjectName);
                    }
                }
            }
        }
    }

    private void validateReserveCategoryWithMandatory(QualificationEligibilityDto dto) {
        if (dto.getCustomReserveCategoryId() != null) {
            CustomReserveCategory customReserveCategory = entityManager.find(CustomReserveCategory.class, dto.getCustomReserveCategoryId());
            if (customReserveCategory == null) {
                throw new IllegalArgumentException("Reserve Category does not exist with id " + dto.getCustomReserveCategoryId());
            }
        }
    }

    private void validateRunningFields(QualificationEligibilityDto dto, Qualification qualification) {
        // Qualification running field validation
        if (!qualification.getQualification_name().equalsIgnoreCase("others") && dto.getQualificationIdRunningField() != null) {
            throw new IllegalArgumentException("Cannot add running field for any other qualification except OTHERS");
        }

        if (qualification.getQualification_name().equalsIgnoreCase("others") &&
                (dto.getQualificationIdRunningField() == null || dto.getQualificationIdRunningField().trim().isEmpty())) {
            throw new IllegalArgumentException("Running field is required for qualification type 'OTHERS'");
        }

        // Subject running field validation
        if (!dto.getCustomSubjectIds().isEmpty() && dto.getCustomSubjectIds().get(0) != 54 && dto.getSubjectIdRunningField() != null) {
            throw new IllegalArgumentException("Cannot add running field for any other subject except OTHERS");
        }

        if (!dto.getCustomSubjectIds().isEmpty() && dto.getCustomSubjectIds().get(0) == 54 &&
                (dto.getSubjectIdRunningField() == null || dto.getSubjectIdRunningField().trim().isEmpty())) {
            throw new IllegalArgumentException("Running field is required for subject type 'OTHERS'");
        }

        // Stream running field validation
        if (!dto.getCustomStreamIds().isEmpty() && dto.getCustomStreamIds().get(0) != 215 && dto.getStreamIdRunningField() != null) {
            throw new IllegalArgumentException("Cannot add running field for any other stream except OTHERS");
        }

        if (!dto.getCustomStreamIds().isEmpty() && dto.getCustomStreamIds().get(0) == 215 &&
                (dto.getStreamIdRunningField() == null || dto.getStreamIdRunningField().trim().isEmpty())) {
            throw new IllegalArgumentException("Running field is required for stream type 'OTHERS'");
        }

        // Reserve category running field validation
        if (dto.getCustomReserveCategoryId() == 6 &&
                (dto.getReserveCatIdRunningField() == null || dto.getReserveCatIdRunningField().trim().isEmpty())) {
            throw new IllegalArgumentException("Running field is required for reserve category 'OTHERS'");
        }
    }

    private void validatePercentageAndCgpa(QualificationEligibilityDto dto, Qualification qualification) {
        if (!dto.getIsAppearing() || dto.getIsAppearing()) {
            Boolean isPercentage = dto.getIsPercentage();

            if (isPercentage != null) {
                if (isPercentage) {
                    // isPercentage == true: percentage is required, CGPA must NOT be provided
                    if (dto.getCgpa() != null) {
                        throw new IllegalArgumentException("CGPA should not be provided when selecting percentage for qualification " + qualification.getQualification_name());
                    }
                    if (dto.getPercentage() == null) {
                        throw new IllegalArgumentException("Percentage is required when 'isPercentage' is true for qualification " + qualification.getQualification_name());
                    }
                    double percentage = dto.getPercentage();
                    if (percentage < 0 || percentage > 100) {
                        throw new IllegalArgumentException("Percentage must be between 0 and 100 for qualification " + qualification.getQualification_name());
                    }
                } else {
                    // isPercentage == false: CGPA is optional, but percentage must NOT be provided
                    if (dto.getPercentage() != null) {
                        throw new IllegalArgumentException("Percentage should not be provided when selecting CGPA for qualification " + qualification.getQualification_name());
                    }
                    // CGPA can be provided or left null
                }
            }
        }
    }

    private void validateQualificationIds(QualificationEligibilityDto dto) {
        if (dto.getQualificationIds() == null) {
            throw new IllegalArgumentException("Qualification cannot be null");
        }

        if (dto.getQualificationIds().isEmpty()) {
            throw new IllegalArgumentException("Qualification cannot be empty");
        }

        if (dto.getQualificationIds().size() > 1) {
            throw new IllegalArgumentException("Enter only one qualification (Highest)");
        }

        Set<Integer> qualificationIdSet = new HashSet<>();
        List<Integer> qualificationIds = dto.getQualificationIds();
        for (Integer qualificationId : qualificationIds) {
            Qualification qualification = entityManager.find(Qualification.class, qualificationId);
            if (qualification == null) {
                throw new IllegalArgumentException("Qualification with id " + qualificationId + " does not exist");
            }
            qualificationIdSet.add(qualificationId);
        }

        if (qualificationIdSet.size() != qualificationIds.size()) {
            throw new IllegalArgumentException("DUPLICATE QUALIFICATION NOT ALLOWED");
        }
    }

    private void validateStreamsWithMapping(QualificationEligibilityDto dto, Qualification qualification) {
        if (dto.getCustomStreamIds() != null && !dto.getCustomStreamIds().isEmpty()) {
            // Check if qualification requires streams
            if (!qualification.getIs_stream_required()) {
                if(qualification.getQualification_id().equals(MATRICULATION_QUALIFICATION))
                {
                    dto.setCustomStreamIds(List.of(MATRICULATION_IMPLICIT_STREAM_ID));
                }
                else {
                    throw new IllegalArgumentException("Stream is not required for qualification: " + qualification.getQualification_name());
                }
            }

            // Get valid streams for this qualification
            List<CustomStream> validStreams = streamService.getStreamByQualificationId(qualification.getQualification_id());
            Set<Long> validStreamIds = validStreams.stream()
                    .map(CustomStream::getStreamId)
                    .collect(Collectors.toSet());

            Set<Long> streamIdSet = new HashSet<>();
            List<Long> streamIds = dto.getCustomStreamIds();

            for (Long streamId : streamIds) {
                // Check if stream exists
                CustomStream customStream = entityManager.find(CustomStream.class, streamId);
                if (customStream == null) {
                    throw new IllegalArgumentException("Stream with id " + streamId + " does not exist");
                }

                // Check if stream is valid for this qualification (except for "Others" stream ID 215)
                if (streamId != 215 && !validStreamIds.contains(streamId)) {
                    throw new IllegalArgumentException("Stream '" + customStream.getStreamName() +
                            "' is not valid for qualification '" + qualification.getQualification_name() + "'");
                }

                streamIdSet.add(streamId);
            }

            if (streamIdSet.size() != streamIds.size()) {
                throw new IllegalArgumentException("DUPLICATE STREAMS NOT ALLOWED");
            }
        }
    }

    private void validateSubjectsWithMapping(QualificationEligibilityDto dto, Qualification qualification) {
        Integer qualificationId = qualification.getQualification_id();

        // For qualifications 1 and 2, use predefined subjects
        if (qualificationId == 1 || qualificationId == 2) {
            if (dto.getCustomSubjectIds() != null && !dto.getCustomSubjectIds().isEmpty()) {
                // Check if qualification requires subjects
                if (!qualification.getIs_subjects_required()) {
                    throw new IllegalArgumentException("Subjects are not required for qualification: " + qualification.getQualification_name());
                }

                // Get valid subjects based on selected streams
                Set<Long> validSubjectIds = new HashSet<>();
                if (dto.getCustomStreamIds() != null && !dto.getCustomStreamIds().isEmpty()) {
                    for (Long streamId : dto.getCustomStreamIds()) {
                        List<CustomSubject> streamSubjects = subjectService.getSubjectsByStreamIds(streamId);
                        validSubjectIds.addAll(streamSubjects.stream()
                                .map(CustomSubject::getSubjectId)
                                .collect(Collectors.toSet()));
                    }
                }

                Set<Long> subjectIdsSet = new HashSet<>();
                List<Long> subjectIds = dto.getCustomSubjectIds();

                for (Long subjectId : subjectIds) {
                    CustomSubject customSubject = entityManager.find(CustomSubject.class, subjectId);
                    if (customSubject == null) {
                        throw new IllegalArgumentException("Subject with id " + subjectId + " does not exist");
                    }

                    // Check if subject is valid for selected streams (except for "Others" subject ID 54)
                    if (subjectId != 54 && !validSubjectIds.isEmpty() && !validSubjectIds.contains(subjectId)) {
                        throw new IllegalArgumentException("Subject '" + customSubject.getSubjectName() +
                                "' is not valid for the selected stream(s)");
                    }

                    subjectIdsSet.add(subjectId);
                }

                if (subjectIdsSet.size() != subjectIds.size()) {
                    throw new IllegalArgumentException("DUPLICATE SUBJECTS NOT ALLOWED");
                }
            }

            // For qualifications 1 and 2, manual subject names should not be used
            if (dto.getHighestQualificationSubjectNames() != null && !dto.getHighestQualificationSubjectNames().isEmpty()) {
                throw new IllegalArgumentException("Manual subject names are not allowed for qualification: " + qualification.getQualification_name() +
                        ". Please select from predefined subjects.");
            }
        }
        else {
            // For other qualifications, use manual subject names
            if (dto.getCustomSubjectIds() != null && !dto.getCustomSubjectIds().isEmpty()) {
                throw new IllegalArgumentException("Predefined subjects are not allowed for qualification: " + qualification.getQualification_name() +
                        ". Please use manual subject names.");
            }

            // Validate manual subject names if qualification requires subjects
            if (qualification.getIs_subjects_required()) {
                Set<String> subjectNameSet = new HashSet<>();
                for (String subjectName : dto.getHighestQualificationSubjectNames()) {
                    if (subjectName == null || subjectName.trim().isEmpty()) {
                        throw new IllegalArgumentException("Subject name cannot be empty");
                    }

                    if (!subjectName.matches("^[a-zA-Z0-9 ,.!?';:()&-]*$")) {
                        throw new IllegalArgumentException("Invalid subject name format: " + subjectName);
                    }

                    if (!subjectNameSet.add(subjectName.trim().toLowerCase())) {
                        throw new IllegalArgumentException("Duplicate subject name found: " + subjectName);
                    }
                }
            }
        }
    }

    private void validatePercentageRange(QualificationEligibilityDto dto) {
        if (dto.getPercentage() != null) {
            if (dto.getPercentage() > 100 || dto.getPercentage() < 0) {
                throw new IllegalArgumentException("Percentage cannot be less than 0 and greater than 100");
            }
        }
    }

    public void validateDistrictStateRelationship(StateDistributionDto stateDistribution) {
        if (!Boolean.TRUE.equals(stateDistribution.getIsDistrictDistribution())) {
            return;
        }

        // Get state code using EntityManager
        StateCode stateCode = entityManager.find(StateCode.class, stateDistribution.getStateCodeId());
        if (stateCode == null) {
            throw new IllegalArgumentException("Invalid state code: " + stateDistribution.getStateCodeId());
        }

        List<DistrictDistributionDto> districtDistributions = stateDistribution.getDistrictDistributions();
        if (districtDistributions == null || districtDistributions.isEmpty()) {
            throw new IllegalArgumentException("District distributions are required when isDistrictDistribution is true");
        }

        // Get all districts for this state
        List<Districts> stateDistricts = districtService.findDistrictsByStateCode(stateCode.getState_code(),false);
        Set<Integer> validDistrictIds = stateDistricts.stream()
                .map(Districts::getDistrict_id)
                .collect(Collectors.toSet());

        // Validate each district in the distribution
        for (DistrictDistributionDto districtDto : districtDistributions) {
            if (!validDistrictIds.contains(districtDto.getDistrictId().intValue())) {
                // Find the actual state code for this district if it exists
                Districts district = entityManager.find(Districts.class, districtDto.getDistrictId().intValue());
                if (district == null) {
                    throw new IllegalArgumentException("District not found with id: " + districtDto.getDistrictId());
                }
                if(district.getDistrict_id()!=786)
                    throw new IllegalArgumentException(
                        String.format("District with ID %d belongs to state %s, not state %s",
                                districtDto.getDistrictId(), district.getState_code(), stateCode.getState_code()));
            }
        }
    }

    public boolean validatePostRequirement(AddProductDto addProductDto, Integer roleId,Long userId) throws Exception {
        List<PostDto> postDtos = addProductDto.getPosts();

        if(addProductDto.getIsMultiplePostSameFee()!=null)
        {
            if(!Boolean.TRUE.equals(addProductDto.getIsMultiplePostSameFee()))
            {
                if(postDtos.size()>1)
                {
                    throw new IllegalArgumentException("Only one post can be saved because multiple posts of this product does not have same fees");
                }
            }
        }

        for (PostDto postDto : postDtos) {
            validatePostBasics(postDto);
            validateVacancyDistribution(postDto);
            // Validate vacancy distribution only if distribution types are present
            List<Integer> distributionTypes = postDto.getVacancyDistributionTypeIds();
            if (distributionTypes != null && !distributionTypes.isEmpty()) {
                if (distributionTypes.contains(1)) {
                    validateStateDistribution(postDto);
                } else if (distributionTypes.contains(2)) {
                    validateZoneDistribution(postDto);
                } else if (distributionTypes.contains(3)) {
                    validateGenderDistribution(postDto, postDto.getGenderWiseDistribution());
                }
                  else if(distributionTypes.contains(4))
                {
                    validateOtherVacancyDistribution(postDto);
                }
            }
            if(postDto.getPhysicalRequirements()!=null)
            {
                validatePhysicalRequirement(postDto, null);
            }
            if (postDto.getQualificationEligibility() != null && !postDto.getQualificationEligibility().isEmpty()) {
                qualificationGroupService.validateQualificationGroups(postDto);}
        }
        return true;
    }
    private void validatePostBasics(PostDto postDto) {
        /*if (postDto.getPostName() == null || postDto.getPostName().trim().isEmpty()) {
            throw new IllegalArgumentException("Post name cannot be null or empty");
        }*/
       /* if(postDto.getDuration()<0)
            throw new IllegalArgumentException("Post duration cannot be < 0");*/
      /*  if (!postDto.getPostName().matches("^[a-zA-Z0-9/_\\-(),.\"' \\[\\]{}]*$")) {
            throw new IllegalArgumentException("Post name can only contain alphanumeric values, /_-(),.\"' []{}, and cannot have leading spaces.");
        }*/
        if(postDto.getIncome()!=null)
        {
            if (postDto.getIncome()<0)
                throw new IllegalArgumentException("Income threshold cannot be less than 0");
        }
        if(postDto.getReligion()!=null) {
            for (String religion : postDto.getReligion()) {
                if (!religion.matches("^[a-zA-Z\\s]+$")) {
                    throw new IllegalArgumentException("Invalid religion name: " + religion + ". Only alphabets and spaces are allowed.");
                }
            }
        }
     /*   if (postDto.getPostTotalVacancies() == null || postDto.getPostTotalVacancies() < 0) {
            throw new IllegalArgumentException("Invalid Post Total Vacancies");
        }*/
    }

    private void validateVacancyDistribution(PostDto postDto) {
        List<Integer> vacancyDistributionTypeIds = postDto.getVacancyDistributionTypeIds();
        Long postTotalVacancies = postDto.getPostTotalVacancies();
        GenderDistributionDto genderDistributionDto = postDto.getGenderWiseDistribution();

        // Case: No distribution type selected (empty or null list)
        if (vacancyDistributionTypeIds == null || vacancyDistributionTypeIds.isEmpty()) {
            if ((postDto.getStateDistributions() != null && !postDto.getStateDistributions().isEmpty()) || (postDto.getZoneDistributions() != null && !postDto.getZoneDistributions().isEmpty()) || (postDto.getGenderWiseDistribution() != null && !isDtoEmpty(postDto.getGenderWiseDistribution()))) {
                throw new IllegalArgumentException("No any distribution can be given if vacancy Distribution Type Id is null or empty");
            }
        }

        if(vacancyDistributionTypeIds!=null )
        {
            if (vacancyDistributionTypeIds.size() > 1) {
                throw new IllegalArgumentException("Exactly one vacancy distribution type is required.");
            }
            if(!vacancyDistributionTypeIds.isEmpty())
            {
                int distributionTypeId = vacancyDistributionTypeIds.get(0);
                switch (distributionTypeId) {
                    case 1:
                        validateStatesDistribution(postDto.getStateDistributions(), postTotalVacancies);
                        break;
                    case 2:
                        validateZonesDistribution(postDto.getZoneDistributions(), postTotalVacancies);
                        break;
                    case 3:
                        validateGenderDistribution(postDto, genderDistributionDto);
                        break;
                    case 4:
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid vacancy distribution type: " + distributionTypeId);
                }
            }
        }
    }

    private void validateStatesDistribution(List<StateDistributionDto> stateDistributions, Long postTotalVacancies) {
        if (stateDistributions == null || stateDistributions.isEmpty()) {
            throw new IllegalArgumentException("State distributions are required");
        }
        long totalStateVacancies = 0;
        for (StateDistributionDto state : stateDistributions) {
            long stateVacancies = validateStateDistribution(state);
            totalStateVacancies += stateVacancies;
        }

        if (totalStateVacancies != postTotalVacancies) {
            throw new IllegalArgumentException(
                    String.format("Total state vacancies (%d) must equal post total vacancies (%d)",
                            totalStateVacancies, postTotalVacancies));
        }
        if(totalStateVacancies<0)
            throw new IllegalArgumentException("Total state vacancies cannot be < 0");
    }

    private long validateStateDistribution(StateDistributionDto state) {
        if (state.getStateCodeId() == null) {
            throw new IllegalArgumentException("State code ID is required");
        }

        if (Boolean.TRUE.equals(state.getIsDistrictDistribution())) {
            return validateDistrictBasedState(state);
        } else {
            return validateNonDistrictBasedState(state);
        }
    }

    private long validateDistrictBasedState(StateDistributionDto state) {
        // For district-based distribution, state level gender fields are not required
        if (state.getDistrictDistributions() == null || state.getDistrictDistributions().isEmpty()) {
            throw new IllegalArgumentException("District distributions are required when isDistrictDistribution is true");
        }

        long totalDistrictVacancies = 0;
        for (DistrictDistributionDto district : state.getDistrictDistributions()) {
            long districtVacancies = validateDistrictDistribution(district);
            totalDistrictVacancies += districtVacancies;
        }

        return totalDistrictVacancies;
    }

    private long validateDistrictDistribution(DistrictDistributionDto district) {
        if (district.getDistrictId() == null) {
            throw new IllegalArgumentException("District ID is required");
        }
      /*  long sum=0;
        for(DistrictCategoryDistributionDto categoryDistributionDto:district.getCategoryDistributions())
        {
          sum=sum+categoryDistributionDto.getTotalVacancy();
          if(categoryDistributionDto.getMaleVacancy()+categoryDistributionDto.getFemaleVacancy()!=categoryDistributionDto.getTotalVacancy())
          {
              throw new IllegalArgumentException("Sum of Male vacancy and female vacancy should be equal to total for the category id :"+categoryDistributionDto.getCategoryId());
          }
        }
        if(sum!=district.getTotalVacancy())
            throw new IllegalArgumentException("Total vacancies for distribution must be equal to sum of distribution of categories");*/

        if (Boolean.TRUE.equals(district.getIsGenderWise())) {
            return validateGenderWiseDistrict(district);
        } else {
            return validateNonGenderWiseDistrict(district);
        }
    }

    private long validateGenderWiseDistrict(DistrictDistributionDto district) {
        if (district.getMaleVacancy() == null || district.getFemaleVacancy() == null) {
            throw new IllegalArgumentException("Male and female vacancies are required for gender-wise district distribution");
        }

        long totalGenderVacancies = district.getMaleVacancy() + district.getFemaleVacancy();

        if (!district.getCategoryDistributions().isEmpty()) {
            long categorySum = district.getCategoryDistributions().stream()
                    .mapToLong(DistrictCategoryDistributionDto::getVacancyCount)
                    .sum();

            if (categorySum != totalGenderVacancies) {
                throw new IllegalArgumentException(
                        String.format("Category total (%d) must equal gender total (%d) for district %d",
                                categorySum, totalGenderVacancies, district.getDistrictId()));
            }
        }

        return totalGenderVacancies;
    }

    private long validateNonGenderWiseDistrict(DistrictDistributionDto district) {
        if (!district.getCategoryDistributions().isEmpty()) {
            return district.getCategoryDistributions().stream()
                    .mapToLong(DistrictCategoryDistributionDto::getVacancyCount)
                    .sum();
        } else {
            if (district.getTotalVacancy() == null) {
                throw new IllegalArgumentException(
                        "Total vacancy is required for non-gender-wise district without category distribution");
            }
            return district.getTotalVacancy();
        }
    }

    private long validateNonDistrictBasedState(StateDistributionDto state) {
        if (Boolean.TRUE.equals(state.getIsGenderWise())) {
            return validateGenderWiseState(state);
        } else {
            return validateNonGenderWiseState(state);
        }
    }

    private long validateGenderWiseState(StateDistributionDto state) {
        if (state.getMaleVacancy() == null || state.getFemaleVacancy() == null) {
            throw new IllegalArgumentException("Male and female vacancies are required for gender-wise state distribution");
        }

        long totalGenderVacancies = state.getMaleVacancy() + state.getFemaleVacancy();

        if (!state.getCategoryDistributions().isEmpty()) {
            long categorySum = state.getCategoryDistributions().stream()
                    .mapToLong(CategoryDistributionDto::getVacancyCount)
                    .sum();

            if (categorySum != totalGenderVacancies) {
                throw new IllegalArgumentException(
                        String.format("Category total (%d) must equal gender total (%d) for state %d",
                                categorySum, totalGenderVacancies, state.getStateCodeId()));
            }
        }

        return totalGenderVacancies;
    }

    private long validateNonGenderWiseState(StateDistributionDto state) {
        if (!state.getCategoryDistributions().isEmpty()) {
            return state.getCategoryDistributions().stream()
                    .mapToLong(CategoryDistributionDto::getVacancyCount)
                    .sum();
        } else {
            if (state.getTotalVacanciesInState() == null) {
                throw new IllegalArgumentException(
                        "Total vacancies is required for non-gender-wise state without category distribution");
            }
            return state.getTotalVacanciesInState();
        }
    }

    public void validateZoneDistributionRelationship(ZoneDistributionDto zoneDistribution) {
        // Skip validation if not division distribution
        if (!Boolean.TRUE.equals(zoneDistribution.getIsDivisionDistribution())) {
            return;
        }

        if (zoneDistribution.getZoneId() == null) {
            throw new IllegalArgumentException("Zone ID is required for validation.");
        }

        // Get all valid division IDs for this zone
        List<DivisionProjectionDTO> validDivisionIds;
        try {
            validDivisionIds = zoneDivisionService.getDivisionsByZoneId(zoneDistribution.getZoneId());
        } catch (NotFoundException e) {
            throw new IllegalArgumentException("Invalid zone ID: " + zoneDistribution.getZoneId(), e);
        }

        // Validate division distributions
        List<DivisionDistributionDto> divisionDistributions = zoneDistribution.getDivisionDistributions();
        if (divisionDistributions == null || divisionDistributions.isEmpty()) {
            throw new IllegalArgumentException(
                    "Division distributions are required when isDivisionDistribution is true");
        }

        // Validate each division ID belongs to the zone
        for (DivisionDistributionDto divisionDto : divisionDistributions) {
            if (divisionDto.getDivisionId() == null) {
                throw new IllegalArgumentException("Division ID cannot be null");
            }
            List<Integer>ids=new ArrayList<>();
            for(DivisionProjectionDTO dto :validDivisionIds)
            {
                ids.add(dto.getDivisionId());
            }

            if (!ids.contains(divisionDto.getDivisionId().intValue())) {
                throw new IllegalArgumentException(
                        String.format("Division ID %d is not associated with Zone ID %d",
                                divisionDto.getDivisionId(), zoneDistribution.getZoneId()));
            }
        }

    }

    private void validateZonesDistribution(List<ZoneDistributionDto> zoneDistributions, Long postTotalVacancies) {
        if (zoneDistributions == null || zoneDistributions.isEmpty()) {
            throw new IllegalArgumentException("Zone distributions are required");
        }

        long totalZoneVacancies = 0;
        for (ZoneDistributionDto zone : zoneDistributions) {
            long zoneVacancies = validateZoneDistribution(zone);
            totalZoneVacancies += zoneVacancies;
        }

        if (totalZoneVacancies != postTotalVacancies) {
            throw new IllegalArgumentException(
                    String.format("Total zone vacancies (%d) must equal post total vacancies (%d)",
                            totalZoneVacancies, postTotalVacancies));
        }
    }

    private long validateZoneDistribution(ZoneDistributionDto zone) {
        if (zone.getZoneId() == null) {
            throw new IllegalArgumentException("Zone ID is required");
        }

        if (Boolean.TRUE.equals(zone.getIsDivisionDistribution())) {
            return validateDivisionBasedZone(zone);
        } else {
            return validateNonDivisionBasedZone(zone);
        }
    }

    private long validateDivisionBasedZone(ZoneDistributionDto zone) {
        if (zone.getDivisionDistributions() == null || zone.getDivisionDistributions().isEmpty()) {
            throw new IllegalArgumentException("Division distributions are required when isDivisionDistribution is true");
        }

        long totalDivisionVacancies = 0;

        for (DivisionDistributionDto division : zone.getDivisionDistributions()) {
            // Validate each division and get its total
            long divisionVacancies = validateDivisionDistribution(division);
            totalDivisionVacancies += divisionVacancies;

            // Extra validation only if gender-wise
            if (Boolean.TRUE.equals(division.getIsGenderWise())) {
                long male = division.getMaleVacancy() != null ? division.getMaleVacancy() : 0;
                long female = division.getFemaleVacancy() != null ? division.getFemaleVacancy() : 0;

                if (!division.getCategoryDistributions().isEmpty()) {
                    long sumOfCat = 0;
                    for (DivisionCategoryDistributionDto cat : division.getCategoryDistributions()) {
                        if ((cat.getMaleVacancy() != null || cat.getFemaleVacancy() != null) && cat.getTotalVacancy() == null) {
                            throw new IllegalArgumentException("Need to provide total vacancy for category");
                        }

                        if (cat.getFemaleVacancy() == null) cat.setFemaleVacancy(0L);
                        if (cat.getMaleVacancy() == null) cat.setMaleVacancy(0L);

                        if (cat.getMaleVacancy() < 0 || cat.getFemaleVacancy() < 0)
                            throw new IllegalArgumentException("Gender-wise vacancy cannot be negative");

                        if (cat.getTotalVacancy() < 0)
                            throw new IllegalArgumentException("Total vacancy cannot be < 0");

                        sumOfCat += cat.getMaleVacancy() + cat.getFemaleVacancy();
                    }

                    if (sumOfCat != male + female) {
                        throw new IllegalArgumentException("Category male and female vacancy should be equal to total vacancy of that division");
                    }
                }
            }
        }

        // Check zone total (optional if you already check at top level)
        if (zone.getTotalVacanciesInZone() != null && totalDivisionVacancies != zone.getTotalVacanciesInZone()) {
            throw new IllegalArgumentException("Total of division vacancies does not match zone total vacancies");
        }

        return totalDivisionVacancies;
    }

    private long validateDivisionDistribution(DivisionDistributionDto division) {
        if (division.getDivisionId() == null) {
            throw new IllegalArgumentException("Division ID is required");
        }
        if(division.getDivisionId()!=37&&division.getDivisionRunningField()!=null)
        {
            throw new IllegalArgumentException("Cannot add running field for zone except OTHERS");
        }

        else if(division.getDivisionId()==37&&(division.getDivisionRunningField()==null||division.getDivisionRunningField().trim().isEmpty()))
        {
            throw new IllegalArgumentException("Need running field when selecting others for Division");
        }
        if (Boolean.TRUE.equals(division.getIsGenderWise())) {
            return validateGenderWiseDivision(division);
        } else {
            return validateNonGenderWiseDivision(division);
        }
    }

    private long validateGenderWiseDivision(DivisionDistributionDto division) {
        if (division.getMaleVacancy() == null || division.getFemaleVacancy() == null) {
            throw new IllegalArgumentException("Male and female vacancies are required for gender-wise division distribution");
        }

        long totalGenderVacancies = division.getMaleVacancy() + division.getFemaleVacancy();

        if (!division.getCategoryDistributions().isEmpty()) {
            long categorySum = division.getCategoryDistributions().stream()
                    .mapToLong(DivisionCategoryDistributionDto::getVacancyCount)
                    .sum();

            if (categorySum != totalGenderVacancies) {
                throw new IllegalArgumentException(
                        String.format("Category total (%d) must equal gender total (%d) for division %d",
                                categorySum, totalGenderVacancies, division.getDivisionId()));
            }
        }
        return totalGenderVacancies;
    }

    private long validateNonGenderWiseDivision(DivisionDistributionDto division) {
        if (!division.getCategoryDistributions().isEmpty()) {
            return division.getCategoryDistributions().stream()
                    .mapToLong(dto -> dto.getVacancyCount() != null ? dto.getVacancyCount() : 0L)
                    .sum();
        } else {
            if (division.getTotalVacancy() == null) {
                throw new IllegalArgumentException(
                        "Total vacancy is required for non-gender-wise division without category distribution");
            }
            return division.getTotalVacancy();
        }
    }

    private long validateNonDivisionBasedZone(ZoneDistributionDto zone) {
        if (Boolean.TRUE.equals(zone.getIsGenderWise())) {
            return validateGenderWiseZone(zone);
        } else {
            return validateNonGenderWiseZone(zone);
        }
    }

    private long validateGenderWiseZone(ZoneDistributionDto zone) {
        if (zone.getMaleVacancy() == null || zone.getFemaleVacancy() == null) {
            throw new IllegalArgumentException("Male and female vacancies are required for gender-wise zone distribution");
        }

        int totalGenderVacancies = zone.getMaleVacancy() + zone.getFemaleVacancy();

        if (!zone.getCategoryDistributions().isEmpty()) {
            int categorySum = zone.getCategoryDistributions().stream()
                    .mapToInt(CategoryDistributionDto::getVacancyCount)
                    .sum();

            if (categorySum != totalGenderVacancies) {
                throw new IllegalArgumentException(
                        String.format("Category total (%d) must equal gender total (%d) for zone %d",
                                categorySum, totalGenderVacancies, zone.getZoneId()));
            }
        }
        return totalGenderVacancies;
    }

    private long validateNonGenderWiseZone(ZoneDistributionDto zone) {
        if (!zone.getCategoryDistributions().isEmpty()) {
            return zone.getCategoryDistributions().stream()
                    .mapToLong(CategoryDistributionDto::getVacancyCount)
                    .sum();
        } else {
            if (zone.getTotalVacanciesInZone() == null) {
                throw new IllegalArgumentException(
                        "Total vacancies is required for non-gender-wise zone without category distribution");
            }
            return zone.getTotalVacanciesInZone();
        }
    }


    private void validateCategoryDistributions(List<CategoryDistributionDto> categoryDistributions, Long totalVacancy) {
        Long categoryVacancySum = categoryDistributions.stream()
                .filter(category -> category.getVacancyCount() != null)  // Ensure no null categoryVacancies
                .mapToLong(CategoryDistributionDto::getVacancyCount)
                .sum();

        if (!categoryVacancySum.equals(totalVacancy)) {
            throw new IllegalArgumentException("Sum of category vacancies must equal the post total vacancies.");
        }

        for (CategoryDistributionDto categoryDistribution : categoryDistributions) {

            if(categoryDistribution.getIsStateLevelCategory()==null)
            {
                throw new IllegalArgumentException("isStateLevelCategory cannot be null");
            }
            if(categoryDistribution.getIsStateLevelCategory().equals(false))
            {
                if (categoryDistribution.getCategoryId() == null || categoryDistribution.getVacancyCount() == null) {
                    throw new IllegalArgumentException("Category ID and vacancies must be provided for each category if isStateLevelCategory is false.");
                }
            }

            if(categoryDistribution.getIsStateLevelCategory().equals(true))
            {
                if(categoryDistribution.getStateLevelCategory()==null || categoryDistribution.getStateLevelCategory().trim().isEmpty())
                {
                    throw new IllegalArgumentException("State level category cannot be empty or null if isStateLevelCategory is true");
                }
                if (!categoryDistribution.getStateLevelCategory().matches("^[a-zA-Z0-9 ]*$")) {
                    throw new IllegalArgumentException("Only alphanumeric characters are allowed in state category");
                }
                if(categoryDistribution.getStateId()==null)
                {
                    throw new IllegalArgumentException("Provide the state for which you are adding state level category");
                }
                if (categoryDistribution.getStateId() <= 0) {
                    throw new IllegalArgumentException("state id cannot be negative or equal to 0");
                }
            }
            if(categoryDistribution.getIsGenderWise()==null)
            {
                throw new IllegalArgumentException("You have to provide if isGenderWise true or false in category");
            }
            if(categoryDistribution.getIsGenderWise().equals(true))
            {
                if(categoryDistribution.getMaleVacancy()==null)
                {
                    throw new IllegalArgumentException("You have to provide male vacancy in category if the isGenderWise is true for that category");
                }
                if(categoryDistribution.getFemaleVacancy()==null)
                {
                    throw new IllegalArgumentException("You have to provide female vacancy in category if the isGenderWise is true for that category");
                }
                if(categoryDistribution.getMaleVacancy()<0)
                    throw new IllegalArgumentException("Male vacancies cannot be <0");
                else if(categoryDistribution.getFemaleVacancy()<0)
                    throw new IllegalArgumentException("Female vacancies cannot be <0");

                if(categoryDistribution.getVacancyCount()!= categoryDistribution.getMaleVacancy()+ categoryDistribution.getFemaleVacancy())
                {
                    throw new IllegalArgumentException("Category vacancies is not equal to sum of male vacancies and female vacancies");
                }
            }
        }
    }

    private void validateStateDistribution(PostDto postDto) {
        if (postDto.getStateDistributions() == null || postDto.getStateDistributions().isEmpty()) {
            throw new IllegalArgumentException("You have to distribute the vacancies State-wise");
        }
        if (postDto.getZoneDistributions() != null && !postDto.getZoneDistributions().isEmpty()) {
            throw new IllegalArgumentException("You cannot distribute vacancies Zone wise");
        }
        if (postDto.getOtherDistributions() != null && !postDto.getOtherDistributions().isEmpty()) {
            throw new IllegalArgumentException("You cannot give other distributions");
        }
        if (postDto.getGenderWiseDistribution() != null && !isDtoEmpty(postDto.getGenderWiseDistribution()) ) {
            throw new IllegalArgumentException("You cannot distribute vacancies Gender wise");
        }
        for (StateDistributionDto stateDistribution : postDto.getStateDistributions()) {
            validateDistrictStateRelationship(stateDistribution);
            long sum = 0;
            long f=0;
            long m=0;
            if (stateDistribution.getCategoryDistributions() != null&&!stateDistribution.getCategoryDistributions().isEmpty()) {
                for (CategoryDistributionDto categoryDistributionDto : stateDistribution.getCategoryDistributions()) {

                    if(stateDistribution.getIsGenderWise())
                    {
                        if(categoryDistributionDto.getMaleVacancy()==null&&stateDistribution.getIsGenderWise())
                            throw new IllegalArgumentException("Male vacancy is not given");
                        if(categoryDistributionDto.getFemaleVacancy()==null&&stateDistribution.getIsGenderWise())
                            throw new IllegalArgumentException("Female vacancy is not given");
                        if(categoryDistributionDto.getTotalVacancy()==null&&stateDistribution.getIsGenderWise())
                            throw new IllegalArgumentException("Total vacancy is not given");
                        sum+=categoryDistributionDto.getTotalVacancy();
                        f+=categoryDistributionDto.getFemaleVacancy();
                        m+=categoryDistributionDto.getMaleVacancy();
                        if (categoryDistributionDto.getTotalVacancy()!= categoryDistributionDto.getFemaleVacancy() + categoryDistributionDto.getMaleVacancy()) {
                            throw new IllegalArgumentException("female vacancy +male vacancy for category is not equal to total");
                        }
                    }

                }
                if(stateDistribution.getIsGenderWise())
                {
                    if(f!=stateDistribution.getFemaleVacancy())
                        throw new IllegalArgumentException("Total category female vacancies not equal to total female vacancy in state");
                    if(m!=stateDistribution.getMaleVacancy())
                        throw new IllegalArgumentException("Total category male vacancies not equal to total male vacancy in state");
                    if (stateDistribution.getMaleVacancy() + stateDistribution.getFemaleVacancy() != sum)
                        throw new IllegalArgumentException("Total vacancy sum for state is not equal to the sum of vacancies in category wise distribution");
                }
            }
        }
    }

    private void validateZoneDistribution(PostDto postDto) {
        if (postDto.getZoneDistributions() == null || postDto.getZoneDistributions().isEmpty()) {
            throw new IllegalArgumentException("You have to distribute the vacancies Zone-wise");
        }

        if (postDto.getStateDistributions() != null && !postDto.getStateDistributions().isEmpty()) {
            throw new IllegalArgumentException("You cannot distribute vacancies State wise");
        }
        if (postDto.getOtherDistributions() != null && !postDto.getOtherDistributions().isEmpty()) {
            throw new IllegalArgumentException("You cannot give other distributions");
        }
        if (postDto.getGenderWiseDistribution() != null && !isDtoEmpty(postDto.getGenderWiseDistribution())) {
            throw new IllegalArgumentException("You cannot distribute vacancies Gender wise");
        }
        for (ZoneDistributionDto zoneDistribution : postDto.getZoneDistributions()) {
            validateZoneDistributionRelationship(zoneDistribution);
            if(zoneDistribution.getZoneId()!=8&&zoneDistribution.getZoneRunningField()!=null)
            {
                throw new IllegalArgumentException("Cannot add running field for zone except OTHERS");
            }
            else if(zoneDistribution.getZoneId()==8&&(zoneDistribution.getZoneRunningField()==null||zoneDistribution.getZoneRunningField().trim().isEmpty()))
            {
                throw new IllegalArgumentException("Running field for zone is required when selecting OTHERS");
            }
            if(zoneDistribution.getFemaleVacancy()!=null&&zoneDistribution.getFemaleVacancy()<0)
                throw new IllegalArgumentException("Female vacancy in zone cannot be < 0");
            if(zoneDistribution.getMaleVacancy()!=null&&zoneDistribution.getMaleVacancy()<0)
                throw new IllegalArgumentException("Male vacancy in zone cannot be < 0");
            if(zoneDistribution.getTotalVacanciesInZone()!=null&&zoneDistribution.getTotalVacanciesInZone()<0)
                throw new IllegalArgumentException("Total vacancy in zone cannot be < 0");
        }
    }

    private void validateGenderDistribution(PostDto postDto, GenderDistributionDto genderDto) {
        // First validate basic gender distribution
        if (postDto.getZoneDistributions() != null && !postDto.getZoneDistributions().isEmpty()) {
            throw new IllegalArgumentException("You cannot distribute vacancies Zone wise");
        }
        if (postDto.getStateDistributions() != null && !postDto.getStateDistributions().isEmpty()) {
            throw new IllegalArgumentException("You cannot distribute vacancies State wise");
        }
        if (postDto.getOtherDistributions() != null && !postDto.getOtherDistributions().isEmpty()) {
            throw new IllegalArgumentException("You cannot give other distributions");
        }
        // Additional validation for category distributions when type is 3
        List<CategoryDistributionDto> categoryDtos = genderDto.getCategoryDistributionDtos();
        if (categoryDtos == null || categoryDtos.isEmpty()) {
            throw new IllegalArgumentException("Category distributions are required when distribution type is 3");
        }
//        validateBasicGenderDistribution(postDto, genderDto);

        // Validate category distributions match total
//        Long totalVacancy = genderDto.getTotalVacancy();
//        if(totalVacancy==null)
//        {
//            totalVacancy= postDto.getPostTotalVacancies();
//        }
        validateCategoryDistributions(categoryDtos, postDto.getPostTotalVacancies());
    }

    private void validateBasicGenderDistribution(PostDto postDto, GenderDistributionDto genderDto) {
        if (genderDto == null) {
            throw new IllegalArgumentException("Gender distribution data must be provided");
        } if (postDto.getZoneDistributions() != null && !postDto.getZoneDistributions().isEmpty()) {
            throw new IllegalArgumentException("You cannot distribute vacancies Zone wise");
        }
        if (postDto.getStateDistributions() != null && !postDto.getStateDistributions().isEmpty()) {
            throw new IllegalArgumentException("You cannot distribute vacancies State wise");
        }
        if (postDto.getOtherDistributions() != null && !postDto.getOtherDistributions().isEmpty()) {
            throw new IllegalArgumentException("You cannot give other distributions");
        }
        Long postTotalVacancies = postDto.getPostTotalVacancies();
        boolean isGenderWise = Boolean.TRUE.equals(genderDto.getIsGenderWise());

        if (isGenderWise && (genderDto.getCategoryDistributionDtos()!=null&& !genderDto.getCategoryDistributionDtos().isEmpty()) ) {
            // Case 1: Gender-wise is true
            if (genderDto.getMaleVacancy() == null || genderDto.getFemaleVacancy() == null) {
                throw new IllegalArgumentException("Male and Female vacancy counts must be provided when gender-wise is enabled");
            }

            // Auto-calculate total vacancy
            Long calculatedTotalVacancy = genderDto.getMaleVacancy() + genderDto.getFemaleVacancy();
            genderDto.setTotalVacancy(calculatedTotalVacancy);

            if (!calculatedTotalVacancy.equals(postTotalVacancies)) {
                throw new IllegalArgumentException("Sum of male and female vacancies must equal post total vacancies");
            }
        } else {
            // Case 2: Gender-wise is false
            if((genderDto.getCategoryDistributionDtos()==null || genderDto.getCategoryDistributionDtos().isEmpty()) )
            {
                if (genderDto.getTotalVacancy() == null) {
                    throw new IllegalArgumentException("Total vacancy must be provided when gender-wise is disabled");
                }

                if (!genderDto.getTotalVacancy().equals(postTotalVacancies)) {
                    throw new IllegalArgumentException("Total vacancy must equal post total vacancies");
                }
            }

        }
    }

    public void validateOtherVacancyDistribution(PostDto postDto) {
        if (postDto.getZoneDistributions() != null && !postDto.getZoneDistributions().isEmpty()) {
            throw new IllegalArgumentException("You cannot distribute vacancies Zone wise");
        }
        if (postDto.getStateDistributions() != null && !postDto.getStateDistributions().isEmpty()) {
            throw new IllegalArgumentException("You cannot distribute vacancies State wise");
        }
        if(postDto.getGenderWiseDistribution()!=null&& !isDtoEmpty(postDto.getGenderWiseDistribution()))
        {
            throw new IllegalArgumentException("You cannot distribute vacancies category wise");
        }
        List<OtherDistribution> otherDistributions = postDto.getOtherDistributions();

        // Check if the list is empty
        if (otherDistributions == null || otherDistributions.isEmpty()) {
            throw new IllegalArgumentException("OtherDistribution list cannot be empty for VacancyTypeId 4.");
        }

        long totalVacanciesSum = 0L;

        // Validate each OtherDistribution in the list
        for (OtherDistribution distribution : otherDistributions) {
            if (distribution.getOtherDistributionValue() == null || distribution.getOtherDistributionValue().trim().isEmpty()) {
                throw new IllegalArgumentException("OtherDistributionValue cannot be null or empty.");
            }

            if (distribution.getTotalVacancy() == null) {
                throw new IllegalArgumentException("TotalVacancy cannot be null.");
            }

            // Add the totalVacancy to the sum
            totalVacanciesSum += distribution.getTotalVacancy();
        }

        // Check if the sum matches postTotalVacancies
        if (postDto.getPostTotalVacancies()!=null&&totalVacanciesSum != postDto.getPostTotalVacancies()) {
            throw new IllegalArgumentException("The sum of total vacancies in OtherDistributions must equal PostTotalVacancies.");
        }
    }

    private boolean isDtoEmpty(Object dto) {
        return Arrays.stream(dto.getClass().getDeclaredFields())
                .peek(field -> field.setAccessible(true))
                .allMatch(field -> {
                    try {
                        return field.get(dto) == null;
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Error accessing field value", e);
                    }
                });
    }

    public CustomSector validateSector(AddProductDto addProductDto) throws Exception {
        try {
            if (addProductDto.getSector() != null) {
                CustomSector customSector = sectorService.getSectorBySectorId(addProductDto.getSector());
                if (customSector == null) {
                    throw new IllegalArgumentException("No sector found with this id.");
                }
                return customSector;
            }
            return null;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception while validating sector: " + exception.getMessage() + "\n");
        }
    }

    public Boolean validateSelectionCriteria(AddProductDto addProductDto) throws Exception {
        try {
            if (addProductDto.getSelectionCriteria() != null) {
                addProductDto.setSelectionCriteria(addProductDto.getSelectionCriteria().trim());
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception while validating selection criteria: " + exception.getMessage() + "\n");
        }
    }

    public Advertisement validateAdvertisement(AddProductDto addProductDto) throws Exception {
        try {

            if (addProductDto.getAdvertisement() != null) {
                Advertisement advertisement = advertisementService.getAdvertisementById(addProductDto.getAdvertisement());
                if (advertisement == null) {
                    throw new IllegalArgumentException("Advertisement not found with this id.");
                }
                if ('Y' == advertisement.getArchived() ||(advertisement.getNotificationEndDate()!=null&& advertisement.getNotificationEndDate().before(new Date()))) {
                    throw new IllegalArgumentException("Advertisement is either archived or expired");
                }
                return advertisement;
            }
            return null;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception while validating advertisement: " + exception.getMessage() + "\n");
        }
    }

    public List<CustomProduct> getAllProductsByAdvertisementId (Advertisement advertisement) throws Exception {
        try {
            String sql = "SELECT c FROM CustomProduct c WHERE c.advertisement = :advertisementId";
            return entityManager.createQuery(sql, CustomProduct.class).setParameter("advertisementId", advertisement).getResultList();// Use this to simplify appending conditions
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception occured while fetching product w.r.t advertisement: " + exception.getMessage() + "\n");
        }
    }

    private boolean isSameOrFutureDate(Date dateToValidate) {
        // Strip time from both dates
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(dateToValidate);
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        // Compare only the date parts
        return !cal1.before(cal2);
    }
    public static Date stripTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }


    public void validateLastDateToPayFromForNonNullDates(CustomProduct customProduct)
    {
        if(customProduct.getIsLateDateToPayFeeNa()!=null )
        {
            if(customProduct.getIsLateDateToPayFeeNa().equals(true))
            {
                throw new IllegalArgumentException("You have to fill isLateDateToPayFeeNa as false if u want to give last date to pay fee");
            }
        }
    }
    public void validateModificationDateFromForNonNullDates(CustomProduct customProduct)
    {
        if(customProduct.getIsModificationDateFromNa()!=null )
        {
            if(customProduct.getIsModificationDateFromNa().equals(true))
            {
                throw new IllegalArgumentException("You have to fill isModificationDateFromNa as false if u want to give modification date from");
            }
        }
    }
    public void validateModificationDateToForNonNullDates(CustomProduct customProduct)
    {
        if(customProduct.getIsModificationDateToNa()!=null )
        {
            if(customProduct.getIsModificationDateToNa().equals(true))
            {
                throw new IllegalArgumentException("You have to fill isModificationDateToNa as false if u want to give modification date to");
            }
        }
    }
    public void validateAdmitDateFromForNonNullDates(CustomProduct customProduct)
    {
        if(customProduct.getIsAdmitCardDateFromNa()!=null )
        {
            if(customProduct.getIsAdmitCardDateFromNa().equals(true))
            {
                throw new IllegalArgumentException("You have to fill isAdmitCardDateFromNa as false if u want to give admit card date from");
            }
        }
    }
    public void validateAdmitCardDateToForNonNullDates(CustomProduct customProduct)
    {
        if(customProduct.getIsAdmitCardDateToNa()!=null )
        {
            if(customProduct.getIsAdmitCardDateToNa().equals(true))
            {
                throw new IllegalArgumentException("You have to fill isAdmitCardDateToNa as false if u want to give admit card date to");
            }
        }
    }
    public void validateExamDateFromForNonNullDates(CustomProduct customProduct)
    {
        if(customProduct.getIsExamDateFromNa()!=null )
        {
            if(customProduct.getIsExamDateFromNa().equals(true))
            {
                throw new IllegalArgumentException("You have to fill isExamDateFromNa as false if u want to give exam date from");
            }
        }
    }
    public void validateExamDateToForNonNullDates(CustomProduct customProduct)
    {
        if(customProduct.getIsExamDateToNa()!=null )
        {
            if(customProduct.getIsExamDateToNa().equals(true))
            {
                throw new IllegalArgumentException("You have to fill isExamDateToNa as false if u want to give exam date to");
            }
        }
    }

}