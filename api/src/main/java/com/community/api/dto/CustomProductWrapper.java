package com.community.api.dto;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import com.broadleafcommerce.rest.api.wrapper.MediaWrapper;
import com.community.api.entity.*;

import com.community.api.services.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.broadleafcommerce.common.rest.api.wrapper.APIWrapper;
import org.broadleafcommerce.common.rest.api.wrapper.BaseWrapper;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.common.persistence.Status;
import org.springframework.beans.factory.annotation.Autowired;


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
    @JsonProperty("long_description")
    protected String longDescription;
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
    @JsonProperty("default_category_id")
    protected Long defaultCategoryId;
    @JsonProperty("archived")
    protected Character archived;

    @JsonProperty("url")
    protected String url;
    @JsonProperty("active")
    protected Boolean active;
    @JsonProperty("promo_message")
    protected String promoMessage;
    @JsonProperty("quantity")
    protected Integer quantity;
    @JsonProperty("media")
    protected List<MediaWrapper> media;

    @JsonProperty("reserve_category_fee")
    protected List<ReserveCategoryDto> reserveCategoryDtoList = new ArrayList<>();
    @JsonProperty("physical_attribute_list")
    protected List<PhysicalRequirementDto> physicalRequirementDtoList = new ArrayList<>();

    @JsonProperty("platform_fee")
    protected Double platformFee;
    @JsonProperty("state")
    protected StateCode state;
    @JsonProperty("custom_application_scope")
    protected CustomApplicationScope customApplicationScope;
    @JsonProperty("custom_product_state")
    protected CustomProductState customProductState;
    @JsonProperty("custom_job_group")
    protected CustomJobGroup customJobGroup;
    @JsonProperty("custom_rejection_status")
    protected CustomProductRejectionStatus customProductRejectionStatus;

    @JsonProperty("creator_user_id")
    protected Long creatorUserId;
    @JsonProperty("creator_role_id")
    protected Role creatorRoleId;



    @JsonProperty("modified_date")
    protected Date modifiedDate;
    @JsonProperty("advertiser_url")
    protected String advertiserUrl;
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
    @JsonProperty("qualification")
    Qualification qualification;
    @JsonProperty("sector")
    CustomSector customSector;
    @JsonProperty("gender_specific")
    CustomGender customGender;
    @JsonProperty("stream")
    CustomStream customStream;
    @JsonProperty("subject")
    CustomSubject customSubject;
    @JsonProperty("selection_criteria")
    String selectionCriteria;
    @JsonProperty("created_date")
    Date createdDate;
    @JsonProperty("notifying_authority")
    String notifyingAuthority;
    @JsonProperty("post_name")
    String postName;
    @JsonProperty("is_review_required")
    Boolean isReviewRequired;
    @JsonProperty("advertisement")
    AdvertisementWrapper advertisement;


    @JsonProperty("posts")
    List<PostProjectionDTO> postDTOList=new ArrayList<>();


    public void wrapDetailsAddProduct(Product product, AddProductDto addProductDto, CustomJobGroup customJobGroup, CustomProductState customProductState, CustomApplicationScope customApplicationScope, Long creatorUserId, Role creatorRole, ReserveCategoryService reserveCategoryService, StateCode state, CustomGender customGender, CustomSector customSector, Qualification qualification, CustomStream customStream, CustomSubject customSubject, Date currentDate, Advertisement advertisement,GenderService genderService,EntityManager entityManager,List<Post> postList) throws Exception {

        this.id = product.getId();
        this.metaTitle = product.getMetaTitle();
        this.displayTemplate = product.getDisplayTemplate();
        this.longDescription = product.getLongDescription();
        this.active = product.isActive();
        this.quantity = product.getDefaultSku().getQuantityAvailable();
        this.activeGoLiveDate = addProductDto.getGoLiveDate();
        this.categoryName = product.getDefaultCategory().getName();
        this.priorityLevel = addProductDto.getPriorityLevel();
        this.archived = 'N';
        this.createdDate = currentDate;
        this.promoMessage = product.getPromoMessage();
        this.activeGoLiveDate = addProductDto.getGoLiveDate();
        this.activeEndDate = product.getDefaultSku().getActiveEndDate();
        this.activeStartDate = product.getDefaultSku().getActiveStartDate();
        this.url = product.getUrl();
        this.metaDescription = product.getMetaDescription();

        this.displayTemplate = product.getDisplayTemplate();
        this.postName = addProductDto.getPostName();
        this.isReviewRequired=addProductDto.getIsReviewRequired();


        if(addProductDto.getReservedCategory()!=null)
        {
            for(int i=0; i<addProductDto.getReservedCategory().size(); i++) {

                CustomReserveCategory customReserveCategory = reserveCategoryService.getReserveCategoryById(addProductDto.getReservedCategory().get(i).reserveCategory);

                ReserveCategoryDto reserveCategoryDto = new ReserveCategoryDto();
                reserveCategoryDto.setProductId(product.getId());
                reserveCategoryDto.setReserveCategoryId(addProductDto.getReservedCategory().get(i).getReserveCategory());
                reserveCategoryDto.setReserveCategory(customReserveCategory.getReserveCategoryName());
                reserveCategoryDto.setFee(addProductDto.getReservedCategory().get(i).getFee());
                reserveCategoryDto.setPost(addProductDto.getReservedCategory().get(i).getPost());
                /*reserveCategoryDto.setBornBefore(addProductDto.getReservedCategory().get(i).getBornBefore());
                reserveCategoryDto.setBornAfter(addProductDto.getReservedCategory().get(i).getBornAfter());*/
                reserveCategoryDto.setGenderId(addProductDto.getReservedCategory().get(i).getGender());
                reserveCategoryDto.setGenderName(genderService.getGenderByGenderId(addProductDto.getReservedCategory().get(i).getGender()).getGenderName());
                reserveCategoryDtoList.add(reserveCategoryDto);
            }
        }
        if(!postList.isEmpty())
        {
            for(Post post:postList)
            {
                PostProjectionDTO postProjectionDTO=new PostProjectionDTO();
                postProjectionDTO.setPostCode(post.getPostCode());
                postProjectionDTO.setPostName(post.getPostName());
                postProjectionDTO.setOtherVacancyDistribution(post.getOtherVacancyDistribution());
                postProjectionDTO.setPostTotalVacancies(post.getPostTotalVacancies());
                postProjectionDTO.setVacancyDistributionTypeIds(post.getVacancyDistributionTypes());
                postProjectionDTO.setQualificationEligibility(post.getQualificationEligibility());
                postProjectionDTO.setStateDistributions(post.getStateDistributions());
                postProjectionDTO.setGenderWiseDistribution(post.getGenderWiseDistribution());
                postProjectionDTO.setPhysicalRequirements(post.getPhysicalRequirements());

                Query query =entityManager.createNativeQuery("SELECT age_requirement_id from post_age_requirement where post_id = :postId");
                query.setParameter("postId",post.getPostId());
                List<BigInteger>resultList=query.getResultList();
                List<ReserveCategoryAgeDto>listD=new ArrayList<>();
                for(BigInteger bigInteger:resultList)
                {
                    System.out.println(resultList.size());
                    System.out.println("PID"+bigInteger);
                    CustomProductReserveCategoryBornBeforeAfterRef refDetails=refService.getCustomProductReserveCategoryBornBeforeAfterRefByUId(bigInteger.longValue());
                    ReserveCategoryAgeDto reserveCategoryAgeDto=new ReserveCategoryAgeDto();
                    reserveCategoryAgeDto.setProductId(refDetails.getCustomProduct().getId());
                    reserveCategoryAgeDto.setBornBefore(refDetails.getBornBefore());
                    listD.add(reserveCategoryAgeDto);
                }
                postProjectionDTO.setReserveCategoryAge(listD);
                postDTOList.add(postProjectionDTO);
            }
        }
        this.platformFee = addProductDto.getPlatformFee();
        this.notifyingAuthority = addProductDto.getNotifyingAuthority();

        this.customApplicationScope = customApplicationScope;
        this.customJobGroup = customJobGroup;
        this.customProductState = customProductState;

        this.modifiedDate = product.getActiveStartDate();
        this.creatorUserId = creatorUserId;
        this.creatorRoleId = creatorRole;
        this.modifierUserId = null;
        this.modifierRoleId = null;

        this.domicileRequired = addProductDto.getDomicileRequired();
        this.advertiserUrl = addProductDto.getAdvertiserUrl();
        this.examDateFrom = addProductDto.getExamDateFrom();
        this.examDateTo = addProductDto.getExamDateTo();

        this.lateDateToPayFee = addProductDto.getLastDateToPayFee();
        this.admitCardDateFrom = addProductDto.getAdmitCardDateFrom();
        this.adminCardDateTo = addProductDto.getAdmitCardDateTo();
        this.modificationDateFrom = addProductDto.getModificationDateFrom();
        this.modificationDateTo = addProductDto.getModificationDateTo();
        this.downloadNotificationLink = addProductDto.getDownloadNotificationLink();
        this.downloadSyllabusLink = addProductDto.getDownloadSyllabusLink();
        this.formComplexity = addProductDto.getFormComplexity();

        this.customGender = customGender;
        this.customSector = customSector;
        this.qualification = qualification;
        this.customStream = customStream;
        this.customSubject = customSubject;
        this.selectionCriteria = addProductDto.getSelectionCriteria();
        this.state = state;
        AdvertisementWrapper advertisementWrapper = new AdvertisementWrapper();
        advertisementWrapper.wrapDetails(advertisement, null);
        this.advertisement = advertisementWrapper;

        if (product.getDefaultCategory() != null) {
            this.defaultCategoryId = product.getDefaultCategory().getId();
        }

    }

    public void wrapDetails(CustomProduct customProduct, List<ReserveCategoryDto> reserveCategoryDtoList) {
        this.id = customProduct.getId();
        this.metaTitle = customProduct.getMetaTitle();
        this.displayTemplate = customProduct.getDisplayTemplate();
        this.longDescription = customProduct.getLongDescription();
        this.active = customProduct.isActive();
        this.quantity = customProduct.getDefaultSku().getQuantityAvailable();
        this.activeGoLiveDate = customProduct.getGoLiveDate();
        this.categoryName = customProduct.getDefaultCategory().getName();
        this.priorityLevel = customProduct.getPriorityLevel();
        this.archived = customProduct.getArchived();
        this.promoMessage = customProduct.getPromoMessage();
        this.activeGoLiveDate = customProduct.getGoLiveDate();
        this.activeEndDate = customProduct.getDefaultSku().getActiveEndDate();
        this.activeStartDate = customProduct.getDefaultSku().getActiveStartDate();
        this.url = customProduct.getUrl();
        this.metaDescription = customProduct.getMetaDescription();

        this.displayTemplate = customProduct.getDisplayTemplate();
        this.platformFee = customProduct.getPlatformFee();
        this.state = customProduct.getState();

        this.customApplicationScope = customProduct.getCustomApplicationScope();
        this.customJobGroup = customProduct.getJobGroup();
        this.customProductState = customProduct.getProductState();
        this.reserveCategoryDtoList = reserveCategoryDtoList;
        this.modifiedDate = customProduct.getModifiedDate();

        this.creatorUserId = customProduct.getUserId();
        this.creatorRoleId = customProduct.getCreatoRole();
        this.modifierUserId = customProduct.getModifierUserId();
        this.modifierRoleId = customProduct.getModifierRole();

        this.domicileRequired = customProduct.getDomicileRequired();
        this.advertiserUrl = customProduct.getAdvertiserUrl();
        this.examDateFrom = customProduct.getExamDateFrom();
        this.examDateTo = customProduct.getExamDateTo();
        this.notifyingAuthority = customProduct.getNotifyingAuthority();
        this.customProductRejectionStatus = customProduct.getRejectionStatus();
        this.createdDate = customProduct.getCreatedDate();
        this.postName = customProduct.getPostName();
        this.isReviewRequired=customProduct.getIsReviewRequired();

        if (customProduct.getDefaultCategory() != null) {
            this.defaultCategoryId = customProduct.getDefaultCategory().getId();
        }
    }


    public void wrapDetails(CustomProduct customProduct, List<Post> postList, List<PostProjectionDTO>postProjectionDTOS, ProductReserveCategoryFeePostRefService feeService) {
        this.id = customProduct.getId();
        this.metaTitle = customProduct.getMetaTitle();
        this.feeService=feeService;
        this.displayTemplate = customProduct.getDisplayTemplate();
        this.longDescription = customProduct.getLongDescription();
        this.active = customProduct.isActive();
        this.quantity = customProduct.getDefaultSku().getQuantityAvailable();
        this.activeGoLiveDate = customProduct.getGoLiveDate();
        this.categoryName = customProduct.getDefaultCategory().getName();
        this.priorityLevel = customProduct.getPriorityLevel();
        this.archived = customProduct.getArchived();
        this.promoMessage = customProduct.getPromoMessage();
        this.activeGoLiveDate = customProduct.getGoLiveDate();
        this.activeEndDate = customProduct.getDefaultSku().getActiveEndDate();
        this.activeStartDate = customProduct.getDefaultSku().getActiveStartDate();
        this.url = customProduct.getUrl();
        this.metaDescription = customProduct.getMetaDescription();
        this.displayTemplate = customProduct.getDisplayTemplate();
        this.platformFee = customProduct.getPlatformFee();

        this.customApplicationScope = customProduct.getCustomApplicationScope();
        this.customJobGroup = customProduct.getJobGroup();
        this.customProductState = customProduct.getProductState();
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
            /*reserveCategoryDto.setBornBefore(addProductDto.getReservedCategory().get(i).getBornBefore());
            reserveCategoryDto.setBornAfter(addProductDto.getReservedCategory().get(i).getBornAfter());*/
                reserveCategoryDto.setGenderId(fee.getGender().getGenderId());
                reserveCategoryDto.setGenderName(fee.getGender().getGenderName());
                feeDto.add(reserveCategoryDto);
            }
        }
        this.reserveCategoryDtoList = feeDto;
        this.physicalRequirementDtoList = physicalRequirementDtoList;

        this.modifiedDate = customProduct.getModifiedDate();

        this.creatorUserId = customProduct.getUserId();
        this.creatorRoleId = customProduct.getCreatoRole();
        this.modifierUserId = customProduct.getModifierUserId();
        this.modifierRoleId = customProduct.getModifierRole();

        this.domicileRequired = customProduct.getDomicileRequired();
        this.advertiserUrl = customProduct.getAdvertiserUrl();
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

        this.customGender = customProduct.getGenderSpecific();
        this.customSector = customProduct.getSector();
        this.qualification = customProduct.getQualification();
        this.customStream = customProduct.getStream();
        this.customSubject = customProduct.getSubject();
        this.selectionCriteria = customProduct.getSelectionCriteria();
        this.state = customProduct.getState();
        this.notifyingAuthority = customProduct.getNotifyingAuthority();
        this.customProductRejectionStatus = customProduct.getRejectionStatus();
        this.createdDate = customProduct.getCreatedDate();
        this.postName = customProduct.getPostName();

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
        this.longDescription = customProduct.getLongDescription();
        this.active = customProduct.isActive();
        this.quantity = customProduct.getDefaultSku().getQuantityAvailable();
        this.activeGoLiveDate = customProduct.getGoLiveDate();
        this.categoryName = customProduct.getDefaultCategory().getName();
        this.priorityLevel = customProduct.getPriorityLevel();
        this.archived = customProduct.getArchived();
        this.promoMessage = customProduct.getPromoMessage();
        this.activeGoLiveDate = customProduct.getGoLiveDate();
        this.activeEndDate = customProduct.getDefaultSku().getActiveEndDate();
        this.activeStartDate = customProduct.getDefaultSku().getActiveStartDate();
        this.url = customProduct.getUrl();
        this.metaDescription = customProduct.getMetaDescription();
        this.postName = customProduct.getPostName();

        this.platformFee = customProduct.getPlatformFee();
        this.state = customProduct.getState();

        this.customApplicationScope = customProduct.getCustomApplicationScope();
        this.customJobGroup = customProduct.getJobGroup();
        this.customProductState = customProduct.getProductState();

        this.creatorUserId = customProduct.getUserId();
        this.creatorRoleId = customProduct.getCreatoRole();
        this.modifierUserId = customProduct.getModifierUserId();
        this.modifierRoleId = customProduct.getModifierRole();

        this.advertiserUrl = customProduct.getAdvertiserUrl();

        this.examDateFrom = customProduct.getExamDateFrom();
        this.examDateTo = customProduct.getExamDateTo();
        this.selectionCriteria = customProduct.getSelectionCriteria();
        this.notifyingAuthority = customProduct.getNotifyingAuthority();
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
        this.qualification = customProduct.getQualification();
        this.customSector = customProduct.getSector();
        this.customStream = customProduct.getStream();
        this.customSubject = customProduct.getSubject();
        this.customGender = customProduct.getGenderSpecific();
        this.customProductRejectionStatus = customProduct.getRejectionStatus();
        this.postName = customProduct.getPostName();

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
        this.longDescription = product.getLongDescription();
        this.url = product.getUrl();
        this.activeStartDate = product.getDefaultSku().getActiveStartDate();
        this.activeEndDate = product.getDefaultSku().getActiveEndDate();
        this.promoMessage = product.getPromoMessage();
        this.archived = ((Status) product).getArchived();
        this.categoryName = product.getDefaultCategory().getName();
        this.active = product.isActive();
        this.quantity = product.getDefaultSku().getQuantityAvailable();

        if (product.getDefaultCategory() != null) {
            this.defaultCategoryId = product.getDefaultCategory().getId();
        }
    }


    public void wrapSummary(Product model, HttpServletRequest request) {
        this.id = model.getId();
        this.metaTitle = model.getName();
        this.metaDescription = model.getDescription();
        this.longDescription = model.getLongDescription();
        this.url = model.getUrl();
        this.active = model.isActive();
    }
}