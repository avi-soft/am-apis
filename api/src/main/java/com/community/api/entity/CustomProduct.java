package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.broadleafcommerce.core.catalog.domain.ProductImpl;
import org.springframework.lang.Nullable;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
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
    @Column(name = "is_exam_date_from_na")
    protected Boolean isExamDateFromNa;

    @Column(name = "answer_key_available_date")
    protected Date answerKeyAvailableDate;
    @Column(name = "is_answer_key_available_date_na")
    protected Boolean isAnswerKeyAvailableDateNa;

    @Column(name = "result_declaration_date")
    protected Date resultDeclarationDate;
    @Column(name = "is_result_declaration_date_na")
    protected Boolean isResultDeclarationDateNa;

    @Column(name = "counselling_date")
    protected Date counsellingDate;
    @Column(name = "is_counselling_date_na")
    protected Boolean isCounsellingDateNa;

    @Column(name = "tentative_document_verification_from")
    Date tentativeVerificationFrom;
    @Column(name = "is_tentative_document_verification_from_na")
    protected Boolean isTentativeVerificationFromNa;

    @Column(name = "tentative_document_verification_to")
    Date tentativeVerificationTo;
    @Column(name = "is_tentative_document_verification_to_na")
    protected Boolean isTentativeVerificationToNa;

    @Column(name = "exam_date_to")
    protected Date examDateTo;
    @Column(name = "is_exam_date_to_na")
    protected Boolean isExamDateToNa;

    @Column(name = "exam_center_available_date")
    Date examCenterAvailableDate;
    @Column(name = "is_exam_center_available_date_na")
    protected Boolean isExamCenterAvailableDateNa;

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
    @Column(name = "is_last_date_to_pay_fee_na")
    protected Boolean isLateDateToPayFeeNa;

    @Nullable
    @Column(name = "admit_card_date_from")
    protected Date admitCardDateFrom;
    @Column(name = "is_admit_card_date_from_na")
    protected Boolean isAdmitCardDateFromNa;

    @Nullable
    @Column(name = "admit_card_date_to")
    protected Date admitCardDateTo;
    @Column(name = "is_admit_card_date_to_na")
    protected Boolean isAdmitCardDateToNa;

    @Column(name = "modification_date_from")
    protected Date modificationDateFrom;
    @Column(name = "is_modification_date_from_na")
    protected Boolean isModificationDateFromNa;

    @Column(name = "modification_date_to")
    protected Date modificationDateTo;
    @Column(name = "is_modification_date_to_na")
    protected Boolean isModificationDateToNa;

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

    @Column(name = "sector_running_field")
    protected String sectorRunningField;

    @NotNull
    @Column(name = "created_date")
    protected Date createdDate;

    @Column(name = "is_review_required")
    protected Boolean isReviewRequired;

    @Column(name = "is_approved", columnDefinition = "BOOLEAN DEFAULT FALSE")
    protected Boolean isApproved=false;

    @Column(name = "resubmit_comment",columnDefinition = "text")
    protected String resubmitComment;

    @ManyToOne
    @JoinColumn(name = "advertisement_id")
    protected Advertisement advertisement;

    @Column(name = "is_multiple_post_same_fee",columnDefinition = "BOOLEAN DEFAULT FALSE")
    protected Boolean isMultiplePostSameFee;

    @Column(name = "is_edited",columnDefinition = "BOOLEAN DEFAULT FALSE")
    protected Boolean isEdited;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts;

    @Column(name = "totalVacanciesInProduct")
    protected Long totalVacanciesInProduct;

    @Column(name = "number_of_posts")
    protected Long numberOfPosts;
    @OneToMany(mappedBy = "customProduct", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OtherItem> otherItems = new ArrayList<>();

    @Column(name = "additional_comments", columnDefinition = "text")
    @JsonProperty("additional_comments")
    private String additionalComments;

    @Column(name = "fee_additional_comments", columnDefinition = "text")
    private String feeAdditionalComments;

    @Column(name = "views", columnDefinition = "BIGINT DEFAULT 0")
    private Long views;

    @Column(name = "soft_delete", columnDefinition = "VARCHAR(1) DEFAULT 'N'")
    private String del;

    @Column(name = "rejection_comment")
    private String rejectionComment;

    @ElementCollection
    @CollectionTable(name = "product_purchasers",
            joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "customer_id")
    private List<Long>purchasedBy = new ArrayList<>();

}