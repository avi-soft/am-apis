package com.community.api.dto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import com.community.api.component.Constant;
import com.community.api.entity.*;

import com.community.api.services.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.broadleafcommerce.common.rest.api.wrapper.APIWrapper;
import org.broadleafcommerce.common.rest.api.wrapper.BaseWrapper;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.common.persistence.Status;
import org.springframework.beans.factory.annotation.Autowired;

import static com.community.api.endpoint.avisoft.controller.Customer.CustomerEndpoint.convertStringToDate;
import static com.community.api.endpoint.avisoft.controller.product.ProductController.getPosts;


@Data
@NoArgsConstructor
public class CustomProductWrapper extends BaseWrapper implements APIWrapper<Product> {

    @Autowired
    private GenderService genderService;
    @Autowired
    private ProductReserveCategoryBornBeforeAfterRefService refService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ProductReserveCategoryFeePostRefService feeService;


    @Autowired
    private PostService postService;
    @JsonProperty("product_id")
    protected Long id;
    @JsonProperty("meta_title")
    protected String metaTitle;
    @JsonProperty("display_template")
    protected String displayTemplate;
    @JsonProperty("meta_description")
    protected String metaDescription;
    @JsonProperty("category_name")
    protected String categoryName;
    @JsonProperty("priority_level")
    protected Integer priorityLevel;
    @JsonProperty("active_start_date")
    protected Date activeStartDate;
    @JsonProperty("active_end_date")
    protected Date activeEndDate;
    @JsonProperty("go_live_date")
    protected Date activeGoLiveDate;
    @JsonProperty("rejection_comment")
    protected String rejectionComment;
    @JsonProperty("default_category_id")
    protected Long defaultCategoryId;
    @JsonProperty("archived")
    protected Character archived;
    @JsonProperty("active")
    protected Boolean active;

    @JsonProperty("reserve_category_fee")
    protected List<ReserveCategoryDto> reserveCategoryDtoList = new ArrayList<>();

    @JsonProperty("platform_fee")
    protected Double platformFee;
    @JsonProperty("state")
    protected StateCode state;
    @JsonProperty("custom_application_scope")
    protected CustomApplicationScope customApplicationScope;
    @JsonProperty("custom_product_state")
    protected CustomProductState customProductState;
    @JsonProperty("custom_rejection_status")
    protected CustomProductRejectionStatus customProductRejectionStatus;

    @JsonProperty("creator_user_id")
    protected Long creatorUserId;
    @JsonProperty("creator_role_id")
    protected Role creatorRoleId;



    @JsonProperty("modified_date")
    protected Date modifiedDate;
    @JsonProperty("order_id")
    protected Long orderId;
    @JsonProperty("domicile_required")
    protected Boolean domicileRequired;
    @JsonProperty("modifier_user_id")
    protected Long modifierUserId;
    @JsonProperty("modifier_role_id")
    protected Role modifierRoleId;
    @JsonProperty("exam_date_from")
    protected Date examDateFrom;
    @JsonProperty("exam_date_to")
    protected Date examDateTo;

    @JsonProperty("last_date_to_pay_fee")
    Date lateDateToPayFee;
    @JsonProperty("admit_card_date_from")
    Date admitCardDateFrom;
    @JsonProperty("admit_card_date_to")
    Date adminCardDateTo;
    @JsonProperty("modification_date_from")
    Date modificationDateFrom;
    @JsonProperty("modification_date_to")
    Date modificationDateTo;
    @JsonProperty("download_notification_link")
    String downloadNotificationLink;
    @JsonProperty("download_syllabus_link")
    String downloadSyllabusLink;
    @JsonProperty("form_complexity")
    Long formComplexity;
    @JsonProperty("sector")
    CustomSector customSector;
    @JsonProperty("sector_running_filed")
    protected String sectorRunningField;
    @JsonProperty("selection_criteria")
    String selectionCriteria;
    @JsonProperty("created_date")
    Date createdDate;
    @JsonProperty("is_review_required")
    Boolean isReviewRequired;
    @JsonProperty("advertisement")
    AdvertisementWrapper advertisement;
    @JsonProperty("posts")
    List<PostProjectionDTO> postDTOList=new ArrayList<>();
    @JsonProperty("is_multiple_post_same_fee")
    Boolean isMultiplePostSameFee;
    @JsonProperty("total_vacancies_in_product")
    Long totalVacanciesInProduct;
    @JsonProperty("other_info")
    String otherInfo;
    @JsonProperty("number_of_posts")
    Integer numberOfPosts;
    @JsonProperty("additional_comments")
    String additionalComments;
    @JsonProperty("answer_key_available_date")
    protected Date answerKeyAvailableDate;
    @JsonProperty("result_declaration_date")
    protected Date resultDeclarationDate;
    @JsonProperty("counselling_date")
    protected Date counsellingDate;
    @JsonProperty("tentative_document_verification_from")
    Date tentativeVerificationFrom;
    @JsonProperty("tentative_document_verification_to")
    Date tentativeVerificationTo;
    @JsonProperty("exam_center_available_date")
    Date examCenterAvailableDate;
    @JsonProperty("fee_additional_comments")
    String feeComments;
    @JsonIgnore
    protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    @JsonProperty("fee")
    Double fee;
    @JsonProperty("age_limit")
    String ageLimit;
    @JsonProperty("is_approved")
    Boolean isApproved;
    @JsonProperty("is_exam_date_from_na")
    protected Boolean isExamDateFromNa;
    @JsonProperty("is_answer_key_available_date_na")
    protected Boolean isAnswerKeyAvailableDateNa;
    @JsonProperty("is_edited")
    protected Boolean isEdited;
    @JsonProperty( "is_result_declaration_date_na")
    protected Boolean isResultDeclarationDateNa;
    @JsonProperty( "is_counselling_date_na")
    protected Boolean isCounsellingDateNa;
    @JsonProperty("is_tentative_document_verification_from_na")
    protected Boolean isTentativeVerificationFromNa;
    @JsonProperty("is_tentative_document_verification_to_na")
    protected Boolean isTentativeVerificationToNa;
    @JsonProperty( "is_exam_date_to_na")
    protected Boolean isExamDateToNa;
    @JsonProperty("is_exam_center_available_date_na")
    protected Boolean isExamCenterAvailableDateNa;
    @JsonProperty("is_last_date_to_pay_fee_na")
    protected Boolean isLateDateToPayFeeNa;
    @JsonProperty( "is_admit_card_date_from_na")
    protected Boolean isAdmitCardDateFromNa;
    @JsonProperty( "is_admit_card_date_to_na")
    protected Boolean isAdmitCardDateToNa;
    @JsonProperty("is_modification_date_from_na")
    protected Boolean isModificationDateFromNa;
    @JsonProperty("is_modification_date_to_na")
    protected Boolean isModificationDateToNa;



