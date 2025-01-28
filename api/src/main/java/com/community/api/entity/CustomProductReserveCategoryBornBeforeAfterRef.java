package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.print.attribute.standard.MediaSize;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "custom_product_reserve_category_born_before_after_reference")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CustomProductReserveCategoryBornBeforeAfterRef {

    @Id
    @Column(name = "product_reserve_category_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long productReservedCategoryId;

    @ManyToOne
    @NotNull
    @JsonBackReference
    @JsonIgnore
    @JoinColumn(name = "product_id")
    protected CustomProduct customProduct;

    @ManyToOne
    @JoinColumn(name = "reserve_category_id")
    protected CustomReserveCategory customReserveCategory;

    @Column(name = "born_before")
    Date bornBefore;

    @Column(name = "born_after")
    Date bornAfter;

    @ManyToOne
    @JoinColumn(name = "gender_id")
    protected CustomGender gender;

    @Column(name = "maximum_age")
    protected Integer maximumAge;
    @Column(name = "minimum_age")
    protected Integer minimumAge;
    @Column(name = "born_before_after")
    protected Boolean bornBeforeAfter;
    @Column(name = "as_of_date")
    java.sql.Date asOfDate;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "post_id")
    private Post post;

}
