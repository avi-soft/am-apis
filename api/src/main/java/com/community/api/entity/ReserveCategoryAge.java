package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
@Entity
@Table(name = "reserve_category_age")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReserveCategoryAge
{
    @Id
    @Column(name = "product_reserve_category_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long productReservedCategoryId;

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
    protected Date asOfDate;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
}
