package com.community.api.dto;

import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomProductReserveCategoryFeePostRef;
import com.community.api.entity.Role;
import com.community.api.services.GenderService;
import com.community.api.services.ReserveCategoryAgeService;
import com.community.api.services.ReserveCategoryService;
import com.community.api.services.SharedUtilityService;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.broadleafcommerce.common.rest.api.wrapper.APIWrapper;
import org.broadleafcommerce.common.rest.api.wrapper.BaseWrapper;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

public class CustomAdvertisementProductWrapper extends BaseWrapper implements APIWrapper<Product> {
    @JsonProperty("product_id")
    protected Long id;
    @JsonProperty("meta_title")
    protected String metaTitle;
    @JsonProperty("display_template")
    protected String displayTemplate;
    @JsonProperty("meta_description")
    protected String metaDescription;
    @JsonProperty("active_start_date")
    protected Date activeStartDate;
    @JsonProperty("active_end_date")
    protected Date activeEndDate;
    @JsonProperty("go_live_date")
    protected Date activeGoLiveDate;
    @JsonProperty("archived")
    protected Character archived;
    @JsonProperty("active")
    protected Boolean active;


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
    @JsonProperty("selection_criteria")
    String selectionCriteria;
    @JsonProperty("created_date")
    Date createdDate;
    @JsonProperty("is_review_required")
    Boolean isReviewRequired;
    @JsonProperty("is_multiple_post_same_fee")
    Boolean isMultiplePostSameFee;
    @JsonProperty("total_vacancies_in_product")
    Long totalVacancies;
    @JsonProperty("fee")
    Double fee;
    @JsonProperty("age_limit")
    String ageLimit;
    @JsonProperty("number_of_posts")
    Integer numberOfPosts;

    public void wrapDetails(CustomProduct product, HttpServletRequest httpServletRequest) {
        this.id = product.getId();
        this.metaTitle = product.getMetaTitle();
        this.displayTemplate = product.getDisplayTemplate();
        this.active = product.isActive();
        this.archived = 'N';
        this.createdDate = product.getCreatedDate();
        this.activeGoLiveDate = product.getGoLiveDate();
        this.activeEndDate = product.getDefaultSku().getActiveEndDate();
        this.activeStartDate = product.getDefaultSku().getActiveStartDate();
        this.metaDescription = product.getMetaDescription();

        this.displayTemplate = product.getDisplayTemplate();
        this.isReviewRequired=product.getIsReviewRequired();

        this.modifiedDate = product.getActiveStartDate();
        this.creatorUserId = product.getUserId();
        this.creatorRoleId = product.getCreatoRole();
        this.modifierUserId = null;
        this.modifierRoleId = null;

        this.domicileRequired = product.getDomicileRequired();
        this.examDateFrom = product.getExamDateFrom();
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
        this.totalVacancies = product.getTotalVacanciesInProduct();
    }
    public void wrapDetails(CustomProduct product, HttpServletRequest httpServletRequest, ReserveCategoryService reserveCategoryService, ReserveCategoryAgeService reserveCategoryAgeService, GenderService genderService, CustomCustomer customCustomer, SharedUtilityService sharedUtilityService) {
        this.id = product.getId();
        this.metaTitle = product.getMetaTitle();
        this.displayTemplate = product.getDisplayTemplate();
        this.active = product.isActive();
        this.archived = 'N';
        this.createdDate = product.getCreatedDate();
        this.activeGoLiveDate = product.getGoLiveDate();
        this.activeEndDate = product.getDefaultSku().getActiveEndDate();
        this.activeStartDate = product.getDefaultSku().getActiveStartDate();
        this.metaDescription = product.getMetaDescription();

        this.displayTemplate = product.getDisplayTemplate();
        this.isReviewRequired=product.getIsReviewRequired();

        this.modifiedDate = product.getActiveStartDate();
        this.creatorUserId = product.getUserId();
        this.creatorRoleId = product.getCreatoRole();
        this.modifierUserId = null;
        this.modifierRoleId = null;

        this.domicileRequired = product.getDomicileRequired();
        this.examDateFrom = product.getExamDateFrom();
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
        this.totalVacancies = product.getTotalVacanciesInProduct();
        this.numberOfPosts=product.getPosts().size();
        var genderId=0L;
        var categoryId=0L;
        try {
            categoryId = reserveCategoryService.getCategoryByName(customCustomer.getCategory()).getReserveCategoryId();
            genderId = genderService.getGenderByName(customCustomer.getGender()).getGenderId();
        }catch (Exception exception)
        {
            if(genderId==0L)
                genderId=1L;
            if(categoryId==0L)
                categoryId=1L;
        }

        this.fee =(reserveCategoryService.getReserveCategoryFee(product.getId(), 1L, 1L) != null
                ? reserveCategoryService.getReserveCategoryFee(product.getId(), 1L, 1L)
                : 0);
        var ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(product, 1L,1L);
        int[]ageLimits=null;
        if(ageLimitResult!=null&&(ageLimitResult.getBornBefore()!=null&&ageLimitResult.getBornAfter()!=null))
            ageLimits=(sharedUtilityService.calculateAgeRange(ageLimitResult.getBornBefore(),ageLimitResult.getBornAfter()));
        this.ageLimit = (ageLimitResult != null && (ageLimitResult.getMaximumAge() != null &&ageLimitResult.getMaximumAge()!=0 && ageLimitResult.getMinimumAge() != null &&ageLimitResult.getMinimumAge()!=0))
                ? ageLimitResult.getMinimumAge().toString() + "-" + ageLimitResult.getMaximumAge().toString()
                : (ageLimits != null && ageLimits.length >= 2)
                ? ageLimits[0] + "-" + ageLimits[1]
                : "N/A";
    }

    @Override
    public void wrapDetails(Product product, HttpServletRequest httpServletRequest) {

    }

    @Override
    public void wrapSummary(Product product, HttpServletRequest httpServletRequest) {

    }

}