    public void wrapDetailsAddProduct(Product product, AddProductDto addProductDto, CustomProductState customProductState, CustomApplicationScope customApplicationScope, Long creatorUserId, Role creatorRole, ReserveCategoryService reserveCategoryService, StateCode state, CustomSector customSector, Date currentDate, Advertisement advertisement,GenderService genderService,EntityManager entityManager,List<Post> postList,List<PostDto> postDtos, Long totalVacanciesInProduct, Long totalPostsInProduct) throws Exception {

        this.id = product.getId();
        this.metaTitle = product.getMetaTitle();
        this.displayTemplate = product.getDisplayTemplate();
        this.active = product.isActive();
        this.activeGoLiveDate = addProductDto.getGoLiveDate();
        if(product.getDefaultCategory()!=null)
            this.categoryName = product.getDefaultCategory().getName();
        else
            this.categoryName=null;
        this.priorityLevel = addProductDto.getPriorityLevel();
        this.isEdited=false;
        this.archived = 'N';
        this.createdDate = currentDate;
        this.examCenterAvailableDate=addProductDto.getExamCenterAvailableDate();
        this.activeGoLiveDate = addProductDto.getGoLiveDate();
        this.activeEndDate = product.getDefaultSku().getActiveEndDate();
        this.activeStartDate = product.getDefaultSku().getActiveStartDate();
        this.metaDescription = product.getMetaDescription();
        this.resultDeclarationDate=addProductDto.getResultDeclarationDate();
        this.counsellingDate=addProductDto.getCounsellingDate();
        this.answerKeyAvailableDate=addProductDto.getAnswerKeyAvailableDate();
        this.tentativeVerificationFrom=addProductDto.getTentativeVerificationFrom();
        this.isApproved=false;
        this.tentativeVerificationTo=addProductDto.getTentativeVerificationTo();
        this.displayTemplate = product.getDisplayTemplate();
        this.sectorRunningField=addProductDto.getSectorRunningField();
        this.isReviewRequired=addProductDto.getIsReviewRequired();
        this.otherInfo=addProductDto.getOtherInfo();
        this.additionalComments=addProductDto.getAdditionalComments();
        this.feeComments=addProductDto.getFeeAdditionalComments();
        this.isExamDateFromNa=addProductDto.getIsExamDateFromNa();
        this.answerKeyAvailableDate=addProductDto.getAnswerKeyAvailableDate();
        this.isAnswerKeyAvailableDateNa=addProductDto.getIsAnswerKeyAvailableDateNa();
        this.isResultDeclarationDateNa=addProductDto.getIsResultDeclarationDateNa();
        this.resultDeclarationDate=addProductDto.getResultDeclarationDate();
        this.isCounsellingDateNa=addProductDto.getIsCounsellingDateNa();
        this.isTentativeVerificationFromNa=addProductDto.getIsTentativeVerificationFromNa();
        this.isTentativeVerificationToNa=addProductDto.getIsTentativeVerificationToNa();
        this.isExamDateToNa=addProductDto.getIsExamDateToNa();
        this.isExamCenterAvailableDateNa=addProductDto.getIsExamCenterAvailableDateNa();
        this.isLateDateToPayFeeNa=addProductDto.getIsLateDateToPayFeeNa();
        this.isAdmitCardDateFromNa=addProductDto.getIsAdmitCardDateFromNa();
        this.isAdmitCardDateToNa=addProductDto.getIsAdmitCardDateToNa();
        this.isModificationDateFromNa=addProductDto.getIsModificationDateFromNa();
        this.isModificationDateToNa=addProductDto.getIsModificationDateToNa();

        if(addProductDto.getReservedCategory()!=null)
        {
            for(int i=0; i<addProductDto.getReservedCategory().size(); i++) {

                CustomReserveCategory customReserveCategory = reserveCategoryService.getReserveCategoryById(addProductDto.getReservedCategory().get(i).reserveCategory);

                ReserveCategoryDto reserveCategoryDto = new ReserveCategoryDto();
                reserveCategoryDto.setProductId(product.getId());
                reserveCategoryDto.setReserveCategoryId(addProductDto.getReservedCategory().get(i).getReserveCategory());
                if(!addProductDto.getReservedCategory().get(i).getIsOtherOrStateCategory()) {
                    reserveCategoryDto.setReserveCategory(customReserveCategory.getReserveCategoryName());
                }
                else{
                    Long id =addProductDto.getReservedCategory().get(i).getReserveCategory();
                    if(id!=null)
                     reserveCategoryDto.setReserveCategory(customReserveCategory.getReserveCategoryName());
                    else
                        reserveCategoryDto.setReserveCategory(null);
                }


                reserveCategoryDto.setFee(addProductDto.getReservedCategory().get(i).getFee());
                reserveCategoryDto.setPost(addProductDto.getReservedCategory().get(i).getPost());
                reserveCategoryDto.setRunningField(addProductDto.getReservedCategory().get(i).getRunningField());
                reserveCategoryDto.setGenderRunningField(addProductDto.getReservedCategory().get(i).getGenderRunningField());
                reserveCategoryDto.setAdditionalComments(addProductDto.getReservedCategory().get(i).getAdditionalComment());
                /*reserveCategoryDto.setBornBefore(addProductDto.getReservedCategory().get(i).getBornBefore());
                reserveCategoryDto.setBornAfter(addProductDto.getReservedCategory().get(i).getBornAfter());*/
                reserveCategoryDto.setGenderId(addProductDto.getReservedCategory().get(i).getGender());
                reserveCategoryDto.setGenderName(genderService.getGenderByGenderId(addProductDto.getReservedCategory().get(i).getGender()).getGenderName());
                reserveCategoryDto.setIsOtherOrStateCategory(addProductDto.getReservedCategory().get(i).getIsOtherOrStateCategory());
                reserveCategoryDto.setOtherOrStateCategory(addProductDto.getReservedCategory().get(i).getOtherOrStateCategory());
                reserveCategoryDtoList.add(reserveCategoryDto);
            }
        }
        if(!postList.isEmpty())
        {
            int postDtoIndex=0;
            for(Post post:postList)
            {
                PostProjectionDTO postProjectionDTO=new PostProjectionDTO();
                postProjectionDTO.setPostId(post.getPostId());
                postProjectionDTO.setDuration(post.getDuration());
                postProjectionDTO.setPostCode(post.getPostCode());
                postProjectionDTO.setPostName(post.getPostName());
                postProjectionDTO.setOtherDistributions(post.getOtherDistributions());
                postProjectionDTO.setAdditionalComments(post.getAdditionalComments());
                postProjectionDTO.setPostTotalVacancies(post.getPostTotalVacancies());
                postProjectionDTO.setVacancyDistributionTypeIds(post.getVacancyDistributionTypes());
                postProjectionDTO.setQualificationEligibilitydto(postDtos.get(postDtoIndex).getQualificationEligibility());
                postProjectionDTO.setStateDistributions(post.getStateDistributions());
                postProjectionDTO.setZoneDistributions(post.getZoneDistributions());
                postProjectionDTO.setGenderWiseDistribution(post.getGenderWiseDistribution());
                postProjectionDTO.setPhysicalRequirements(post.getPhysicalRequirements());
                postProjectionDTO.setStateDistributionAdditionalComments(post.getStateDistributionAdditionalComments());
                postProjectionDTO.setZoneDistributionAdditionalComments(post.getZoneDistributionAdditionalComments());
                postProjectionDTO.setGenderDistributionAdditionalComments(post.getGenderDistributionAdditionalComments());
                postProjectionDTO.setQualificationAdditionalComments(post.getQualificationAdditionalComments());
                postProjectionDTO.setReligionAdditionalComments(post.getReligionAdditionalComments());
                postProjectionDTO.setAdditionalEligibility(post.getAdditionalEligibility());
                postProjectionDTO.setIncomeAdditionalComments(post.getIncomeAdditionalComments());
                postProjectionDTO.setPhysicalAdditionalComments(post.getPhysicalAdditionalComments());
                postProjectionDTO.setOtherDistributionAdditionalComments(post.getOtherDistributionAdditionalComments());
                postProjectionDTO.setReserveCatAgeAdditionalComments(post.getReserveCatAgeAdditionalComments());
                postProjectionDTO.setTotalSeatsVisible(post.getTotalSeatsVisible());
                postProjectionDTO.setIncome(post.getIncome());
                postProjectionDTO.setReligion(post.getReligion());
                List<ReserveCategoryAgeDto>listD=new ArrayList<>();
                for(AddProductAgeDTO ageRequirement:postDtos.get(postDtoIndex).getReserveCategoryAge())
                {
                    System.out.println("PID"+ageRequirement);
                    AddProductAgeDTO refDetails=ageRequirement;
                    ReserveCategoryAgeDto reserveCategoryAgeDto=new ReserveCategoryAgeDto();

                    if(refDetails.getBornBeofreAfter().equals(true))
                    {
                        java.util.Date utilDate = dateFormat.parse(refDetails.getAsOfDate());
                        reserveCategoryAgeDto.setBornBefore(refDetails.getBornBefore());
                        reserveCategoryAgeDto.setBornAfter(refDetails.getBornAfter());
                        reserveCategoryAgeDto.setMaxAge(refDetails.getMaxAge());
                        reserveCategoryAgeDto.setMinAge(refDetails.getMinAge());
                        reserveCategoryAgeDto.setAsOfDate(utilDate);
                    }
                    else {
                        reserveCategoryAgeDto.setAsOfDate(convertStringToDate(refDetails.getAsOfDate(),"yyyy-MM-dd"));
                        reserveCategoryAgeDto.setMinAge(refDetails.getMinAge());
                        reserveCategoryAgeDto.setMaxAge(refDetails.getMaxAge());
                    }
                    reserveCategoryAgeDto.setCategoryRunningField(refDetails.getCategoryRunningField());
                    reserveCategoryAgeDto.setGenderRunningField(refDetails.getGenderRunningField());
                    reserveCategoryAgeDto.setReserveCategoryId(refDetails.getReserveCategory());
                    reserveCategoryAgeDto.setBornBeforeAfter(refDetails.getBornBeofreAfter());
                    reserveCategoryAgeDto.setAdditionalComments(refDetails.getAdditionalComments());
                    CustomReserveCategory customReserveCategory= entityManager.find(CustomReserveCategory.class,refDetails.getReserveCategory());
                    if(customReserveCategory==null)
                    {
                        throw new IllegalArgumentException("Reserve category with id "+ refDetails.getReserveCategory()+ " does not exists");
                    }
                    reserveCategoryAgeDto.setReserveCategory(customReserveCategory.getReserveCategoryName());
                    reserveCategoryAgeDto.setGenderId(refDetails.getGender());
                    CustomGender gender= entityManager.find(CustomGender.class,refDetails.getGender());
                    if(gender==null)
                    {
                        throw new IllegalArgumentException("Gender with id "+ refDetails.getGender()+ " does not exists");
                    }
                    reserveCategoryAgeDto.setGenderName(gender.getGenderName());
                    reserveCategoryAgeDto.setPost(Math.toIntExact(post.getPostId()));

                    listD.add(reserveCategoryAgeDto);
                }
                postProjectionDTO.setReserveCategoryAge(listD);
                postDTOList.add(postProjectionDTO);
                postDtoIndex++;
            }
        }
        this.numberOfPosts = Math.toIntExact(totalPostsInProduct);
        this.platformFee = addProductDto.getPlatformFee();

        this.customApplicationScope = customApplicationScope;
        this.customProductState = customProductState;

        this.modifiedDate = product.getActiveStartDate();
        this.creatorUserId = creatorUserId;
        this.creatorRoleId = creatorRole;
        this.modifierUserId = null;
        this.modifierRoleId = null;

        this.domicileRequired = addProductDto.getDomicileRequired();
        this.examDateFrom = addProductDto.getExamDateFrom();
        this.examDateTo = addProductDto.getExamDateTo();
        this.lateDateToPayFee = addProductDto.getLastDateToPayFee();
        this.admitCardDateFrom = addProductDto.getAdmitCardDateFrom();
        this.answerKeyAvailableDate=addProductDto.getAnswerKeyAvailableDate();
        this.adminCardDateTo = addProductDto.getAdmitCardDateTo();
        this.resultDeclarationDate=addProductDto.getResultDeclarationDate();
        this.modificationDateFrom = addProductDto.getModificationDateFrom();
        this.modificationDateTo = addProductDto.getModificationDateTo();
        this.downloadNotificationLink = addProductDto.getDownloadNotificationLink();
        this.downloadSyllabusLink = addProductDto.getDownloadSyllabusLink();
        this.formComplexity = addProductDto.getFormComplexity();

        this.customSector = customSector;
        this.isMultiplePostSameFee= addProductDto.getIsMultiplePostSameFee();
        this.selectionCriteria = addProductDto.getSelectionCriteria();
        this.totalVacanciesInProduct=totalVacanciesInProduct;
        this.state = state;
        AdvertisementWrapper advertisementWrapper = new AdvertisementWrapper();
        if(addProductDto.getAdvertisement()!=null)
            advertisementWrapper.wrapDetails(advertisement, null);
        this.advertisement = advertisementWrapper;

        if (product.getDefaultCategory() != null) {
            this.defaultCategoryId = product.getDefaultCategory().getId();
        }

    }

