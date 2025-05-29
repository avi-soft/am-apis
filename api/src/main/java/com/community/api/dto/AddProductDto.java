package com.community.api.dto;

import com.community.api.entity.AddProductAgeDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddProductDto {

    @JsonProperty("is_multiple_post_same_fee")
    protected Boolean isMultiplePostSameFee;
    @JsonProperty("meta_title")
    @NotNull
    String metaTitle;
    @JsonProperty("platform_fee")
    @NotNull
    Double platformFee;
    protected String sectorRunningField;
    @JsonProperty("application_scope_id")
    @NotNull
    Long applicationScope;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("active_start_date")
    Date activeStartDate;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("active_end_date")
    Date activeEndDate;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("go_live_date")
    Date goLiveDate;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("exam_date_from")
    Date examDateFrom;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("exam_date_to")
    Date examDateTo;
    @JsonProperty("priority_level")
    Integer priorityLevel;
    @JsonProperty("meta_description")
    String metaDescription;
    @JsonProperty("reserve_category_fee")
    List<AddReserveCategoryDto> reservedCategory;
    @JsonProperty("state_id")
    Integer state;
    @JsonProperty("quantity")
    Integer quantity;
    @JsonProperty("tentative_document_verification_from")
    Date tentativeVerificationFrom;
    @JsonProperty("tentative_document_verification_to")
    Date tentativeVerificationTo;
    @JsonProperty("exam_center_available_date")
    Date examCenterAvailableDate;
    @JsonProperty("domicile_required")
    Boolean domicileRequired;
    @JsonProperty("product_state_id")
    Long productState;
    @JsonProperty("display_template")
    String displayTemplate;
    @JsonProperty("other_info")
    String otherInfo;
    @JsonProperty("answer_key_available_date")
    protected Date answerKeyAvailableDate;
    @JsonProperty("result_declaration_date")
    protected Date resultDeclarationDate;
    @JsonProperty("counselling_date")
    protected Date counsellingDate;
    @JsonProperty("rejection_status_id")
    Long rejectionStatus;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("last_date_to_pay_fee")
    Date lastDateToPayFee;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("admit_card_date_from")
    Date admitCardDateFrom;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("admit_card_date_to")
    Date admitCardDateTo;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("modification_date_from")
    Date modificationDateFrom;
    @Temporal(TemporalType.TIMESTAMP)
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
    @JsonProperty("sector_id")
    Long sector;
    @JsonProperty("is_review_required")
    Boolean isReviewRequired;
    @JsonProperty("resubmit_comment")
    protected String resubmitComment;
    @JsonProperty("is_resubmit_product")
    protected Boolean isResubmitProduct;
    @JsonProperty("advertisement_id")
    Long advertisement;
    @JsonProperty("posts")
    private List<PostDto> posts;
    @JsonProperty("additional_comments")
    private String additionalComments;
    @JsonProperty("fee_additional_comments")
    private String feeAdditionalComments;
    @JsonProperty("is_exam_date_from_na")
    protected Boolean isExamDateFromNa;
    @JsonProperty("is_answer_key_available_date_na")
    protected Boolean isAnswerKeyAvailableDateNa;
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

}