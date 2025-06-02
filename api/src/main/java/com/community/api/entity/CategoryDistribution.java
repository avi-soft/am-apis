package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "category_distribution")
@Getter
@Setter
public class CategoryDistribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "male_vacancy", columnDefinition = "BIGINT DEFAULT 0")
    private Long maleVacancy;

    @Column(name = "female_vacancy", columnDefinition = "BIGINT DEFAULT 0")
    private Long femaleVacancy;

    @Column(name = "total_vacancy", columnDefinition = "BIGINT DEFAULT 0")
    private Long totalVacancy;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "state_distribution_id")
    private StateDistribution stateDistribution;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private CustomReserveCategory category;

    private String categoryRunningField;

    @Column(name = "isStateLevelCategory",columnDefinition = "BOOLEAN DEFAULT FALSE" )
    private Boolean isStateLevelCategory;

    @Column(name = "stateLevelCategory" )
    private String stateLevelCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id")
    private StateCode state;

    @Column(name = "vacancy_count")
    private Integer vacancyCount;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "district_category_distribution_id")
    private DistrictCategoryDistribution districtCategoryDistribution;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "zone_distribution_id")
    private ZoneDistribution zoneDistribution;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "division_category_distribution_id")
    private DivisionCategoryDistribution divisionCategoryDistribution;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "gender_wise_distribution")
    private GenderWiseDistribution genderWiseDistribution;

    @Column(name = "additional_comment")
    private String additionalComment;
}