    public void wrapDetails(CustomProduct customProduct, List<ReserveCategoryDto> reserveCategoryDtoList) {
        this.id = customProduct.getId();
        this.metaTitle = customProduct.getMetaTitle();
        this.isEdited=customProduct.getIsEdited();
        this.displayTemplate = customProduct.getDisplayTemplate();
        this.active = customProduct.isActive();
        this.isApproved=customProduct.getIsApproved();
        this.activeGoLiveDate = customProduct.getGoLiveDate();
        this.answerKeyAvailableDate=customProduct.getAnswerKeyAvailableDate();
        this.examCenterAvailableDate=customProduct.getExamCenterAvailableDate();
        this.categoryName = customProduct.getDefaultCategory().getName();
        this.priorityLevel = customProduct.getPriorityLevel();
        this.archived = customProduct.getArchived();
        this.activeGoLiveDate = customProduct.getGoLiveDate();
        this.resultDeclarationDate=customProduct.getResultDeclarationDate();
        this.activeEndDate = customProduct.getDefaultSku().getActiveEndDate();
        this.activeStartDate = customProduct.getDefaultSku().getActiveStartDate();
        this.metaDescription = customProduct.getMetaDescription();
        this.numberOfPosts =customProduct.getPosts().size();
        this.additionalComments=customProduct.getAdditionalComments();
        this.displayTemplate = customProduct.getDisplayTemplate();
        this.platformFee = customProduct.getPlatformFee();
        this.state = customProduct.getState();
        this.sectorRunningField=customProduct.getSectorRunningField();
        this.resultDeclarationDate=customProduct.getResultDeclarationDate();
        this.counsellingDate=customProduct.getCounsellingDate();
        this.answerKeyAvailableDate=customProduct.getAnswerKeyAvailableDate();
        this.customApplicationScope = customProduct.getCustomApplicationScope();
        this.feeComments=customProduct.getFeeAdditionalComments();
        this.customProductState = customProduct.getProductState();
        this.reserveCategoryDtoList = reserveCategoryDtoList;
        this.modifiedDate = customProduct.getModifiedDate();
        this.tentativeVerificationFrom=customProduct.getTentativeVerificationFrom();
        this.tentativeVerificationTo=customProduct.getTentativeVerificationTo();
        this.creatorUserId = customProduct.getUserId();
        this.creatorRoleId = customProduct.getCreatoRole();
        this.modifierUserId = customProduct.getModifierUserId();
        this.modifierRoleId = customProduct.getModifierRole();
        this.isExamDateFromNa=customProduct.getIsExamDateFromNa();
        this.isAnswerKeyAvailableDateNa=customProduct.getIsAnswerKeyAvailableDateNa();
        this.isResultDeclarationDateNa=customProduct.getIsResultDeclarationDateNa();
        this.isCounsellingDateNa=customProduct.getIsCounsellingDateNa();
        this.isTentativeVerificationFromNa=customProduct.getIsTentativeVerificationFromNa();
        this.isTentativeVerificationToNa=customProduct.getIsTentativeVerificationToNa();
        this.isExamDateToNa=customProduct.getIsExamDateToNa();
        this.isExamCenterAvailableDateNa=customProduct.getIsExamCenterAvailableDateNa();
        this.isLateDateToPayFeeNa=customProduct.getIsLateDateToPayFeeNa();
        this.isAdmitCardDateFromNa=customProduct.getIsAdmitCardDateFromNa();
        this.isAdmitCardDateToNa=customProduct.getIsAdmitCardDateToNa();
        this.isModificationDateFromNa=customProduct.getIsModificationDateFromNa();
        this.isModificationDateToNa=customProduct.getIsModificationDateToNa();
        this.domicileRequired = customProduct.getDomicileRequired();
        this.examDateFrom = customProduct.getExamDateFrom();
        this.examDateTo = customProduct.getExamDateTo();
        this.customProductRejectionStatus = customProduct.getRejectionStatus();
        this.createdDate = customProduct.getCreatedDate();
        this.isReviewRequired=customProduct.getIsReviewRequired();
        this.isMultiplePostSameFee= customProduct.getIsMultiplePostSameFee();
        this.otherInfo=customProduct.getOtherInfo();
        if (customProduct.getDefaultCategory() != null) {
            this.defaultCategoryId = customProduct.getDefaultCategory().getId();
        }
    }


