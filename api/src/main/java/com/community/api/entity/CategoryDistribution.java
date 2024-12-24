package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
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

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "state_distribution_id")
    private StateDistribution stateDistribution;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CustomReserveCategory category;

    @Column(name = "vacancies", nullable = false)
    private Integer categoryVacancies;

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
}
