package com.community.api.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

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
    Long numberOfPosts;

    public void wrapDetailsAddProduct(Product product, AddProductDto addProductDto, CustomProductState customProductState, CustomApplicationScope customApplicationScope, Long creatorUserId, Role creatorRole, ReserveCategoryService reserveCategoryService, StateCode state, CustomSector customSector, Date currentDate, Advertisement advertisement,GenderService genderService,EntityManager entityManager,List<Post> postList,List<PostDto> postDtos, Long totalVacanciesInProduct, Long totalPostsInProduct) throws Exception {

        this.id = product.getId();
        this.metaTitle = product.getMetaTitle();
        this.displayTemplate = product.getDisplayTemplate();
        this.active = product.isActive();
        this.activeGoLiveDate = addProductDto.getGoLiveDate();
        this.categoryName = product.getDefaultCategory().getName();
        this.priorityLevel = addProductDto.getPriorityLevel();
        this.archived = 'N';
        this.createdDate = currentDate;
        this.activeGoLiveDate = addProductDto.getGoLiveDate();
        this.activeEndDate = product.getDefaultSku().getActiveEndDate();
        this.activeStartDate = product.getDefaultSku().getActiveStartDate();
        this.metaDescription = product.getMetaDescription();

        this.displayTemplate = product.getDisplayTemplate();
        this.isReviewRequired=addProductDto.getIsReviewRequired();
        this.otherInfo=addProductDto.getOtherInfo();


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
            int postDtoIndex=0;
            for(Post post:postList)
            {
                PostProjectionDTO postProjectionDTO=new PostProjectionDTO();
                postProjectionDTO.setPostId(post.getPostId());
                postProjectionDTO.setPostCode(post.getPostCode());
                postProjectionDTO.setPostName(post.getPostName());
                postProjectionDTO.setOtherDistributions(post.getOtherDistributions());
                postProjectionDTO.setPostTotalVacancies(post.getPostTotalVacancies());
                postProjectionDTO.setVacancyDistributionTypeIds(post.getVacancyDistributionTypes());
                postProjectionDTO.setQualificationEligibility(post.getQualificationEligibility());
                postProjectionDTO.setStateDistributions(post.getStateDistributions());
                postProjectionDTO.setZoneDistributions(post.getZoneDistributions());
                postProjectionDTO.setGenderWiseDistribution(post.getGenderWiseDistribution());
                postProjectionDTO.setPhysicalRequirements(post.getPhysicalRequirements());

                List<ReserveCategoryAgeDto>listD=new ArrayList<>();
                for(AddProductAgeDTO ageRequirement:postDtos.get(postDtoIndex).getReserveCategoryAge())
                {
                    System.out.println("PID"+ageRequirement);
                    AddProductAgeDTO refDetails=ageRequirement;
                    ReserveCategoryAgeDto reserveCategoryAgeDto=new ReserveCategoryAgeDto();
                    if(refDetails.getBornBeofreAfter().equals(true))
                    {
                        reserveCategoryAgeDto.setBornBefore(refDetails.getBornBefore());
                        reserveCategoryAgeDto.setBornAfter(refDetails.getBornAfter());
                    }
                    else {
                        reserveCategoryAgeDto.setAsOfDate(convertStringToDate(refDetails.getAsOfDate(),"yyyy-MM-dd"));
                        reserveCategoryAgeDto.setMinAge(refDetails.getMinAge());
                        reserveCategoryAgeDto.setMaxAge(refDetails.getMaxAge());
                    }
                    reserveCategoryAgeDto.setReserveCategoryId(refDetails.getReserveCategory());
                    reserveCategoryAgeDto.setBornBeforeAfter(refDetails.getBornBeofreAfter());
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
        this.numberOfPosts =totalPostsInProduct;
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
        this.adminCardDateTo = addProductDto.getAdmitCardDateTo();
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
        this.active = customProduct.isActive();
        this.activeGoLiveDate = customProduct.getGoLiveDate();
        this.categoryName = customProduct.getDefaultCategory().getName();
        this.priorityLevel = customProduct.getPriorityLevel();
        this.archived = customProduct.getArchived();
        this.activeGoLiveDate = customProduct.getGoLiveDate();
        this.activeEndDate = customProduct.getDefaultSku().getActiveEndDate();
        this.activeStartDate = customProduct.getDefaultSku().getActiveStartDate();
        this.metaDescription = customProduct.getMetaDescription();
        this.numberOfPosts =(long)customProduct.getPosts().size();

        this.displayTemplate = customProduct.getDisplayTemplate();
        this.platformFee = customProduct.getPlatformFee();
        this.state = customProduct.getState();

        this.customApplicationScope = customProduct.getCustomApplicationScope();
        this.customProductState = customProduct.getProductState();
        this.reserveCategoryDtoList = reserveCategoryDtoList;
        this.modifiedDate = customProduct.getModifiedDate();

        this.creatorUserId = customProduct.getUserId();
        this.creatorRoleId = customProduct.getCreatoRole();
        this.modifierUserId = customProduct.getModifierUserId();
        this.modifierRoleId = customProduct.getModifierRole();

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
        this.feeService=feeService;
        this.activeStartDate=customProduct.getActiveStartDate();
        this.activeEndDate=customProduct.getActiveEndDate();
        this.displayTemplate = customProduct.getDisplayTemplate();
        this.active = customProduct.isActive();
        this.activeGoLiveDate = customProduct.getGoLiveDate();
        this.categoryName = customProduct.getDefaultCategory().getName();
        this.priorityLevel = customProduct.getPriorityLevel();
        this.archived = customProduct.getArchived();
        this.activeGoLiveDate = customProduct.getGoLiveDate();
        this.activeEndDate = customProduct.getDefaultSku().getActiveEndDate();
        this.activeStartDate = customProduct.getDefaultSku().getActiveStartDate();
        this.metaDescription = customProduct.getMetaDescription();
        this.displayTemplate = customProduct.getDisplayTemplate();
        this.platformFee = customProduct.getPlatformFee();
        this.otherInfo=customProduct.getOtherInfo();
       this.numberOfPosts= (long) customProduct.getPosts().size();
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
        this.active = customProduct.isActive();
        this.activeGoLiveDate = customProduct.getGoLiveDate();
        this.categoryName = customProduct.getDefaultCategory().getName();
        this.priorityLevel = customProduct.getPriorityLevel();
        this.archived = customProduct.getArchived();
        this.activeGoLiveDate = customProduct.getGoLiveDate();
        this.activeEndDate = customProduct.getDefaultSku().getActiveEndDate();
        this.activeStartDate = customProduct.getDefaultSku().getActiveStartDate();
        this.metaDescription = customProduct.getMetaDescription();
        this.otherInfo=customProduct.getOtherInfo();
        this.numberOfPosts =(long)customProduct.getPosts().size();

        this.platformFee = customProduct.getPlatformFee();
        this.state = customProduct.getState();

        this.customApplicationScope = customProduct.getCustomApplicationScope();
        this.customProductState = customProduct.getProductState();

        this.creatorUserId = customProduct.getUserId();
        this.creatorRoleId = customProduct.getCreatoRole();
        this.modifierUserId = customProduct.getModifierUserId();
        this.modifierRoleId = customProduct.getModifierRole();


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
}