    public void wrapDetails(CustomProduct customProduct, List<Post> postList, List<PostProjectionDTO>postProjectionDTOS, ProductReserveCategoryFeePostRefService feeService) {
        this.id = customProduct.getId();
        this.metaTitle = customProduct.getMetaTitle();
        this.rejectionComment=customProduct.getRejectionComment();
        this.feeService=feeService;
        this.activeStartDate=customProduct.getActiveStartDate();
        this.activeEndDate=customProduct.getActiveEndDate();
        this.displayTemplate = customProduct.getDisplayTemplate();
        this.active = customProduct.isActive();
        this.activeGoLiveDate = customProduct.getGoLiveDate();
        this.resultDeclarationDate=customProduct.getResultDeclarationDate();
        if(customProduct.getDefaultCategory()!=null)
            this.categoryName = customProduct.getDefaultCategory().getName();
        else
            this.categoryName=null;
        this.isEdited=customProduct.getIsEdited();
        this.priorityLevel = customProduct.getPriorityLevel();
        this.archived = customProduct.getArchived();
        this.activeGoLiveDate = customProduct.getGoLiveDate();
        this.answerKeyAvailableDate=customProduct.getAnswerKeyAvailableDate();
        this.counsellingDate=customProduct.getCounsellingDate();
        this.activeEndDate = customProduct.getDefaultSku().getActiveEndDate();
        this.activeStartDate = customProduct.getDefaultSku().getActiveStartDate();
        this.metaDescription = customProduct.getMetaDescription();
        this.displayTemplate = customProduct.getDisplayTemplate();
        this.examCenterAvailableDate=customProduct.getExamCenterAvailableDate();
        this.tentativeVerificationFrom=customProduct.getTentativeVerificationFrom();
        this.tentativeVerificationTo=customProduct.getTentativeVerificationTo();
        this.platformFee = customProduct.getPlatformFee();
        this.sectorRunningField=customProduct.getSectorRunningField();
        this.isApproved=customProduct.getIsApproved();
        this.feeComments=customProduct.getFeeAdditionalComments();
        this.otherInfo=customProduct.getOtherInfo();
        this.additionalComments=customProduct.getAdditionalComments();
        this.numberOfPosts= customProduct.getPosts().size();
        this.isExamDateFromNa=customProduct.getIsExamDateFromNa();
        this.isAnswerKeyAvailableDateNa=customProduct.getIsAnswerKeyAvailableDateNa();
        this.isResultDeclarationDateNa=customProduct.getIsResultDeclarationDateNa();
        this.isCounsellingDateNa=customProduct.getIsCounsellingDateNa();
        this.isTentativeVerificationFromNa=customProduct.getIsTentativeVerificationFromNa();
        this.isTentativeVerificationToNa=customProduct.getIsTentativeVerificationToNa();
        this.isExamDateToNa=customProduct.getIsExamDateToNa();
        this.isExamCenterAvailableDateNa=customProduct.getIsExamCenterAvailableDateNa();
        this.isLateDateToPayFeeNa=customProduct.getIsLateDateToPayFeeNa();
        this.isAdmitCardDateFromNa=customProduct.getIsAdmitCardDateFromNa();
        this.isAdmitCardDateToNa=customProduct.getIsAdmitCardDateToNa();
        this.isModificationDateFromNa=customProduct.getIsModificationDateFromNa();
        this.isModificationDateToNa=customProduct.getIsModificationDateToNa();
        this.state = customProduct.getState();
        this.customApplicationScope = customProduct.getCustomApplicationScope();
        this.customProductState = customProduct.getProductState();
        this.totalVacanciesInProduct=customProduct.getTotalVacanciesInProduct();
        this.isMultiplePostSameFee=customProduct.getIsMultiplePostSameFee();
        List<CustomProductReserveCategoryFeePostRef>feeList=feeService.getProductReserveCategoryFeeAndPostByProductId(customProduct.getId());
        List<ReserveCategoryDto>feeDto=new ArrayList<>();
        if(feeList!=null) {
            for (CustomProductReserveCategoryFeePostRef fee : feeList) {
                ReserveCategoryDto reserveCategoryDto = new ReserveCategoryDto();
                reserveCategoryDto.setProductId(customProduct.getId());
                reserveCategoryDto.setReserveCategoryId(fee.getCustomReserveCategory().getReserveCategoryId());
                reserveCategoryDto.setReserveCategory(fee.getCustomReserveCategory().getReserveCategoryName());
                reserveCategoryDto.setFee(fee.getFee());
                reserveCategoryDto.setPost(fee.getPost());
                reserveCategoryDto.setGenderRunningField(fee.getGenderRunningField());
                reserveCategoryDto.setRunningField(fee.getRunningField());
                reserveCategoryDto.setAdditionalComments(fee.getAdditionalComments());
                reserveCategoryDto.setIsOtherOrStateCategory(fee.getIsOtherOrStateCategory());
                reserveCategoryDto.setOtherOrStateCategory(fee.getOtherOrStateCategory());
            /*reserveCategoryDto.setBornBefore(addProductDto.getReservedCategory().get(i).getBornBefore());
            reserveCategoryDto.setBornAfter(addProductDto.getReservedCategory().get(i).getBornAfter());*/
                if(fee.getGender()!=null)
                {
                    reserveCategoryDto.setGenderId(fee.getGender().getGenderId());
                    reserveCategoryDto.setGenderName(fee.getGender().getGenderName());
                }
                feeDto.add(reserveCategoryDto);
            }
        }

        this.reserveCategoryDtoList = feeDto;
        this.modifiedDate = customProduct.getModifiedDate();

        this.creatorUserId = customProduct.getUserId();
        this.creatorRoleId = customProduct.getCreatoRole();
        this.modifierUserId = customProduct.getModifierUserId();
        this.modifierRoleId = customProduct.getModifierRole();

        this.domicileRequired = customProduct.getDomicileRequired();
        this.examDateFrom = customProduct.getExamDateFrom();
        this.examDateTo = customProduct.getExamDateTo();

        this.lateDateToPayFee = customProduct.getLateDateToPayFee();
        this.admitCardDateFrom = customProduct.getAdmitCardDateFrom();
        this.adminCardDateTo = customProduct.getAdmitCardDateTo();
        this.modificationDateFrom = customProduct.getModificationDateFrom();
        this.modificationDateTo = customProduct.getModificationDateTo();
        this.downloadNotificationLink = customProduct.getDownloadNotificationLink();
        this.downloadSyllabusLink = customProduct.getDownloadSyllabusLink();
        this.formComplexity = customProduct.getFormComplexity();

        this.customSector = customProduct.getSector();
        this.selectionCriteria = customProduct.getSelectionCriteria();
        this.state = customProduct.getState();
        this.customProductRejectionStatus = customProduct.getRejectionStatus();
        this.createdDate = customProduct.getCreatedDate();
        this.isReviewRequired = customProduct.getIsReviewRequired();

        AdvertisementWrapper advertisementWrapper = new AdvertisementWrapper();

        if(customProduct.getAdvertisement() != null) {
            advertisementWrapper.wrapDetails(customProduct.getAdvertisement(), null);
            this.advertisement = advertisementWrapper;
        } else {
            this.advertisement = null;
        }

        if (customProduct.getDefaultCategory() != null) {
            this.defaultCategoryId = customProduct.getDefaultCategory().getId();
        }

        if(postProjectionDTOS!=null )
        {
            if(!postProjectionDTOS.isEmpty())
            {
                this.postDTOList=postProjectionDTOS;
            }
        }
    }
    public void wrapDetails(CustomProduct customProduct) {
        this.id = customProduct.getId();
        this.metaTitle = customProduct.getMetaTitle();
        this.displayTemplate = customProduct.getDisplayTemplate();
        this.createdDate = customProduct.getCreatedDate();
        this.isEdited=customProduct.getIsEdited();
        this.active = customProduct.isActive();
        this.activeGoLiveDate = customProduct.getGoLiveDate();
        this.examCenterAvailableDate=customProduct.getExamCenterAvailableDate();
        this.categoryName = customProduct.getDefaultCategory().getName();
        this.priorityLevel = customProduct.getPriorityLevel();
        this.counsellingDate=customProduct.getCounsellingDate();
        this.answerKeyAvailableDate=customProduct.getAnswerKeyAvailableDate();
        this.archived = customProduct.getArchived();
        this.sectorRunningField=customProduct.getSectorRunningField();
        this.isApproved=customProduct.getIsApproved();
        this.resultDeclarationDate=customProduct.getResultDeclarationDate();
        this.activeGoLiveDate = customProduct.getGoLiveDate();
        this.feeComments=customProduct.getFeeAdditionalComments();
        this.activeEndDate = customProduct.getDefaultSku().getActiveEndDate();
        this.activeStartDate = customProduct.getDefaultSku().getActiveStartDate();
        this.metaDescription = customProduct.getMetaDescription();
        this.otherInfo=customProduct.getOtherInfo();
        this.numberOfPosts =customProduct.getPosts().size();
        this.additionalComments=customProduct.getAdditionalComments();
        this.platformFee = customProduct.getPlatformFee();
        this.state = customProduct.getState();

        this.customApplicationScope = customProduct.getCustomApplicationScope();
        this.customProductState = customProduct.getProductState();

        this.creatorUserId = customProduct.getUserId();
        this.creatorRoleId = customProduct.getCreatoRole();
        this.modifierUserId = customProduct.getModifierUserId();
        this.modifierRoleId = customProduct.getModifierRole();
        this.tentativeVerificationFrom=customProduct.getTentativeVerificationFrom();
        this.tentativeVerificationTo=customProduct.getTentativeVerificationTo();

        this.examDateFrom = customProduct.getExamDateFrom();
        this.examDateTo = customProduct.getExamDateTo();
        this.selectionCriteria = customProduct.getSelectionCriteria();
        this.formComplexity = customProduct.getFormComplexity();
        this.downloadNotificationLink = customProduct.getDownloadNotificationLink();
        this.downloadSyllabusLink = customProduct.getDownloadSyllabusLink();
        this.modificationDateFrom = customProduct.getModificationDateFrom();
        this.modificationDateTo = customProduct.getModificationDateTo();
        this.admitCardDateFrom = customProduct.getAdmitCardDateFrom();
        this.adminCardDateTo = customProduct.getAdmitCardDateTo();
        this.lateDateToPayFee = customProduct.getLateDateToPayFee();
        this.domicileRequired = customProduct.getDomicileRequired();
        this.modifiedDate = customProduct.getModifiedDate();
        this.isExamDateFromNa=customProduct.getIsExamDateFromNa();
        this.isAnswerKeyAvailableDateNa=customProduct.getIsAnswerKeyAvailableDateNa();
        this.isResultDeclarationDateNa=customProduct.getIsResultDeclarationDateNa();
        this.isCounsellingDateNa=customProduct.getIsCounsellingDateNa();
        this.isTentativeVerificationFromNa=customProduct.getIsTentativeVerificationFromNa();
        this.isTentativeVerificationToNa=customProduct.getIsTentativeVerificationToNa();
        this.isExamDateToNa=customProduct.getIsExamDateToNa();
        this.isExamCenterAvailableDateNa=customProduct.getIsExamCenterAvailableDateNa();
        this.isLateDateToPayFeeNa=customProduct.getIsLateDateToPayFeeNa();
        this.isAdmitCardDateFromNa=customProduct.getIsAdmitCardDateFromNa();
        this.isAdmitCardDateToNa=customProduct.getIsAdmitCardDateToNa();
        this.isModificationDateFromNa=customProduct.getIsModificationDateFromNa();
        this.isModificationDateToNa=customProduct.getIsModificationDateToNa();
        this.customSector = customProduct.getSector();
        this.customProductRejectionStatus = customProduct.getRejectionStatus();
        this.totalVacanciesInProduct=customProduct.getTotalVacanciesInProduct();
        this.isMultiplePostSameFee=customProduct.getIsMultiplePostSameFee();
        List<PostProjectionDTO> postProjectionDTOS= getPosts(customProduct.getPosts());
        if(postProjectionDTOS!=null )
        {
            if(!postProjectionDTOS.isEmpty())
            {
                this.postDTOList=postProjectionDTOS;
            }
        }
        AdvertisementWrapper advertisementWrapper = new AdvertisementWrapper();
        if(advertisement != null) {
            advertisementWrapper.wrapDetails(customProduct.getAdvertisement(), null);
            this.advertisement = advertisementWrapper;
        } else {
            this.advertisement = null;
        }

        if (customProduct.getDefaultCategory() != null) {
            this.defaultCategoryId = customProduct.getDefaultCategory().getId();
        }


    }

