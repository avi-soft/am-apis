package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.broadleafcommerce.core.catalog.domain.ProductImpl;
import org.springframework.lang.Nullable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "custom_product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomProduct extends ProductImpl {

    @Column(name = "go_live_date")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date goLiveDate;

    @Column(name = "priority_level")
    @Min(value = 1, message = "Value must be between 1 and 5")
    @Max(value = 5, message = "Value must be between 1 and 5")
    protected Integer priorityLevel;

    @Column(name = "platform_fee")
    protected Double platformFee;

    @Column(name = "exam_date_from")
    protected Date examDateFrom;

    @Column(name = "exam_date_to")
    protected Date examDateTo;

    @Column(name = "last_modified")
    protected Date modifiedDate;

    @Column(name = "other_info",columnDefinition = "text")
    protected String otherInfo;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "product_state_id")
    protected CustomProductState productState;

    @ManyToOne
    @JoinColumn(name = "application_scope_id")
    protected CustomApplicationScope customApplicationScope;

    @ManyToOne
    @JoinColumn(name = "creator_role_id")
    protected Role creatoRole;

    @Column(name = "creator_user_id")
    protected Long userId;

    @ManyToOne
    @JoinColumn(name = "state_id")
    protected StateCode state;

    @Column(name = "domicile_required")
    protected Boolean domicileRequired;

    @Column(name = "modifier_user_id")
    protected Long modifierUserId;

    @ManyToOne
    @JoinColumn(name = "modifier_role_id")
    protected Role modifierRole;

    @ManyToOne
    @JoinColumn(name = "rejection_status_id")
    protected CustomProductRejectionStatus rejectionStatus;

    @Column(name = "last_date_to_pay_fee")
    protected Date lateDateToPayFee;

    @Nullable
    @Column(name = "admit_card_date_from")
    protected Date admitCardDateFrom;

    @Nullable
    @Column(name = "admit_card_date_to")
    protected Date admitCardDateTo;

    @Column(name = "modification_date_from")
    protected Date modificationDateFrom;

    @Column(name = "modification_date_to")
    protected Date modificationDateTo;

    @Column(name = "download_notification_link")
    protected String downloadNotificationLink;

    @Column(name = "download_syllabus_link")
    protected String downloadSyllabusLink;

    @Column(name = "form_complexity")
    @Min(value = 1, message = "Value must be between 1 and 5")
    @Max(value = 5, message = "Value must be between 1 and 5")
    protected Long formComplexity;

    @Column(name = "selection_criteria", columnDefinition = "text")
    protected String selectionCriteria;

    @ManyToOne
    @JoinColumn(name = "sector_id")
    protected CustomSector sector;

    @NotNull
    @Column(name = "created_date")
    protected Date createdDate;

    @Column(name = "is_review_required",columnDefinition = "BOOLEAN DEFAULT FALSE")
    protected Boolean isReviewRequired;

    @ManyToOne
    @JoinColumn(name = "advertisement_id")
    protected Advertisement advertisement;

    @Column(name = "is_multiple_post_same_fee",columnDefinition = "BOOLEAN DEFAULT FALSE")
    protected Boolean isMultiplePostSameFee;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts;

    @Column(name = "totalVacanciesInProduct")
    protected Long totalVacanciesInProduct;

    @Column(name = "number_of_posts")
    protected Long numberOfPosts;
    @OneToMany(mappedBy = "customProduct", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OtherItem> otherItems = new ArrayList<>();

}