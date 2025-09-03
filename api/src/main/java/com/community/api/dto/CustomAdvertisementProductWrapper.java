package com.community.api.dto;

import com.community.api.component.Constant;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomProductReserveCategoryBornBeforeAfterRef;
import com.community.api.entity.CustomProductReserveCategoryFeePostRef;
import com.community.api.entity.CustomSector;
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

import javax.persistence.Column;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

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
    public void wrapDetails(CustomProduct product,
                            HttpServletRequest httpServletRequest,
                            ReserveCategoryService reserveCategoryService,
                            ReserveCategoryAgeService reserveCategoryAgeService,
                            GenderService genderService,
                            CustomCustomer customCustomer,
                            SharedUtilityService sharedUtilityService) {

        // Basic product info
        System.out.println("=== STARTING WRAP DETAILS ===");
        System.out.println("Product ID: " + product.getId());

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
        this.isReviewRequired = product.getIsReviewRequired();

        this.modifiedDate = product.getActiveStartDate();
        this.creatorUserId = product.getUserId();
        this.creatorRoleId = product.getCreatoRole();
        this.modifierUserId = null;
        this.modifierRoleId = null;

        // Product dates and links
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

        // Post and vacancy info
        this.isMultiplePostSameFee = product.getIsMultiplePostSameFee();
        this.selectionCriteria = product.getSelectionCriteria();
        this.totalVacancies = product.getTotalVacanciesInProduct();
        this.numberOfPosts = product.getPosts().size();

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
        LocalDate localDate = LocalDate.now();
        Date utilDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        int[] ageLimits=null;
        if(ageLimitResult.getBornAfter()!=null&&ageLimitResult.getBornBefore()!=null) {
            ageLimits = sharedUtilityService.calculateAgeRange(
                    ageLimitResult.getBornBefore(),
                    ageLimitResult.getBornAfter(),
                    utilDate);
        }


        this.ageLimit = (ageLimitResult.getMaximumAge() != null && ageLimitResult.getMinimumAge() != null &&
                ageLimitResult.getMaximumAge() != 0 && ageLimitResult.getMinimumAge() != 0)
                ? ageLimitResult.getMinimumAge() + "-" + ageLimitResult.getMaximumAge()
                : (ageLimits != null && ageLimits.length >= 2)
                ? ageLimits[0] + "-" + ageLimits[1]
                : "N/A";
    }

    public void wrapDetailsSimplified(CustomProduct product,
                            HttpServletRequest httpServletRequest,
                            ReserveCategoryService reserveCategoryService,
                            ReserveCategoryAgeService reserveCategoryAgeService,
                            GenderService genderService,
                            CustomCustomer customCustomer,
                            SharedUtilityService sharedUtilityService) {
        this.id = product.getId();
        this.metaTitle = product.getMetaTitle();
        this.displayTemplate = product.getDisplayTemplate();
    }
    @Override
    public void wrapDetails(Product product, HttpServletRequest httpServletRequest) {

    }

    @Override
    public void wrapSummary(Product product, HttpServletRequest httpServletRequest) {

    }

}