    @Override
    public void wrapDetails(Product product, HttpServletRequest httpServletRequest) {
        this.id = product.getId();
        this.metaTitle = product.getMetaTitle();
        this.metaDescription = product.getMetaDescription();
        this.activeStartDate = product.getDefaultSku().getActiveStartDate();
        this.activeEndDate = product.getDefaultSku().getActiveEndDate();
        this.archived = ((Status) product).getArchived();
        this.categoryName = product.getDefaultCategory().getName();
        this.active = product.isActive();

        if (product.getDefaultCategory() != null) {
            this.defaultCategoryId = product.getDefaultCategory().getId();
        }
    }


    public void wrapSummary(Product model, HttpServletRequest request) {
        this.id = model.getId();
        this.metaTitle = model.getName();
        this.metaDescription = model.getDescription();
        this.active = model.isActive();
    }
    public void wrapDetails(Long orderId,CustomProduct product, HttpServletRequest httpServletRequest, ReserveCategoryService reserveCategoryService, ReserveCategoryAgeService reserveCategoryAgeService, GenderService genderService, CustomCustomer customCustomer, SharedUtilityService sharedUtilityService) {
        this.id = product.getId();
        this.orderId=orderId;
        this.metaTitle = product.getMetaTitle();
        this.displayTemplate = product.getDisplayTemplate();
        this.active = product.isActive();
        this.isApproved=product.getIsApproved();
        this.archived = 'N';
        this.createdDate = product.getCreatedDate();
        this.answerKeyAvailableDate=product.getAnswerKeyAvailableDate();
        this.isEdited=product.getIsEdited();
        this.activeGoLiveDate = product.getGoLiveDate();
        this.activeEndDate = product.getDefaultSku().getActiveEndDate();
        this.activeStartDate = product.getDefaultSku().getActiveStartDate();
        this.metaDescription = product.getMetaDescription();
        this.displayTemplate = product.getDisplayTemplate();
        this.isReviewRequired=product.getIsReviewRequired();
        this.feeComments=product.getFeeAdditionalComments();
        this.modifiedDate = product.getModifiedDate();
        this.creatorUserId = product.getUserId();
        this.counsellingDate=product.getCounsellingDate();
        this.creatorRoleId = product.getCreatoRole();
        this.modifierUserId = null;
        this.modifierRoleId = null;

        this.domicileRequired = product.getDomicileRequired();
        this.examDateFrom = product.getExamDateFrom();this.sectorRunningField=product.getSectorRunningField();
        this.examDateTo = product.getExamDateTo();

        this.lateDateToPayFee = product.getLateDateToPayFee();
        this.admitCardDateFrom = product.getAdmitCardDateFrom();
        this.adminCardDateTo = product.getAdmitCardDateTo();
        this.modificationDateFrom = product.getModificationDateFrom();
        this.modificationDateTo = product.getModificationDateTo();
        this.downloadNotificationLink = product.getDownloadNotificationLink();
        this.downloadSyllabusLink = product.getDownloadSyllabusLink();
        this.formComplexity = product.getFormComplexity();

        this.isMultiplePostSameFee= product.getIsMultiplePostSameFee();
        this.selectionCriteria = product.getSelectionCriteria();
        this.totalVacanciesInProduct = product.getTotalVacanciesInProduct();
        this.numberOfPosts=  product.getPosts().size();
        Long genderId = 1L;  // Default to 1 (MALE)
        Long categoryId = 1L; // Default to 1 (GEN)
        int flag = 0;
        this.fee = null;

        System.out.println("\n=== FEE CALCULATION DEBUG ===");
        System.out.println("Initial customer state: " + (customCustomer != null ? "Logged in" : "Not logged in"));

        // === PRIORITIZED FEE CALCULATION ===
        if (customCustomer != null) {
            try {
                System.out.println("\nCustomer details:");
                System.out.println("Raw category: " + customCustomer.getCategory());
                System.out.println("Raw gender: " + customCustomer.getGender());

                categoryId = reserveCategoryService.getCategoryByName(customCustomer.getCategory()).getReserveCategoryId();
                genderId = genderService.getGenderByName(customCustomer.getGender()).getGenderId();

                System.out.println("Resolved categoryId: " + categoryId);
                System.out.println("Resolved genderId: " + genderId);

                // 1. Most specific: Exact category + gender (e.g., SC + MALE = 50)
                System.out.println("\nChecking exact match (categoryId=" + categoryId + ", genderId=" + genderId + ")");
                this.fee = reserveCategoryService.getReserveCategoryFee(product.getId(), categoryId, genderId);
                System.out.println("Exact match fee result: " + this.fee);

                if (this.fee != null) {
                    flag++;
                    System.out.println("Found exact match fee: " + this.fee);
                } else {
                    // 2. Customer's category + ALL genders
                    System.out.println("\nChecking category match (categoryId=" + categoryId + ", GENDER_ALL)");
                    this.fee = reserveCategoryService.getReserveCategoryFee(product.getId(), categoryId, Constant.GENDER_ALL);
                    System.out.println("Category match fee result: " + this.fee);

                    if (this.fee != null) {
                        flag++;
                        System.out.println("Found category match fee: " + this.fee);
                    } else {
                        // 3. ALL categories + Customer's gender
                        System.out.println("\nChecking gender match (RESERVED_CATEGORY_ALL, genderId=" + genderId + ")");
                        this.fee = reserveCategoryService.getReserveCategoryFee(product.getId(), Constant.RESERVED_CATEGORY_ALL, genderId);
                        System.out.println("Gender match fee result: " + this.fee);

                        if (this.fee != null) {
                            flag++;
                            System.out.println("Found gender match fee: " + this.fee);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("\nERROR in customer-specific fee lookup:");
                e.printStackTrace();
            }
        }

        // 4. Final fallbacks
        if (this.fee == null) {
            System.out.println("\nNo customer-specific fee found, checking fallbacks:");

            System.out.println("Checking GEN+MALE (1L, 1L)");
            this.fee = reserveCategoryService.getReserveCategoryFee(product.getId(), 1L, 1L);
            System.out.println("GEN+MALE fee result: " + this.fee);

            if (this.fee == null) {
                System.out.println("Checking ALL+ALL");
                this.fee = reserveCategoryService.getReserveCategoryFee(
                        product.getId(), Constant.RESERVED_CATEGORY_ALL, Constant.GENDER_ALL);
                System.out.println("ALL+ALL fee result: " + this.fee);
            }

            if (this.fee != null) {
                flag++;
            } else {
                this.fee = 0.0;
                System.out.println("Using absolute fallback fee: 0.0");
            }
        }

        // === AGE LIMIT CALCULATION ===
        System.out.println("\n=== AGE LIMIT CALCULATION DEBUG ===");
        CustomProductReserveCategoryBornBeforeAfterRef ageLimitResult = null;

        if (customCustomer != null) {
            try {
                ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(product, Constant.RESERVED_CATEGORY_ALL, Constant.GENDER_ALL);
                if (ageLimitResult == null) {
                    System.out.println("\nChecking exact age limit (categoryId=" + categoryId + ", genderId=" + genderId + ")");
                    ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(product, categoryId, genderId);
                    System.out.println("Exact age limit result: " + ageLimitResult);

                    if (ageLimitResult == null) {
                        System.out.println("\nChecking category age limit (categoryId=" + categoryId + ", GENDER_ALL)");
                        ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(product, categoryId, Constant.GENDER_ALL);
                        System.out.println("Category age limit result: " + ageLimitResult);

                        if (ageLimitResult == null) {
                            System.out.println("\nChecking gender age limit (RESERVED_CATEGORY_ALL, genderId=" + genderId + ")");
                            ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(
                                    product, Constant.RESERVED_CATEGORY_ALL, genderId);
                            System.out.println("Gender age limit result: " + ageLimitResult);
                        }
                    }
                }
            }catch (Exception e) {
                System.out.println("\nERROR in customer-specific age lookup:");
                e.printStackTrace();
            }
        }

        // Final fallback for age
        if (ageLimitResult == null) {
            System.out.println("\nNo customer-specific age limit found, checking ALL+ALL");
            ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(
                    product, Constant.RESERVED_CATEGORY_ALL, Constant.GENDER_ALL);
            System.out.println("ALL+ALL age limit result: " + ageLimitResult);
        }

        // Set age limit if found
        if (ageLimitResult != null) {
            System.out.println("\nSetting age limit with result: " + ageLimitResult);
            setAgeLimit(ageLimitResult, sharedUtilityService);
            flag++;
        }

        System.out.println("\n=== FINAL CHECKS ===");
        System.out.println("Flag value: " + flag);
        System.out.println("Current fee: " + this.fee);
        System.out.println("Current age limit: " + this.ageLimit);

        // === FALLBACK FOR BOTH FEE AND AGE (if no matches) ===
        if (flag < 2) {
            System.out.println("\nInsufficient matches (flag < 2), applying final fallbacks");

            if (this.fee == null) {
                System.out.println("Rechecking GEN+MALE fee");
                this.fee = reserveCategoryService.getReserveCategoryFee(product.getId(), 1L, 1L);
                if (this.fee == null) {
                    this.fee = 0.0;
                    System.out.println("Setting fee to 0.0");
                }
            }

            if (this.ageLimit == null) {
                System.out.println("Rechecking GEN+MALE age limit");
                ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(product, 1L, 1L);
                if (ageLimitResult != null) {
                    setAgeLimit(ageLimitResult, sharedUtilityService);
                }
            }
        }

        System.out.println("\n=== FINAL VALUES ===");
        System.out.println("Final fee: " + this.fee);
        System.out.println("Final age limit: " + this.ageLimit);
        System.out.println("=== PROCESS COMPLETE ===");
    }
    public void wrapDetails(CustomProduct product, HttpServletRequest httpServletRequest, ReserveCategoryService reserveCategoryService, ReserveCategoryAgeService reserveCategoryAgeService, GenderService genderService, CustomCustomer customCustomer, SharedUtilityService sharedUtilityService) {
        this.id = product.getId();
        this.metaTitle = product.getMetaTitle();
        this.displayTemplate = product.getDisplayTemplate();
        this.active = product.isActive();
        this.isApproved=product.getIsApproved();
        this.archived = 'N';
        this.createdDate = product.getCreatedDate();
        this.answerKeyAvailableDate=product.getAnswerKeyAvailableDate();
        this.isEdited=product.getIsEdited();
        this.activeGoLiveDate = product.getGoLiveDate();
        this.activeEndDate = product.getDefaultSku().getActiveEndDate();
        this.activeStartDate = product.getDefaultSku().getActiveStartDate();
        this.metaDescription = product.getMetaDescription();
        this.displayTemplate = product.getDisplayTemplate();
        this.isReviewRequired=product.getIsReviewRequired();
        this.feeComments=product.getFeeAdditionalComments();
        this.modifiedDate = product.getModifiedDate();
        this.creatorUserId = product.getUserId();
        this.counsellingDate=product.getCounsellingDate();
        this.creatorRoleId = product.getCreatoRole();
        this.modifierUserId = null;
        this.modifierRoleId = null;

        this.domicileRequired = product.getDomicileRequired();
        this.examDateFrom = product.getExamDateFrom();this.sectorRunningField=product.getSectorRunningField();
        this.examDateTo = product.getExamDateTo();

        this.lateDateToPayFee = product.getLateDateToPayFee();
        this.admitCardDateFrom = product.getAdmitCardDateFrom();
        this.adminCardDateTo = product.getAdmitCardDateTo();
        this.modificationDateFrom = product.getModificationDateFrom();
        this.modificationDateTo = product.getModificationDateTo();
        this.downloadNotificationLink = product.getDownloadNotificationLink();
        this.downloadSyllabusLink = product.getDownloadSyllabusLink();
        this.formComplexity = product.getFormComplexity();

        this.isMultiplePostSameFee= product.getIsMultiplePostSameFee();
        this.selectionCriteria = product.getSelectionCriteria();
        this.totalVacanciesInProduct = product.getTotalVacanciesInProduct();
        this.numberOfPosts=  product.getPosts().size();
        Long genderId = 1L;  // Default to 1 (MALE)
        Long categoryId = 1L; // Default to 1 (GEN)
        int flag = 0;
        this.fee = null;

        System.out.println("\n=== FEE CALCULATION DEBUG ===");
        System.out.println("Initial customer state: " + (customCustomer != null ? "Logged in" : "Not logged in"));

        // === PRIORITIZED FEE CALCULATION ===
        if (customCustomer != null) {
            try {
                System.out.println("\nCustomer details:");
                System.out.println("Raw category: " + customCustomer.getCategory());
                System.out.println("Raw gender: " + customCustomer.getGender());

                categoryId = reserveCategoryService.getCategoryByName(customCustomer.getCategory()).getReserveCategoryId();
                genderId = genderService.getGenderByName(customCustomer.getGender()).getGenderId();

                System.out.println("Resolved categoryId: " + categoryId);
                System.out.println("Resolved genderId: " + genderId);

                // 1. Most specific: Exact category + gender (e.g., SC + MALE = 50)
                System.out.println("\nChecking exact match (categoryId=" + categoryId + ", genderId=" + genderId + ")");
                this.fee = reserveCategoryService.getReserveCategoryFee(product.getId(), categoryId, genderId);
                System.out.println("Exact match fee result: " + this.fee);

                if (this.fee != null) {
                    flag++;
                    System.out.println("Found exact match fee: " + this.fee);
                } else {
                    // 2. Customer's category + ALL genders
                    System.out.println("\nChecking category match (categoryId=" + categoryId + ", GENDER_ALL)");
                    this.fee = reserveCategoryService.getReserveCategoryFee(product.getId(), categoryId, Constant.GENDER_ALL);
                    System.out.println("Category match fee result: " + this.fee);

                    if (this.fee != null) {
                        flag++;
                        System.out.println("Found category match fee: " + this.fee);
                    } else {
                        // 3. ALL categories + Customer's gender
                        System.out.println("\nChecking gender match (RESERVED_CATEGORY_ALL, genderId=" + genderId + ")");
                        this.fee = reserveCategoryService.getReserveCategoryFee(product.getId(), Constant.RESERVED_CATEGORY_ALL, genderId);
                        System.out.println("Gender match fee result: " + this.fee);

                        if (this.fee != null) {
                            flag++;
                            System.out.println("Found gender match fee: " + this.fee);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("\nERROR in customer-specific fee lookup:");
                e.printStackTrace();
            }
        }

        // 4. Final fallbacks
        if (this.fee == null) {
            System.out.println("\nNo customer-specific fee found, checking fallbacks:");

            System.out.println("Checking GEN+MALE (1L, 1L)");
            this.fee = reserveCategoryService.getReserveCategoryFee(product.getId(), 1L, 1L);
            System.out.println("GEN+MALE fee result: " + this.fee);

            if (this.fee == null) {
                System.out.println("Checking ALL+ALL");
                this.fee = reserveCategoryService.getReserveCategoryFee(
                        product.getId(), Constant.RESERVED_CATEGORY_ALL, Constant.GENDER_ALL);
                System.out.println("ALL+ALL fee result: " + this.fee);
            }

            if (this.fee != null) {
                flag++;
            } else {
                this.fee = 0.0;
                System.out.println("Using absolute fallback fee: 0.0");
            }
        }

        // === AGE LIMIT CALCULATION ===
        System.out.println("\n=== AGE LIMIT CALCULATION DEBUG ===");
        CustomProductReserveCategoryBornBeforeAfterRef ageLimitResult = null;

        if (customCustomer != null) {
            try {
                ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(product, Constant.RESERVED_CATEGORY_ALL, Constant.GENDER_ALL);
                if (ageLimitResult == null) {
                    System.out.println("\nChecking exact age limit (categoryId=" + categoryId + ", genderId=" + genderId + ")");
                    ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(product, categoryId, genderId);
                    System.out.println("Exact age limit result: " + ageLimitResult);

                    if (ageLimitResult == null) {
                        System.out.println("\nChecking category age limit (categoryId=" + categoryId + ", GENDER_ALL)");
                        ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(product, categoryId, Constant.GENDER_ALL);
                        System.out.println("Category age limit result: " + ageLimitResult);

                        if (ageLimitResult == null) {
                            System.out.println("\nChecking gender age limit (RESERVED_CATEGORY_ALL, genderId=" + genderId + ")");
                            ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(
                                    product, Constant.RESERVED_CATEGORY_ALL, genderId);
                            System.out.println("Gender age limit result: " + ageLimitResult);
                        }
                    }
                }
            }catch (Exception e) {
                System.out.println("\nERROR in customer-specific age lookup:");
                e.printStackTrace();
            }
        }

        // Final fallback for age
        if (ageLimitResult == null) {
            System.out.println("\nNo customer-specific age limit found, checking ALL+ALL");
            ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(
                    product, Constant.RESERVED_CATEGORY_ALL, Constant.GENDER_ALL);
            System.out.println("ALL+ALL age limit result: " + ageLimitResult);
        }

        // Set age limit if found
        if (ageLimitResult != null) {
            System.out.println("\nSetting age limit with result: " + ageLimitResult);
            setAgeLimit(ageLimitResult, sharedUtilityService);
            flag++;
        }

        System.out.println("\n=== FINAL CHECKS ===");
        System.out.println("Flag value: " + flag);
        System.out.println("Current fee: " + this.fee);
        System.out.println("Current age limit: " + this.ageLimit);

        // === FALLBACK FOR BOTH FEE AND AGE (if no matches) ===
        if (flag < 2) {
            System.out.println("\nInsufficient matches (flag < 2), applying final fallbacks");

            if (this.fee == null) {
                System.out.println("Rechecking GEN+MALE fee");
                this.fee = reserveCategoryService.getReserveCategoryFee(product.getId(), 1L, 1L);
                if (this.fee == null) {
                    this.fee = 0.0;
                    System.out.println("Setting fee to 0.0");
                }
            }

            if (this.ageLimit == null) {
                System.out.println("Rechecking GEN+MALE age limit");
                ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(product, 1L, 1L);
                if (ageLimitResult != null) {
                    setAgeLimit(ageLimitResult, sharedUtilityService);
                }
            }
        }

        System.out.println("\n=== FINAL VALUES ===");
        System.out.println("Final fee: " + this.fee);
        System.out.println("Final age limit: " + this.ageLimit);
        System.out.println("=== PROCESS COMPLETE ===");
    }
    private void setAgeLimit(CustomProductReserveCategoryBornBeforeAfterRef ageLimitResult, SharedUtilityService sharedUtilityService) {
        if (ageLimitResult == null) {
            this.ageLimit = "N/A";
            return;
        }
        int[] ageLimits=null;
        if(ageLimitResult.getBornAfter()!=null&&ageLimitResult.getBornBefore()!=null) {
            ageLimits = sharedUtilityService.calculateAgeRange(
                    ageLimitResult.getBornBefore(),
                    ageLimitResult.getBornAfter(),
                    null);
        }


        this.ageLimit = (ageLimitResult.getMaximumAge() != null && ageLimitResult.getMinimumAge() != null &&
                ageLimitResult.getMaximumAge() != 0 && ageLimitResult.getMinimumAge() != 0)
                ? ageLimitResult.getMinimumAge() + "-" + ageLimitResult.getMaximumAge()
                : (ageLimits != null && ageLimits.length >= 2)
                ? ageLimits[0] + "-" + ageLimits[1]
                : "N/A";
    }
    public AgeAndFeeDetails getAgeAndFee(CustomProduct product, HttpServletRequest httpServletRequest, ReserveCategoryService reserveCategoryService, ReserveCategoryAgeService reserveCategoryAgeService, GenderService genderService, CustomCustomer customCustomer, SharedUtilityService sharedUtilityService) {
        AgeAndFeeDetails ageAndFeeDetails=new AgeAndFeeDetails();
        Long genderId = 1L;  // Default to 1 (MALE)
        Long categoryId = 1L; // Default to 1 (GEN)
        int flag = 0;
        Double fee = null;

        System.out.println("\n=== FEE CALCULATION DEBUG ===");
        System.out.println("Initial customer state: " + (customCustomer != null ? "Logged in" : "Not logged in"));

        // === PRIORITIZED FEE CALCULATION ===
        if (customCustomer != null) {
            try {
                System.out.println("\nCustomer details:");
                System.out.println("Raw category: " + customCustomer.getCategory());
                System.out.println("Raw gender: " + customCustomer.getGender());

                categoryId = reserveCategoryService.getCategoryByName(customCustomer.getCategory()).getReserveCategoryId();
                genderId = genderService.getGenderByName(customCustomer.getGender()).getGenderId();

                System.out.println("Resolved categoryId: " + categoryId);
                System.out.println("Resolved genderId: " + genderId);

                // 1. Most specific: Exact category + gender (e.g., SC + MALE = 50)
                System.out.println("\nChecking exact match (categoryId=" + categoryId + ", genderId=" + genderId + ")");
                fee = reserveCategoryService.getReserveCategoryFee(product.getId(), categoryId, genderId);
                System.out.println("Exact match fee result: " + fee);

                if (fee != null) {
                    flag++;
                    System.out.println("Found exact match fee: " + fee);
                } else {
                    // 2. Customer's category + ALL genders
                    System.out.println("\nChecking category match (categoryId=" + categoryId + ", GENDER_ALL)");
                    fee = reserveCategoryService.getReserveCategoryFee(product.getId(), categoryId, Constant.GENDER_ALL);
                    System.out.println("Category match fee result: " + fee);

                    if (fee != null) {
                        flag++;
                        System.out.println("Found category match fee: " + fee);
                    } else {
                        // 3. ALL categories + Customer's gender
                        System.out.println("\nChecking gender match (RESERVED_CATEGORY_ALL, genderId=" + genderId + ")");
                        fee = reserveCategoryService.getReserveCategoryFee(product.getId(), Constant.RESERVED_CATEGORY_ALL, genderId);
                        System.out.println("Gender match fee result: " + fee);

                        if (fee != null) {
                            flag++;
                            System.out.println("Found gender match fee: " + fee);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("\nERROR in customer-specific fee lookup:");
                e.printStackTrace();
            }
        }

        // 4. Final fallbacks
        if (fee == null) {
            System.out.println("\nNo customer-specific fee found, checking fallbacks:");

            System.out.println("Checking GEN+MALE (1L, 1L)");
            fee = reserveCategoryService.getReserveCategoryFee(product.getId(), 1L, 1L);
            System.out.println("GEN+MALE fee result: " + fee);

            if (fee == null) {
                System.out.println("Checking ALL+ALL");
                fee = reserveCategoryService.getReserveCategoryFee(
                        product.getId(), Constant.RESERVED_CATEGORY_ALL, Constant.GENDER_ALL);
                System.out.println("ALL+ALL fee result: " + fee);
            }

            if (fee != null) {
                flag++;
            } else {
                fee = 0.0;
                System.out.println("Using absolute fallback fee: 0.0");
            }
        }

        // === AGE LIMIT CALCULATION ===
        System.out.println("\n=== AGE LIMIT CALCULATION DEBUG ===");
        CustomProductReserveCategoryBornBeforeAfterRef ageLimitResult = null;

        if (customCustomer != null) {
            try {
                ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(product,Constant.RESERVED_CATEGORY_ALL,Constant.GENDER_ALL);
                if(ageLimitResult==null) {
                    System.out.println("\nChecking exact age limit (categoryId=" + categoryId + ", genderId=" + genderId + ")");
                    ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(product, categoryId, genderId);
                    System.out.println("Exact age limit result: " + ageLimitResult);

                    if (ageLimitResult == null) {
                        System.out.println("\nChecking category age limit (categoryId=" + categoryId + ", GENDER_ALL)");
                        ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(product, categoryId, Constant.GENDER_ALL);
                        System.out.println("Category age limit result: " + ageLimitResult);

                        if (ageLimitResult == null) {
                            System.out.println("\nChecking gender age limit (RESERVED_CATEGORY_ALL, genderId=" + genderId + ")");
                            ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(
                                    product, Constant.RESERVED_CATEGORY_ALL, genderId);
                            System.out.println("Gender age limit result: " + ageLimitResult);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("\nERROR in customer-specific age lookup:");
                e.printStackTrace();
            }
        }

        // Final fallback for age
        if (ageLimitResult == null) {
            System.out.println("\nNo customer-specific age limit found, checking ALL+ALL");
            ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(
                    product, Constant.RESERVED_CATEGORY_ALL, Constant.GENDER_ALL);
            System.out.println("ALL+ALL age limit result: " + ageLimitResult);
        }

        // Set age limit if found
        if (ageLimitResult != null) {
            System.out.println("\nSetting age limit with result: " + ageLimitResult);
            setAgeLimit(ageLimitResult, sharedUtilityService);
            flag++;
        }

        System.out.println("\n=== FINAL CHECKS ===");
        System.out.println("Flag value: " + flag);
        System.out.println("Current fee: " + fee);
        System.out.println("Current age limit: " + ageLimit);

        // === FALLBACK FOR BOTH FEE AND AGE (if no matches) ===
        if (flag < 2) {
            System.out.println("\nInsufficient matches (flag < 2), applying final fallbacks");

            if (fee == null) {
                System.out.println("Rechecking GEN+MALE fee");
                fee = reserveCategoryService.getReserveCategoryFee(product.getId(), 1L, 1L);
                if (fee == null) {
                    fee = 0.0;
                    System.out.println("Setting fee to 0.0");
                }
            }

            if (ageLimit == null) {
                System.out.println("Rechecking GEN+MALE age limit");
                ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(product, 1L, 1L);
                if (ageLimitResult != null) {
                    setAgeLimit(ageLimitResult, sharedUtilityService);
                }
            }
        }

        System.out.println("\n=== FINAL VALUES ===");
        System.out.println("Final fee: " + fee);
        System.out.println("Final age limit: " + ageLimit);
        System.out.println("=== PROCESS COMPLETE ===");
        ageAndFeeDetails.setAgeLimit(ageLimit);
        ageAndFeeDetails.setFee(fee);
        return ageAndFeeDetails;
    }
}