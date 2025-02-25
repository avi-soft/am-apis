package com.community.api.dto;

import com.community.api.entity.AddProductAgeDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @JsonProperty("domicile_required")
    Boolean domicileRequired;
    @JsonProperty("product_state_id")
    Long productState;
    @JsonProperty("display_template")
    String displayTemplate;
    @JsonProperty("other_info")
    String otherInfo;
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
    @JsonProperty("advertisement_id")
    Long advertisement;
    @JsonProperty("posts")
    private List<PostDto> posts;

}