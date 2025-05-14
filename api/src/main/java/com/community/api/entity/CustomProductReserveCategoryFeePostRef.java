package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.broadleafcommerce.common.money.Money;

import javax.lang.model.element.Name;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "custom_product_reserve_category_fee_post_reference")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CustomProductReserveCategoryFeePostRef {

    @Id
    @Column(name = "product_reserve_category_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long productReservedCategoryId;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "product_id")
    protected CustomProduct customProduct;

    @ManyToOne
    @JoinColumn(name = "reserve_category_id")
    protected CustomReserveCategory customReserveCategory;

    @Column(name = "running_field", length = 256)
    protected String runningField;

    @Column(name = "gender_running_field", length = 256)
    protected String genderRunningField;

    @Column(name = "fee")
    Double fee;

    @ManyToOne
    @JoinColumn(name = "gender_id")
    protected CustomGender gender;

    @Column(name = "post")
    Integer post;

    @JsonIgnore
    @Column(name = "fee_additional_comments")
    String additionalComments;

    @Column(name = "is_other_or_state_category")
    private Boolean isOtherOrStateCategory;

    @Column(name = "other_or_state_category", columnDefinition = "text")
    private String otherOrStateCategory;
}
